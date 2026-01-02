package com.nick.industrialcraft.content.block.generator;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import com.nick.industrialcraft.content.item.WrenchItem;
import com.nick.industrialcraft.content.item.StoredEnergyData;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.registry.ModDataComponents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import java.util.Collections;
import java.util.List;
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

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.api.energy.OvervoltageHandler;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class GeneratorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public GeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.SOUTH)
            .setValue(POWERED, false));
    }

    // Register energy capability for this block
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof GeneratorBlockEntity generator) {
                    return generator.getEnergyStorage();
                }
                return null;
            },
            com.nick.industrialcraft.registry.ModBlocks.GENERATOR.get()
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneratorBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GeneratorBlockEntity generator && player instanceof ServerPlayer sp) {
                sp.openMenu(generator, pos);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // If holding a wrench, don't open GUI - let the wrench handle it
        if (stack.getItem() instanceof WrenchItem) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ModBlockEntity.GENERATOR.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> GeneratorBlockEntity.serverTick(lvl, pos, st, (GeneratorBlockEntity) be);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            if (!realLevel.isClientSide()) {
                EnergyNetworkManager.invalidateAt(realLevel, pos);
            }
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof GeneratorBlockEntity generator) {
                // Drop the fuel item if present
                var fuel = generator.getInventory().getStackInSlot(GeneratorBlockEntity.FUEL_SLOT);
                if (!fuel.isEmpty()) {
                    popResource(realLevel, pos, fuel);
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

    // ========== Overvoltage Check on Placement ==========

    /**
     * When a generator is placed, check if it's connected to machines
     * that can't handle its voltage tier. If so, apply consequences immediately.
     */
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

    // ========== Wrench Support ==========

    /**
     * Restore stored energy when placing a machine that was wrenched.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
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

    /**
     * No drops when mined with pickaxe - use wrench to preserve energy.
     */
    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Collections.emptyList();
    }

    /**
     * Play warning sound when attacked (mined) without wrench.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);
        if (player.getMainHandItem().getItem() instanceof WrenchItem) {
            return;
        }
        if (!level.isClientSide) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 0.8f);
        }
    }

    /**
     * Play destruction sound when broken without wrench.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (!(player.getMainHandItem().getItem() instanceof WrenchItem)) {
                level.playSound(null, pos, SoundEvents.ANVIL_DESTROY, SoundSource.BLOCKS, 1.0f, 0.5f);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
