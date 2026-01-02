package com.nick.industrialcraft.content.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data component for machine block items that store energy.
 * When a machine is wrenched, its stored energy is preserved in this component.
 * Items only stack if the stored energy matches exactly.
 */
public record StoredEnergyData(int energy) {

    // Codec for saving/loading from NBT/JSON
    public static final Codec<StoredEnergyData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("energy").forGetter(StoredEnergyData::energy)
            ).apply(instance, StoredEnergyData::new)
    );

    // Stream codec for network sync
    public static final StreamCodec<RegistryFriendlyByteBuf, StoredEnergyData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, StoredEnergyData::energy,
            StoredEnergyData::new
    );

    /**
     * Create a StoredEnergyData with the given energy value.
     */
    public static StoredEnergyData of(int energy) {
        return new StoredEnergyData(energy);
    }

    /**
     * Check if this data represents a machine with stored energy.
     */
    public boolean hasEnergy() {
        return energy > 0;
    }
}
