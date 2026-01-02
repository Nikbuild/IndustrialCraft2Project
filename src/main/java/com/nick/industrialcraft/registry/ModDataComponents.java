package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.item.CannedFoodData;
import com.nick.industrialcraft.content.item.StoredEnergyData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

/**
 * Registry for custom data components used by IndustrialCraft items.
 */
public final class ModDataComponents {

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, IndustrialCraft.MODID);

    /**
     * Canned food data component - stores source item, food value, and saturation.
     * Items only stack if this component matches exactly.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CannedFoodData>> CANNED_FOOD =
            DATA_COMPONENTS.registerComponentType(
                    "canned_food",
                    builder -> builder
                            .persistent(CannedFoodData.CODEC)
                            .networkSynchronized(CannedFoodData.STREAM_CODEC)
            );

    /**
     * Stored energy data component - stores energy value for machine block items.
     * When a machine is wrenched, its stored energy is preserved in this component.
     * Items only stack if the stored energy matches exactly.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoredEnergyData>> STORED_ENERGY =
            DATA_COMPONENTS.registerComponentType(
                    "stored_energy",
                    builder -> builder
                            .persistent(StoredEnergyData.CODEC)
                            .networkSynchronized(StoredEnergyData.STREAM_CODEC)
            );
}
