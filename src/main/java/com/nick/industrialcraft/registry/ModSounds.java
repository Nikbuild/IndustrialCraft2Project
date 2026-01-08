package com.nick.industrialcraft.registry;

import com.nick.industrialcraft.IndustrialCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry for all IC2 custom sounds.
 *
 * Sound categories:
 * - Machines: Processing machine operation sounds
 * - Generators: Power generation sounds
 * - Tools: Tool usage sounds
 */
public final class ModSounds {

    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, IndustrialCraft.MODID);

    // ========== Machine Sounds ==========

    /** Macerator grinding operation sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> MACERATOR =
            registerSound("machines.macerator");

    /** Compressor operation sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPRESSOR =
            registerSound("machines.compressor");

    /** Extractor operation sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> EXTRACTOR =
            registerSound("machines.extractor");

    /** Recycler operation sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> RECYCLER =
            registerSound("machines.recycler");

    /** Iron Furnace cooking sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> IRON_FURNACE =
            registerSound("machines.iron_furnace");

    /** Electric Furnace loop sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> ELECTRIC_FURNACE =
            registerSound("machines.electric_furnace");

    /** Induction Furnace loop sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> INDUCTION_FURNACE =
            registerSound("machines.induction_furnace");

    /** Machine interrupt/power loss sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE_INTERRUPT =
            registerSound("machines.interrupt");

    // ========== Generator Sounds ==========

    /** Generator running loop sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> GENERATOR =
            registerSound("generators.generator");

    /** Geothermal Generator running loop sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> GEOTHERMAL_GENERATOR =
            registerSound("generators.geothermal");

    // ========== Tool Sounds ==========

    /** Wrench operation sound */
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH =
            registerSound("tools.wrench");

    /**
     * Helper method to register a sound event.
     */
    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, name);
        return SOUNDS.register(name.replace(".", "_"), () -> SoundEvent.createVariableRangeEvent(id));
    }
}
