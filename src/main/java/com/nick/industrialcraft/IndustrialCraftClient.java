package com.nick.industrialcraft;

import com.nick.industrialcraft.client.item.CannedFoodSelectProperty;
import com.nick.industrialcraft.client.screen.ElectricFurnaceScreen;
import com.nick.industrialcraft.client.screen.GeneratorScreen;
import com.nick.industrialcraft.client.screen.GeothermalGeneratorScreen;
import com.nick.industrialcraft.client.screen.MaceratorScreen;
import com.nick.industrialcraft.client.screen.ExtractorScreen;
import com.nick.industrialcraft.client.screen.CompressorScreen;
import com.nick.industrialcraft.client.screen.RecyclerScreen;
import com.nick.industrialcraft.client.screen.InductionFurnaceScreen;
import com.nick.industrialcraft.client.screen.CanningMachineScreen;
import com.nick.industrialcraft.client.screen.BatBoxScreen;
import com.nick.industrialcraft.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
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
        event.register(ModMenus.GEOTHERMAL_GENERATOR.get(), GeothermalGeneratorScreen::new);
        event.register(ModMenus.ELECTRIC_FURNACE.get(), ElectricFurnaceScreen::new);
        event.register(ModMenus.MACERATOR.get(), MaceratorScreen::new);
        event.register(ModMenus.EXTRACTOR.get(), ExtractorScreen::new);
        event.register(ModMenus.COMPRESSOR.get(), CompressorScreen::new);
        event.register(ModMenus.RECYCLER.get(), RecyclerScreen::new);
        event.register(ModMenus.INDUCTION_FURNACE.get(), InductionFurnaceScreen::new);
        event.register(ModMenus.CANNING_MACHINE.get(), CanningMachineScreen::new);
        event.register(ModMenus.BATBOX.get(), BatBoxScreen::new);
    }

    @SubscribeEvent
    static void onRegisterSelectItemModelProperties(RegisterSelectItemModelPropertyEvent event) {
        // Register canned food select property for dynamic texture selection
        event.register(
            ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "canned_food_type"),
            CannedFoodSelectProperty.TYPE
        );
    }
}
