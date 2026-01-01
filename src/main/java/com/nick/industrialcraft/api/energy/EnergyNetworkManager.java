package com.nick.industrialcraft.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.nick.industrialcraft.Config;
import com.nick.industrialcraft.content.block.cable.BaseCableBlock;
import com.nick.industrialcraft.content.block.cable.CableBlockEntity;

import java.util.*;

/**
 * Centralized energy network manager with caching for optimal performance.
 *
 * Performance optimizations:
 * - Caches network topology per source position
 * - Uses iterative BFS (not recursive DFS) to prevent stack overflow
 * - Uses HashSet for O(1) duplicate detection
 * - Invalidates cache only when network topology changes
 * - Limits maximum network size to prevent infinite loops
 *
 * This reduces network scanning from O(n) per tick per generator to
 * O(1) amortized (only rescans when network changes).
 */
public class EnergyNetworkManager {

    // Per-level cache of network topologies
    // Key: Source BlockPos, Value: Cached network result
    private static final Map<Level, Map<BlockPos, CachedNetwork>> networkCache = new WeakHashMap<>();

    /**
     * Represents a cached network scan result.
     *
     * @param machines List of connected machines that can receive energy
     * @param cablePositions Set of cable positions in the network (for invalidation)
     * @param cacheTime Game time when this cache was created
     */
    public record CachedNetwork(
            List<MachineConnection> machines,
            Set<BlockPos> cablePositions,
            long cacheTime
    ) {
        public boolean isExpired(long currentTime) {
            return (currentTime - cacheTime) > Config.CACHE_EXPIRY_TICKS.get();
        }
    }

    /**
     * Information about a connected machine.
     *
     * @param pos The position of the machine
     * @param storage The energy storage capability of the machine
     * @param blockEntity The block entity (may be null if removed)
     */
    public record MachineConnection(
            BlockPos pos,
            IEnergyStorage storage,
            BlockEntity blockEntity
    ) {}

    /**
     * Get connected machines for a generator/storage block at the given position.
     * Uses caching for optimal performance - only rescans if cache is invalid.
     *
     * @param level The world level
     * @param sourcePos The position of the energy source
     * @param directions Which directions to scan from (usually all 6, or just output face)
     * @return List of connected machines that can receive energy
     */
    public static List<MachineConnection> getConnectedMachines(Level level, BlockPos sourcePos, Direction... directions) {
        if (level == null || level.isClientSide()) {
            return Collections.emptyList();
        }

        // Get or create level cache
        Map<BlockPos, CachedNetwork> levelCache = networkCache.computeIfAbsent(level, k -> new HashMap<>());

        // Check if we have a valid cached result
        long currentTime = level.getGameTime();
        CachedNetwork cached = levelCache.get(sourcePos);

        if (cached != null && !cached.isExpired(currentTime)) {
            // Verify machines still exist and have valid storage
            List<MachineConnection> validMachines = new ArrayList<>();
            for (MachineConnection machine : cached.machines()) {
                IEnergyStorage storage = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    machine.pos(),
                    null
                );
                if (storage != null && storage.canReceive()) {
                    BlockEntity be = level.getBlockEntity(machine.pos());
                    validMachines.add(new MachineConnection(machine.pos(), storage, be));
                }
            }
            return validMachines;
        }

        // Cache miss or expired - perform full scan
        CachedNetwork newCache = scanNetwork(level, sourcePos, currentTime, directions);
        levelCache.put(sourcePos, newCache);

        Config.debugLog("Network scan at {}: found {} machines via {} cables",
            sourcePos, newCache.machines().size(), newCache.cablePositions().size());

