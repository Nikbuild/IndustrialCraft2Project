package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.worldgen.RubberTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Worldgen feature registration for IndustrialCraft.
 */
public final class ModFeatures {

    private ModFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, IndustrialCraft.MODID);

    public static final DeferredHolder<Feature<?>, RubberTreeFeature> RUBBER_TREE =
            FEATURES.register("rubber_tree",
                    () -> new RubberTreeFeature(NoneFeatureConfiguration.CODEC));
}
