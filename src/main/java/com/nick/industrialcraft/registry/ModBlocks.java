package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.cable.CopperCableBlock;
import com.nick.industrialcraft.content.block.cable.GlassFiberCableBlock;
import com.nick.industrialcraft.content.block.cable.GoldCableBlock;
import com.nick.industrialcraft.content.block.cable.GoldCableDoubleInsulatedBlock;
import com.nick.industrialcraft.content.block.cable.GoldCableInsulatedBlock;
import com.nick.industrialcraft.content.block.cable.HighVoltageCableBlock;
import com.nick.industrialcraft.content.block.cable.HighVoltageCableDoubleInsulatedBlock;
import com.nick.industrialcraft.content.block.cable.HighVoltageCableInsulatedBlock;
import com.nick.industrialcraft.content.block.cable.HighVoltageCableQuadrupleInsulatedBlock;
import com.nick.industrialcraft.content.block.cable.InsulatedCopperCableBlock;
import com.nick.industrialcraft.content.block.cable.UltraLowCurrentCableBlock;
import com.nick.industrialcraft.content.block.generator.GeneratorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * All IndustrialCraft 2 blocks.
 *
 * Block categories:
 *  - Ores (copper, tin, uranium)
 *  - Rubber tree blocks (wood, leaves, sapling)
 *  - Decoration/Materials (resin, rubber sheet, reinforced blocks, foam, scaffolds, metal)
 *  - Technical blocks (cables, generators, machines, reactors)
 *  - Explosives (TNT, nuke, dynamite)
 *  - Agriculture (crops, barrels)
 */
