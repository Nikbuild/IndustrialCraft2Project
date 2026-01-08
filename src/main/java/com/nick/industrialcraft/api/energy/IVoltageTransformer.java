package com.nick.industrialcraft.api.energy;

import net.minecraft.core.Direction;

/**
 * Marker interface for voltage transformers.
 *
 * Transformers bridge different voltage networks and should NOT be treated
 * as regular sources/consumers in overvoltage checks. They have different
 * voltage tiers on different sides.
 *
 * Implement this interface to exclude a block from standard overvoltage
 * source/consumer pairing during placement checks.
 */
public interface IVoltageTransformer {

    /**
     * Get the voltage tier for a specific side of the transformer.
     *
     * @param side The side being queried
     * @return The energy tier for that side
     */
    EnergyTier getTierForSide(Direction side);

    /**
     * Check if energy can be safely received on a specific side.
     *
     * @param side The side receiving energy
     * @param packetSize The size of the energy packet
     * @return true if the side can safely handle this packet size
     */
    boolean canSideReceive(Direction side, int packetSize);
}
