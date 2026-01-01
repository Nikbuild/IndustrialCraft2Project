package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.item.CannedFoodData;
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
}
