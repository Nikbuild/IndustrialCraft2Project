package com.nick.industrialcraft.content.item;

import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IElectricItem;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electric Wrench - faster machine removal using EU instead of durability.
 *
 * Mechanics:
 * - Right-click: Start holding to remove machine (2 seconds - 3x faster than regular)
 * - Shift+Right-click: Instant rotate machine
 * - 12,000 EU capacity
 * - 50 EU per rotation
 * - 500 EU per removal
 * - Preserves machine energy when removed
 *
 * IElectricItem tier: LV (can be charged in BatBox and above)
 */
public class ElectricWrenchItem extends WrenchItem implements IElectricItem {

    private static final int MAX_ENERGY = 12000;
    private static final int TRANSFER_LIMIT = 32;  // LV tier transfer rate
    private static final int REMOVE_TIME_TICKS = 40;  // 2 seconds (3x faster)
    private static final int ENERGY_PER_ROTATE = 50;
    private static final int ENERGY_PER_REMOVE = 500;

    // Store target position while using
    private BlockPos targetPos = null;

    public ElectricWrenchItem(Properties properties) {
        // No durability for electric tools - use stacksTo(1) instead
        super(properties.durability(0).stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        Direction clickedFace = context.getClickedFace();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        // Check if we have enough energy
        int storedEnergy = getStoredEnergy(stack);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IWrenchable wrenchable)) {
            return InteractionResult.PASS;
        }

        // Shift+click = rotate
        if (player.isShiftKeyDown()) {
            if (storedEnergy < ENERGY_PER_ROTATE) {
                // Not enough energy - play fail sound
                if (!level.isClientSide) {
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.5f);
                }
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide) {
                Direction currentFacing = wrenchable.getFacing();
                Direction newFacing = getNextFacing(currentFacing, clickedFace);

                if (wrenchable.canWrenchRotate(player, newFacing)) {
                    wrenchable.setFacing(newFacing);
                    level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 0.5f, 1.2f);

                    // Use energy instead of durability
                    setStoredEnergy(stack, storedEnergy - ENERGY_PER_ROTATE);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Normal click = start removal process
        if (storedEnergy < ENERGY_PER_REMOVE) {
            // Not enough energy
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.5f);
            }
            return InteractionResult.FAIL;
        }

        if (wrenchable.canWrenchRemove(player)) {
            this.targetPos = pos;
            player.startUsingItem(context.getHand());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
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

        // Check energy again
        int storedEnergy = getStoredEnergy(stack);
        if (storedEnergy < ENERGY_PER_REMOVE) {
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

        // Use energy
        setStoredEnergy(stack, storedEnergy - ENERGY_PER_REMOVE);

        targetPos = null;
        return stack;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        targetPos = null;
        return false;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseTicks) {
        if (!(entity instanceof Player player)) return;

        int usedTicks = getUseDuration(stack, entity) - remainingUseTicks;

        // Swing arm like attacking - every 6 ticks matches arm swing duration
        if (usedTicks % 6 == 0) {
            player.swing(player.getUsedItemHand(), true);
        }

        // Play faster electric ratcheting sound (every 5 ticks, server only)
        if (usedTicks > 0 && usedTicks % 5 == 0) {
            if (!level.isClientSide && targetPos != null) {
                float progress = (float) usedTicks / getRemoveTimeTicks();
                float pitch = 1.2f + (progress * 0.6f);  // 1.2 -> 1.8
                level.playSound(null, targetPos,
                        SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.3f, pitch);
            }
        }
    }

    @Override
    protected int getRemoveTimeTicks() {
        return REMOVE_TIME_TICKS;
    }

    @Override
    public boolean isElectricWrench() {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;  // Always show energy bar
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int stored = getStoredEnergy(stack);
        return Math.round((float) stored / MAX_ENERGY * 13.0f);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00BFFF;  // Electric blue color
    }

    // ========== Energy Storage Methods ==========

    public int getStoredEnergy(ItemStack stack) {
        StoredEnergyData data = stack.get(com.nick.industrialcraft.registry.ModDataComponents.STORED_ENERGY.get());
        return data != null ? data.energy() : 0;
    }

    public void setStoredEnergy(ItemStack stack, int energy) {
        energy = Math.max(0, Math.min(energy, MAX_ENERGY));
        stack.set(com.nick.industrialcraft.registry.ModDataComponents.STORED_ENERGY.get(),
                StoredEnergyData.of(energy));
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    // ========== IElectricItem Implementation ==========

    @Override
    public int getMaxCharge(ItemStack stack) {
        return MAX_ENERGY;
    }

    @Override
    public int getCharge(ItemStack stack) {
        return getStoredEnergy(stack);
    }

    @Override
    public void setCharge(ItemStack stack, int charge) {
        setStoredEnergy(stack, charge);
    }

    @Override
    public int getTransferLimit(ItemStack stack) {
        return TRANSFER_LIMIT;
    }

    @Override
    public EnergyTier getTier(ItemStack stack) {
        return EnergyTier.LV;  // Electric Wrench is LV tier
    }

    @Override
    public boolean canProvideEnergy(ItemStack stack) {
        return false;  // Tools don't discharge into machines
    }

    // Uses getNextFacing from parent WrenchItem class
}
