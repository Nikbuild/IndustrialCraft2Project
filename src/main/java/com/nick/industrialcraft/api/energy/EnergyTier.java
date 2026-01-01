package com.nick.industrialcraft.api.energy;

/**
 * Energy tier system based on IC2's voltage tiers.
 *
 * In IC2, "voltage" was really packet size - the maximum EU that could be
 * transferred in a single tick. Machines explode if they receive a packet
 * larger than their tier allows.
 *
 * Tier progression:
 * - LV (Low Voltage): Basic machines, BatBox, basic generators
 * - MV (Medium Voltage): Advanced machines, MFE, geothermal
 * - HV (High Voltage): Industrial machines, MFSU
 * - EV (Extreme Voltage): End-game machines, fusion reactor
 */
public enum EnergyTier {

    /** Low Voltage - 32 EU/t max packet size */
    LV(1, 32, "Low Voltage"),

    /** Medium Voltage - 128 EU/t max packet size */
    MV(2, 128, "Medium Voltage"),

    /** High Voltage - 512 EU/t max packet size */
    HV(3, 512, "High Voltage"),

    /** Extreme Voltage - 2048 EU/t max packet size */
    EV(4, 2048, "Extreme Voltage");

    private final int tier;
    private final int maxPacketSize;
    private final String displayName;

    EnergyTier(int tier, int maxPacketSize, String displayName) {
        this.tier = tier;
        this.maxPacketSize = maxPacketSize;
        this.displayName = displayName;
    }

    /**
     * Get the numeric tier level (1-4).
     */
    public int getTierLevel() {
        return tier;
    }

    /**
     * Get the maximum packet size (EU/t) this tier can handle.
     * Packets larger than this will cause machines to explode.
     */
    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    /**
     * Get the display name for this tier.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if this tier can safely receive a packet of the given size.
     * @param packetSize The incoming packet size in EU/t
     * @return true if safe, false if the packet would cause an explosion
     */
    public boolean canReceivePacket(int packetSize) {
        return packetSize <= maxPacketSize;
    }

    /**
     * Get the tier from a packet size.
     * Returns the minimum tier required to handle the given packet size.
     */
    public static EnergyTier fromPacketSize(int packetSize) {
        if (packetSize <= LV.maxPacketSize) return LV;
        if (packetSize <= MV.maxPacketSize) return MV;
        if (packetSize <= HV.maxPacketSize) return HV;
        return EV;
    }

    /**
     * Get the tier from the tier level number (1-4).
     */
    public static EnergyTier fromLevel(int level) {
        return switch (level) {
            case 1 -> LV;
            case 2 -> MV;
            case 3 -> HV;
            case 4 -> EV;
            default -> LV;
        };
    }

    /**
     * Calculate the tier gap between a source tier and a consumer tier.
     * Positive values indicate overvoltage (source is higher tier than consumer).
     * Zero or negative means safe (consumer can handle the voltage).
     *
     * @param sourceTier The tier of the energy source
     * @param consumerTier The tier of the energy consumer
     * @return The tier gap (positive = overvoltage danger)
     */
    public static int getTierGap(EnergyTier sourceTier, EnergyTier consumerTier) {
        return sourceTier.tier - consumerTier.tier;
    }

    /**
     * Check if a source tier would cause overvoltage to a consumer tier.
     *
     * @param sourceTier The tier of the energy source
     * @param consumerTier The tier of the energy consumer
     * @return true if the source would overvolt the consumer
     */
    public static boolean wouldOvervolt(EnergyTier sourceTier, EnergyTier consumerTier) {
        return getTierGap(sourceTier, consumerTier) > 0;
    }
}
