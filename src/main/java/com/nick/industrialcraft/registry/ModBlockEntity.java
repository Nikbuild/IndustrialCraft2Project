package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeneratorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

/**
 * BlockEntityType registration scaffold for IndustrialCraft.
 */
public final class ModBlockEntity {

    private ModBlockEntity() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, IndustrialCraft.MODID);

    // Back-compat alias
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = BLOCK_ENTITY_TYPES;

    // Generator Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorBlockEntity>> GENERATOR =
            BLOCK_ENTITY_TYPES.register("generator", () ->
                    new BlockEntityType<>(GeneratorBlockEntity::new, Set.of(ModBlocks.GENERATOR.get()))
            );
}
