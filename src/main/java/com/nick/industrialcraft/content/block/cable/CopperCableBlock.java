package com.nick.industrialcraft.content.block.cable;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Uninsulated Copper Cable (IC2 Type 1)
 * - Transfer rate: 40 EU/t
 * - Capacity: 40 EU
 * - 6-way connectivity
 */
public class CopperCableBlock extends BaseCableBlock {

    public CopperCableBlock(BlockBehaviour.Properties props) {
        super(props);
    }
}
