package com.nick.industrialcraft;

import com.nick.industrialcraft.client.screen.GeneratorScreen;
import com.nick.industrialcraft.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = IndustrialCraft.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = IndustrialCraft.MODID, value = Dist.CLIENT)
public final class IndustrialCraftClient {

    public IndustrialCraftClient(ModContainer container) {
        // Hook config screen into Mods UI (optional)
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        IndustrialCraft.LOGGER.info("IndustrialCraft client setup");
    }

    @SubscribeEvent
    static void onRegisterScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GENERATOR.get(), GeneratorScreen::new);
    }
}
