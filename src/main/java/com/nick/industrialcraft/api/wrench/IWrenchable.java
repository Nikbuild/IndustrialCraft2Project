package com.nick.industrialcraft.api.wrench;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Interface for machines that can be interacted with using a wrench.
 * Machines implementing this interface can be rotated and safely removed.
 */
public interface IWrenchable {

    /**
     * Check if this machine can be rotated to the given facing direction.
     * @param player The player attempting to rotate
     * @param newFacing The desired facing direction
     * @return true if the rotation is allowed
     */
    boolean canWrenchRotate(Player player, Direction newFacing);

    /**
     * Get the current facing direction of this machine.
     * @return The current facing direction
     */
    Direction getFacing();

    /**
     * Set the facing direction of this machine.
     * @param facing The new facing direction
     */
    void setFacing(Direction facing);

    /**
     * Check if this machine can be removed by the player with a wrench.
     * @param player The player attempting to remove
     * @return true if removal is allowed
     */
    boolean canWrenchRemove(Player player);

    /**
     * Get the current stored energy in this machine.
     * Used when dropping the machine to preserve energy in the item.
     * @return The stored energy in EU
     */
    int getStoredEnergy();

    /**
     * Set the stored energy in this machine.
     * Used when placing a machine that has stored energy in its item form.
     * @param energy The energy to set in EU
     */
    void setStoredEnergy(int energy);

    /**
     * Get the maximum energy this machine can store.
     * @return The maximum energy capacity in EU
     */
    int getMaxStoredEnergy();

    /**
     * Create an ItemStack representing this machine with its current state.
     * The ItemStack should include stored energy data if applicable.
     * @return The ItemStack for this machine
     */
    ItemStack createWrenchDrop();

    /**
     * Get the time in ticks required to wrench-remove this machine.
     * Default implementation returns 120 ticks (6 seconds) for regular wrench.
     * @param isElectricWrench true if using electric wrench
     * @return Time in ticks to remove
     */
    default int getWrenchRemoveTime(boolean isElectricWrench) {
        return isElectricWrench ? 40 : 120;  // 2 seconds for electric, 6 for regular
    }
}
