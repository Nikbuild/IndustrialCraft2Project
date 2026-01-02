package com.nick.industrialcraft.content.block.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

import com.nick.industrialcraft.registry.ModBlockEntity;

/**
 * Iron Furnace - A fuel-based furnace that smelts 20% faster than vanilla.
 * Uses fuel (coal, wood, etc.) instead of electricity.
 * Operation time: 160 ticks (8 seconds) vs vanilla's 200 ticks (10 seconds).
 */
public class IronFurnaceBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public IronFurnaceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
            .setValue(FACING, Direction.SOUTH)
            .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronFurnaceBlockEntity(pos, state);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IronFurnaceBlockEntity ironFurnace && player instanceof ServerPlayer sp) {
                sp.openMenu(ironFurnace, pos);
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
        if (blockEntityType != ModBlockEntity.IRON_FURNACE.get()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> IronFurnaceBlockEntity.serverTick(lvl, pos, st, (IronFurnaceBlockEntity) be);
    }

    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level realLevel) {
            BlockEntity be = realLevel.getBlockEntity(pos);
            if (be instanceof IronFurnaceBlockEntity ironFurnace) {
                var inv = ironFurnace.getInventory();
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

    /**
     * Spawn flame and smoke particles when the furnace is burning.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5;
            double y = pos.getY() + random.nextDouble() * 6.0 / 16.0;
            double z = pos.getZ() + 0.5;
            double offset = 0.52;
            double randOffset = random.nextDouble() * 0.6 - 0.3;

            switch (facing) {
                case WEST -> {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x - offset, y, z + randOffset, 0, 0, 0);
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x - offset, y, z + randOffset, 0, 0, 0);
                }
                case EAST -> {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x + offset, y, z + randOffset, 0, 0, 0);
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x + offset, y, z + randOffset, 0, 0, 0);
                }
                case NORTH -> {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x + randOffset, y, z - offset, 0, 0, 0);
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x + randOffset, y, z - offset, 0, 0, 0);
                }
                case SOUTH -> {
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x + randOffset, y, z + offset, 0, 0, 0);
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x + randOffset, y, z + offset, 0, 0, 0);
                }
                default -> {}
            }
        }
    }

    // ========== Wrench Support ==========

    /**
     * Restore stored energy when placing a machine that was wrenched.
     * (Iron Furnace doesn't have energy, but we implement for consistency)
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
     * No drops when mined with pickaxe - use wrench to preserve contents.
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
