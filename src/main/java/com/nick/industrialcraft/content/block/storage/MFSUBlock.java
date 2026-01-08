package com.nick.industrialcraft.content.block.storage;

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
import net.minecraft.world.level.Level;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
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
 * MFSU - High Voltage energy storage block (HV tier)
 *
 * Features:
 * - 10,000,000 EU storage capacity
 * - 512 EU/t max input/output (HV tier)
 * - Directional output face (6 possible directions)
 * - 2 inventory slots for charging/discharging items
 * - Input from 5 sides, output from 1 side (the output face)
 */
public class MFSUBlock extends Block implements EntityBlock {

    // MFSU can face any direction (not just horizontal like machines)
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

    public MFSUBlock(Properties properties) {
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
        return new MFSUBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntity.MFSU.get()
                ? (lvl, pos, st, be) -> MFSUBlockEntity.serverTick(lvl, pos, st, (MFSUBlockEntity) be)
                : null;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MFSUBlockEntity mfsuBE) {
                serverPlayer.openMenu(mfsuBE, pos);
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

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            if (!realLevel.isClientSide()) {
                EnergyNetworkManager.invalidateAt(realLevel, pos);
            }
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof MFSUBlockEntity mfsu) {
                // Drop all inventory items
                var inv = mfsu.getInventory();
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
                ModBlockEntity.MFSU.get(),
                (be, side) -> be.getEnergyStorageForSide(side)
        );
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
