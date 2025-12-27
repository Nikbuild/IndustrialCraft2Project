package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registration.
 *
 * NOTES:
 *  - Hybrid IC: non-vanilla materials only.
 *  - Vanilla copper ingot is reused (not registered here).
 */
public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(IndustrialCraft.MODID);

    /* --------------------------------------------------------------------- */
    /* Raw Materials                                                         */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> STICKY_RESIN =
            ITEMS.register("sticky_resin",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "sticky_resin"))
                    )));


    public static final DeferredItem<Item> RUBBER =
            ITEMS.register("rubber",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "rubber"))
                    )));

    public static final DeferredItem<Item> URANIUM_DROP =
            ITEMS.register("uranium_drop",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "uranium_drop"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Dusts                                                                 */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> BRONZE_DUST =
            ITEMS.register("bronze_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_dust"))
                    )));
    public static final DeferredItem<Item> LEAD_DUST =
            ITEMS.register("lead_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lead_dust"))
                    )));

    public static final DeferredItem<Item> LITHIUM_DUST =
            ITEMS.register("lithium_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lithium_dust"))
                    )));

    public static final DeferredItem<Item> DIAMOND_DUST =
            ITEMS.register("diamond_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "diamond_dust"))
                    )));

    public static final DeferredItem<Item> EMERALD_DUST =
            ITEMS.register("emerald_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "emerald_dust"))
                    )));

    public static final DeferredItem<Item> LAPIS_DUST =
            ITEMS.register("lapis_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lapis_dust"))
                    )));

    public static final DeferredItem<Item> SULFUR_DUST =
            ITEMS.register("sulfur_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "sulfur_dust"))
                    )));

    public static final DeferredItem<Item> SILICON_DIOXIDE_DUST =
            ITEMS.register("silicon_dioxide_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "silicon_dioxide_dust"))
                    )));


    public static final DeferredItem<Item> CLAY_DUST =
            ITEMS.register("clay_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "clay_dust"))
                    )));

    public static final DeferredItem<Item> COAL_DUST =
            ITEMS.register("coal_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coal_dust"))
                    )));

    public static final DeferredItem<Item> COPPER_DUST =
            ITEMS.register("copper_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "copper_dust"))
                    )));

    public static final DeferredItem<Item> GOLD_DUST =
            ITEMS.register("gold_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "gold_dust"))
                    )));

    public static final DeferredItem<Item> IRON_DUST =
            ITEMS.register("iron_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "iron_dust"))
                    )));

    public static final DeferredItem<Item> SILVER_DUST =
            ITEMS.register("silver_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "silver_dust"))
                    )));

    public static final DeferredItem<Item> SMALL_IRON_DUST =
            ITEMS.register("small_iron_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "small_iron_dust"))
                    )));

    public static final DeferredItem<Item> TIN_DUST =
            ITEMS.register("tin_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "tin_dust"))
                    )));

    public static final DeferredItem<Item> HYDRATED_COAL_DUST =
            ITEMS.register("hydrated_coal_dust",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "hydrated_coal_dust"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Ingots / Processed                                                    */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> COPPER_INGOT =
            ITEMS.register("copper_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "copper_ingot"))
                    )));
    public static final DeferredItem<Item> LEAD_INGOT =
            ITEMS.register("lead_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lead_ingot"))
                    )));

    public static final DeferredItem<Item> SILVER_INGOT =
            ITEMS.register("silver_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "silver_ingot"))
                    )));

    public static final DeferredItem<Item> STEEL_INGOT =
            ITEMS.register("steel_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "steel_ingot"))
                    )));

    public static final DeferredItem<Item> TIN_INGOT =
            ITEMS.register("tin_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "tin_ingot"))
                    )));

    public static final DeferredItem<Item> BRONZE_INGOT =
            ITEMS.register("bronze_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_ingot"))
                    )));

    public static final DeferredItem<Item> ALLOY_INGOT =
            ITEMS.register("alloy_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "alloy_ingot"))
                    )));

    public static final DeferredItem<Item> REFINED_IRON_INGOT =
            ITEMS.register("refined_iron_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "refined_iron_ingot"))
                    )));

    public static final DeferredItem<Item> URANIUM_INGOT =
            ITEMS.register("uranium_ingot",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "uranium_ingot"))
                    )));
    /* --------------------------------------------------------------------- */
    /* Tools                                                                 */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> TREETAP =
            ITEMS.register("treetap",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "treetap"))
                    )));

    public static final DeferredItem<Item> WRENCH =
            ITEMS.register("wrench",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "wrench"))
                    )));

    public static final DeferredItem<Item> CUTTER =
            ITEMS.register("cutter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cutter"))
                    )));

    public static final DeferredItem<Item> CONSTRUCTION_FOAM_SPRAYER =
            ITEMS.register("construction_foam_sprayer",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "construction_foam_sprayer"))
                    )));

    public static final DeferredItem<Item> BRONZE_PICKAXE =
            ITEMS.register("bronze_pickaxe",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_pickaxe"))
                    )));

    public static final DeferredItem<Item> BRONZE_AXE =
            ITEMS.register("bronze_axe",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_axe"))
                    )));

    public static final DeferredItem<Item> BRONZE_SWORD =
            ITEMS.register("bronze_sword",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_sword"))
                    )));

    public static final DeferredItem<Item> BRONZE_SHOVEL =
            ITEMS.register("bronze_shovel",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_shovel"))
                    )));

    public static final DeferredItem<Item> BRONZE_HOE =
            ITEMS.register("bronze_hoe",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_hoe"))
                    )));

    public static final DeferredItem<Item> MINING_DRILL =
            ITEMS.register("mining_drill",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "mining_drill"))
                    )));

    public static final DeferredItem<Item> DIAMOND_DRILL =
            ITEMS.register("diamond_drill",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "diamond_drill"))
                    )));

    public static final DeferredItem<Item> CHAINSAW =
            ITEMS.register("chainsaw",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "chainsaw"))
                    )));

    public static final DeferredItem<Item> ELECTRIC_WRENCH =
            ITEMS.register("electric_wrench",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "electric_wrench"))
                    )));

    public static final DeferredItem<Item> ELECTRIC_TREETAP =
            ITEMS.register("electric_treetap",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "electric_treetap"))
                    )));

    public static final DeferredItem<Item> MINING_LASER =
            ITEMS.register("mining_laser",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "mining_laser"))
                    )));

    public static final DeferredItem<Item> EC_METER =
            ITEMS.register("ec_meter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "ec_meter"))
                    )));

    public static final DeferredItem<Item> OD_SCANNER =
            ITEMS.register("od_scanner",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "od_scanner"))
                    )));

    public static final DeferredItem<Item> OV_SCANNER =
            ITEMS.register("ov_scanner",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "ov_scanner"))
                    )));

    public static final DeferredItem<Item> FREQUENCY_TRANSMITTER =
            ITEMS.register("frequency_transmitter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "frequency_transmitter"))
                    )));

    public static final DeferredItem<Item> NANO_SABER =
            ITEMS.register("nano_saber",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "nano_saber"))
                    )));

    public static final DeferredItem<Item> ENABLED_NANO_SABER =
            ITEMS.register("enabled_nano_saber",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "enabled_nano_saber"))
                    )));

    public static final DeferredItem<Item> TOOLBOX =
            ITEMS.register("toolbox",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "toolbox"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Armor                                                                 */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> RUBBER_BOOTS =
            ITEMS.register("rubber_boots",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "rubber_boots"))
                    )));

    public static final DeferredItem<Item> BRONZE_HELMET =
            ITEMS.register("bronze_helmet",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_helmet"))
                    )));

    public static final DeferredItem<Item> BRONZE_CHESTPLATE =
            ITEMS.register("bronze_chestplate",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_chestplate"))
                    )));

    public static final DeferredItem<Item> BRONZE_LEGGINGS =
            ITEMS.register("bronze_leggings",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_leggings"))
                    )));

    public static final DeferredItem<Item> BRONZE_BOOTS =
            ITEMS.register("bronze_boots",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bronze_boots"))
                    )));

    public static final DeferredItem<Item> COMPOSITE_ARMOR =
            ITEMS.register("composite_armor",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "composite_armor"))
                    )));

    public static final DeferredItem<Item> NANO_HELMET =
            ITEMS.register("nano_helmet",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "nano_helmet"))
                    )));

    public static final DeferredItem<Item> NANO_BODYARMOR =
            ITEMS.register("nano_bodyarmor",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "nano_bodyarmor"))
                    )));

    public static final DeferredItem<Item> NANO_LEGGINGS =
            ITEMS.register("nano_leggings",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "nano_leggings"))
                    )));

    public static final DeferredItem<Item> NANO_BOOTS =
            ITEMS.register("nano_boots",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "nano_boots"))
                    )));

    public static final DeferredItem<Item> QUANTUM_HELMET =
            ITEMS.register("quantum_helmet",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "quantum_helmet"))
                    )));

    public static final DeferredItem<Item> QUANTUM_BODYARMOR =
            ITEMS.register("quantum_bodyarmor",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "quantum_bodyarmor"))
                    )));

    public static final DeferredItem<Item> QUANTUM_LEGGINGS =
            ITEMS.register("quantum_leggings",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "quantum_leggings"))
                    )));

    public static final DeferredItem<Item> QUANTUM_BOOTS =
            ITEMS.register("quantum_boots",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "quantum_boots"))
                    )));

    public static final DeferredItem<Item> JETPACK =
            ITEMS.register("jetpack",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "jetpack"))
                    )));

    public static final DeferredItem<Item> ELECTRIC_JETPACK =
            ITEMS.register("electric_jetpack",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "electric_jetpack"))
                    )));

    public static final DeferredItem<Item> BAT_PACK =
            ITEMS.register("bat_pack",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bat_pack"))
                    )));

    public static final DeferredItem<Item> LAP_PACK =
            ITEMS.register("lap_pack",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lap_pack"))
                    )));

    public static final DeferredItem<Item> CF_PACK =
            ITEMS.register("cf_pack",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cf_pack"))
                    )));

    public static final DeferredItem<Item> SOLAR_HELMET =
            ITEMS.register("solar_helmet",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "solar_helmet"))
                    )));

    public static final DeferredItem<Item> STATIC_BOOTS =
            ITEMS.register("static_boots",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "static_boots"))
                    )));
    /* --------------------------------------------------------------------- */
    /* Power Storage / Batteries                                             */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> RE_BATTERY =
            ITEMS.register("re_battery",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "re_battery"))
                    )));

    public static final DeferredItem<Item> ADVANCED_CHARGING_RE_BATTERY =
            ITEMS.register("advanced_charging_re_battery",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "advanced_charging_re_battery"))
                    )));

    public static final DeferredItem<Item> ADVANCED_RE_BATTERY =
            ITEMS.register("advanced_re_battery",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "advanced_re_battery"))
                    )));


    public static final DeferredItem<Item> ENERGY_CRYSTAL =
            ITEMS.register("energy_crystal",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "energy_crystal"))
                    )));

    public static final DeferredItem<Item> LAPOTRON_CRYSTAL =
            ITEMS.register("lapotron_crystal",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lapotron_crystal"))
                    )));

    public static final DeferredItem<Item> SU_BATTERY =
            ITEMS.register("su_battery",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "su_battery"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Cables (Item form only)                                               */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<BlockItem> COPPER_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("copper_cable_item", ModBlocks.COPPER_CABLE);

    public static final DeferredItem<BlockItem> INSULATED_COPPER_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("insulated_copper_cable_item", ModBlocks.INSULATED_COPPER_CABLE);

    public static final DeferredItem<BlockItem> GOLD_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("gold_cable_item", ModBlocks.GOLD_CABLE);

    public static final DeferredItem<BlockItem> INSULATED_GOLD_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("insulated_gold_cable_item", ModBlocks.GOLD_CABLE_INSULATED);

    public static final DeferredItem<BlockItem> DOUBLE_INSULATED_GOLD_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("double_insulated_gold_cable_item", ModBlocks.GOLD_CABLE_DOUBLE_INSULATED);

    public static final DeferredItem<BlockItem> HIGH_VOLTAGE_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("high_voltage_cable_item", ModBlocks.HIGH_VOLTAGE_CABLE);

    public static final DeferredItem<BlockItem> HIGH_VOLTAGE_CABLE_INSULATED_ITEM =
            ITEMS.registerSimpleBlockItem("high_voltage_cable_insulated_item", ModBlocks.HIGH_VOLTAGE_CABLE_INSULATED);

    public static final DeferredItem<BlockItem> HIGH_VOLTAGE_CABLE_DOUBLE_INSULATED_ITEM =
            ITEMS.registerSimpleBlockItem("high_voltage_cable_double_insulated_item", ModBlocks.HIGH_VOLTAGE_CABLE_DOUBLE_INSULATED);

    public static final DeferredItem<BlockItem> HIGH_VOLTAGE_CABLE_QUADRUPLE_INSULATED_ITEM =
            ITEMS.registerSimpleBlockItem("high_voltage_cable_quadruple_insulated_item", ModBlocks.HIGH_VOLTAGE_CABLE_QUADRUPLE_INSULATED);

    public static final DeferredItem<BlockItem> ULTRA_LOW_CURRENT_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("ultra_low_current_cable_item", ModBlocks.ULTRA_LOW_CURRENT_CABLE);

    public static final DeferredItem<BlockItem> GLASS_FIBER_CABLE_ITEM =
            ITEMS.registerSimpleBlockItem("glass_fiber_cable_item", ModBlocks.GLASS_FIBER_CABLE);

    public static final DeferredItem<Item> DETECTOR_CABLE_ITEM =
            ITEMS.register("detector_cable_item",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "detector_cable_item"))
                    )));

    public static final DeferredItem<Item> SPLITTER_CABLE_ITEM =
            ITEMS.register("splitter_cable_item",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "splitter_cable_item"))
                    )));
    /* --------------------------------------------------------------------- */
    /* Cells / Containers                                                    */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> CELL =
            ITEMS.register("cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cell"))
                    )));

    public static final DeferredItem<Item> LAVA_CELL =
            ITEMS.register("lava_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lava_cell"))
                    )));

    public static final DeferredItem<Item> HYDRATED_COAL_CELL =
            ITEMS.register("hydrated_coal_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "hydrated_coal_cell"))
                    )));

    public static final DeferredItem<Item> BIO_CELL =
            ITEMS.register("bio_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bio_cell"))
                    )));

    public static final DeferredItem<Item> COALFUEL_CELL =
            ITEMS.register("coalfuel_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coalfuel_cell"))
                    )));

    public static final DeferredItem<Item> BIOFUEL_CELL =
            ITEMS.register("biofuel_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "biofuel_cell"))
                    )));

    public static final DeferredItem<Item> WATER_CELL =
            ITEMS.register("water_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "water_cell"))
                    )));

    public static final DeferredItem<Item> ELECTROLYZED_WATER_CELL =
            ITEMS.register("electrolyzed_water_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "electrolyzed_water_cell"))
                    )));

    public static final DeferredItem<Item> FUEL_CAN =
            ITEMS.register("fuel_can",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "fuel_can"))
                    )));

    public static final DeferredItem<Item> FILLED_FUEL_CAN =
            ITEMS.register("filled_fuel_can",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "filled_fuel_can"))
                    )));

    public static final DeferredItem<Item> TIN_CAN =
            ITEMS.register("tin_can",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "tin_can"))
                    )));

    public static final DeferredItem<Item> FILLED_TIN_CAN =
            ITEMS.register("filled_tin_can",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "filled_tin_can"))
                    )));

    public static final DeferredItem<Item> URANIUM_CELL =
            ITEMS.register("uranium_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "uranium_cell"))
                    )));

    public static final DeferredItem<Item> COOLING_CELL =
            ITEMS.register("cooling_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cooling_cell"))
                    )));

    public static final DeferredItem<Item> DEPLETED_ISOTOPE_CELL =
            ITEMS.register("depleted_isotope_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "depleted_isotope_cell"))
                    )));

    public static final DeferredItem<Item> RE_ENRICHED_URANIUM_CELL =
            ITEMS.register("re_enriched_uranium_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "re_enriched_uranium_cell"))
                    )));

    public static final DeferredItem<Item> NEAR_DEPLETED_URANIUM_CELL =
            ITEMS.register("near_depleted_uranium_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "near_depleted_uranium_cell"))
                    )));

    public static final DeferredItem<Item> HYDRATING_CELL =
            ITEMS.register("hydrating_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "hydrating_cell"))
                    )));

    public static final DeferredItem<Item> HYDRATION_CELL =
            ITEMS.register("hydration_cell",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "hydration_cell"))
                    )));


    /* --------------------------------------------------------------------- */
    /* Reactor / Machine Components                                          */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> INTEGRATED_REACTOR_PLATING =
            ITEMS.register("integrated_reactor_plating",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "integrated_reactor_plating"))
                    )));

    public static final DeferredItem<Item> INTEGRATED_HEAT_DISPERSER =
            ITEMS.register("integrated_heat_disperser",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "integrated_heat_disperser"))
                    )));

    public static final DeferredItem<Item> OVERCLOCKER_UPGRADE =
            ITEMS.register("overclocker_upgrade",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "overclocker_upgrade"))
                    )));

    public static final DeferredItem<Item> TRANSFORMER_UPGRADE =
            ITEMS.register("transformer_upgrade",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "transformer_upgrade"))
                    )));

    public static final DeferredItem<Item> ENERGY_STORAGE_UPGRADE =
            ITEMS.register("energy_storage_upgrade",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "energy_storage_upgrade"))
                    )));
    /* --------------------------------------------------------------------- */
    /* Crafting Components                                                   */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> COAL_BALL =
            ITEMS.register("coal_ball",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coal_ball"))
                    )));

    public static final DeferredItem<Item> SCRAP =
            ITEMS.register("scrap",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "scrap"))
                    )));

    public static final DeferredItem<Item> SCRAP_BOX =
            ITEMS.register("scrap_box",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "scrap_box"))
                    )));

    public static final DeferredItem<Item> COMPRESSED_COAL_BALL =
            ITEMS.register("compressed_coal_ball",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "compressed_coal_ball"))
                    )));

    public static final DeferredItem<Item> COAL_CHUNK =
            ITEMS.register("coal_chunk",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coal_chunk"))
                    )));

    public static final DeferredItem<Item> INDUSTRIAL_DIAMOND =
            ITEMS.register("industrial_diamond",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "industrial_diamond"))
                    )));

    public static final DeferredItem<Item> HYDRATED_COAL_CLUMP =
            ITEMS.register("hydrated_coal_clump",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "hydrated_coal_clump"))
                    )));

    public static final DeferredItem<Item> PLANT_BALL =
            ITEMS.register("plant_ball",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "plant_ball"))
                    )));

    public static final DeferredItem<Item> COMPRESSED_PLANT_BALL =
            ITEMS.register("compressed_plant_ball",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "compressed_plant_ball"))
                    )));

    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT =
            ITEMS.register("electronic_circuit",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "electronic_circuit"))
                    )));

    public static final DeferredItem<Item> ADVANCED_CIRCUIT =
            ITEMS.register("advanced_circuit",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "advanced_circuit"))
                    )));

    public static final DeferredItem<Item> ADVANCED_ALLOY =
            ITEMS.register("advanced_alloy",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "advanced_alloy"))
                    )));

    public static final DeferredItem<Item> CARBON_FIBER =
            ITEMS.register("carbon_fiber",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "carbon_fiber"))
                    )));

    public static final DeferredItem<Item> CARBON_MESH =
            ITEMS.register("carbon_mesh",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "carbon_mesh"))
                    )));

    public static final DeferredItem<Item> CARBON_PLATE =
            ITEMS.register("carbon_plate",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "carbon_plate"))
                    )));

    public static final DeferredItem<Item> MATTER =
            ITEMS.register("matter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "matter"))
                    )));

    public static final DeferredItem<Item> IRIDIUM_PLATE =
            ITEMS.register("iridium_plate",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "iridium_plate"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Terraformer Blueprints                                                */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> TERRAFORMER_BLUEPRINT =
            ITEMS.register("terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "terraformer_blueprint"))
                    )));

    public static final DeferredItem<Item> CULTIVATION_TERRAFORMER_BLUEPRINT =
            ITEMS.register("cultivation_terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cultivation_terraformer_blueprint"))
                    )));

    public static final DeferredItem<Item> IRRIGATION_TERRAFORMER_BLUEPRINT =
            ITEMS.register("irrigation_terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "irrigation_terraformer_blueprint"))
                    )));

    public static final DeferredItem<Item> CHILLING_TERRAFORMER_BLUEPRINT =
            ITEMS.register("chilling_terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "chilling_terraformer_blueprint"))
                    )));

    public static final DeferredItem<Item> DESERTIFICATION_TERRAFORMER_BLUEPRINT =
            ITEMS.register("desertification_terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "desertification_terraformer_blueprint"))
                    )));

    public static final DeferredItem<Item> FLATIFICATOR_TERRAFORMER_BLUEPRINT =
            ITEMS.register("flatificator_terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "flatificator_terraformer_blueprint"))
                    )));

    public static final DeferredItem<Item> MUSHROOM_TERRAFORMER_BLUEPRINT =
            ITEMS.register("mushroom_terraformer_blueprint",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "mushroom_terraformer_blueprint"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Painters                                                              */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> PAINTER =
            ITEMS.register("painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "painter"))
                    )));

    public static final DeferredItem<Item> BLACK_PAINTER =
            ITEMS.register("black_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "black_painter"))
                    )));

    public static final DeferredItem<Item> RED_PAINTER =
            ITEMS.register("red_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "red_painter"))
                    )));

    public static final DeferredItem<Item> GREEN_PAINTER =
            ITEMS.register("green_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "green_painter"))
                    )));

    public static final DeferredItem<Item> BROWN_PAINTER =
            ITEMS.register("brown_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "brown_painter"))
                    )));

    public static final DeferredItem<Item> BLUE_PAINTER =
            ITEMS.register("blue_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "blue_painter"))
                    )));

    public static final DeferredItem<Item> PURPLE_PAINTER =
            ITEMS.register("purple_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "purple_painter"))
                    )));

    public static final DeferredItem<Item> CYAN_PAINTER =
            ITEMS.register("cyan_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cyan_painter"))
                    )));

    public static final DeferredItem<Item> LIGHT_GREY_PAINTER =
            ITEMS.register("light_grey_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "light_grey_painter"))
                    )));

    public static final DeferredItem<Item> DARK_GREY_PAINTER =
            ITEMS.register("dark_grey_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "dark_grey_painter"))
                    )));

    public static final DeferredItem<Item> PINK_PAINTER =
            ITEMS.register("pink_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "pink_painter"))
                    )));

    public static final DeferredItem<Item> LIME_PAINTER =
            ITEMS.register("lime_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "lime_painter"))
                    )));

    public static final DeferredItem<Item> YELLOW_PAINTER =
            ITEMS.register("yellow_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "yellow_painter"))
                    )));

    public static final DeferredItem<Item> CLOUD_PAINTER =
            ITEMS.register("cloud_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cloud_painter"))
                    )));

    public static final DeferredItem<Item> MAGENTA_PAINTER =
            ITEMS.register("magenta_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "magenta_painter"))
                    )));

    public static final DeferredItem<Item> ORANGE_PAINTER =
            ITEMS.register("orange_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "orange_painter"))
                    )));

    public static final DeferredItem<Item> WHITE_PAINTER =
            ITEMS.register("white_painter",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "white_painter"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Misc / Utility                                                        */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> DYNAMITE =
            ITEMS.register("dynamite",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "dynamite"))
                    )));

    public static final DeferredItem<Item> STICKY_DYNAMITE =
            ITEMS.register("sticky_dynamite",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "sticky_dynamite"))
                    )));

    public static final DeferredItem<Item> REMOTE =
            ITEMS.register("remote",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "remote"))
                    )));

    public static final DeferredItem<Item> CONSTRUCTION_FOAM_PELLET =
            ITEMS.register("construction_foam_pellet",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "construction_foam_pellet"))
                    )));

    public static final DeferredItem<Item> GRIN_POWDER =
            ITEMS.register("grin_powder",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "grin_powder"))
                    )));

    public static final DeferredItem<Item> COIN =
            ITEMS.register("coin",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coin"))
                    )));

    public static final DeferredItem<Item> DEBUG =
            ITEMS.register("debug",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "debug"))
                    )));
    /* --------------------------------------------------------------------- */
    /* Crops / Farming                                                       */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> CROP_SEED_BAG =
            ITEMS.register("crop_seed_bag",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "crop_seed_bag"))
                    )));

    public static final DeferredItem<Item> OIL_BERRY =
            ITEMS.register("oil_berry",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "oil_berry"))
                    )));
    public static final DeferredItem<Item> WEED =
            ITEMS.register("weed",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "weed"))
                    )));

    public static final DeferredItem<Item> CROP_STICK =
            ITEMS.register("crop_stick",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "crop_stick"))
                    )));


    public static final DeferredItem<Item> MILK_WART =
            ITEMS.register("milk_wart",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "milk_wart"))
                    )));

    public static final DeferredItem<Item> CROP_UNKNOWN =
            ITEMS.register("crop_unknown",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "crop_unknown"))
                    )));

    public static final DeferredItem<Item> CROPNALYZER =
            ITEMS.register("cropnalyzer",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "cropnalyzer"))
                    )));

    public static final DeferredItem<Item> BOBS_YER_UNCLE_RANKS_BERRY =
            ITEMS.register("bobs_yer_uncle_ranks_berry",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "bobs_yer_uncle_ranks_berry"))
                    )));


    public static final DeferredItem<Item> WEEDING_TROWEL =
            ITEMS.register("weeding_trowel",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "weeding_trowel"))
                    )));

    public static final DeferredItem<Item> FERTILIZER =
            ITEMS.register("fertilizer",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "fertilizer"))
                    )));

    public static final DeferredItem<Item> ELECTRIC_HOE =
            ITEMS.register("electric_hoe",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "electric_hoe"))
                    )));

    public static final DeferredItem<Item> TERRA_WART =
            ITEMS.register("terra_wart",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "terra_wart"))
                    )));

    public static final DeferredItem<Item> CROP_INVALID =
            ITEMS.register("crop_invalid",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "crop_invalid"))
                    )));

    public static final DeferredItem<Item> WEED_EX =
            ITEMS.register("weed_ex",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "weed_ex"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Food / Brewing                                                        */
    /* --------------------------------------------------------------------- */

    public static final DeferredItem<Item> MUG_EMPTY =
            ITEMS.register("mug_empty",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "mug_empty"))
                    )));

    public static final DeferredItem<Item> COFFEE_BEANS =
            ITEMS.register("coffee_beans",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coffee_beans"))
                    )));

    public static final DeferredItem<Item> COFFEE_POWDER =
            ITEMS.register("coffee_powder",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "coffee_powder"))
                    )));

    public static final DeferredItem<Item> MUG_COFFEE =
            ITEMS.register("mug_coffee",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "mug_coffee"))
                    )));

    public static final DeferredItem<Item> HOPS =
            ITEMS.register("hops",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "hops"))
                    )));

    public static final DeferredItem<Item> MUG_BOOZE =
            ITEMS.register("mug_booze",
                    () -> new Item(new Item.Properties().setId(
                            ResourceKey.create(Registries.ITEM,
                                    ResourceLocation.fromNamespaceAndPath(
                                            IndustrialCraft.MODID, "mug_booze"))
                    )));

    /* --------------------------------------------------------------------- */
    /* Blocks (Block Items)                                                  */
    /* --------------------------------------------------------------------- */

    // Ores
    public static final DeferredItem<BlockItem> COPPER_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("copper_ore", ModBlocks.COPPER_ORE);

    public static final DeferredItem<BlockItem> TIN_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("tin_ore", ModBlocks.TIN_ORE);

    public static final DeferredItem<BlockItem> URANIUM_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("uranium_ore", ModBlocks.URANIUM_ORE);

    // Rubber Tree
    public static final DeferredItem<BlockItem> RUBBER_WOOD_ITEM =
            ITEMS.registerSimpleBlockItem("rubber_wood", ModBlocks.RUBBER_WOOD);

    public static final DeferredItem<BlockItem> RUBBER_LEAVES_ITEM =
            ITEMS.registerSimpleBlockItem("rubber_leaves", ModBlocks.RUBBER_LEAVES);

    public static final DeferredItem<BlockItem> RUBBER_SAPLING_ITEM =
            ITEMS.registerSimpleBlockItem("rubber_sapling", ModBlocks.RUBBER_SAPLING);

    // Decoration / Materials
    public static final DeferredItem<BlockItem> RESIN_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("resin_block", ModBlocks.RESIN_BLOCK);

    public static final DeferredItem<BlockItem> RUBBER_SHEET_ITEM =
            ITEMS.registerSimpleBlockItem("rubber_sheet", ModBlocks.RUBBER_SHEET);

    public static final DeferredItem<BlockItem> REINFORCED_STONE_ITEM =
            ITEMS.registerSimpleBlockItem("reinforced_stone", ModBlocks.REINFORCED_STONE);

    public static final DeferredItem<BlockItem> REINFORCED_GLASS_ITEM =
            ITEMS.registerSimpleBlockItem("reinforced_glass", ModBlocks.REINFORCED_GLASS);

    public static final DeferredItem<BlockItem> REINFORCED_DOOR_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("reinforced_door_block", ModBlocks.REINFORCED_DOOR_BLOCK);

    public static final DeferredItem<BlockItem> FOAM_ITEM =
            ITEMS.registerSimpleBlockItem("foam", ModBlocks.FOAM);

    public static final DeferredItem<BlockItem> WALL_ITEM =
            ITEMS.registerSimpleBlockItem("wall", ModBlocks.WALL);

    public static final DeferredItem<BlockItem> SCAFFOLD_ITEM =
            ITEMS.registerSimpleBlockItem("scaffold", ModBlocks.SCAFFOLD);

    public static final DeferredItem<BlockItem> IRON_SCAFFOLD_ITEM =
            ITEMS.registerSimpleBlockItem("iron_scaffold", ModBlocks.IRON_SCAFFOLD);

    public static final DeferredItem<BlockItem> METAL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("metal_block", ModBlocks.METAL_BLOCK);

    // Technical Blocks
    public static final DeferredItem<BlockItem> GENERATOR_ITEM =
            ITEMS.registerSimpleBlockItem("generator", ModBlocks.GENERATOR);

    public static final DeferredItem<BlockItem> REACTOR_CHAMBER_ITEM =
            ITEMS.registerSimpleBlockItem("reactor_chamber", ModBlocks.REACTOR_CHAMBER);

    public static final DeferredItem<BlockItem> ELECTRIC_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("electric_block", ModBlocks.ELECTRIC_BLOCK);

    public static final DeferredItem<BlockItem> MACHINE_ITEM =
            ITEMS.registerSimpleBlockItem("machine", ModBlocks.MACHINE);

    public static final DeferredItem<BlockItem> MACHINE2_ITEM =
            ITEMS.registerSimpleBlockItem("machine2", ModBlocks.MACHINE2);

    public static final DeferredItem<BlockItem> LUMINATOR_ITEM =
            ITEMS.registerSimpleBlockItem("luminator", ModBlocks.LUMINATOR);

    public static final DeferredItem<BlockItem> ACTIVE_LUMINATOR_ITEM =
            ITEMS.registerSimpleBlockItem("active_luminator", ModBlocks.ACTIVE_LUMINATOR);

    public static final DeferredItem<BlockItem> MINING_PIPE_ITEM =
            ITEMS.registerSimpleBlockItem("mining_pipe", ModBlocks.MINING_PIPE);

    public static final DeferredItem<BlockItem> MINING_TIP_ITEM =
            ITEMS.registerSimpleBlockItem("mining_tip", ModBlocks.MINING_TIP);

    public static final DeferredItem<BlockItem> PERSONAL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("personal_block", ModBlocks.PERSONAL_BLOCK);

    // Explosives
    public static final DeferredItem<BlockItem> INDUSTRIAL_TNT_ITEM =
            ITEMS.registerSimpleBlockItem("industrial_tnt", ModBlocks.INDUSTRIAL_TNT);

    public static final DeferredItem<BlockItem> NUKE_ITEM =
            ITEMS.registerSimpleBlockItem("nuke", ModBlocks.NUKE);

    public static final DeferredItem<BlockItem> DYNAMITE_STICK_ITEM =
            ITEMS.registerSimpleBlockItem("dynamite_stick", ModBlocks.DYNAMITE_STICK);

    public static final DeferredItem<BlockItem> DYNAMITE_STICK_WITH_REMOTE_ITEM =
            ITEMS.registerSimpleBlockItem("dynamite_stick_with_remote", ModBlocks.DYNAMITE_STICK_WITH_REMOTE);

    // Agriculture
    public static final DeferredItem<BlockItem> CROP_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("crop_block", ModBlocks.CROP_BLOCK);

    public static final DeferredItem<BlockItem> BARREL_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem("barrel_block", ModBlocks.BARREL_BLOCK);
}

