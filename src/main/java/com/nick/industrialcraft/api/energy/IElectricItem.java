package com.nick.industrialcraft.api.energy;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for IC2-style electric items (tools, batteries, etc.)
 *
 * Based on original IC2's IElectricItem interface.
 * Items implementing this can be charged in machines with charging slots
 * (BatBox, MFE, MFSU, Generators, etc.)
 *
 * The tier system prevents charging items in machines of lower tier
 * (e.g., can't charge an MV item in an LV BatBox).
 */
public interface IElectricItem {

    /**
     * Get the maximum energy capacity of this item in EU.
     * @param stack The item stack
     * @return Maximum energy storage
     */
    int getMaxCharge(ItemStack stack);

    /**
     * Get the current stored energy in this item.
     * @param stack The item stack
     * @return Current energy stored
     */
    int getCharge(ItemStack stack);

    /**
     * Set the stored energy in this item.
     * @param stack The item stack
     * @param charge The energy to set
     */
    void setCharge(ItemStack stack, int charge);

    /**
     * Get the maximum transfer rate per tick for this item.
     * This is the maximum EU/t that can be charged or discharged.
     * @param stack The item stack
     * @return Transfer limit in EU/t
     */
    int getTransferLimit(ItemStack stack);

    /**
     * Get the energy tier of this item.
     * Items can only be charged in machines of equal or higher tier.
     * @param stack The item stack
     * @return The energy tier
     */
    EnergyTier getTier(ItemStack stack);

    /**
     * Check if this item can provide energy (discharge).
     * Tools typically return false (consume-only).
     * Batteries return true (can discharge into machines).
     * @param stack The item stack
     * @return true if this item can provide energy
     */
    boolean canProvideEnergy(ItemStack stack);

    /**
     * Charge this item with energy.
     * @param stack The item stack to charge
     * @param amount The amount of EU to add
     * @param tier The tier of the charging source (items with higher tier than source cannot be charged)
     * @param ignoreTransferLimit If true, ignore the transfer limit
     * @param simulate If true, don't actually modify the item
     * @return The amount of EU actually accepted
     */
    default int charge(ItemStack stack, int amount, EnergyTier tier, boolean ignoreTransferLimit, boolean simulate) {
        // Can't charge if source tier is lower than item tier
        if (tier.getTierLevel() < getTier(stack).getTierLevel()) {
            return 0;
        }

        int current = getCharge(stack);
        int max = getMaxCharge(stack);
        int limit = ignoreTransferLimit ? Integer.MAX_VALUE : getTransferLimit(stack);

        int toAccept = Math.min(amount, Math.min(limit, max - current));
        if (toAccept <= 0) return 0;

        if (!simulate) {
            setCharge(stack, current + toAccept);
        }

        return toAccept;
    }

    /**
     * Discharge energy from this item.
     * @param stack The item stack to discharge
     * @param amount The amount of EU to extract
     * @param tier The tier of the discharging target
     * @param ignoreTransferLimit If true, ignore the transfer limit
     * @param simulate If true, don't actually modify the item
     * @return The amount of EU actually extracted
     */
    default int discharge(ItemStack stack, int amount, EnergyTier tier, boolean ignoreTransferLimit, boolean simulate) {
        if (!canProvideEnergy(stack)) {
            return 0;
        }

        int current = getCharge(stack);
        int limit = ignoreTransferLimit ? Integer.MAX_VALUE : getTransferLimit(stack);

        int toExtract = Math.min(amount, Math.min(limit, current));
        if (toExtract <= 0) return 0;

        if (!simulate) {
            setCharge(stack, current - toExtract);
        }

        return toExtract;
    }
}
