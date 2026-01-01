package com.nick.industrialcraft.api.energy;

/**
 * Interface for blocks/block entities that have an energy tier.
 *
 * Implementing this allows the energy system to check whether
 * incoming energy packets are safe for the machine to receive.
 */
public interface IEnergyTier {

    /**
     * Get the energy tier of this block.
     * For energy consumers: the maximum input tier they can safely receive.
     * For energy producers: the tier they output at.
     * For storage blocks: their operating tier.
     *
     * @return The energy tier of this block
     */
    EnergyTier getEnergyTier();

    /**
     * Check if this machine can safely receive the given packet size.
     * Only relevant for energy consumers.
     *
     * @param packetSize The incoming packet size in EU/t
     * @return true if the packet is safe, false if it would cause explosion
     */
    default boolean canSafelyReceive(int packetSize) {
        return getEnergyTier().canReceivePacket(packetSize);
    }

    /**
     * Get the output packet size this block sends.
     * Only relevant for energy producers.
     * Default implementation returns the tier's max packet size.
     *
     * @return The packet size in EU/t
     */
    default int getOutputPacketSize() {
        return getEnergyTier().getMaxPacketSize();
    }
}
