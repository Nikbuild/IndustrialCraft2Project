package com.nick.industrialcraft.api.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;
import java.util.function.IntConsumer;

/**
 * A reusable, configurable energy storage implementation.
 *
 * Reduces code duplication across machine block entities by providing
 * a single implementation that can be configured for different use cases:
 * - Machines (receive only)
 * - Generators (extract only)
 * - Storage blocks like BatBox (both receive and extract)
 *
 * Usage example:
 * <pre>
 * private final SimpleEnergyStorage energyStorage = SimpleEnergyStorage.receiverOnly(
 *     () -> this.energy,
 *     e -> { this.energy = e; setChanged(); },
 *     MAX_ENERGY,
 *     MAX_RECEIVE
 * );
 * </pre>
 */
public class SimpleEnergyStorage implements IEnergyStorage {

    private final IntSupplier energyGetter;
    private final IntConsumer energySetter;
    private final int maxEnergy;
    private final int maxReceive;
    private final int maxExtract;
    private final boolean canReceive;
    private final boolean canExtract;

    private SimpleEnergyStorage(
            IntSupplier energyGetter,
            IntConsumer energySetter,
            int maxEnergy,
            int maxReceive,
            int maxExtract,
            boolean canReceive,
            boolean canExtract
    ) {
        this.energyGetter = energyGetter;
        this.energySetter = energySetter;
        this.maxEnergy = maxEnergy;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.canReceive = canReceive;
        this.canExtract = canExtract;
    }

    /**
     * Create an energy storage that can only receive energy (for machines).
     *
     * @param energyGetter Supplier to get current energy level
     * @param energySetter Consumer to set energy level (should call setChanged())
     * @param maxEnergy Maximum energy capacity
     * @param maxReceive Maximum energy to receive per tick
     */
    public static SimpleEnergyStorage receiverOnly(
            IntSupplier energyGetter,
            IntConsumer energySetter,
            int maxEnergy,
            int maxReceive
    ) {
        return new SimpleEnergyStorage(
                energyGetter, energySetter, maxEnergy,
                maxReceive, 0, true, false
        );
    }

    /**
     * Create an energy storage that can only extract energy (for generators).
     *
     * @param energyGetter Supplier to get current energy level
     * @param energySetter Consumer to set energy level (should call setChanged())
     * @param maxEnergy Maximum energy capacity
     * @param maxExtract Maximum energy to extract per tick
     */
    public static SimpleEnergyStorage extractorOnly(
            IntSupplier energyGetter,
            IntConsumer energySetter,
            int maxEnergy,
            int maxExtract
    ) {
        return new SimpleEnergyStorage(
                energyGetter, energySetter, maxEnergy,
                0, maxExtract, false, true
        );
    }

    /**
     * Create an energy storage that can both receive and extract energy (for storage blocks).
     *
     * @param energyGetter Supplier to get current energy level
     * @param energySetter Consumer to set energy level (should call setChanged())
     * @param maxEnergy Maximum energy capacity
     * @param maxTransfer Maximum energy to transfer per tick (both receive and extract)
     */
    public static SimpleEnergyStorage bidirectional(
            IntSupplier energyGetter,
            IntConsumer energySetter,
            int maxEnergy,
            int maxTransfer
    ) {
        return new SimpleEnergyStorage(
                energyGetter, energySetter, maxEnergy,
                maxTransfer, maxTransfer, true, true
        );
    }

    /**
     * Create an energy storage with full customization.
     *
     * @param energyGetter Supplier to get current energy level
     * @param energySetter Consumer to set energy level (should call setChanged())
     * @param maxEnergy Maximum energy capacity
     * @param maxReceive Maximum energy to receive per tick
     * @param maxExtract Maximum energy to extract per tick
     * @param canReceive Whether this storage can receive energy
     * @param canExtract Whether this storage can extract energy
     */
    public static SimpleEnergyStorage custom(
            IntSupplier energyGetter,
            IntConsumer energySetter,
            int maxEnergy,
            int maxReceive,
            int maxExtract,
            boolean canReceive,
            boolean canExtract
    ) {
        return new SimpleEnergyStorage(
                energyGetter, energySetter, maxEnergy,
                maxReceive, maxExtract, canReceive, canExtract
        );
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive) return 0;

        int currentEnergy = energyGetter.getAsInt();
        if (currentEnergy >= maxEnergy) return 0;

        int toReceive = Math.min(maxReceive, Math.min(this.maxReceive, maxEnergy - currentEnergy));

        if (!simulate && toReceive > 0) {
            energySetter.accept(currentEnergy + toReceive);
        }

        return toReceive;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract) return 0;

        int currentEnergy = energyGetter.getAsInt();
        if (currentEnergy <= 0) return 0;

        int toExtract = Math.min(maxExtract, Math.min(this.maxExtract, currentEnergy));

        if (!simulate && toExtract > 0) {
            energySetter.accept(currentEnergy - toExtract);
        }

        return toExtract;
    }

    @Override
    public int getEnergyStored() {
        return energyGetter.getAsInt();
    }

    @Override
    public int getMaxEnergyStored() {
        return maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return canExtract;
    }

    @Override
    public boolean canReceive() {
        return canReceive;
    }
}
