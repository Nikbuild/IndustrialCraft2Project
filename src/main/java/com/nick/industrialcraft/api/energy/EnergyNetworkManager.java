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
    // Key: CacheKey (sourcePos + directions), Value: Cached network result
    private static final Map<Level, Map<CacheKey, CachedNetwork>> networkCache = new WeakHashMap<>();

    /**
     * Cache key that includes both position AND directions.
     * Different direction scans from the same position must be cached separately!
     */
    private record CacheKey(BlockPos pos, Set<Direction> directions) {
        static CacheKey of(BlockPos pos, Direction... dirs) {
            return new CacheKey(pos, Set.of(dirs));
        }
    }

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
     * @param accessSide The side of the machine that the cable connects to (for side-specific storage)
     */
    public record MachineConnection(
            BlockPos pos,
            IEnergyStorage storage,
            BlockEntity blockEntity,
            Direction accessSide
    ) {
        // Backwards compatibility constructor
        public MachineConnection(BlockPos pos, IEnergyStorage storage, BlockEntity blockEntity) {
            this(pos, storage, blockEntity, null);
        }
    }

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

        System.out.println("[BFS DEBUG] getConnectedMachines called from " + sourcePos + " dirs=" + java.util.Arrays.toString(directions));

        // Get or create level cache
        Map<CacheKey, CachedNetwork> levelCache = networkCache.computeIfAbsent(level, k -> new HashMap<>());

        // Cache key includes BOTH position AND directions - different direction scans are different!
        CacheKey cacheKey = CacheKey.of(sourcePos, directions);

        // Check if we have a valid cached result
        long currentTime = level.getGameTime();
        CachedNetwork cached = levelCache.get(cacheKey);

        if (cached != null && !cached.isExpired(currentTime)) {
            System.out.println("[BFS DEBUG]   Using cached result with " + cached.machines().size() + " machines");
            // Verify machines still exist and have valid storage
            List<MachineConnection> validMachines = new ArrayList<>();
            for (MachineConnection machine : cached.machines()) {
                IEnergyStorage storage = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    machine.pos(),
                    machine.accessSide()  // Use the stored access side!
                );
                if (storage != null && storage.canReceive()) {
                    BlockEntity be = level.getBlockEntity(machine.pos());
                    validMachines.add(new MachineConnection(machine.pos(), storage, be, machine.accessSide()));
                }
            }
            System.out.println("[BFS DEBUG]   Validated " + validMachines.size() + " machines from cache");
            return validMachines;
        }

        System.out.println("[BFS DEBUG]   Cache miss/expired, performing full scan");
        // Cache miss or expired - perform full scan
        CachedNetwork newCache = scanNetwork(level, sourcePos, currentTime, directions);
        levelCache.put(cacheKey, newCache);

        System.out.println("[BFS DEBUG]   Scan result: " + newCache.machines().size() + " machines via " + newCache.cablePositions().size() + " cables");

        return new ArrayList<>(newCache.machines());
    }

    /**
     * Entry in the BFS queue that tracks both position and the direction we came from.
     * This allows us to know which side of a machine to access for side-specific storage.
     */
    private record QueueEntry(BlockPos pos, Direction fromDirection) {}

    /**
     * Scan the network using iterative BFS (breadth-first search).
     * This prevents stack overflow on large networks.
     */
    private static CachedNetwork scanNetwork(Level level, BlockPos sourcePos, long currentTime, Direction... directions) {
        Set<BlockPos> visitedCables = new HashSet<>();
        Set<BlockPos> visitedMachines = new HashSet<>();
        List<MachineConnection> machines = new ArrayList<>();

        // BFS queue for iterative traversal - now tracks direction we came from
        Deque<QueueEntry> queue = new ArrayDeque<>();

        System.out.println("[BFS DEBUG] scanNetwork starting from " + sourcePos + " in directions " + java.util.Arrays.toString(directions));

        // Seed the queue with initial positions
        for (Direction dir : directions) {
            // fromDirection is the direction FROM the neighbor's perspective (opposite of our scan direction)
            BlockPos startPos = sourcePos.relative(dir);
            System.out.println("[BFS DEBUG]   Seeding queue with " + startPos + " (dir=" + dir + ", accessSide=" + dir.getOpposite() + ")");
            queue.add(new QueueEntry(startPos, dir.getOpposite()));
        }

        int blocksScanned = 0;
        int maxNetworkSize = Config.MAX_NETWORK_SIZE.get();

        while (!queue.isEmpty() && blocksScanned < maxNetworkSize) {
            QueueEntry entry = queue.poll();
            BlockPos pos = entry.pos();
            Direction accessSide = entry.fromDirection();  // The side we're accessing this block from
            blocksScanned++;

            System.out.println("[BFS DEBUG] Processing " + pos + " (accessSide=" + accessSide + ")");

            // Skip the source position itself
            if (pos.equals(sourcePos)) {
                System.out.println("[BFS DEBUG]   Skipping (is source position)");
                continue;
            }

            BlockState state = level.getBlockState(pos);
            System.out.println("[BFS DEBUG]   BlockState: " + state.getBlock().getClass().getSimpleName() + " (" + state.getBlock() + ")");

            // If this is a cable, explore its connections
            if (state.getBlock() instanceof BaseCableBlock) {
                System.out.println("[BFS DEBUG]   Is a cable!");
                // Skip if already visited
                if (visitedCables.contains(pos)) {
                    continue;
                }
                visitedCables.add(pos);
                Config.debugLog("  BFS found cable at {}", pos);

                // Get the cable block entity to check connections
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CableBlockEntity cable) {
                    // Add all connected neighbors to the queue
                    for (Direction dir : Direction.values()) {
                        if (!cable.isConnectedInDirection(state, dir)) {
                            continue;
                        }

                        BlockPos neighborPos = pos.relative(dir);
                        Config.debugLog("    Cable at {} connected to {} in direction {}", pos, neighborPos, dir);

                        // Only add if not already visited
                        if (!visitedCables.contains(neighborPos) && !visitedMachines.contains(neighborPos)) {
                            // The neighbor's access side is opposite of the direction we're going
                            queue.add(new QueueEntry(neighborPos, dir.getOpposite()));
                        }
                    }
                } else {
                    // Block entity missing or wrong type - use blockstate properties directly as fallback
                    Config.debugLog("  WARNING: Cable at {} has no/wrong block entity, using blockstate", pos);
                    for (Direction dir : Direction.values()) {
                        boolean connected = switch (dir) {
                            case NORTH -> state.getValue(BaseCableBlock.NORTH);
                            case SOUTH -> state.getValue(BaseCableBlock.SOUTH);
                            case EAST -> state.getValue(BaseCableBlock.EAST);
                            case WEST -> state.getValue(BaseCableBlock.WEST);
                            case UP -> state.getValue(BaseCableBlock.UP);
                            case DOWN -> state.getValue(BaseCableBlock.DOWN);
                        };
                        if (!connected) continue;

                        BlockPos neighborPos = pos.relative(dir);
                        if (!visitedCables.contains(neighborPos) && !visitedMachines.contains(neighborPos)) {
                            queue.add(new QueueEntry(neighborPos, dir.getOpposite()));
                        }
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

                // Try to get energy capability WITH THE CORRECT SIDE
                // accessSide is the side of the machine that the cable is connected to
                BlockEntity neighborBe = level.getBlockEntity(pos);
                IEnergyStorage neighborEnergy = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos,
                    accessSide  // Pass the correct side for side-specific storage!
                );

                if (neighborEnergy != null && neighborEnergy.canReceive()) {
                    Config.debugLog("  BFS found MACHINE at {} (type={}, accessSide={}, canReceive=true)",
                        pos, neighborBe != null ? neighborBe.getClass().getSimpleName() : "null", accessSide);
                    machines.add(new MachineConnection(pos, neighborEnergy, neighborBe, accessSide));
                } else {
                    Config.debugLog("  BFS found non-energy block at {} (type={}, accessSide={})",
                        pos, state.getBlock().getClass().getSimpleName(), accessSide);
                }
            }
        }

        // Warn if network was truncated due to size limit
        if (!queue.isEmpty()) {
            System.out.println("[BFS DEBUG] WARNING: Network scan at " + sourcePos + " was truncated at " + blocksScanned + " blocks (limit: " + maxNetworkSize + ")");
        }

        System.out.println("[BFS DEBUG] Scan complete. Found " + machines.size() + " machines, " + visitedCables.size() + " cables");
        for (MachineConnection mc : machines) {
            System.out.println("[BFS DEBUG]   Machine: " + mc.pos() + " (" + (mc.blockEntity() != null ? mc.blockEntity().getClass().getSimpleName() : "null") + ")");
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

        Map<CacheKey, CachedNetwork> levelCache = networkCache.get(level);
        if (levelCache == null) {
            return;
        }

        // Invalidate any cache that contains this position
        // This is more thorough than just removing pos from cache
        List<CacheKey> toRemove = new ArrayList<>();

        for (Map.Entry<CacheKey, CachedNetwork> entry : levelCache.entrySet()) {
            CachedNetwork cached = entry.getValue();
            CacheKey key = entry.getKey();

            // If this position is in the cable network or is the source position, invalidate
            if (cached.cablePositions().contains(pos) || key.pos().equals(pos)) {
                toRemove.add(key);
                continue;
            }

            // Also check if pos is one of the machines
            for (MachineConnection machine : cached.machines()) {
                if (machine.pos().equals(pos)) {
                    toRemove.add(key);
                    break;
                }
            }
        }

        for (CacheKey key : toRemove) {
            levelCache.remove(key);
        }

        // Also invalidate caches of adjacent positions (they might now connect to pos)
        // We need to remove ALL cache entries where the source pos is adjacent
        List<CacheKey> adjacentToRemove = new ArrayList<>();
        for (CacheKey key : levelCache.keySet()) {
            for (Direction dir : Direction.values()) {
                if (key.pos().equals(pos.relative(dir))) {
                    adjacentToRemove.add(key);
                    break;
                }
            }
        }
        for (CacheKey key : adjacentToRemove) {
            levelCache.remove(key);
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

        Map<CacheKey, CachedNetwork> levelCache = networkCache.get(level);
        if (levelCache == null) {
            return;
        }

        // Just clear all caches that are within range
        // This is a simple but effective approach
        List<CacheKey> toRemove = new ArrayList<>();
        int radiusSq = radius * radius;

        for (CacheKey key : levelCache.keySet()) {
            if (key.pos().distSqr(pos) <= radiusSq) {
                toRemove.add(key);
            }
        }

        for (CacheKey key : toRemove) {
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
        Map<CacheKey, CachedNetwork> levelCache = networkCache.get(level);
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
