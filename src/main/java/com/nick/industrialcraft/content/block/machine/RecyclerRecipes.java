package com.nick.industrialcraft.content.block.machine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.nick.industrialcraft.registry.ModItems;

import java.util.HashMap;
import java.util.Map;

/**
 * Recycler Recipes - Points-based scrap production system
 *
 * Unlike IC2's RNG-based 1/8 chance system, this implementation uses a deterministic
 * points system where crafted items contribute points based on their complexity.
 * When the accumulated points reach the threshold (100), one scrap is produced.
 *
 * Design Philosophy:
 * - Only crafted/processed items produce scrap (raw materials = 0 points)
 * - More complex crafting recipes = more points
 * - Deterministic output - no RNG frustration
 * - Counter overflow is preserved for next scrap
 *
 * Point Values:
 * - 0 points: Raw/natural items (cobblestone, dirt, logs, ores, etc.)
 * - 20 points: Simple 1-step crafts (planks, sticks, torches)
 * - 35 points: 2-step crafts or shaped recipes (fences, ladders, bowls)
 * - 50 points: Medium complexity (tools, basic machines, rails)
 * - 75 points: Complex crafts (armor, advanced items)
 * - 100 points: Very complex items (1 item = 1 scrap guaranteed)
 *
 * 100 points = 1 scrap
 */
public class RecyclerRecipes {

    private static final Map<Item, Integer> SCRAP_VALUES = new HashMap<>();

    /** Points required to produce one scrap */
    public static final int POINTS_PER_SCRAP = 100;

    /** Maximum points that can be stored (prevents overflow exploits) */
    public static final long MAX_STORED_POINTS = 10000L;

    static {
        initVanillaRecipes();
        initIC2Recipes();
    }

