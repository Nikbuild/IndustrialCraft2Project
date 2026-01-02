package com.nick.industrialcraft.content.item;

import com.nick.industrialcraft.api.energy.IElectricItem;
import com.nick.industrialcraft.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Registers item energy capabilities for all IElectricItem implementations.
 * This allows electric items to be charged/discharged through the standard
 * NeoForge energy capability system.
 */
public class ElectricItemCapability {

    /**
     * Register energy capabilities for all electric items.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Electric Wrench
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, context) -> createEnergyStorage(stack),
            ModItems.ELECTRIC_WRENCH.get()
        );

        // Add more electric items here as they are created:
        // event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> createEnergyStorage(stack), ModItems.MINING_DRILL.get());
        // event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> createEnergyStorage(stack), ModItems.DIAMOND_DRILL.get());
        // event.registerItem(Capabilities.EnergyStorage.ITEM, (stack, context) -> createEnergyStorage(stack), ModItems.CHAINSAW.get());
        // etc.
    }

    /**
     * Create an IEnergyStorage wrapper for an IElectricItem.
     * This bridges the IC2-style IElectricItem interface with NeoForge's capability system.
     */
    private static IEnergyStorage createEnergyStorage(ItemStack stack) {
        if (!(stack.getItem() instanceof IElectricItem electricItem)) {
            return null;
        }

        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                // Use the IElectricItem's charge method with LV tier (machines will pass their own tier)
                // For now, assume charging comes from at least LV tier
                return electricItem.charge(stack, maxReceive,
                    electricItem.getTier(stack),  // Use item's own tier as minimum
                    false, simulate);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if (!electricItem.canProvideEnergy(stack)) {
                    return 0;
                }
                return electricItem.discharge(stack, maxExtract,
                    electricItem.getTier(stack),
                    false, simulate);
            }

            @Override
            public int getEnergyStored() {
                return electricItem.getCharge(stack);
            }

            @Override
            public int getMaxEnergyStored() {
                return electricItem.getMaxCharge(stack);
            }

            @Override
            public boolean canExtract() {
                return electricItem.canProvideEnergy(stack);
            }

            @Override
            public boolean canReceive() {
                return electricItem.getCharge(stack) < electricItem.getMaxCharge(stack);
            }
        };
    }
}
