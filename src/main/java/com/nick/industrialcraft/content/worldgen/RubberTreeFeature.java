package com.nick.industrialcraft.content.worldgen;

import com.mojang.serialization.Codec;
import com.nick.industrialcraft.content.block.tree.RubberWoodBlock;
import com.nick.industrialcraft.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Custom rubber tree feature for world generation.
 *
 * Based on IC2's WorldGenRubTree mechanics (exact implementation):
 * - Height: 2-8 blocks (h/2 + random(h/2+1))
 * - treeholechance starts at 25, if random.nextInt(100) <= treeholechance, place resin spot
 * - After placing resin spot, treeholechance -= 10 (so 25 -> 15 -> 5 -> -5)
 * - Resin spot direction is random.nextInt(4) + 2 (sides 2-5: NESW)
 * - Leaves placed around trunk at certain heights
 */
public class RubberTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MAX_HEIGHT = 8;

    // Direction mapping: 2=NORTH, 3=SOUTH, 4=EAST, 5=WEST (IC2 uses sides 2-5)
    private static final Direction[] RESIN_DIRECTIONS = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public RubberTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Check ground - IC2 checks for grass or dirt
        BlockState groundState = level.getBlockState(origin.below());
        if (!groundState.is(BlockTags.DIRT) && !groundState.is(Blocks.GRASS_BLOCK)) {
            return false;
        }

        // Get available grow height (IC2's getGrowHeight logic)
        int availableHeight = getGrowHeight(level, origin);
        if (availableHeight < 2) {
            return false;
        }

        // Calculate actual tree height (IC2 logic: h/2 + random(h/2+1))
        int halfHeight = availableHeight / 2;
        int remaining = availableHeight - halfHeight;
        int height = halfHeight + random.nextInt(remaining + 1);

        // IC2's treeholechance system - starts at 25
        int treeholechance = 25;

        RubberWoodBlock rubberWood = (RubberWoodBlock) ModBlocks.RUBBER_WOOD.get();
        BlockState woodState = rubberWood.defaultBlockState();

        // Place trunk with resin spots (IC2 exact logic)
        for (int i = 0; i < height; i++) {
            BlockPos trunkPos = origin.above(i);

            // Place the rubber wood block
            BlockState stateToPlace;

            // IC2 logic: if random.nextInt(100) <= treeholechance, place resin spot
            if (random.nextInt(100) <= treeholechance) {
                treeholechance -= 10; // Reduce chance for next spot

                // IC2 uses random.nextInt(4) + 2 for direction (sides 2-5)
                Direction resinDir = RESIN_DIRECTIONS[random.nextInt(4)];
                stateToPlace = rubberWood.withResinSpot(resinDir);
            } else {
                stateToPlace = woodState;
            }

            level.setBlock(trunkPos, stateToPlace, Block.UPDATE_ALL);

            // Place leaves (IC2 logic for leaf placement)
            if (height < 4 || (height < 7 && i > 1) || i > 2) {
                placeLeavesLayer(level, origin, i, height, random);
            }
        }

        // Place top leaves (above trunk)
        int topLeaves = height / 4 + random.nextInt(2);
        for (int i = 0; i <= topLeaves; i++) {
            BlockPos leafPos = origin.above(height + i);
            if (isReplaceable(level, leafPos)) {
                // Distance increases as we go higher above the trunk
                int distance = i + 1; // 1 for directly above trunk, increases with height
                distance = Math.min(distance, 6); // Clamp to prevent decay

                BlockState leavesState = ModBlocks.RUBBER_LEAVES.get().defaultBlockState()
                        .setValue(LeavesBlock.DISTANCE, distance)
                        .setValue(LeavesBlock.PERSISTENT, false);
                level.setBlock(leafPos, leavesState, Block.UPDATE_ALL);
            }
        }

        return true;
    }

    /**
     * IC2's getGrowHeight logic - checks how much room above for tree growth
     */
    private int getGrowHeight(WorldGenLevel level, BlockPos origin) {
        // Check ground is grass or dirt
        BlockState below = level.getBlockState(origin.below());
        if (!below.is(BlockTags.DIRT) && !below.is(Blocks.GRASS_BLOCK)) {
            return 0;
        }

        // Check origin is air or sapling
        BlockState atOrigin = level.getBlockState(origin);
        if (!atOrigin.isAir() && !atOrigin.is(ModBlocks.RUBBER_SAPLING.get())) {
            return 0;
        }

        // Count available height (up to MAX_HEIGHT)
        int height = 1;
        for (int y = 1; y < MAX_HEIGHT && level.getBlockState(origin.above(y)).isAir(); y++) {
            height++;
        }

        return height;
    }

    /**
     * Place leaves in a layer around the trunk (IC2 logic)
     */
    private void placeLeavesLayer(WorldGenLevel level, BlockPos origin, int trunkY, int treeHeight, RandomSource random) {
        int y = origin.getY() + trunkY;

        // IC2 uses a 5x5 area (-2 to +2) with probability falloff
        for (int a = -2; a <= 2; a++) {
            for (int b = -2; b <= 2; b++) {
                int c = trunkY + 4 - treeHeight;
                if (c < 1) c = 1;

                // IC2's leaf placement probability
                boolean gen = (a > -2 && a < 2 && b > -2 && b < 2) ||
                        (a > -2 && a < 2 && random.nextInt(c) == 0) ||
                        (b > -2 && b < 2 && random.nextInt(c) == 0);

                if (gen) {
                    BlockPos leafPos = new BlockPos(origin.getX() + a, y, origin.getZ() + b);
                    if (isReplaceable(level, leafPos)) {
                        // Calculate distance from trunk (taxicab distance)
                        int distance = Math.abs(a) + Math.abs(b);
                        if (distance == 0) distance = 1; // On trunk, distance is 1
                        // Clamp distance to valid range (1-7), leaves decay at 7
                        distance = Math.min(distance, 6);

                        BlockState leavesState = ModBlocks.RUBBER_LEAVES.get().defaultBlockState()
                                .setValue(LeavesBlock.DISTANCE, distance)
                                .setValue(LeavesBlock.PERSISTENT, false);
                        level.setBlock(leafPos, leavesState, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES) ||
                state.is(BlockTags.LEAVES) || state.is(Blocks.WATER);
    }
}
