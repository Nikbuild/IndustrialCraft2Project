package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeneratorMenu;
import com.nick.industrialcraft.content.block.generator.GeothermalGeneratorMenu;
import com.nick.industrialcraft.content.block.machine.ElectricFurnaceMenu;
import com.nick.industrialcraft.content.block.machine.MaceratorMenu;
import com.nick.industrialcraft.content.block.machine.ExtractorMenu;
import com.nick.industrialcraft.content.block.machine.CompressorMenu;
import com.nick.industrialcraft.content.block.machine.RecyclerMenu;
import com.nick.industrialcraft.content.block.machine.InductionFurnaceMenu;
import com.nick.industrialcraft.content.block.machine.CanningMachineMenu;
import com.nick.industrialcraft.content.block.machine.IronFurnaceMenu;
import com.nick.industrialcraft.content.block.storage.BatBoxMenu;
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

    // Macerator Menu
    public static final DeferredHolder<MenuType<?>, MenuType<MaceratorMenu>> MACERATOR =
            MENUS.register("macerator", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new MaceratorMenu(id, inv, buf))
            );

    // Extractor Menu
    public static final DeferredHolder<MenuType<?>, MenuType<ExtractorMenu>> EXTRACTOR =
            MENUS.register("extractor", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new ExtractorMenu(id, inv, buf))
            );

    // Compressor Menu
    public static final DeferredHolder<MenuType<?>, MenuType<CompressorMenu>> COMPRESSOR =
            MENUS.register("compressor", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new CompressorMenu(id, inv, buf))
            );

    // Recycler Menu
    public static final DeferredHolder<MenuType<?>, MenuType<RecyclerMenu>> RECYCLER =
            MENUS.register("recycler", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new RecyclerMenu(id, inv, buf))
            );

    // Induction Furnace Menu
    public static final DeferredHolder<MenuType<?>, MenuType<InductionFurnaceMenu>> INDUCTION_FURNACE =
            MENUS.register("induction_furnace", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new InductionFurnaceMenu(id, inv, buf))
            );

    // BatBox Menu
    public static final DeferredHolder<MenuType<?>, MenuType<BatBoxMenu>> BATBOX =
            MENUS.register("batbox", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new BatBoxMenu(id, inv, buf))
            );

    // Canning Machine Menu
    public static final DeferredHolder<MenuType<?>, MenuType<CanningMachineMenu>> CANNING_MACHINE =
            MENUS.register("canning_machine", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new CanningMachineMenu(id, inv, buf))
            );

    // Iron Furnace Menu
    public static final DeferredHolder<MenuType<?>, MenuType<IronFurnaceMenu>> IRON_FURNACE =
            MENUS.register("iron_furnace", () ->
                    IMenuTypeExtension.create((id, inv, buf) -> new IronFurnaceMenu(id, inv, buf))
            );
}
