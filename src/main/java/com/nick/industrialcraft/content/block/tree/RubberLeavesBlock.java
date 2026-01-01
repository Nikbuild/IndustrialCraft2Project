package com.nick.industrialcraft.content.block.tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Rubber Leaves block with proper leaf decay mechanics.
 *
 * Features:
 * - Decays when not connected to rubber wood
 * - Drops rubber sapling with small chance
 * - Can be sheared
 */
public class RubberLeavesBlock extends LeavesBlock {
    public static final MapCodec<RubberLeavesBlock> CODEC = simpleCodec(RubberLeavesBlock::new);

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    public RubberLeavesBlock(BlockBehaviour.Properties properties) {
        super(0.0f, properties); // 0 particle chance (no falling leaves)
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        // Rubber leaves don't have falling particles (like oak)
        // But we can add a subtle drip effect occasionally
        if (random.nextInt(50) == 0) {
            ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypes.DRIPPING_WATER);
        }
    }
}
