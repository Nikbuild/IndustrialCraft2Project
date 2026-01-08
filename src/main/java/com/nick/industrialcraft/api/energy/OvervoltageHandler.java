package com.nick.industrialcraft.api.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.nick.industrialcraft.Config;
import com.nick.industrialcraft.content.block.cable.BaseCableBlock;
import com.nick.industrialcraft.content.block.cable.CableBlockEntity;

import java.util.*;

/**
 * Handles overvoltage consequences when incompatible voltage tiers are connected.
 *
 * Consequences are graduated based on tier gap:
 * - 1 tier gap (e.g., MV → LV): Fire around the machine
 * - 2 tier gap (e.g., HV → LV): Explosion destroying the machine
 * - 3 tier gap (e.g., EV → LV): Explosion + lethal electrical shock to nearby players
 *
 * The check happens INSTANTLY when a connection is made (cable/machine placed),
 * simulating real-world electrical behavior where overvoltage damage occurs
 * the moment incompatible systems are connected.
 */
public class OvervoltageHandler {

    /**
     * Consequence type based on tier gap.
     */
    public enum OvervoltageConsequence {
        /** Safe - no overvoltage */
        SAFE(0),
        /** 1 tier gap - fire damage */
        FIRE(1),
        /** 2 tier gap - explosion */
        EXPLOSION(2),
        /** 3+ tier gap - explosion with lethal shock */
        EXPLOSION_AND_SHOCK(3);

        private final int minTierGap;

        OvervoltageConsequence(int minTierGap) {
            this.minTierGap = minTierGap;
        }

        public int getMinTierGap() {
            return minTierGap;
        }

        /**
         * Get the consequence for a given tier gap.
         */
        public static OvervoltageConsequence fromTierGap(int tierGap) {
            if (tierGap <= 0) return SAFE;
            if (tierGap == 1) return FIRE;
            if (tierGap == 2) return EXPLOSION;
            return EXPLOSION_AND_SHOCK; // 3+ tiers
        }
    }

    /**
     * Check and handle overvoltage when a new block is placed.
     * This scans the network to find any voltage incompatibilities
     * and triggers appropriate consequences.
     *
     * @param level The world level
     * @param placedPos The position where a block was just placed
     * @return true if an overvoltage event occurred
     */
    public static boolean checkOnPlacement(Level level, BlockPos placedPos) {
        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return false;
        }

        // Check if overvoltage explosions are enabled
        if (!Config.ENABLE_OVERVOLTAGE_EXPLOSIONS.get()) {
            return false;
        }

        // Special handling for transformers - they have different voltage tiers on different sides
        BlockEntity placedBe = level.getBlockEntity(placedPos);
        if (placedBe instanceof IVoltageTransformer transformer) {
            return checkTransformerConnections(level, placedPos, transformer);
        }

        // Scan the network to find all sources and consumers
        Set<BlockPos> visitedCables = new HashSet<>();
        List<TieredBlock> sources = new ArrayList<>();
        List<TieredBlock> consumers = new ArrayList<>();

        // Start scanning from the placed position
        scanNetworkFromPosition(level, placedPos, visitedCables, sources, consumers);

        // Check all source-consumer pairs for overvoltage
        boolean hadOvervoltage = false;
        for (TieredBlock source : sources) {
            for (TieredBlock consumer : consumers) {
                int tierGap = EnergyTier.getTierGap(source.tier, consumer.tier);
                if (tierGap > 0) {
                    // Overvoltage! Apply consequence to the consumer
                    applyConsequence(level, consumer.pos, tierGap);
                    hadOvervoltage = true;
                }
            }
        }

