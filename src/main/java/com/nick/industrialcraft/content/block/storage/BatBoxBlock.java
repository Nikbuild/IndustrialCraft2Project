package com.nick.industrialcraft.content.block.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.api.energy.OvervoltageHandler;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;

/**
 * BatBox - Basic energy storage block (LV tier)
 *
 * Features:
 * - 40,000 EU storage capacity
 * - 32 EU/t max input/output (LV tier)
 * - Directional output face (6 possible directions)
 * - 2 inventory slots for charging/discharging items
 * - Input from 5 sides, output from 1 side (the output face)
 */
public class BatBoxBlock extends Block implements EntityBlock {

    // BatBox can face any direction (not just horizontal like machines)
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public BatBoxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Output face points toward where the player is looking (opposite of player facing)
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BatBoxBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntity.BATBOX.get()
                ? (lvl, pos, st, be) -> BatBoxBlockEntity.serverTick(lvl, pos, st, (BatBoxBlockEntity) be)
                : null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BatBoxBlockEntity batBoxBE) {
                serverPlayer.openMenu(batBoxBE, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            if (!realLevel.isClientSide()) {
                EnergyNetworkManager.invalidateAt(realLevel, pos);
            }
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof BatBoxBlockEntity batBox) {
                // Drop all inventory items
                var inv = batBox.getInventory();
                for (int i = 0; i < inv.getSlots(); i++) {
                    var stack = inv.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        popResource(realLevel, pos, stack);
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

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntity.BATBOX.get(),
                (be, side) -> be.getEnergyStorageForSide(side)
        );
    }
}
