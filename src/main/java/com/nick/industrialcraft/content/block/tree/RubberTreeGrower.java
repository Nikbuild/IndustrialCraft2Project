package com.nick.industrialcraft.content.block.tree;

import com.nick.industrialcraft.IndustrialCraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Optional;

/**
 * Tree grower for rubber trees.
 * This hooks into Minecraft's tree growing system and references our configured feature.
 */
public class RubberTreeGrower {
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_FEATURE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "rubber_tree"));

    public static final TreeGrower RUBBER = new TreeGrower(
            "industrialcraft_rubber",
            Optional.empty(),           // No mega tree variant
            Optional.of(RUBBER_TREE_FEATURE),  // Normal tree
            Optional.empty()             // No flower variant
    );
}
