package com.nick.industrialcraft.content.block.generator;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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

public class GeothermalGeneratorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public GeothermalGeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.SOUTH)
            .setValue(POWERED, false));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeothermalGeneratorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GeothermalGeneratorBlockEntity generator && player instanceof ServerPlayer sp) {
                sp.openMenu(generator, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ModBlockEntity.GEOTHERMAL_GENERATOR.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> GeothermalGeneratorBlockEntity.serverTick(lvl, pos, st, (GeothermalGeneratorBlockEntity) be);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof GeothermalGeneratorBlockEntity generator) {
                // Drop the fuel item if present
                var fuel = generator.getInventory().getStackInSlot(GeothermalGeneratorBlockEntity.FUEL_SLOT);
                if (!fuel.isEmpty()) {
                    popResource(realLevel, pos, fuel);
                }
                // Drop the output item if present
                var output = generator.getInventory().getStackInSlot(GeothermalGeneratorBlockEntity.OUTPUT_SLOT);
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
            .setValue(FACING, context.getHorizontalDirection());
    }

    // ========== Capability Registration ==========

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof GeothermalGeneratorBlockEntity generator) {
                    return generator.getEnergyStorage();
                }
                return null;
            },
            com.nick.industrialcraft.registry.ModBlocks.GEOTHERMAL_GENERATOR.get()
        );
    }
}
