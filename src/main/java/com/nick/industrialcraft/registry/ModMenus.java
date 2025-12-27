package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeneratorMenu;
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
}
