package com.nick.industrialcraft.content.item;

import com.nick.industrialcraft.api.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Wrench item for rotating and removing IC2 machines.
 *
 * Mechanics:
 * - Right-click: Start holding to remove machine (6 seconds)
 * - Shift+Right-click: Instant rotate machine
 * - 300 durability
 * - Preserves machine energy when removed
 */
public class WrenchItem extends Item {

    private static final int MAX_DAMAGE = 300;
    private static final int REMOVE_TIME_TICKS = 120;  // 6 seconds

    // Store the target position while using
    private BlockPos targetPos = null;

    public WrenchItem(Properties properties) {
        super(properties.durability(MAX_DAMAGE));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        Direction clickedFace = context.getClickedFace();

        if (player == null) return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IWrenchable wrenchable)) {
            return InteractionResult.PASS;
        }

        // Shift+click = rotate
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                // Cycle through horizontal facings or use clicked face
                Direction currentFacing = wrenchable.getFacing();
                Direction newFacing = getNextFacing(currentFacing, clickedFace);

                if (wrenchable.canWrenchRotate(player, newFacing)) {
                    wrenchable.setFacing(newFacing);
                    level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5f, 1.2f);

                    // Damage wrench
                    context.getItemInHand().hurtAndBreak(1, player, player.getEquipmentSlotForItem(context.getItemInHand()));
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Normal click = start removal process (hold to remove)
        if (wrenchable.canWrenchRemove(player)) {
            // Store target and start using
            this.targetPos = pos;
            player.startUsingItem(context.getHand());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!(entity instanceof Player player)) return;

        int usedTicks = getUseDuration(stack, entity) - remainingUseTicks;

        // Swing arm like attacking - every 6 ticks matches arm swing duration
        if (usedTicks % 6 == 0) {
            player.swing(player.getUsedItemHand(), true);
        }

        // Play periodic ratcheting sound while wrenching (every 10 ticks, server only)
        if (usedTicks > 0 && usedTicks % 10 == 0) {
            if (!level.isClientSide && targetPos != null) {
                // Pitch increases as we get closer to completion for tension
                float progress = (float) usedTicks / getRemoveTimeTicks();
                float pitch = 0.8f + (progress * 0.6f);  // 0.8 -> 1.4
                level.playSound(null, targetPos,
                        SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.4f, pitch);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return stack;
        if (level.isClientSide) return stack;

        if (targetPos == null) return stack;

        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        if (!(blockEntity instanceof IWrenchable wrenchable)) {
            targetPos = null;
            return stack;
        }

        if (!wrenchable.canWrenchRemove(player)) {
            targetPos = null;
            return stack;
        }

        // Get the machine drop with stored energy
        ItemStack machineDrop = wrenchable.createWrenchDrop();

        // Remove the block
        level.destroyBlock(targetPos, false);

        // Drop the machine item
        if (!machineDrop.isEmpty()) {
            if (!player.getInventory().add(machineDrop)) {
                player.drop(machineDrop, false);
            }
        }

        // Play success sound
        level.playSound(null, targetPos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.8f, 1.0f);

        // Damage wrench (more for removal)
        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));

        targetPos = null;
        return stack;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // Player released early - cancel operation
        targetPos = null;
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return getRemoveTimeTicks();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BRUSH;  // Brush animation allows arm movement
    }

    /**
     * Get the time in ticks to remove a machine. Can be overridden by subclasses.
     */
    protected int getRemoveTimeTicks() {
        return REMOVE_TIME_TICKS;
    }

    /**
     * Check if this is an electric wrench (for IWrenchable timing).
     */
    public boolean isElectricWrench() {
        return false;
    }

    /**
     * Get the next facing direction when rotating.
     * - Click on horizontal side (N/S/E/W): Front faces toward the clicked face (toward you)
     * - Click on top or bottom: Rotate clockwise through horizontal facings
     */
    protected Direction getNextFacing(Direction current, Direction clickedFace) {
        // If clicking on a horizontal face, set front to face toward the clicked face
        // (the clicked face normal points toward the player, so the machine faces you)
        if (clickedFace.getAxis().isHorizontal()) {
            return clickedFace;
        }
        // If clicking top or bottom, rotate clockwise
        return switch (current) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> Direction.NORTH;
        };
    }
}
