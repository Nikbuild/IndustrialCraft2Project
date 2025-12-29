package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeneratorMenu;
import com.nick.industrialcraft.content.block.generator.GeothermalGeneratorMenu;
import com.nick.industrialcraft.content.block.machine.ElectricFurnaceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, IndustrialCraft.MODID);

    // Generator Menu
    public static final DeferredHolder<MenuType<?>, MenuType<GeneratorMenu>> GENERATOR =
            MENUS.register("generator", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new GeneratorMenu(id, inv, buf))
            );

    // Geothermal Generator Menu
    public static final DeferredHolder<MenuType<?>, MenuType<GeothermalGeneratorMenu>> GEOTHERMAL_GENERATOR =
            MENUS.register("geothermal_generator", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new GeothermalGeneratorMenu(id, inv, buf))
            );

    // Electric Furnace Menu
    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFurnaceMenu>> ELECTRIC_FURNACE =
            MENUS.register("electric_furnace", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new ElectricFurnaceMenu(id, inv, buf))
            );
}
