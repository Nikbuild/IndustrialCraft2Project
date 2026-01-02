package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeneratorBlockEntity;
import com.nick.industrialcraft.content.block.generator.GeothermalGeneratorBlockEntity;
import com.nick.industrialcraft.content.block.machine.ElectricFurnaceBlockEntity;
import com.nick.industrialcraft.content.block.machine.MaceratorBlockEntity;
import com.nick.industrialcraft.content.block.machine.ExtractorBlockEntity;
import com.nick.industrialcraft.content.block.machine.CompressorBlockEntity;
import com.nick.industrialcraft.content.block.machine.RecyclerBlockEntity;
import com.nick.industrialcraft.content.block.machine.InductionFurnaceBlockEntity;
import com.nick.industrialcraft.content.block.machine.CanningMachineBlockEntity;
import com.nick.industrialcraft.content.block.machine.IronFurnaceBlockEntity;
import com.nick.industrialcraft.content.block.storage.BatBoxBlockEntity;
import com.nick.industrialcraft.content.block.cable.CableBlockEntity;
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
                    new BlockEntityType<>(GeneratorBlockEntity::new, Set.of(
                            ModBlocks.GENERATOR.get()
                    ))
            );

    // Geothermal Generator Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeothermalGeneratorBlockEntity>> GEOTHERMAL_GENERATOR =
            BLOCK_ENTITY_TYPES.register("geothermal_generator", () ->
                    new BlockEntityType<>(GeothermalGeneratorBlockEntity::new, Set.of(
                            ModBlocks.GEOTHERMAL_GENERATOR.get()
                    ))
            );

    // Electric Furnace Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFurnaceBlockEntity>> ELECTRIC_FURNACE =
            BLOCK_ENTITY_TYPES.register("electric_furnace", () ->
                    new BlockEntityType<>(ElectricFurnaceBlockEntity::new, Set.of(
                            ModBlocks.ELECTRIC_FURNACE.get()
                    ))
            );

    // Macerator Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaceratorBlockEntity>> MACERATOR =
            BLOCK_ENTITY_TYPES.register("macerator", () ->
                    new BlockEntityType<>(MaceratorBlockEntity::new, Set.of(
                            ModBlocks.MACERATOR.get()
                    ))
            );

    // Extractor Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractorBlockEntity>> EXTRACTOR =
            BLOCK_ENTITY_TYPES.register("extractor", () ->
                    new BlockEntityType<>(ExtractorBlockEntity::new, Set.of(
                            ModBlocks.EXTRACTOR.get()
                    ))
            );

    // Compressor Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CompressorBlockEntity>> COMPRESSOR =
            BLOCK_ENTITY_TYPES.register("compressor", () ->
                    new BlockEntityType<>(CompressorBlockEntity::new, Set.of(
                            ModBlocks.COMPRESSOR.get()
                    ))
            );

    // Recycler Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RecyclerBlockEntity>> RECYCLER =
            BLOCK_ENTITY_TYPES.register("recycler", () ->
                    new BlockEntityType<>(RecyclerBlockEntity::new, Set.of(
                            ModBlocks.RECYCLER.get()
                    ))
            );

    // Induction Furnace Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InductionFurnaceBlockEntity>> INDUCTION_FURNACE =
            BLOCK_ENTITY_TYPES.register("induction_furnace", () ->
                    new BlockEntityType<>(InductionFurnaceBlockEntity::new, Set.of(
                            ModBlocks.INDUCTION_FURNACE.get()
                    ))
            );

    // BatBox Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BatBoxBlockEntity>> BATBOX =
            BLOCK_ENTITY_TYPES.register("batbox", () ->
                    new BlockEntityType<>(BatBoxBlockEntity::new, Set.of(
                            ModBlocks.BATBOX.get()
                    ))
            );

    // Canning Machine Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CanningMachineBlockEntity>> CANNING_MACHINE =
            BLOCK_ENTITY_TYPES.register("canning_machine", () ->
                    new BlockEntityType<>(CanningMachineBlockEntity::new, Set.of(
                            ModBlocks.CANNING_MACHINE.get()
                    ))
            );

    // Iron Furnace Block Entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IronFurnaceBlockEntity>> IRON_FURNACE =
            BLOCK_ENTITY_TYPES.register("iron_furnace", () ->
                    new BlockEntityType<>(IronFurnaceBlockEntity::new, Set.of(
                            ModBlocks.IRON_FURNACE.get()
                    ))
            );

    // Cable Block Entity (shared by all cable types)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CableBlockEntity>> CABLE =
            BLOCK_ENTITY_TYPES.register("cable", () ->
                    new BlockEntityType<>(CableBlockEntity::new, Set.of(
                            ModBlocks.COPPER_CABLE.get(),
                            ModBlocks.INSULATED_COPPER_CABLE.get(),
                            ModBlocks.GOLD_CABLE.get(),
                            ModBlocks.GOLD_CABLE_INSULATED.get(),
                            ModBlocks.GOLD_CABLE_DOUBLE_INSULATED.get(),
                            ModBlocks.HIGH_VOLTAGE_CABLE.get(),
                            ModBlocks.HIGH_VOLTAGE_CABLE_INSULATED.get(),
                            ModBlocks.HIGH_VOLTAGE_CABLE_DOUBLE_INSULATED.get(),
                            ModBlocks.HIGH_VOLTAGE_CABLE_QUADRUPLE_INSULATED.get(),
                            ModBlocks.GLASS_FIBER_CABLE.get(),
                            ModBlocks.ULTRA_LOW_CURRENT_CABLE.get()
                    ))
            );
}
