package com.nick.industrialcraft.client.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.nick.industrialcraft.content.item.CannedFoodData;
import com.nick.industrialcraft.registry.ModDataComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * SelectItemModelProperty for canned food items.
 * Returns the source item path (e.g., "cooked_beef", "cod") for model selection.
 */
public record CannedFoodSelectProperty() implements SelectItemModelProperty<String> {

    public static final SelectItemModelProperty.Type<CannedFoodSelectProperty, String> TYPE =
        SelectItemModelProperty.Type.create(
            // MapCodec for deserializing this property from JSON
            MapCodec.unit(new CannedFoodSelectProperty()),
            // Codec for the value being selected (the "when" field in JSON)
            Codec.STRING
        );

    @Nullable
    @Override
    public String get(ItemStack stack, @Nullable ClientLevel level,
                      @Nullable LivingEntity entity, int seed,
                      ItemDisplayContext displayContext) {
        CannedFoodData data = stack.get(ModDataComponents.CANNED_FOOD.get());
        if (data != null) {
            // Return just the path portion (e.g., "cooked_beef", "cod")
            return data.sourceItem().getPath();
        }
        return null; // Uses fallback model
    }

    @Override
    public SelectItemModelProperty.Type<CannedFoodSelectProperty, String> type() {
        return TYPE;
    }

    @Override
    public Codec<String> valueCodec() {
        return Codec.STRING;
    }
}