public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(IndustrialCraft.MODID);

    /* --------------------------------------------------------------------- */
    /* Ores                                                                   */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> COPPER_ORE = BLOCKS.register(
            "copper_ore",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0f, 5.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "copper_ore"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> TIN_ORE = BLOCKS.register(
            "tin_ore",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0f, 5.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "tin_ore"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> URANIUM_ORE = BLOCKS.register(
            "uranium_ore",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(4.0f, 6.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "uranium_ore"
                                    )
                            ))
            )
    );

    /* --------------------------------------------------------------------- */
    /* Rubber Tree                                                            */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> RUBBER_WOOD = BLOCKS.register(
            "rubber_wood",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.0f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "rubber_wood"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> RUBBER_LEAVES = BLOCKS.register(
            "rubber_leaves",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .strength(0.2f)
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "rubber_leaves"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> RUBBER_SAPLING = BLOCKS.register(
            "rubber_sapling",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .noCollission()
                            .strength(0.0f)
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "rubber_sapling"
                                    )
                            ))
            )
    );

    /* --------------------------------------------------------------------- */
    /* Decoration / Materials                                                 */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> RESIN_BLOCK = BLOCKS.register(
            "resin_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_YELLOW)
                            .strength(1.0f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "resin_block"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> RUBBER_SHEET = BLOCKS.register(
            "rubber_sheet",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .strength(1.5f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "rubber_sheet"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> REINFORCED_STONE = BLOCKS.register(
            "reinforced_stone",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(80.0f, 150.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "reinforced_stone"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> REINFORCED_GLASS = BLOCKS.register(
            "reinforced_glass",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(5.0f, 150.0f)
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "reinforced_glass"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> REINFORCED_DOOR_BLOCK = BLOCKS.register(
            "reinforced_door_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(50.0f, 150.0f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "reinforced_door_block"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> FOAM = BLOCKS.register(
            "foam",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.SNOW)
                            .strength(0.5f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "foam"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> WALL = BLOCKS.register(
            "wall",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0f, 10.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "wall"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> SCAFFOLD = BLOCKS.register(
            "scaffold",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(1.0f)
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "scaffold"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> IRON_SCAFFOLD = BLOCKS.register(
            "iron_scaffold",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.0f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "iron_scaffold"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> METAL_BLOCK = BLOCKS.register(
            "metal_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(5.0f, 10.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "metal_block"
                                    )
                            ))
            )
    );

    /* --------------------------------------------------------------------- */
    /* Technical Blocks - Cables                                              */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> COPPER_CABLE = BLOCKS.register(
            "copper_cable",
            () -> new CopperCableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "copper_cable"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> INSULATED_COPPER_CABLE = BLOCKS.register(
            "insulated_copper_cable",
            () -> new InsulatedCopperCableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "insulated_copper_cable"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> GOLD_CABLE = BLOCKS.register(
            "gold_cable",
            () -> new GoldCableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "gold_cable"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> GOLD_CABLE_INSULATED = BLOCKS.register(
            "gold_cable_insulated",
            () -> new GoldCableInsulatedBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "gold_cable_insulated"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> GOLD_CABLE_DOUBLE_INSULATED = BLOCKS.register(
            "gold_cable_double_insulated",
            () -> new GoldCableDoubleInsulatedBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "gold_cable_double_insulated"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> HIGH_VOLTAGE_CABLE = BLOCKS.register(
            "high_voltage_cable",
            () -> new HighVoltageCableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "high_voltage_cable"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> HIGH_VOLTAGE_CABLE_INSULATED = BLOCKS.register(
            "high_voltage_cable_insulated",
            () -> new HighVoltageCableInsulatedBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "high_voltage_cable_insulated"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> HIGH_VOLTAGE_CABLE_DOUBLE_INSULATED = BLOCKS.register(
            "high_voltage_cable_double_insulated",
            () -> new HighVoltageCableDoubleInsulatedBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "high_voltage_cable_double_insulated"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> HIGH_VOLTAGE_CABLE_QUADRUPLE_INSULATED = BLOCKS.register(
            "high_voltage_cable_quadruple_insulated",
            () -> new HighVoltageCableQuadrupleInsulatedBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "high_voltage_cable_quadruple_insulated"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> GLASS_FIBER_CABLE = BLOCKS.register(
            "glass_fiber_cable",
            () -> new GlassFiberCableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "glass_fiber_cable"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> ULTRA_LOW_CURRENT_CABLE = BLOCKS.register(
            "ultra_low_current_cable",
            () -> new UltraLowCurrentCableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(0.5f)
                            .noOcclusion()
                            .isRedstoneConductor((s, l, p) -> false)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "ultra_low_current_cable"
                                    )
                            ))
            )
    );

    /* --------------------------------------------------------------------- */
    /* Technical Blocks - Generators & Machines                               */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> GENERATOR = BLOCKS.register(
            "generator",
            () -> new GeneratorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "generator"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> REACTOR_CHAMBER = BLOCKS.register(
            "reactor_chamber",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "reactor_chamber"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> ELECTRIC_BLOCK = BLOCKS.register(
            "electric_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "electric_block"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> MACHINE = BLOCKS.register(
            "machine",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "machine"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> MACHINE2 = BLOCKS.register(
            "machine2",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.5f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "machine2"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> LUMINATOR = BLOCKS.register(
            "luminator",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.QUARTZ)
                            .strength(1.0f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "luminator"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> ACTIVE_LUMINATOR = BLOCKS.register(
            "active_luminator",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.QUARTZ)
                            .strength(1.0f)
                            .lightLevel(state -> 15)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "active_luminator"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> MINING_PIPE = BLOCKS.register(
            "mining_pipe",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.0f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "mining_pipe"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> MINING_TIP = BLOCKS.register(
            "mining_tip",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(2.5f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "mining_tip"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> PERSONAL_BLOCK = BLOCKS.register(
            "personal_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "personal_block"
                                    )
                            ))
            )
    );

    /* --------------------------------------------------------------------- */
    /* Explosives                                                             */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> INDUSTRIAL_TNT = BLOCKS.register(
            "industrial_tnt",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.FIRE)
                            .strength(0.0f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "industrial_tnt"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> NUKE = BLOCKS.register(
            "nuke",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.FIRE)
                            .strength(0.0f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "nuke"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> DYNAMITE_STICK = BLOCKS.register(
            "dynamite_stick",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.FIRE)
                            .strength(0.0f)
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "dynamite_stick"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> DYNAMITE_STICK_WITH_REMOTE = BLOCKS.register(
            "dynamite_stick_with_remote",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.FIRE)
                            .strength(0.0f)
                            .noOcclusion()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "dynamite_stick_with_remote"
                                    )
                            ))
            )
    );

    /* --------------------------------------------------------------------- */
    /* Agriculture                                                            */
    /* --------------------------------------------------------------------- */

    public static final DeferredBlock<Block> CROP_BLOCK = BLOCKS.register(
            "crop_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.PLANT)
                            .strength(0.5f)
                            .noOcclusion()
                            .noCollission()
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "crop_block"
                                    )
                            ))
            )
    );

    public static final DeferredBlock<Block> BARREL_BLOCK = BLOCKS.register(
            "barrel_block",
            () -> new Block(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(2.5f)
                            .setId(ResourceKey.create(
                                    Registries.BLOCK,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID,
                                            "barrel_block"
                                    )
                            ))
            )
    );
}
