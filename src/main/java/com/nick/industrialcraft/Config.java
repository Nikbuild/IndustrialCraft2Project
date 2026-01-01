package com.nick.industrialcraft;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * IndustrialCraft Configuration System
 *
 * Server owners can modify these values to customize the mod experience.
 * All energy values are in EU (Energy Units), matching classic IC2.
 *
 * Configuration is split into categories:
 * - Energy Tiers: Voltage limits for each tier (explosion thresholds)
 * - Generators: Power output and fuel burn times
 * - Machines: Processing speeds and energy consumption
 * - Storage: BatBox/MFE/MFSU capacities and transfer rates
 * - Network: Cable scanning and caching settings for performance tuning
 * - Gameplay: Overvoltage explosions, debug mode, etc.
 */
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==========================================================================
    // ENERGY TIERS (Voltage Limits)
    // ==========================================================================

    static {
        BUILDER.comment(
            "Energy Tier Configuration",
            "Each tier defines the maximum packet size (EU/t) a machine can receive.",
            "Receiving a packet larger than the machine's tier will cause an explosion!",
            "These values define the classic IC2 voltage system."
        ).push("energy_tiers");
    }

    public static final ModConfigSpec.IntValue LV_MAX_PACKET = BUILDER
            .comment("Low Voltage (LV) - Maximum packet size in EU/t",
                     "Default: 32 (used by basic machines, BatBox, generators)")
            .defineInRange("lv_max_packet", 32, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MV_MAX_PACKET = BUILDER
            .comment("Medium Voltage (MV) - Maximum packet size in EU/t",
                     "Default: 128 (used by advanced machines, MFE, geothermal)")
            .defineInRange("mv_max_packet", 128, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue HV_MAX_PACKET = BUILDER
            .comment("High Voltage (HV) - Maximum packet size in EU/t",
                     "Default: 512 (used by industrial machines, MFSU)")
            .defineInRange("hv_max_packet", 512, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue EV_MAX_PACKET = BUILDER
            .comment("Extreme Voltage (EV) - Maximum packet size in EU/t",
                     "Default: 2048 (used by end-game machines, fusion reactor)")
            .defineInRange("ev_max_packet", 2048, 1, Integer.MAX_VALUE);

    static { BUILDER.pop(); }

    // ==========================================================================
    // GENERATOR SETTINGS
    // ==========================================================================

    static {
        BUILDER.comment(
            "Generator Configuration",
            "Settings for power generation machines."
        ).push("generators");
    }

    // Basic Generator
    static { BUILDER.push("basic_generator"); }

    public static final ModConfigSpec.IntValue GENERATOR_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 4000")
            .defineInRange("max_energy", 4000, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue GENERATOR_EU_PER_TICK = BUILDER
            .comment("Energy generated per tick in EU/t",
                     "Default: 10")
            .defineInRange("eu_per_tick", 10, 1, 1000);

    public static final ModConfigSpec.IntValue GENERATOR_BURN_TIME_COAL = BUILDER
            .comment("Burn time for coal/charcoal in ticks",
                     "Default: 400 (20 seconds, produces 4000 EU total)")
            .defineInRange("burn_time_coal", 400, 1, 100000);

    static { BUILDER.pop(); }

    // Geothermal Generator
    static { BUILDER.push("geothermal_generator"); }

    public static final ModConfigSpec.IntValue GEOTHERMAL_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 24000")
            .defineInRange("max_energy", 24000, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue GEOTHERMAL_EU_PER_TICK = BUILDER
            .comment("Energy generated per tick in EU/t",
                     "Default: 20")
            .defineInRange("eu_per_tick", 20, 1, 1000);

    public static final ModConfigSpec.IntValue GEOTHERMAL_EU_PER_LAVA_BUCKET = BUILDER
            .comment("Total EU generated per lava bucket",
                     "Default: 20000")
            .defineInRange("eu_per_lava_bucket", 20000, 1000, Integer.MAX_VALUE);

    static { BUILDER.pop(); }

    static { BUILDER.pop(); } // pop generators

    // ==========================================================================
    // MACHINE SETTINGS
    // ==========================================================================

    static {
        BUILDER.comment(
            "Machine Configuration",
            "Settings for processing machines (furnace, macerator, etc.).",
            "Each machine has its own processing time and energy consumption."
        ).push("machines");
    }

    // Electric Furnace
    static { BUILDER.push("electric_furnace"); }

    public static final ModConfigSpec.IntValue ELECTRIC_FURNACE_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 416")
            .defineInRange("max_energy", 416, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue ELECTRIC_FURNACE_EU_PER_TICK = BUILDER
            .comment("Energy consumed per tick while processing in EU/t",
                     "Default: 4")
            .defineInRange("eu_per_tick", 4, 1, 100);

    public static final ModConfigSpec.IntValue ELECTRIC_FURNACE_PROCESS_TIME = BUILDER
            .comment("Processing time in ticks (20 ticks = 1 second)",
                     "Default: 100 (5 seconds)")
            .defineInRange("process_time", 100, 1, 10000);

    static { BUILDER.pop(); }

    // Macerator
    static { BUILDER.push("macerator"); }

    public static final ModConfigSpec.IntValue MACERATOR_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 832")
            .defineInRange("max_energy", 832, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MACERATOR_EU_PER_TICK = BUILDER
            .comment("Energy consumed per tick while processing in EU/t",
                     "Default: 2")
            .defineInRange("eu_per_tick", 2, 1, 100);

    public static final ModConfigSpec.IntValue MACERATOR_PROCESS_TIME = BUILDER
            .comment("Processing time in ticks (20 ticks = 1 second)",
                     "Default: 400 (20 seconds)")
            .defineInRange("process_time", 400, 1, 10000);

    static { BUILDER.pop(); }

    // Compressor
    static { BUILDER.push("compressor"); }

    public static final ModConfigSpec.IntValue COMPRESSOR_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 832")
            .defineInRange("max_energy", 832, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue COMPRESSOR_EU_PER_TICK = BUILDER
            .comment("Energy consumed per tick while processing in EU/t",
                     "Default: 2")
            .defineInRange("eu_per_tick", 2, 1, 100);

    public static final ModConfigSpec.IntValue COMPRESSOR_PROCESS_TIME = BUILDER
            .comment("Processing time in ticks",
                     "Default: 400 (20 seconds)")
            .defineInRange("process_time", 400, 1, 10000);

    static { BUILDER.pop(); }

    // Extractor
    static { BUILDER.push("extractor"); }

    public static final ModConfigSpec.IntValue EXTRACTOR_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 832")
            .defineInRange("max_energy", 832, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue EXTRACTOR_EU_PER_TICK = BUILDER
            .comment("Energy consumed per tick while processing in EU/t",
                     "Default: 2")
            .defineInRange("eu_per_tick", 2, 1, 100);

    public static final ModConfigSpec.IntValue EXTRACTOR_PROCESS_TIME = BUILDER
            .comment("Processing time in ticks",
                     "Default: 400 (20 seconds)")
            .defineInRange("process_time", 400, 1, 10000);

    static { BUILDER.pop(); }

    // Recycler
    static { BUILDER.push("recycler"); }

    public static final ModConfigSpec.IntValue RECYCLER_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 77")
            .defineInRange("max_energy", 77, 50, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue RECYCLER_EU_PER_TICK = BUILDER
            .comment("Energy consumed per tick while processing in EU/t",
                     "Default: 1")
            .defineInRange("eu_per_tick", 1, 1, 100);

    public static final ModConfigSpec.IntValue RECYCLER_PROCESS_TIME = BUILDER
            .comment("Processing time in ticks",
                     "Default: 45 (2.25 seconds)")
            .defineInRange("process_time", 45, 1, 10000);

    public static final ModConfigSpec.IntValue RECYCLER_POINTS_PER_SCRAP = BUILDER
            .comment("Points needed to produce one scrap",
                     "Default: 100")
            .defineInRange("points_per_scrap", 100, 1, 10000);

    static { BUILDER.pop(); }

    // Canning Machine
    static { BUILDER.push("canning_machine"); }

    public static final ModConfigSpec.IntValue CANNING_MACHINE_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 1200")
            .defineInRange("max_energy", 1200, 100, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue CANNING_MACHINE_EU_PER_TICK = BUILDER
            .comment("Energy consumed per tick while processing in EU/t",
                     "Default: 1")
            .defineInRange("eu_per_tick", 1, 1, 100);

    public static final ModConfigSpec.IntValue CANNING_MACHINE_PROCESS_TIME = BUILDER
            .comment("Processing time in ticks",
                     "Default: 100 (5 seconds)")
            .defineInRange("process_time", 100, 1, 10000);

    static { BUILDER.pop(); }

    // Induction Furnace
    static { BUILDER.push("induction_furnace"); }

    public static final ModConfigSpec.IntValue INDUCTION_FURNACE_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 10000")
            .defineInRange("max_energy", 10000, 1000, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue INDUCTION_FURNACE_EU_PER_TICK = BUILDER
            .comment("Base energy consumed per tick while processing in EU/t",
                     "Default: 16")
            .defineInRange("eu_per_tick", 16, 1, 1000);

    public static final ModConfigSpec.IntValue INDUCTION_FURNACE_PROCESS_TIME = BUILDER
            .comment("Base processing time in ticks (decreases with heat)",
                     "Default: 400")
            .defineInRange("process_time", 400, 1, 10000);

    public static final ModConfigSpec.IntValue INDUCTION_FURNACE_MAX_HEAT = BUILDER
            .comment("Maximum heat level (affects processing speed)",
                     "Default: 10000")
            .defineInRange("max_heat", 10000, 100, Integer.MAX_VALUE);

    static { BUILDER.pop(); }

    static { BUILDER.pop(); } // pop machines

    // ==========================================================================
    // ENERGY STORAGE SETTINGS
    // ==========================================================================

    static {
        BUILDER.comment(
            "Energy Storage Configuration",
            "Settings for BatBox, MFE, MFSU, and other storage blocks."
        ).push("storage");
    }

    // BatBox
    static { BUILDER.push("batbox"); }

    public static final ModConfigSpec.IntValue BATBOX_MAX_ENERGY = BUILDER
            .comment("Maximum energy storage in EU",
                     "Default: 40000")
            .defineInRange("max_energy", 40000, 1000, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BATBOX_MAX_TRANSFER = BUILDER
            .comment("Maximum transfer rate per tick in EU/t",
                     "Default: 32 (LV tier)")
            .defineInRange("max_transfer", 32, 1, 10000);

    static { BUILDER.pop(); }

    static { BUILDER.pop(); } // pop storage

    // ==========================================================================
    // NETWORK PERFORMANCE SETTINGS
    // ==========================================================================

    static {
        BUILDER.comment(
            "Network Performance Configuration",
            "These settings control cable network scanning and caching.",
            "Adjust these for large modpacks or servers with many machines.",
            "CAUTION: Changing these values can impact server performance!"
        ).push("network");
    }

    public static final ModConfigSpec.IntValue MAX_NETWORK_SIZE = BUILDER
            .comment("Maximum number of blocks to scan in a single network",
                     "Prevents infinite loops and reduces lag on large networks",
                     "Default: 1000 (increase for very large factories)")
            .defineInRange("max_network_size", 1000, 100, 100000);

    public static final ModConfigSpec.IntValue CACHE_EXPIRY_TICKS = BUILDER
            .comment("How long network topology is cached before rescanning (in ticks)",
                     "Higher values = better performance but slower reaction to changes",
                     "Default: 100 (5 seconds)")
            .defineInRange("cache_expiry_ticks", 100, 1, 12000);

    public static final ModConfigSpec.IntValue MAX_OVERVOLTAGE_SCAN_SIZE = BUILDER
            .comment("Maximum blocks to scan when checking for overvoltage on placement",
                     "Default: 1000")
            .defineInRange("max_overvoltage_scan_size", 1000, 100, 100000);

    static { BUILDER.pop(); }

    // ==========================================================================
    // GAMEPLAY SETTINGS
    // ==========================================================================

    static {
        BUILDER.comment(
            "Gameplay Configuration",
            "General gameplay mechanics and balance settings."
        ).push("gameplay");
    }

    public static final ModConfigSpec.BooleanValue ENABLE_OVERVOLTAGE_EXPLOSIONS = BUILDER
            .comment("Enable machine explosions when receiving too high voltage",
                     "Disable for a more forgiving experience",
                     "Default: true (classic IC2 behavior)")
            .define("enable_overvoltage_explosions", true);

    public static final ModConfigSpec.DoubleValue OVERVOLTAGE_EXPLOSION_RADIUS = BUILDER
            .comment("Explosion radius when a machine explodes from overvoltage",
                     "Default: 1.0 (small, just destroys the machine)")
            .defineInRange("overvoltage_explosion_radius", 1.0, 0.1, 10.0);

    public static final ModConfigSpec.BooleanValue ENABLE_ENERGY_LOSS = BUILDER
            .comment("Enable energy loss through cables based on distance",
                     "Default: false (not yet implemented)")
            .define("enable_energy_loss", false);

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enable detailed debug logging for energy networks",
                     "Useful for troubleshooting but may spam logs",
                     "Default: false")
            .define("debug_logging", false);

    public static final ModConfigSpec.BooleanValue SHOW_EU_IN_TOOLTIPS = BUILDER
            .comment("Show EU values in item tooltips",
                     "Default: true")
            .define("show_eu_in_tooltips", true);

    static { BUILDER.pop(); }

    // ==========================================================================
    // FOOD & CANNING SETTINGS
    // ==========================================================================

    static {
        BUILDER.comment(
            "Food and Canning Configuration",
            "Settings for canned food and food-related mechanics."
        ).push("food");
    }

    public static final ModConfigSpec.IntValue CANNED_FOOD_HUNGER = BUILDER
            .comment("Hunger points restored by canned food",
                     "Default: 6 (3 drumsticks)")
            .defineInRange("canned_food_hunger", 6, 1, 20);

    public static final ModConfigSpec.DoubleValue CANNED_FOOD_SATURATION = BUILDER
            .comment("Saturation modifier for canned food",
                     "Default: 0.6")
            .defineInRange("canned_food_saturation", 0.6, 0.0, 2.0);

    public static final ModConfigSpec.IntValue TICKS_PER_FOOD_POINT = BUILDER
            .comment("Ticks of energy generation per food point when canning",
                     "Used to calculate energy content of canned food",
                     "Default: 10")
            .defineInRange("ticks_per_food_point", 10, 1, 100);

    static { BUILDER.pop(); }

    // ==========================================================================
    // BUILD THE SPEC
    // ==========================================================================

    public static final ModConfigSpec SPEC = BUILDER.build();

    // ==========================================================================
    // HELPER METHODS FOR ACCESSING CONFIG VALUES
    // ==========================================================================

    /**
     * Get the maximum packet size for a given tier level (1-4).
     */
    public static int getMaxPacketForTier(int tierLevel) {
        return switch (tierLevel) {
            case 1 -> LV_MAX_PACKET.get();
            case 2 -> MV_MAX_PACKET.get();
            case 3 -> HV_MAX_PACKET.get();
            case 4 -> EV_MAX_PACKET.get();
            default -> LV_MAX_PACKET.get();
        };
    }

    /**
     * Check if debug logging is enabled.
     */
    public static boolean isDebugLogging() {
        return DEBUG_LOGGING.get();
    }

    /**
     * Log a debug message if debug logging is enabled.
     */
    public static void debugLog(String message, Object... args) {
        if (DEBUG_LOGGING.get()) {
            IndustrialCraft.LOGGER.info("[IC-DEBUG] " + message, args);
        }
    }
}