        return new ArrayList<>(newCache.machines());
    }

    /**
     * Scan the network using iterative BFS (breadth-first search).
     * This prevents stack overflow on large networks.
     */
    private static CachedNetwork scanNetwork(Level level, BlockPos sourcePos, long currentTime, Direction... directions) {
        Set<BlockPos> visitedCables = new HashSet<>();
        Set<BlockPos> visitedMachines = new HashSet<>();
        List<MachineConnection> machines = new ArrayList<>();

        // BFS queue for iterative traversal
        Deque<BlockPos> queue = new ArrayDeque<>();

        // Seed the queue with initial positions
        for (Direction dir : directions) {
            queue.add(sourcePos.relative(dir));
        }

        int blocksScanned = 0;
        int maxNetworkSize = Config.MAX_NETWORK_SIZE.get();

        while (!queue.isEmpty() && blocksScanned < maxNetworkSize) {
            BlockPos pos = queue.poll();
            blocksScanned++;

            // Skip the source position itself
            if (pos.equals(sourcePos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);

            // If this is a cable, explore its connections
            if (state.getBlock() instanceof BaseCableBlock) {
                // Skip if already visited
                if (visitedCables.contains(pos)) {
                    continue;
                }
                visitedCables.add(pos);

                // Get the cable block entity to check connections
                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof CableBlockEntity cable)) {
                    continue;
                }

                // Add all connected neighbors to the queue
                for (Direction dir : Direction.values()) {
                    if (!cable.isConnectedInDirection(state, dir)) {
                        continue;
                    }

                    BlockPos neighborPos = pos.relative(dir);

                    // Only add if not already visited
                    if (!visitedCables.contains(neighborPos) && !visitedMachines.contains(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
            // If this is a machine (not a cable), check if it can receive energy
            else {
                // Skip if already processed
                if (visitedMachines.contains(pos)) {
                    continue;
                }
                visitedMachines.add(pos);

                // Try to get energy capability
                BlockEntity neighborBe = level.getBlockEntity(pos);
                IEnergyStorage neighborEnergy = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos,
                    null
                );

                if (neighborEnergy != null && neighborEnergy.canReceive()) {
                    machines.add(new MachineConnection(pos, neighborEnergy, neighborBe));
                }
            }
        }

        // Warn if network was truncated due to size limit
        if (!queue.isEmpty()) {
            Config.debugLog("WARNING: Network scan at {} was truncated at {} blocks (limit: {}). " +
                "Consider increasing max_network_size in config if this network is intentionally large.",
                sourcePos, blocksScanned, maxNetworkSize);
        }

        return new CachedNetwork(machines, visitedCables, currentTime);
    }

    /**
     * Invalidate cache for a specific position and all connected networks.
     * Call this when a cable or machine is placed or removed.
     *
     * @param level The world level
     * @param pos The position that changed
     */
    public static void invalidateAt(Level level, BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }

        Map<BlockPos, CachedNetwork> levelCache = networkCache.get(level);
        if (levelCache == null) {
            return;
        }

        // Invalidate any cache that contains this position
        // This is more thorough than just removing pos from cache
        List<BlockPos> toRemove = new ArrayList<>();

        for (Map.Entry<BlockPos, CachedNetwork> entry : levelCache.entrySet()) {
            CachedNetwork cached = entry.getValue();

            // If this position is in the cable network or is a machine in this network, invalidate
            if (cached.cablePositions().contains(pos) || entry.getKey().equals(pos)) {
                toRemove.add(entry.getKey());
                continue;
            }

            // Also check if pos is one of the machines
            for (MachineConnection machine : cached.machines()) {
                if (machine.pos().equals(pos)) {
                    toRemove.add(entry.getKey());
                    break;
                }
            }
        }

        for (BlockPos key : toRemove) {
            levelCache.remove(key);
        }

        // Also invalidate caches of adjacent positions (they might now connect to pos)
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            levelCache.remove(neighbor);
        }

        if (!toRemove.isEmpty()) {
            Config.debugLog("Cache invalidated at {}: removed {} cached networks", pos, toRemove.size());
        }
    }

    /**
     * Invalidate all caches in a radius around a position.
     * More aggressive invalidation for when we're not sure what changed.
     *
     * @param level The world level
     * @param pos The center position
     * @param radius The radius to invalidate
     */
    public static void invalidateRadius(Level level, BlockPos pos, int radius) {
        if (level == null || level.isClientSide()) {
            return;
        }

        Map<BlockPos, CachedNetwork> levelCache = networkCache.get(level);
        if (levelCache == null) {
            return;
        }

        // Just clear all caches that are within range
        // This is a simple but effective approach
        List<BlockPos> toRemove = new ArrayList<>();
        int radiusSq = radius * radius;

        for (BlockPos key : levelCache.keySet()) {
            if (key.distSqr(pos) <= radiusSq) {
                toRemove.add(key);
            }
        }

        for (BlockPos key : toRemove) {
            levelCache.remove(key);
        }
    }

    /**
     * Clear all caches for a level (call on dimension unload).
     */
    public static void clearLevel(Level level) {
        networkCache.remove(level);
    }

    /**
     * Clear all caches (call on server shutdown).
     */
    public static void clearAll() {
        networkCache.clear();
    }

    /**
     * Get cache statistics for debugging.
     */
    public static String getCacheStats(Level level) {
        Map<BlockPos, CachedNetwork> levelCache = networkCache.get(level);
        if (levelCache == null) {
            return "No cache for this level";
        }

        int totalMachines = 0;
        int totalCables = 0;
        for (CachedNetwork cached : levelCache.values()) {
            totalMachines += cached.machines().size();
            totalCables += cached.cablePositions().size();
        }

        return String.format("Networks: %d, Total machines: %d, Total cables: %d",
            levelCache.size(), totalMachines, totalCables);
    }
}
