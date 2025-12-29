package com.nick.industrialcraft.content.block.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.nick.industrialcraft.registry.ModBlockEntity;

public class ElectricFurnaceBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public ElectricFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.SOUTH)
            .setValue(POWERED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFurnaceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ElectricFurnaceBlockEntity furnace && player instanceof ServerPlayer sp) {
                sp.openMenu(furnace, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ModBlockEntity.ELECTRIC_FURNACE.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> ElectricFurnaceBlockEntity.serverTick(lvl, pos, st, (ElectricFurnaceBlockEntity) be);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof ElectricFurnaceBlockEntity furnace) {
                // Drop all inventory items
                var input = furnace.getInventory().getStackInSlot(ElectricFurnaceBlockEntity.INPUT_SLOT);
                if (!input.isEmpty()) {
                    popResource(realLevel, pos, input);
                }
                var battery = furnace.getInventory().getStackInSlot(ElectricFurnaceBlockEntity.BATTERY_SLOT);
                if (!battery.isEmpty()) {
                    popResource(realLevel, pos, battery);
                }
                var output = furnace.getInventory().getStackInSlot(ElectricFurnaceBlockEntity.OUTPUT_SLOT);
                if (!output.isEmpty()) {
                    popResource(realLevel, pos, output);
                }
            }
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // ========== Capability Registration ==========

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof ElectricFurnaceBlockEntity furnace) {
                    return furnace.getEnergyStorage();
                }
                return null;
            },
            com.nick.industrialcraft.registry.ModBlocks.ELECTRIC_FURNACE.get()
        );
    }
}