    private static void initVanillaRecipes() {
        // ========== WOOD CRAFTS (Simple processing) ==========
        // Planks: 1 log -> 4 planks (simple)
        register(Items.OAK_PLANKS, 20);
        register(Items.SPRUCE_PLANKS, 20);
        register(Items.BIRCH_PLANKS, 20);
        register(Items.JUNGLE_PLANKS, 20);
        register(Items.ACACIA_PLANKS, 20);
        register(Items.DARK_OAK_PLANKS, 20);
        register(Items.MANGROVE_PLANKS, 20);
        register(Items.CHERRY_PLANKS, 20);
        register(Items.BAMBOO_PLANKS, 20);
        register(Items.CRIMSON_PLANKS, 20);
        register(Items.WARPED_PLANKS, 20);

        // Sticks: 2 planks -> 4 sticks (2-step)
        register(Items.STICK, 20);

        // Wooden tools (planks + sticks)
        register(Items.WOODEN_SWORD, 35);
        register(Items.WOODEN_PICKAXE, 50);
        register(Items.WOODEN_AXE, 50);
        register(Items.WOODEN_SHOVEL, 35);
        register(Items.WOODEN_HOE, 35);

        // Crafting table, chests, etc.
        register(Items.CRAFTING_TABLE, 35);
        register(Items.CHEST, 50);
        register(Items.BARREL, 50);
        register(Items.LADDER, 20);
        register(Items.OAK_FENCE, 35);
        register(Items.SPRUCE_FENCE, 35);
        register(Items.BIRCH_FENCE, 35);
        register(Items.JUNGLE_FENCE, 35);
        register(Items.ACACIA_FENCE, 35);
        register(Items.DARK_OAK_FENCE, 35);
        register(Items.MANGROVE_FENCE, 35);
        register(Items.CHERRY_FENCE, 35);
        register(Items.BAMBOO_FENCE, 35);
        register(Items.CRIMSON_FENCE, 35);
        register(Items.WARPED_FENCE, 35);

        // Fence gates
        register(Items.OAK_FENCE_GATE, 35);
        register(Items.SPRUCE_FENCE_GATE, 35);
        register(Items.BIRCH_FENCE_GATE, 35);
        register(Items.JUNGLE_FENCE_GATE, 35);
        register(Items.ACACIA_FENCE_GATE, 35);
        register(Items.DARK_OAK_FENCE_GATE, 35);
        register(Items.MANGROVE_FENCE_GATE, 35);
        register(Items.CHERRY_FENCE_GATE, 35);
        register(Items.BAMBOO_FENCE_GATE, 35);
        register(Items.CRIMSON_FENCE_GATE, 35);
        register(Items.WARPED_FENCE_GATE, 35);

        // Doors and trapdoors
        register(Items.OAK_DOOR, 35);
        register(Items.SPRUCE_DOOR, 35);
        register(Items.BIRCH_DOOR, 35);
        register(Items.JUNGLE_DOOR, 35);
        register(Items.ACACIA_DOOR, 35);
        register(Items.DARK_OAK_DOOR, 35);
        register(Items.MANGROVE_DOOR, 35);
        register(Items.CHERRY_DOOR, 35);
        register(Items.BAMBOO_DOOR, 35);
        register(Items.CRIMSON_DOOR, 35);
        register(Items.WARPED_DOOR, 35);
        register(Items.IRON_DOOR, 50);

        register(Items.OAK_TRAPDOOR, 35);
        register(Items.SPRUCE_TRAPDOOR, 35);
        register(Items.BIRCH_TRAPDOOR, 35);
        register(Items.JUNGLE_TRAPDOOR, 35);
        register(Items.ACACIA_TRAPDOOR, 35);
        register(Items.DARK_OAK_TRAPDOOR, 35);
        register(Items.MANGROVE_TRAPDOOR, 35);
        register(Items.CHERRY_TRAPDOOR, 35);
        register(Items.BAMBOO_TRAPDOOR, 35);
        register(Items.CRIMSON_TRAPDOOR, 35);
        register(Items.WARPED_TRAPDOOR, 35);
        register(Items.IRON_TRAPDOOR, 50);

        // Boats
        register(Items.OAK_BOAT, 50);
        register(Items.SPRUCE_BOAT, 50);
        register(Items.BIRCH_BOAT, 50);
        register(Items.JUNGLE_BOAT, 50);
        register(Items.ACACIA_BOAT, 50);
        register(Items.DARK_OAK_BOAT, 50);
        register(Items.MANGROVE_BOAT, 50);
        register(Items.CHERRY_BOAT, 50);
        register(Items.BAMBOO_RAFT, 50);

        // Signs
        register(Items.OAK_SIGN, 35);
        register(Items.SPRUCE_SIGN, 35);
        register(Items.BIRCH_SIGN, 35);
        register(Items.JUNGLE_SIGN, 35);
        register(Items.ACACIA_SIGN, 35);
        register(Items.DARK_OAK_SIGN, 35);
        register(Items.MANGROVE_SIGN, 35);
        register(Items.CHERRY_SIGN, 35);
        register(Items.BAMBOO_SIGN, 35);
        register(Items.CRIMSON_SIGN, 35);
        register(Items.WARPED_SIGN, 35);

        // Buttons and pressure plates
        register(Items.OAK_BUTTON, 20);
        register(Items.SPRUCE_BUTTON, 20);
        register(Items.BIRCH_BUTTON, 20);
        register(Items.JUNGLE_BUTTON, 20);
        register(Items.ACACIA_BUTTON, 20);
        register(Items.DARK_OAK_BUTTON, 20);
        register(Items.MANGROVE_BUTTON, 20);
        register(Items.CHERRY_BUTTON, 20);
        register(Items.BAMBOO_BUTTON, 20);
        register(Items.CRIMSON_BUTTON, 20);
        register(Items.WARPED_BUTTON, 20);
        register(Items.STONE_BUTTON, 20);
        register(Items.POLISHED_BLACKSTONE_BUTTON, 20);

        register(Items.OAK_PRESSURE_PLATE, 20);
        register(Items.SPRUCE_PRESSURE_PLATE, 20);
        register(Items.BIRCH_PRESSURE_PLATE, 20);
        register(Items.JUNGLE_PRESSURE_PLATE, 20);
        register(Items.ACACIA_PRESSURE_PLATE, 20);
        register(Items.DARK_OAK_PRESSURE_PLATE, 20);
        register(Items.MANGROVE_PRESSURE_PLATE, 20);
        register(Items.CHERRY_PRESSURE_PLATE, 20);
        register(Items.BAMBOO_PRESSURE_PLATE, 20);
        register(Items.CRIMSON_PRESSURE_PLATE, 20);
        register(Items.WARPED_PRESSURE_PLATE, 20);
        register(Items.STONE_PRESSURE_PLATE, 20);
        register(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE, 20);
        register(Items.LIGHT_WEIGHTED_PRESSURE_PLATE, 50);
        register(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, 50);

        // Bowl
        register(Items.BOWL, 35);

        // ========== STONE CRAFTS ==========
        // Stone tools (cobble + sticks)
        register(Items.STONE_SWORD, 35);
        register(Items.STONE_PICKAXE, 50);
        register(Items.STONE_AXE, 50);
        register(Items.STONE_SHOVEL, 35);
        register(Items.STONE_HOE, 35);

        // Furnace
        register(Items.FURNACE, 50);
        register(Items.BLAST_FURNACE, 75);
        register(Items.SMOKER, 75);

        // Stone bricks and variants
        register(Items.STONE_BRICKS, 20);
        register(Items.MOSSY_STONE_BRICKS, 20);
        register(Items.CRACKED_STONE_BRICKS, 20);
        register(Items.CHISELED_STONE_BRICKS, 35);
        register(Items.STONE_BRICK_STAIRS, 35);
        register(Items.STONE_BRICK_SLAB, 20);
        register(Items.STONE_BRICK_WALL, 35);

        // Smooth stone
        register(Items.SMOOTH_STONE, 20);
        register(Items.SMOOTH_STONE_SLAB, 20);

        // Cobblestone crafts
        register(Items.COBBLESTONE_STAIRS, 35);
        register(Items.COBBLESTONE_SLAB, 20);
        register(Items.COBBLESTONE_WALL, 35);

        // Bricks
        register(Items.BRICKS, 35);
        register(Items.BRICK_STAIRS, 35);
        register(Items.BRICK_SLAB, 20);
        register(Items.BRICK_WALL, 35);

        // ========== IRON CRAFTS ==========
        // Iron ingot (smelted) = simple processing
        register(Items.IRON_INGOT, 20);
        register(Items.IRON_NUGGET, 5);
        register(Items.IRON_BLOCK, 50);

        // Iron tools
        register(Items.IRON_SWORD, 50);
        register(Items.IRON_PICKAXE, 75);
        register(Items.IRON_AXE, 75);
        register(Items.IRON_SHOVEL, 50);
        register(Items.IRON_HOE, 50);

        // Iron armor
        register(Items.IRON_HELMET, 75);
        register(Items.IRON_CHESTPLATE, 100);
        register(Items.IRON_LEGGINGS, 100);
        register(Items.IRON_BOOTS, 75);

        // Iron utility
        register(Items.BUCKET, 50);
        register(Items.SHEARS, 35);
        register(Items.SHIELD, 75);
        register(Items.IRON_BARS, 35);
        register(Items.CHAIN, 35);
        register(Items.CAULDRON, 100);
        register(Items.HOPPER, 100);
        register(Items.MINECART, 75);
        register(Items.ANVIL, 100);
        register(Items.SMITHING_TABLE, 75);

        // ========== GOLD CRAFTS ==========
        register(Items.GOLD_INGOT, 20);
        register(Items.GOLD_NUGGET, 5);
        register(Items.GOLD_BLOCK, 50);

        register(Items.GOLDEN_SWORD, 50);
        register(Items.GOLDEN_PICKAXE, 75);
        register(Items.GOLDEN_AXE, 75);
        register(Items.GOLDEN_SHOVEL, 50);
        register(Items.GOLDEN_HOE, 50);

        register(Items.GOLDEN_HELMET, 75);
        register(Items.GOLDEN_CHESTPLATE, 100);
        register(Items.GOLDEN_LEGGINGS, 100);
        register(Items.GOLDEN_BOOTS, 75);

        register(Items.CLOCK, 75);
        register(Items.GOLDEN_APPLE, 100);
        register(Items.POWERED_RAIL, 75);

        // ========== DIAMOND CRAFTS ==========
        register(Items.DIAMOND, 35);  // Mined/found, but valuable
        register(Items.DIAMOND_BLOCK, 100);

        register(Items.DIAMOND_SWORD, 75);
        register(Items.DIAMOND_PICKAXE, 100);
        register(Items.DIAMOND_AXE, 100);
        register(Items.DIAMOND_SHOVEL, 75);
        register(Items.DIAMOND_HOE, 75);

        register(Items.DIAMOND_HELMET, 100);
        register(Items.DIAMOND_CHESTPLATE, 100);
        register(Items.DIAMOND_LEGGINGS, 100);
        register(Items.DIAMOND_BOOTS, 100);

        register(Items.ENCHANTING_TABLE, 100);
        register(Items.JUKEBOX, 100);

        // ========== NETHERITE CRAFTS ==========
        register(Items.NETHERITE_INGOT, 100);
        register(Items.NETHERITE_BLOCK, 100);
        register(Items.NETHERITE_SWORD, 100);
        register(Items.NETHERITE_PICKAXE, 100);
        register(Items.NETHERITE_AXE, 100);
        register(Items.NETHERITE_SHOVEL, 100);
        register(Items.NETHERITE_HOE, 100);
        register(Items.NETHERITE_HELMET, 100);
        register(Items.NETHERITE_CHESTPLATE, 100);
        register(Items.NETHERITE_LEGGINGS, 100);
        register(Items.NETHERITE_BOOTS, 100);

        // ========== COPPER CRAFTS ==========
        register(Items.COPPER_INGOT, 20);
        register(Items.COPPER_BLOCK, 50);
        register(Items.CUT_COPPER, 35);
        register(Items.CUT_COPPER_STAIRS, 35);
        register(Items.CUT_COPPER_SLAB, 20);
        register(Items.LIGHTNING_ROD, 50);
        register(Items.SPYGLASS, 50);

        // ========== REDSTONE CRAFTS ==========
        register(Items.REDSTONE, 20);
        register(Items.REDSTONE_BLOCK, 50);
        register(Items.REDSTONE_TORCH, 20);
        register(Items.REPEATER, 50);
        register(Items.COMPARATOR, 75);
        register(Items.LEVER, 20);
        register(Items.TRIPWIRE_HOOK, 35);
        register(Items.TRAPPED_CHEST, 75);
        register(Items.DAYLIGHT_DETECTOR, 75);
        register(Items.OBSERVER, 75);
        register(Items.PISTON, 75);
        register(Items.STICKY_PISTON, 100);
        register(Items.DISPENSER, 75);
        register(Items.DROPPER, 75);
        register(Items.NOTE_BLOCK, 50);
        register(Items.TARGET, 50);
        register(Items.LECTERN, 75);
        register(Items.REDSTONE_LAMP, 50);
        register(Items.TNT, 75);

        // ========== RAILS ==========
        register(Items.RAIL, 35);
        register(Items.ACTIVATOR_RAIL, 75);
        register(Items.DETECTOR_RAIL, 75);
        // POWERED_RAIL already registered under gold

        // Minecart variants
        register(Items.CHEST_MINECART, 100);
        register(Items.FURNACE_MINECART, 100);
        register(Items.HOPPER_MINECART, 100);
        register(Items.TNT_MINECART, 100);

        // ========== GLASS & DECORATION ==========
        register(Items.GLASS, 20);
        register(Items.GLASS_PANE, 20);
        register(Items.TINTED_GLASS, 50);

        // Stained glass (subset for brevity)
        register(Items.WHITE_STAINED_GLASS, 35);
        register(Items.BLACK_STAINED_GLASS, 35);

        // Stained glass panes
        register(Items.WHITE_STAINED_GLASS_PANE, 35);
        register(Items.BLACK_STAINED_GLASS_PANE, 35);

        // Terracotta (smelted clay)
        register(Items.TERRACOTTA, 20);
        register(Items.WHITE_TERRACOTTA, 35);
        register(Items.BLACK_TERRACOTTA, 35);

        // Glazed terracotta
        register(Items.WHITE_GLAZED_TERRACOTTA, 50);
        register(Items.BLACK_GLAZED_TERRACOTTA, 50);

        // Concrete
        register(Items.WHITE_CONCRETE, 35);
        register(Items.BLACK_CONCRETE, 35);

        // Wool
        register(Items.WHITE_WOOL, 20);
        register(Items.BLACK_WOOL, 35);

        // Beds
        register(Items.WHITE_BED, 50);
        register(Items.BLACK_BED, 50);

        // Banners
        register(Items.WHITE_BANNER, 75);
        register(Items.BLACK_BANNER, 75);

        // ========== MISC CRAFTS ==========
        register(Items.TORCH, 20);
        register(Items.SOUL_TORCH, 20);
        register(Items.LANTERN, 50);
        register(Items.SOUL_LANTERN, 50);
        register(Items.CAMPFIRE, 50);
        register(Items.SOUL_CAMPFIRE, 50);

        register(Items.PAPER, 20);
        register(Items.BOOK, 35);
        register(Items.BOOKSHELF, 75);
        register(Items.CHISELED_BOOKSHELF, 75);
        register(Items.WRITABLE_BOOK, 50);
        register(Items.WRITTEN_BOOK, 50);
        register(Items.MAP, 50);
        register(Items.FILLED_MAP, 50);

        register(Items.COMPASS, 75);
        register(Items.RECOVERY_COMPASS, 100);

        register(Items.BOW, 50);
        register(Items.CROSSBOW, 75);
        register(Items.ARROW, 20);
        register(Items.SPECTRAL_ARROW, 50);
        register(Items.TIPPED_ARROW, 50);

        register(Items.FISHING_ROD, 35);
        register(Items.CARROT_ON_A_STICK, 50);
        register(Items.WARPED_FUNGUS_ON_A_STICK, 50);
        register(Items.LEAD, 35);
        register(Items.NAME_TAG, 50);

        register(Items.PAINTING, 50);
        register(Items.ITEM_FRAME, 50);
        register(Items.GLOW_ITEM_FRAME, 75);
        register(Items.FLOWER_POT, 35);
        register(Items.ARMOR_STAND, 75);

        register(Items.LEATHER, 20);
        register(Items.LEATHER_HELMET, 50);
        register(Items.LEATHER_CHESTPLATE, 75);
        register(Items.LEATHER_LEGGINGS, 75);
        register(Items.LEATHER_BOOTS, 50);
        register(Items.LEATHER_HORSE_ARMOR, 75);
        register(Items.SADDLE, 50);

        register(Items.CHAINMAIL_HELMET, 75);
        register(Items.CHAINMAIL_CHESTPLATE, 100);
        register(Items.CHAINMAIL_LEGGINGS, 100);
        register(Items.CHAINMAIL_BOOTS, 75);

        register(Items.TURTLE_HELMET, 100);
        register(Items.ELYTRA, 100);

        register(Items.ENDER_CHEST, 100);
        register(Items.SHULKER_BOX, 100);
        register(Items.BEACON, 100);
        register(Items.CONDUIT, 100);
        register(Items.LODESTONE, 100);
        register(Items.RESPAWN_ANCHOR, 100);
        register(Items.END_CRYSTAL, 100);

        register(Items.BREWING_STAND, 75);
        register(Items.GLASS_BOTTLE, 20);
        register(Items.BLAZE_POWDER, 35);
        register(Items.MAGMA_CREAM, 35);
        register(Items.FERMENTED_SPIDER_EYE, 50);
        register(Items.GLISTERING_MELON_SLICE, 50);
        register(Items.GOLDEN_CARROT, 50);

        register(Items.CAKE, 75);
        register(Items.COOKIE, 20);
        register(Items.PUMPKIN_PIE, 35);
        register(Items.BREAD, 20);

        register(Items.HAY_BLOCK, 50);
        register(Items.DRIED_KELP_BLOCK, 50);

        register(Items.LAPIS_BLOCK, 50);
        register(Items.EMERALD_BLOCK, 50);
        register(Items.COAL_BLOCK, 50);

        register(Items.SCAFFOLDING, 20);
        register(Items.CANDLE, 35);

        // ========== PRISMARINE ==========
        register(Items.PRISMARINE, 35);
        register(Items.PRISMARINE_BRICKS, 50);
        register(Items.DARK_PRISMARINE, 50);
        register(Items.SEA_LANTERN, 75);

        // ========== END ITEMS ==========
        register(Items.END_STONE_BRICKS, 35);
        register(Items.PURPUR_BLOCK, 35);
        register(Items.PURPUR_PILLAR, 50);
        register(Items.PURPUR_STAIRS, 35);
        register(Items.PURPUR_SLAB, 20);
        register(Items.END_ROD, 50);

        // ========== NETHER ITEMS ==========
        register(Items.NETHER_BRICKS, 35);
        register(Items.RED_NETHER_BRICKS, 50);
        register(Items.NETHER_BRICK_FENCE, 35);
        register(Items.NETHER_BRICK_STAIRS, 35);
        register(Items.NETHER_BRICK_SLAB, 20);
        register(Items.NETHER_BRICK_WALL, 35);
        register(Items.QUARTZ_BLOCK, 35);
        register(Items.QUARTZ_BRICKS, 50);
        register(Items.QUARTZ_PILLAR, 50);
        register(Items.CHISELED_QUARTZ_BLOCK, 50);
        register(Items.SMOOTH_QUARTZ, 35);
        register(Items.QUARTZ_STAIRS, 35);
        register(Items.QUARTZ_SLAB, 20);
        register(Items.GLOWSTONE, 35);
        register(Items.MAGMA_BLOCK, 35);

        // Blackstone
        register(Items.POLISHED_BLACKSTONE, 35);
        register(Items.POLISHED_BLACKSTONE_BRICKS, 50);
        register(Items.CHISELED_POLISHED_BLACKSTONE, 50);
        register(Items.POLISHED_BLACKSTONE_STAIRS, 35);
        register(Items.POLISHED_BLACKSTONE_SLAB, 20);
        register(Items.POLISHED_BLACKSTONE_WALL, 35);
        register(Items.POLISHED_BLACKSTONE_BRICK_STAIRS, 35);
        register(Items.POLISHED_BLACKSTONE_BRICK_SLAB, 20);
        register(Items.POLISHED_BLACKSTONE_BRICK_WALL, 35);

        // Deepslate
        register(Items.COBBLED_DEEPSLATE, 20);
        register(Items.COBBLED_DEEPSLATE_STAIRS, 35);
        register(Items.COBBLED_DEEPSLATE_SLAB, 20);
        register(Items.COBBLED_DEEPSLATE_WALL, 35);
        register(Items.POLISHED_DEEPSLATE, 35);
        register(Items.POLISHED_DEEPSLATE_STAIRS, 35);
        register(Items.POLISHED_DEEPSLATE_SLAB, 20);
        register(Items.POLISHED_DEEPSLATE_WALL, 35);
        register(Items.DEEPSLATE_BRICKS, 50);
        register(Items.DEEPSLATE_BRICK_STAIRS, 35);
        register(Items.DEEPSLATE_BRICK_SLAB, 20);
        register(Items.DEEPSLATE_BRICK_WALL, 35);
        register(Items.DEEPSLATE_TILES, 50);
        register(Items.DEEPSLATE_TILE_STAIRS, 35);
        register(Items.DEEPSLATE_TILE_SLAB, 20);
        register(Items.DEEPSLATE_TILE_WALL, 35);
        register(Items.CHISELED_DEEPSLATE, 50);

        // Amethyst
        register(Items.AMETHYST_BLOCK, 35);
    }