        return hadOvervoltage;
    }

    /**
     * Check transformer connections for overvoltage.
     * Transformers have different voltage tiers on different sides, so we need
     * to check each side's connected machines against that side's output tier.
     *
     * For example, an LV Transformer:
     * - HIGH side (front) outputs MV (128 EU) - machines there must handle MV
     * - LOW sides (5 other faces) output LV (32 EU) - machines there must handle LV
     *
     * @param level The world level
     * @param transformerPos The transformer position
     * @param transformer The transformer interface
     * @return true if an overvoltage event occurred
     */
    private static boolean checkTransformerConnections(Level level, BlockPos transformerPos, IVoltageTransformer transformer) {
        boolean hadOvervoltage = false;

        Config.debugLog("Checking transformer connections at {}", transformerPos);

        // Check each side of the transformer
        for (Direction side : Direction.values()) {
            EnergyTier outputTier = transformer.getTierForSide(side);
            Config.debugLog("  Side {}: output tier = {}", side, outputTier);

            // Find machines connected to this side through cables
            List<EnergyNetworkManager.MachineConnection> connectedMachines =
                EnergyNetworkManager.getConnectedMachines(level, transformerPos, side);

            for (EnergyNetworkManager.MachineConnection machine : connectedMachines) {
                // Skip other transformers
                if (machine.blockEntity() instanceof IVoltageTransformer) {
                    continue;
                }

                if (machine.blockEntity() instanceof IEnergyTier tieredMachine) {
                    EnergyTier machineTier = tieredMachine.getEnergyTier();
                    int tierGap = EnergyTier.getTierGap(outputTier, machineTier);

                    Config.debugLog("    Machine at {}: tier = {}, tierGap = {}", machine.pos(), machineTier, tierGap);

                    if (tierGap > 0) {
                        // Machine can't handle this transformer side's voltage!
                        Config.debugLog("    OVERVOLTAGE! Applying consequence to machine at {}", machine.pos());
                        applyConsequence(level, machine.pos(), tierGap);
                        hadOvervoltage = true;
                    }
                }
            }
        }

        return hadOvervoltage;
    }

    
    /**
     * Scan the network starting from a position to find all connected
     * energy sources and consumers using iterative BFS (prevents stack overflow).
     */
    private static void scanNetworkFromPosition(
            Level level,
            BlockPos startPos,
            Set<BlockPos> visitedCables,
            List<TieredBlock> sources,
            List<TieredBlock> consumers
    ) {
        // Use HashSet for O(1) machine position lookups
        Set<BlockPos> visitedMachines = new HashSet<>();

        // BFS queue for iterative traversal
        Deque<BlockPos> queue = new ArrayDeque<>();

        BlockState startState = level.getBlockState(startPos);

        // If starting position is a cable, add it to queue
        if (startState.getBlock() instanceof BaseCableBlock) {
            queue.add(startPos);
        }
        // If starting position is a machine, categorize it and scan from adjacent cables
        else {
            categorizeTieredBlock(level, startPos, sources, consumers, visitedMachines);

            // Add all adjacent cable positions to queue
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = startPos.relative(dir);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof BaseCableBlock) {
                    queue.add(neighborPos);
                }
            }
        }

        int blocksScanned = 0;
        int maxScanSize = Config.MAX_OVERVOLTAGE_SCAN_SIZE.get();

        // Iterative BFS
        while (!queue.isEmpty() && blocksScanned < maxScanSize) {
            BlockPos cablePos = queue.poll();
            blocksScanned++;

            // Skip if already visited
            if (visitedCables.contains(cablePos)) continue;
            visitedCables.add(cablePos);

            BlockState state = level.getBlockState(cablePos);
            if (!(state.getBlock() instanceof BaseCableBlock)) continue;

            // Get cable block entity to check connections
            BlockEntity be = level.getBlockEntity(cablePos);
            if (!(be instanceof CableBlockEntity cable)) continue;

            // Check all 6 directions
            for (Direction dir : Direction.values()) {
                if (!cable.isConnectedInDirection(state, dir)) continue;

                BlockPos neighborPos = cablePos.relative(dir);

                // Skip already visited
                if (visitedCables.contains(neighborPos) || visitedMachines.contains(neighborPos)) {
                    continue;
                }

                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.getBlock() instanceof BaseCableBlock) {
                    // Add cable to queue for later processing
                    queue.add(neighborPos);
                } else {
                    // Check if this is a tiered block
                    categorizeTieredBlock(level, neighborPos, sources, consumers, visitedMachines);
                }
            }
        }
    }

    /**
     * Check if a block is a tiered energy block and categorize it
     * as a source or consumer. Uses HashSet for O(1) duplicate checking.
     *
     * NOTE: Transformers (IVoltageTransformer) are EXCLUDED from this categorization
     * because they bridge different voltage networks. Their whole purpose is to
     * connect incompatible voltage tiers safely.
     */
    private static void categorizeTieredBlock(
            Level level,
            BlockPos pos,
            List<TieredBlock> sources,
            List<TieredBlock> consumers,
            Set<BlockPos> visitedMachines
    ) {
        // O(1) duplicate check using HashSet
        if (visitedMachines.contains(pos)) return;
        visitedMachines.add(pos);

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IEnergyTier tieredBe)) return;

        // Skip transformers - they bridge voltage networks and should not be
        // treated as regular sources/consumers. They handle their own side-specific
        // voltage checking.
        if (be instanceof IVoltageTransformer) {
            Config.debugLog("Skipping transformer at {} in overvoltage check (bridges voltage networks)", pos);
            return;
        }

        // Check energy capability to determine if source or consumer
        IEnergyStorage energyStorage = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                pos,
                null
        );

        if (energyStorage == null) return;

        EnergyTier tier = tieredBe.getEnergyTier();

        if (energyStorage.canExtract() && !energyStorage.canReceive()) {
            // Pure source (generator)
            sources.add(new TieredBlock(pos, tier, true));
        } else if (energyStorage.canReceive() && !energyStorage.canExtract()) {
            // Pure consumer (machine)
            consumers.add(new TieredBlock(pos, tier, false));
        } else if (energyStorage.canExtract() && energyStorage.canReceive()) {
            // Bidirectional (storage block like BatBox) - acts as both
            sources.add(new TieredBlock(pos, tier, true));
            consumers.add(new TieredBlock(pos, tier, false));
        }
    }

    /**
     * Apply the appropriate overvoltage consequence based on tier gap.
     *
     * @param level The world level
     * @param machinePos The position of the machine receiving overvoltage
     * @param tierGap The tier difference (1, 2, or 3+)
     */
    public static void applyConsequence(Level level, BlockPos machinePos, int tierGap) {
        OvervoltageConsequence consequence = OvervoltageConsequence.fromTierGap(tierGap);

        switch (consequence) {
            case FIRE -> applyFireConsequence(level, machinePos);
            case EXPLOSION -> applyExplosionConsequence(level, machinePos);
            case EXPLOSION_AND_SHOCK -> applyExplosionAndShockConsequence(level, machinePos);
            default -> {} // SAFE - do nothing
        }
    }

    /**
     * Apply fire consequence (1 tier gap).
     * Sets fire around the machine, damages it but doesn't destroy immediately.
     */
    private static void applyFireConsequence(Level level, BlockPos machinePos) {
        // Set fire on all air blocks adjacent to the machine
        for (Direction dir : Direction.values()) {
            BlockPos firePos = machinePos.relative(dir);
            BlockState fireState = level.getBlockState(firePos);
            if (fireState.isAir()) {
                level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
            }
        }

        // Small "spark" particles would be nice here but fire is visible enough
        // The machine is NOT destroyed - just catches fire
        // Player can still save it if they break the connection quickly
    }

    /**
     * Apply explosion consequence (2 tier gap).
     * Destroys the machine with a small explosion.
     */
    private static void applyExplosionConsequence(Level level, BlockPos machinePos) {
        // Small explosion like IC2 (configurable size)
        float explosionRadius = Config.OVERVOLTAGE_EXPLOSION_RADIUS.get().floatValue();
        level.explode(
                null,  // No entity caused the explosion
                machinePos.getX() + 0.5,
                machinePos.getY() + 0.5,
                machinePos.getZ() + 0.5,
                explosionRadius,  // Explosion radius from config
                Level.ExplosionInteraction.BLOCK
        );
    }

    /**
     * Apply explosion + shock consequence (3+ tier gap).
     * Destroys the machine and deals lethal electrical damage to nearby players.
     */
    private static void applyExplosionAndShockConsequence(Level level, BlockPos machinePos) {
        // First, the explosion (50% larger than normal for EV)
        float explosionRadius = Config.OVERVOLTAGE_EXPLOSION_RADIUS.get().floatValue() * 1.5f;
        level.explode(
                null,
                machinePos.getX() + 0.5,
                machinePos.getY() + 0.5,
                machinePos.getZ() + 0.5,
                explosionRadius,  // Slightly larger explosion for EV
                Level.ExplosionInteraction.BLOCK
        );

        // Then, electrical shock to nearby players
        // Radius of 5 blocks for the shock
        double shockRadius = 5.0;
        AABB shockArea = new AABB(
                machinePos.getX() - shockRadius,
                machinePos.getY() - shockRadius,
                machinePos.getZ() - shockRadius,
                machinePos.getX() + shockRadius + 1,
                machinePos.getY() + shockRadius + 1,
                machinePos.getZ() + shockRadius + 1
        );

        // Find all players in range
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, shockArea);
        for (Player player : nearbyPlayers) {
            // Deal massive electrical damage (20 hearts = 40 damage)
            // This is intentionally lethal - EV is extremely dangerous
            player.hurt(level.damageSources().lightningBolt(), 40.0f);

            // Set them on fire briefly for visual effect
            player.setRemainingFireTicks(100); // 5 seconds
        }
    }

    /**
     * Helper class to store information about a tiered block.
     */
    private static class TieredBlock {
        final BlockPos pos;
        final EnergyTier tier;
        final boolean isSource;

        TieredBlock(BlockPos pos, EnergyTier tier, boolean isSource) {
            this.pos = pos;
            this.tier = tier;
            this.isSource = isSource;
        }
    }

    // ========== Stub methods for future tier implementations ==========

    /**
     * Future: Check if a cable can handle the voltage passing through it.
     * Cables will have their own voltage limits (tin = LV only, copper = LV-MV, etc.)
     *
     * @param cablePos Position of the cable
     * @param sourceTier The tier of energy passing through
     * @return true if the cable can handle it
     */
    public static boolean canCableHandleVoltage(Level level, BlockPos cablePos, EnergyTier sourceTier) {
        // TODO: Implement when cable voltage limits are added
        // For now, all cables can handle all voltages
        Config.debugLog("Cable voltage check at {} for tier {} - not yet implemented (returning true)",
                cablePos, sourceTier);
        return true;
    }

    /**
     * Future: Handle cable meltdown when voltage exceeds its limit.
     *
     * @param cablePos Position of the cable
     * @param tierGap How much the voltage exceeds the cable's limit
     */
    public static void handleCableOvervoltage(Level level, BlockPos cablePos, int tierGap) {
        // TODO: Implement when cable voltage limits are added
        // Ideas:
        // - 1 tier over: Cable catches fire, needs replacement
        // - 2+ tiers over: Cable burns/melts instantly
        Config.debugLog("Cable overvoltage at {} with tier gap {} - not yet implemented (no action taken)",
                cablePos, tierGap);
    }

    /**
     * Future: Get the voltage tier a cable can safely carry.
     *
     * @param cableBlock The cable block
     * @return The maximum tier this cable supports
     */
    public static EnergyTier getCableMaxTier(BaseCableBlock cableBlock) {
        // TODO: Implement when cable voltage limits are added
        // Planned tiers:
        // - Tin Cable: LV only
        // - Copper Cable: LV-MV
        // - Gold Cable: LV-HV
        // - Glass Fibre: All tiers (LV-EV)
        Config.debugLog("getCableMaxTier called for {} - not yet implemented (returning EV)",
                cableBlock.getClass().getSimpleName());
        return EnergyTier.EV; // For now, all cables support all tiers
    }
}
