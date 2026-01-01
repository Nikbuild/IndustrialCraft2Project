package com.nick.industrialcraft.content.item;

import com.nick.industrialcraft.content.block.tree.RubberWoodBlock;
import com.nick.industrialcraft.registry.ModBlocks;
import com.nick.industrialcraft.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Treetap item for extracting sticky resin from rubber wood.
 *
 * Based on IC2 mechanics:
 * - Right-click on a rubber wood block with a wet resin spot to extract 1-3 resin
 * - Dry spots have 20% chance to still yield 1 resin
 * - Has limited durability (16 uses by default in IC2)
 */
public class TreetapItem extends Item {
    private static final int MAX_DAMAGE = 16;

    public TreetapItem(Properties properties) {
        super(properties.durability(MAX_DAMAGE));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (state.getBlock() instanceof RubberWoodBlock rubberWood) {
            if (!level.isClientSide) {
                int resinCount = rubberWood.extractResin(level, pos, state, player, context.getClickedFace());

                if (resinCount > 0) {
                    // Give player sticky resin
                    ItemStack resinStack = new ItemStack(ModItems.STICKY_RESIN.get(), resinCount);
                    if (player != null) {
                        if (!player.getInventory().add(resinStack)) {
                            player.drop(resinStack, false);
                        }

                        // Damage the treetap
                        ItemStack treetap = context.getItemInHand();
                        treetap.hurtAndBreak(1, player, player.getEquipmentSlotForItem(treetap));
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
