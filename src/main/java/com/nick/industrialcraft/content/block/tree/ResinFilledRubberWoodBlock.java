package com.nick.industrialcraft.content.block.tree;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Resin-Filled Rubber Wood block.
 *
 * This is the item form of rubber wood that was harvested with wet resin still inside.
 * When processed in an Extractor, it yields more rubber than regular rubber wood.
 *
 * - Regular Rubber Wood in Extractor: 1 Rubber
 * - Resin-Filled Rubber Wood in Extractor: 1 Rubber + 3 Sticky Resin
 */
public class ResinFilledRubberWoodBlock extends RotatedPillarBlock {
    public static final MapCodec<ResinFilledRubberWoodBlock> CODEC = simpleCodec(ResinFilledRubberWoodBlock::new);

    @Override
    public MapCodec<? extends RotatedPillarBlock> codec() {
        return CODEC;
    }

    public ResinFilledRubberWoodBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Y));
    }
}
