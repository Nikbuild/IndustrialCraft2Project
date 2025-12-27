package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Centralized block tag keys used by the mod. */
public final class ModTags {

    private ModTags() {}

    /** Any block that machines should auto-connect to for power. */
    public static final TagKey<Block> POWER_ACCEPTORS =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "power_acceptors")
            );
}
