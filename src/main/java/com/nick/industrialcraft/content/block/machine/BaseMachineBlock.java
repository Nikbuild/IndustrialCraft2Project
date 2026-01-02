package com.nick.industrialcraft.content.block.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;

import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import com.nick.industrialcraft.api.energy.OvervoltageHandler;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.content.item.StoredEnergyData;
import com.nick.industrialcraft.content.item.WrenchItem;
import com.nick.industrialcraft.registry.ModDataComponents;

import java.util.Collections;
import java.util.List;

/**
 * Base class for all machine blocks in IndustrialCraft.
 *
 * Provides common functionality:
 * - FACING and POWERED block state properties
 * - Menu opening on interaction
 * - Energy network cache invalidation on placement/removal
 * - Overvoltage checking on placement
 * - Item dropping on block destruction
 *
 * Subclasses need to implement:
 * - newBlockEntity(): Create the specific block entity
 * - getTicker(): Return the ticker for server-side logic
 * - getBlockEntityType(): Return the registered block entity type for ticker validation
 * - getInventorySlotCount(): Return number of inventory slots to drop on destroy
 */
public abstract class BaseMachineBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BaseMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.SOUTH)
            .setValue(POWERED, false));
    }

    // ========== Abstract Methods ==========

    /**
     * Get the registered BlockEntityType for this machine.
     * Used for ticker validation.
     */
    protected abstract BlockEntityType<?> getBlockEntityType();

    /**
     * Get the inventory handler from the block entity for dropping items.
     * Return null if the block entity doesn't have an inventory.
     */
    @Nullable
    protected abstract IItemHandler getInventory(BlockEntity blockEntity);

    // ========== Common Implementation ==========

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider menuProvider && player instanceof ServerPlayer sp) {
                sp.openMenu(menuProvider, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Block GUI opening when player is holding a wrench.
     * The wrench item handles the interaction itself.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        // If holding a wrench, don't open GUI - let the wrench handle it
        if (stack.getItem() instanceof WrenchItem) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            // Invalidate energy network cache
            if (!realLevel.isClientSide()) {
                EnergyNetworkManager.invalidateAt(realLevel, pos);
            }

            // Drop inventory items
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be != null) {
                IItemHandler inventory = getInventory(be);
                if (inventory != null) {
                    for (int i = 0; i < inventory.getSlots(); i++) {
                        var stack = inventory.getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            popResource(realLevel, pos, stack);
                        }
                    }
                }
            }
        }
        super.destroy(level, pos, state);
    }

    // ========== Overvoltage Check on Placement ==========

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !isMoving) {
            EnergyNetworkManager.invalidateAt(level, pos);
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, net.minecraft.server.level.ServerLevel level, BlockPos pos, RandomSource random) {
        OvervoltageHandler.checkOnPlacement(level, pos);
    }

    /**
     * Called when the block is placed. Restores stored energy from the item if present.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide) {
            // Check if the placed item has stored energy
            StoredEnergyData energyData = stack.get(ModDataComponents.STORED_ENERGY.get());
            if (energyData != null && energyData.hasEnergy()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof IWrenchable wrenchable) {
                    wrenchable.setStoredEnergy(energyData.energy());
                    be.setChanged();
                }
            }
        }
    }

    // ========== Wrench-Only Removal ==========

    /**
     * Override getDrops to return empty when broken by pickaxe.
     * Machines should only be removed via wrench to preserve energy.
     */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // Return empty list - machines don't drop when mined with pickaxe
        // The wrench handles proper drops with energy preservation
        return Collections.emptyList();
    }

    /**
     * Play a warning sound when the machine is being attacked (mined).
     * This alerts the player that they're destroying the machine.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);

        // Don't warn if player is using a wrench
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof WrenchItem) {
            return;
        }

        // Play warning sound - machine is being damaged!
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 0.8f);
        }
    }

    /**
     * Called when the block is destroyed. Drop inventory contents but not the machine itself.
     * The machine is lost when broken with a pickaxe - use a wrench!
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Play destruction sound to indicate loss
        if (!level.isClientSide) {
            // Check if using wrench - if so, don't play destruction sound (wrench handles it)
            ItemStack heldItem = player.getMainHandItem();
            if (!(heldItem.getItem() instanceof WrenchItem)) {
                level.playSound(null, pos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
