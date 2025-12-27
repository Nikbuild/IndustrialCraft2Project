package com.nick.industrialcraft.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Central capability wiring scaffold for IndustrialCraft.
 */
public final class ModCapabilities {

    private ModCapabilities() {}

    /** Call once from the mod constructor. */
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModCapabilities::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // Capabilities will be registered here as machines are implemented.
    }
}
