package com.nick.industrialcraft;

import com.mojang.logging.LogUtils;
import com.nick.industrialcraft.registry.ModBlocks;
import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModItems;
import com.nick.industrialcraft.registry.ModMenus;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(IndustrialCraft.MODID)
public class IndustrialCraft {

    public static final String MODID = "industrialcraft";
    public static final Logger LOGGER = LogUtils.getLogger();

    /* --------------------------------------------------------------------- */
    /* Creative Tabs                                                         */
    /* --------------------------------------------------------------------- */

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("main",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.industrialcraft"))
                            .withTabsBefore(CreativeModeTabs.COMBAT)
                            .icon(() -> ModItems.COPPER_DUST.get().getDefaultInstance())
                            .displayItems((params, out) -> {
                                /* === Raw Materials === */
                                out.accept(ModItems.STICKY_RESIN.get());
                                out.accept(ModItems.RUBBER.get());
                                out.accept(ModItems.URANIUM_DROP.get());

                                /* === Dusts === */
                                out.accept(ModItems.BRONZE_DUST.get());
                                out.accept(ModItems.CLAY_DUST.get());
                                out.accept(ModItems.COAL_DUST.get());
                                out.accept(ModItems.LEAD_DUST.get());
                                out.accept(ModItems.DIAMOND_DUST.get());
                                out.accept(ModItems.LITHIUM_DUST.get());
                                out.accept(ModItems.COPPER_DUST.get());
                                out.accept(ModItems.EMERALD_DUST.get());
                                out.accept(ModItems.GOLD_DUST.get());
                                out.accept(ModItems.SULFUR_DUST.get());
                                out.accept(ModItems.IRON_DUST.get());
                                out.accept(ModItems.LAPIS_DUST.get());
                                out.accept(ModItems.SILVER_DUST.get());
                                out.accept(ModItems.SMALL_IRON_DUST.get());
                                out.accept(ModItems.SILICON_DIOXIDE_DUST.get());
                                out.accept(ModItems.TIN_DUST.get());
                                out.accept(ModItems.HYDRATED_COAL_DUST.get());

                                /* === Ingots / Processed === */
                                out.accept(ModItems.REFINED_IRON_INGOT.get());
                                out.accept(ModItems.COPPER_INGOT.get());
                                out.accept(ModItems.TIN_INGOT.get());
                                out.accept(ModItems.BRONZE_INGOT.get());
                                out.accept(ModItems.ALLOY_INGOT.get());
                                out.accept(ModItems.URANIUM_INGOT.get());
                                /* === Tools === */
                                out.accept(ModItems.TREETAP.get());
                                out.accept(ModItems.WRENCH.get());
                                out.accept(ModItems.CUTTER.get());
                                out.accept(ModItems.CONSTRUCTION_FOAM_SPRAYER.get());
                                out.accept(ModItems.BRONZE_PICKAXE.get());
                                out.accept(ModItems.BRONZE_AXE.get());
                                out.accept(ModItems.BRONZE_SWORD.get());
                                out.accept(ModItems.BRONZE_SHOVEL.get());
                                out.accept(ModItems.BRONZE_HOE.get());
                                out.accept(ModItems.MINING_DRILL.get());
                                out.accept(ModItems.DIAMOND_DRILL.get());
                                out.accept(ModItems.CHAINSAW.get());
                                out.accept(ModItems.ELECTRIC_WRENCH.get());
                                out.accept(ModItems.ELECTRIC_TREETAP.get());
                                out.accept(ModItems.MINING_LASER.get());
                                out.accept(ModItems.EC_METER.get());
                                out.accept(ModItems.OD_SCANNER.get());
                                out.accept(ModItems.OV_SCANNER.get());
                                out.accept(ModItems.FREQUENCY_TRANSMITTER.get());
                                out.accept(ModItems.NANO_SABER.get());
                                out.accept(ModItems.ENABLED_NANO_SABER.get());
                                out.accept(ModItems.TOOLBOX.get());

                                /* === Armor === */
                                out.accept(ModItems.RUBBER_BOOTS.get());
                                out.accept(ModItems.BRONZE_HELMET.get());
                                out.accept(ModItems.BRONZE_CHESTPLATE.get());
                                out.accept(ModItems.BRONZE_LEGGINGS.get());
                                out.accept(ModItems.BRONZE_BOOTS.get());
                                out.accept(ModItems.COMPOSITE_ARMOR.get());
                                out.accept(ModItems.NANO_HELMET.get());
                                out.accept(ModItems.NANO_BODYARMOR.get());
                                out.accept(ModItems.NANO_LEGGINGS.get());
                                out.accept(ModItems.NANO_BOOTS.get());
                                out.accept(ModItems.QUANTUM_HELMET.get());
                                out.accept(ModItems.QUANTUM_BODYARMOR.get());
                                out.accept(ModItems.QUANTUM_LEGGINGS.get());
                                out.accept(ModItems.QUANTUM_BOOTS.get());
                                out.accept(ModItems.JETPACK.get());
                                out.accept(ModItems.ELECTRIC_JETPACK.get());
                                out.accept(ModItems.BAT_PACK.get());
                                out.accept(ModItems.LAP_PACK.get());
                                out.accept(ModItems.CF_PACK.get());
                                out.accept(ModItems.SOLAR_HELMET.get());
                                out.accept(ModItems.STATIC_BOOTS.get());
                                /* === Power Storage / Batteries === */
                                out.accept(ModItems.RE_BATTERY.get());
                                out.accept(ModItems.ADVANCED_RE_BATTERY.get());
                                out.accept(ModItems.ADVANCED_CHARGING_RE_BATTERY.get());
                                out.accept(ModItems.ENERGY_CRYSTAL.get());
                                out.accept(ModItems.LAPOTRON_CRYSTAL.get());
                                out.accept(ModItems.SU_BATTERY.get());

                                /* === Cables (Item form only) === */
                                out.accept(ModItems.COPPER_CABLE_ITEM.get());
                                out.accept(ModItems.INSULATED_COPPER_CABLE_ITEM.get());
                                out.accept(ModItems.GOLD_CABLE_ITEM.get());
                                out.accept(ModItems.INSULATED_GOLD_CABLE_ITEM.get());
                                out.accept(ModItems.DOUBLE_INSULATED_GOLD_CABLE_ITEM.get());
                                out.accept(ModItems.HIGH_VOLTAGE_CABLE_ITEM.get());
                                out.accept(ModItems.HIGH_VOLTAGE_CABLE_INSULATED_ITEM.get());
                                out.accept(ModItems.HIGH_VOLTAGE_CABLE_DOUBLE_INSULATED_ITEM.get());
                                out.accept(ModItems.HIGH_VOLTAGE_CABLE_QUADRUPLE_INSULATED_ITEM.get());
                                out.accept(ModItems.ULTRA_LOW_CURRENT_CABLE_ITEM.get());
                                out.accept(ModItems.GLASS_FIBER_CABLE_ITEM.get());
                                out.accept(ModItems.DETECTOR_CABLE_ITEM.get());
                                out.accept(ModItems.SPLITTER_CABLE_ITEM.get());
                                /* === Cells / Containers === */
                                out.accept(ModItems.CELL.get());
                                out.accept(ModItems.LAVA_CELL.get());
                                out.accept(ModItems.HYDRATED_COAL_CELL.get());
                                out.accept(ModItems.BIO_CELL.get());
                                out.accept(ModItems.COALFUEL_CELL.get());
                                out.accept(ModItems.BIOFUEL_CELL.get());
                                out.accept(ModItems.WATER_CELL.get());
                                out.accept(ModItems.ELECTROLYZED_WATER_CELL.get());
                                out.accept(ModItems.FUEL_CAN.get());
                                out.accept(ModItems.FILLED_FUEL_CAN.get());
                                out.accept(ModItems.TIN_CAN.get());
                                out.accept(ModItems.FILLED_TIN_CAN.get());
                                out.accept(ModItems.URANIUM_CELL.get());
                                out.accept(ModItems.COOLING_CELL.get());
                                out.accept(ModItems.DEPLETED_ISOTOPE_CELL.get());
                                out.accept(ModItems.RE_ENRICHED_URANIUM_CELL.get());
                                out.accept(ModItems.NEAR_DEPLETED_URANIUM_CELL.get());
                                out.accept(ModItems.HYDRATING_CELL.get());
                                out.accept(ModItems.HYDRATION_CELL.get());

                                /* === Reactor / Machine Components === */
                                out.accept(ModItems.INTEGRATED_REACTOR_PLATING.get());
                                out.accept(ModItems.INTEGRATED_HEAT_DISPERSER.get());
                                out.accept(ModItems.OVERCLOCKER_UPGRADE.get());
                                out.accept(ModItems.TRANSFORMER_UPGRADE.get());
                                out.accept(ModItems.ENERGY_STORAGE_UPGRADE.get());

                                /* === Crafting Components === */
                                out.accept(ModItems.COAL_BALL.get());
                                out.accept(ModItems.COMPRESSED_COAL_BALL.get());
                                out.accept(ModItems.COAL_CHUNK.get());
                                out.accept(ModItems.INDUSTRIAL_DIAMOND.get());
                                out.accept(ModItems.SCRAP.get());
                                out.accept(ModItems.SCRAP_BOX.get());
                                out.accept(ModItems.HYDRATED_COAL_CLUMP.get());
                                out.accept(ModItems.PLANT_BALL.get());
                                out.accept(ModItems.COMPRESSED_PLANT_BALL.get());
                                out.accept(ModItems.ELECTRONIC_CIRCUIT.get());
                                out.accept(ModItems.ADVANCED_CIRCUIT.get());
                                out.accept(ModItems.ADVANCED_ALLOY.get());
                                out.accept(ModItems.CARBON_FIBER.get());
                                out.accept(ModItems.CARBON_MESH.get());
                                out.accept(ModItems.CARBON_PLATE.get());
                                out.accept(ModItems.MATTER.get());
                                out.accept(ModItems.IRIDIUM_PLATE.get());

                                /* === Terraformer Blueprints === */
                                out.accept(ModItems.TERRAFORMER_BLUEPRINT.get());
                                out.accept(ModItems.CULTIVATION_TERRAFORMER_BLUEPRINT.get());
                                out.accept(ModItems.IRRIGATION_TERRAFORMER_BLUEPRINT.get());
                                out.accept(ModItems.CHILLING_TERRAFORMER_BLUEPRINT.get());
                                out.accept(ModItems.DESERTIFICATION_TERRAFORMER_BLUEPRINT.get());
                                out.accept(ModItems.FLATIFICATOR_TERRAFORMER_BLUEPRINT.get());
                                out.accept(ModItems.MUSHROOM_TERRAFORMER_BLUEPRINT.get());

                                /* === Painters === */
                                out.accept(ModItems.PAINTER.get());
                                out.accept(ModItems.BLACK_PAINTER.get());
                                out.accept(ModItems.RED_PAINTER.get());
                                out.accept(ModItems.GREEN_PAINTER.get());
                                out.accept(ModItems.BROWN_PAINTER.get());
                                out.accept(ModItems.BLUE_PAINTER.get());
                                out.accept(ModItems.PURPLE_PAINTER.get());
                                out.accept(ModItems.CYAN_PAINTER.get());
                                out.accept(ModItems.LIGHT_GREY_PAINTER.get());
                                out.accept(ModItems.DARK_GREY_PAINTER.get());
                                out.accept(ModItems.PINK_PAINTER.get());
                                out.accept(ModItems.LIME_PAINTER.get());
                                out.accept(ModItems.YELLOW_PAINTER.get());
                                out.accept(ModItems.CLOUD_PAINTER.get());
                                out.accept(ModItems.MAGENTA_PAINTER.get());
                                out.accept(ModItems.ORANGE_PAINTER.get());
                                out.accept(ModItems.WHITE_PAINTER.get());

                                /* === Misc / Utility === */
                                out.accept(ModItems.DYNAMITE.get());
                                out.accept(ModItems.STICKY_DYNAMITE.get());
                                out.accept(ModItems.REMOTE.get());
                                out.accept(ModItems.COIN.get());
                                out.accept(ModItems.CONSTRUCTION_FOAM_PELLET.get());
                                out.accept(ModItems.GRIN_POWDER.get());
                                out.accept(ModItems.DEBUG.get());

                                /* === Crops / Food / Brewing === */
                                out.accept(ModItems.CROP_SEED_BAG.get());
                                out.accept(ModItems.CROPNALYZER.get());
                                out.accept(ModItems.FERTILIZER.get());
                                out.accept(ModItems.OIL_BERRY.get());
                                out.accept(ModItems.BOBS_YER_UNCLE_RANKS_BERRY.get());
                                out.accept(ModItems.ELECTRIC_HOE.get());
                                out.accept(ModItems.TERRA_WART.get());
                                out.accept(ModItems.WEED.get());
                                out.accept(ModItems.CROP_INVALID.get());
                                out.accept(ModItems.CROP_UNKNOWN.get());
                                out.accept(ModItems.WEED_EX.get());
                                out.accept(ModItems.WEEDING_TROWEL.get());
                                out.accept(ModItems.CROP_STICK.get());
                                out.accept(ModItems.MILK_WART.get());
                                out.accept(ModItems.MUG_EMPTY.get());
                                out.accept(ModItems.COFFEE_BEANS.get());
                                out.accept(ModItems.COFFEE_POWDER.get());
                                out.accept(ModItems.MUG_COFFEE.get());
                                out.accept(ModItems.HOPS.get());
                                out.accept(ModItems.MUG_BOOZE.get());

                                /* === BLOCKS === */
                                // Ores
                                out.accept(ModItems.COPPER_ORE_ITEM.get());
                                out.accept(ModItems.TIN_ORE_ITEM.get());
                                out.accept(ModItems.URANIUM_ORE_ITEM.get());

                                // Rubber Tree
                                out.accept(ModItems.RUBBER_WOOD_ITEM.get());
                                out.accept(ModItems.RUBBER_LEAVES_ITEM.get());
                                out.accept(ModItems.RUBBER_SAPLING_ITEM.get());

                                // Decoration / Materials
                                out.accept(ModItems.RESIN_BLOCK_ITEM.get());
                                out.accept(ModItems.RUBBER_SHEET_ITEM.get());
                                out.accept(ModItems.REINFORCED_STONE_ITEM.get());
                                out.accept(ModItems.REINFORCED_GLASS_ITEM.get());
                                out.accept(ModItems.REINFORCED_DOOR_BLOCK_ITEM.get());
                                out.accept(ModItems.FOAM_ITEM.get());
                                out.accept(ModItems.WALL_ITEM.get());
                                out.accept(ModItems.SCAFFOLD_ITEM.get());
                                out.accept(ModItems.IRON_SCAFFOLD_ITEM.get());
                                out.accept(ModItems.METAL_BLOCK_ITEM.get());

                                // Technical Blocks
                                out.accept(ModItems.GENERATOR_ITEM.get());
                                out.accept(ModItems.REACTOR_CHAMBER_ITEM.get());
                                out.accept(ModItems.ELECTRIC_BLOCK_ITEM.get());
                                out.accept(ModItems.MACHINE_ITEM.get());
                                out.accept(ModItems.MACHINE2_ITEM.get());
                                out.accept(ModItems.LUMINATOR_ITEM.get());
                                out.accept(ModItems.ACTIVE_LUMINATOR_ITEM.get());
                                out.accept(ModItems.MINING_PIPE_ITEM.get());
                                out.accept(ModItems.MINING_TIP_ITEM.get());
                                out.accept(ModItems.PERSONAL_BLOCK_ITEM.get());

                                // Explosives
                                out.accept(ModItems.INDUSTRIAL_TNT_ITEM.get());
                                out.accept(ModItems.NUKE_ITEM.get());
                                out.accept(ModItems.DYNAMITE_STICK_ITEM.get());
                                out.accept(ModItems.DYNAMITE_STICK_WITH_REMOTE_ITEM.get());

                                // Agriculture
                                out.accept(ModItems.CROP_BLOCK_ITEM.get());
                                out.accept(ModItems.BARREL_BLOCK_ITEM.get());
                            })
                            .build()
            );

    /* --------------------------------------------------------------------- */
    /* Mod Init                                                              */
    /* --------------------------------------------------------------------- */

    public IndustrialCraft(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntity.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    /* --------------------------------------------------------------------- */
    /* Events                                                                */
    /* --------------------------------------------------------------------- */

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("IndustrialCraft common setup ready");
    }

    private void onServerStarting(net.neoforged.neoforge.event.server.ServerStartingEvent event) {
        LOGGER.info("IndustrialCraft server starting");
    }
}

