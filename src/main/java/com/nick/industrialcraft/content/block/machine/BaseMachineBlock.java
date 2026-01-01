package com.nick.industrialcraft.content.block.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;

import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import com.nick.industrialcraft.api.energy.OvervoltageHandler;

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
}