    private static void initIC2Recipes() {
        // ========== IC2 BASIC COMPONENTS ==========
        // Raw rubber (from tree) = minimal
        register(ModItems.RUBBER.get(), 20);

        // Refined iron (processed)
        register(ModItems.REFINED_IRON_INGOT.get(), 35);

        // Bronze (alloy)
        register(ModItems.BRONZE_INGOT.get(), 35);

        // Tin
        register(ModItems.TIN_INGOT.get(), 20);

        // Dusts (macerator output)
        register(ModItems.IRON_DUST.get(), 20);
        register(ModItems.GOLD_DUST.get(), 20);
        register(ModItems.COPPER_DUST.get(), 20);
        register(ModItems.TIN_DUST.get(), 20);
        register(ModItems.BRONZE_DUST.get(), 35);
        register(ModItems.COAL_DUST.get(), 20);

        // Small dusts
        register(ModItems.SMALL_IRON_DUST.get(), 5);

        // ========== IC2 CABLES ==========
        register(ModItems.COPPER_CABLE_ITEM.get(), 35);
        register(ModItems.INSULATED_COPPER_CABLE_ITEM.get(), 50);
        register(ModItems.GOLD_CABLE_ITEM.get(), 35);
        register(ModItems.INSULATED_GOLD_CABLE_ITEM.get(), 50);
        register(ModItems.HIGH_VOLTAGE_CABLE_ITEM.get(), 35);
        register(ModItems.HIGH_VOLTAGE_CABLE_INSULATED_ITEM.get(), 50);
        register(ModItems.GLASS_FIBER_CABLE_ITEM.get(), 75);

        // ========== IC2 CIRCUITS ==========
        register(ModItems.ELECTRONIC_CIRCUIT.get(), 75);
        register(ModItems.ADVANCED_CIRCUIT.get(), 100);

        // ========== IC2 MACHINE COMPONENTS ==========
        register(ModItems.MACHINE_ITEM.get(), 100);
        register(ModItems.MACHINE2_ITEM.get(), 100);
        register(ModItems.RE_BATTERY.get(), 75);
        register(ModItems.ADVANCED_RE_BATTERY.get(), 100);
        register(ModItems.ENERGY_CRYSTAL.get(), 100);
        register(ModItems.LAPOTRON_CRYSTAL.get(), 100);

        // ========== IC2 TOOLS ==========
        register(ModItems.WRENCH.get(), 50);
        register(ModItems.ELECTRIC_WRENCH.get(), 100);
        register(ModItems.TREETAP.get(), 35);
        register(ModItems.ELECTRIC_TREETAP.get(), 100);
        register(ModItems.MINING_DRILL.get(), 100);
        register(ModItems.DIAMOND_DRILL.get(), 100);
        register(ModItems.CHAINSAW.get(), 100);
        register(ModItems.ELECTRIC_HOE.get(), 100);
        register(ModItems.NANO_SABER.get(), 100);

        // ========== IC2 ARMOR ==========
        register(ModItems.BRONZE_HELMET.get(), 75);
        register(ModItems.BRONZE_CHESTPLATE.get(), 100);
        register(ModItems.BRONZE_LEGGINGS.get(), 100);
        register(ModItems.BRONZE_BOOTS.get(), 75);

        register(ModItems.NANO_HELMET.get(), 100);
        register(ModItems.NANO_BODYARMOR.get(), 100);
        register(ModItems.NANO_LEGGINGS.get(), 100);
        register(ModItems.NANO_BOOTS.get(), 100);

        register(ModItems.QUANTUM_HELMET.get(), 100);
        register(ModItems.QUANTUM_BODYARMOR.get(), 100);
        register(ModItems.QUANTUM_LEGGINGS.get(), 100);
        register(ModItems.QUANTUM_BOOTS.get(), 100);

        // Jetpacks and batpacks
        register(ModItems.BAT_PACK.get(), 100);
        register(ModItems.LAP_PACK.get(), 100);
        register(ModItems.JETPACK.get(), 100);
        register(ModItems.ELECTRIC_JETPACK.get(), 100);

        // ========== IC2 MACHINES ==========
        register(ModItems.GENERATOR_ITEM.get(), 100);
        register(ModItems.GEOTHERMAL_GENERATOR_ITEM.get(), 100);

        register(ModItems.ELECTRIC_FURNACE_ITEM.get(), 100);
        register(ModItems.MACERATOR_ITEM.get(), 100);
        register(ModItems.EXTRACTOR_ITEM.get(), 100);
        register(ModItems.COMPRESSOR_ITEM.get(), 100);
        register(ModItems.RECYCLER_ITEM.get(), 100);

        // ========== IC2 NUCLEAR ==========
        register(ModItems.URANIUM_ORE_ITEM.get(), 50);
        register(ModItems.URANIUM_INGOT.get(), 75);
        register(ModItems.URANIUM_CELL.get(), 100);
        register(ModItems.DEPLETED_ISOTOPE_CELL.get(), 50);
        register(ModItems.RE_ENRICHED_URANIUM_CELL.get(), 100);

        register(ModItems.INTEGRATED_REACTOR_PLATING.get(), 75);
        register(ModItems.INTEGRATED_HEAT_DISPERSER.get(), 75);
        register(ModItems.COOLING_CELL.get(), 50);

        // ========== MISC IC2 ==========
        register(ModItems.SCRAP.get(), 0);       // Scrap itself produces nothing
        register(ModItems.SCRAP_BOX.get(), 50);  // Scrap box has some value

        register(ModItems.SCAFFOLD_ITEM.get(), 0);  // Blacklisted in IC2
        register(ModItems.RUBBER_LEAVES_ITEM.get(), 0);  // Blacklisted

        // Building materials
        register(ModItems.CONSTRUCTION_FOAM_PELLET.get(), 20);
        register(ModItems.REINFORCED_STONE_ITEM.get(), 50);
        register(ModItems.REINFORCED_GLASS_ITEM.get(), 50);
    }

    private static void register(Item item, int points) {
        SCRAP_VALUES.put(item, points);
    }

    /**
     * Get the scrap point value for an item.
     * Returns 0 for unregistered items (raw materials, natural blocks).
     * Items with 0 points will be EJECTED by the recycler, not consumed.
     *
     * @param stack The item stack to check
     * @return Points value (0-100), where 100 points = 1 scrap
     */
    public static int getScrapValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return SCRAP_VALUES.getOrDefault(stack.getItem(), 0);
    }
}
