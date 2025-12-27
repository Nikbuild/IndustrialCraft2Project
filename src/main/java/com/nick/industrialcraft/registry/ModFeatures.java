package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Worldgen feature registration scaffold for IndustrialCraft.
 */
public final class ModFeatures {

    private ModFeatures() {}

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, IndustrialCraft.MODID);
}
