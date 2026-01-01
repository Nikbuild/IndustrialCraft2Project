package com.nick.industrialcraft.content.block.tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

import com.nick.industrialcraft.registry.ModItems;

/**
 * Rubber Wood block with resin spot mechanics.
 *
 * Based on IC2 mechanics:
 * - Resin spots can appear on any horizontal face (NORTH, SOUTH, EAST, WEST)
 * - Spots start as "wet" (untapped) and can be tapped with a treetap
 * - After tapping, spots become "dry" (tapped)
 * - Dry spots have a small chance to regenerate via random tick (1/200)
 * - When tapping, yields 1-3 sticky resin
 * - Dry spots have 20% chance to yield more resin when tapped again
 */
public class RubberWoodBlock extends RotatedPillarBlock {
    public static final MapCodec<RubberWoodBlock> CODEC = simpleCodec(RubberWoodBlock::new);

    // Whether this block has a resin spot
    public static final BooleanProperty HAS_RESIN = BooleanProperty.create("has_resin");
    // Whether the resin spot is wet (untapped) or dry (tapped)
    public static final BooleanProperty WET = BooleanProperty.create("wet");
    // The direction the resin spot faces (only meaningful when HAS_RESIN is true)
    public static final EnumProperty<Direction> RESIN_FACING = EnumProperty.create("resin_facing", Direction.class,
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    public RubberWoodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(HAS_RESIN, false)
                .setValue(WET, false)
                .setValue(RESIN_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_RESIN, WET, RESIN_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(HAS_RESIN, false)
                .setValue(WET, false);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        // Only tick if we have a dry resin spot that could regenerate
        return state.getValue(HAS_RESIN) && !state.getValue(WET);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Dry resin spots have 1/200 chance to regenerate per random tick
        if (state.getValue(HAS_RESIN) && !state.getValue(WET)) {
            if (random.nextInt(200) == 0) {
                level.setBlock(pos, state.setValue(WET, true), Block.UPDATE_ALL);
            }
        }
    }

    /**
     * Called when a player uses a treetap on this block.
     * @return The number of sticky resin extracted (0 if none)
     */
    public int extractResin(Level level, BlockPos pos, BlockState state, Player player, Direction clickedFace) {
        if (!state.getValue(HAS_RESIN)) {
            return 0;
        }

        Direction resinFacing = state.getValue(RESIN_FACING);

        // Must click the face where the resin is
        if (clickedFace != resinFacing) {
            return 0;
        }

        RandomSource random = level.getRandom();
        int resinCount = 0;

        if (state.getValue(WET)) {
            // Untapped spot: yields 1-3 resin
            resinCount = 1 + random.nextInt(3);
            // Mark as dry
            level.setBlock(pos, state.setValue(WET, false), Block.UPDATE_ALL);
        } else {
            // Dry spot: 20% chance to get 1 resin anyway
            if (random.nextFloat() < 0.2f) {
                resinCount = 1;
            }
        }

        if (resinCount > 0) {
            // Play extraction sound
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        return resinCount;
    }

    /**
     * Creates a state with a wet resin spot on the given face.
     */
    public BlockState withResinSpot(Direction facing) {
        return this.defaultBlockState()
                .setValue(HAS_RESIN, true)
                .setValue(WET, true)
                .setValue(RESIN_FACING, facing);
    }

    /**
     * Creates a state with a wet resin spot on a random horizontal face.
     */
    public BlockState withRandomResinSpot(RandomSource random) {
        Direction[] horizontalDirs = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        Direction facing = horizontalDirs[random.nextInt(horizontalDirs.length)];
        return withResinSpot(facing);
    }
}
