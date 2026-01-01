package com.nick.industrialcraft.content.item;

import com.nick.industrialcraft.registry.ModDataComponents;
import com.nick.industrialcraft.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

/**
 * Filled tin can item - a consumable food item that stores its source food data.
 * The display name is simplified (e.g., "Canned Beef" not "Canned Cooked Beef").
 * Items only stack if they came from the exact same source food.
 */
public class FilledTinCanItem extends Item {

    public FilledTinCanItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        CannedFoodData data = stack.get(ModDataComponents.CANNED_FOOD.get());
        if (data != null) {
            String simpleName = data.getSimplifiedName();
            return Component.translatable("item.industrialcraft.canned_food_named", simpleName);
        }
        // Fallback for items without data
        return Component.translatable("item.industrialcraft.filled_tin_can");
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CannedFoodData data = stack.get(ModDataComponents.CANNED_FOOD.get());

        // Only allow eating if has food data and player can eat
        if (data != null && data.foodValue() > 0) {
            if (player.canEat(false)) {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof Player player) {
            CannedFoodData data = stack.get(ModDataComponents.CANNED_FOOD.get());

            if (data != null) {
                // Apply food effects
                player.getFoodData().eat(data.foodValue(), data.saturation());

                // Consume the item
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);

                    // Return empty tin can
                    ItemStack emptyCan = new ItemStack(ModItems.TIN_CAN.get());
                    if (!player.getInventory().add(emptyCan)) {
                        player.drop(emptyCan, false);
                    }
                }
            }
        }

        return stack;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        // Standard eating time
        return 32;
    }

    /**
     * Create a filled tin can with the specified source food data.
     */
    public static ItemStack createCannedFood(ResourceLocation sourceItem, int foodValue, float saturation) {
        ItemStack stack = new ItemStack(ModItems.FILLED_TIN_CAN.get());
        stack.set(ModDataComponents.CANNED_FOOD.get(), new CannedFoodData(sourceItem, foodValue, saturation));
        return stack;
    }
}
