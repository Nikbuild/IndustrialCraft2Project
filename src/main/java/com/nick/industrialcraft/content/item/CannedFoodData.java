package com.nick.industrialcraft.content.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Data component for canned food items.
 * Stores the source food item and calculated nutrition value.
 * Items only stack if ALL fields match exactly.
 */
public record CannedFoodData(
        ResourceLocation sourceItem,  // The original food item (e.g., "minecraft:cooked_beef")
        int foodValue,                 // Calculated food value (hunger restored)
        float saturation               // Saturation modifier
) {
    // Codec for saving/loading from NBT/JSON
    public static final Codec<CannedFoodData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("source").forGetter(CannedFoodData::sourceItem),
                    Codec.INT.fieldOf("food").forGetter(CannedFoodData::foodValue),
                    Codec.FLOAT.fieldOf("sat").forGetter(CannedFoodData::saturation)
            ).apply(instance, CannedFoodData::new)
    );

    // Stream codec for network sync
    public static final StreamCodec<RegistryFriendlyByteBuf, CannedFoodData> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, CannedFoodData::sourceItem,
            ByteBufCodecs.INT, CannedFoodData::foodValue,
            ByteBufCodecs.FLOAT, CannedFoodData::saturation,
            CannedFoodData::new
    );

    /**
     * Get a simplified display name for the canned food.
     * Groups similar items into categories (all fish = "Fish", all meat = "Meat", etc.)
     */
    public String getSimplifiedName() {
        String path = sourceItem.getPath();

        // Fish category
        if (path.contains("cod") || path.contains("salmon") ||
            path.equals("tropical_fish") || path.equals("pufferfish")) {
            return "Fish";
        }

        // Meat category (beef, pork, chicken, mutton, rabbit)
        if (path.contains("beef") || path.contains("porkchop") ||
            path.contains("chicken") || path.contains("mutton") ||
            path.contains("rabbit")) {
            return "Meat";
        }

        // Berries category
        if (path.contains("berries")) {
            return "Berries";
        }

        // Stew/Soup category
        if (path.contains("stew") || path.contains("soup")) {
            return "Stew";
        }

        // For other items, clean up the name
        path = path.replace("cooked_", "");
        path = path.replace("raw_", "");
        path = path.replace("baked_", "");
        path = path.replace("dried_", "");

        // Convert underscores to spaces and capitalize
        String[] words = path.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
                result.append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Get the texture variant name for model selection.
     * Returns the full source item path for texture lookup.
     */
    public String getTextureVariant() {
        return sourceItem.getPath();
    }
}
