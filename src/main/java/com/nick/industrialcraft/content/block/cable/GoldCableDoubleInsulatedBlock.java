package com.nick.industrialcraft.content.block.cable;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Double Insulated Gold Cable (IC2 Type 4)
 * - Transfer rate: 64 EU/t
 * - Capacity: 64 EU
 * - Double insulation (no power loss)
 * - 6-way connectivity
 */
public class GoldCableDoubleInsulatedBlock extends BaseCableBlock {

    public GoldCableDoubleInsulatedBlock(BlockBehaviour.Properties props) {
        super(props);
    }
}
