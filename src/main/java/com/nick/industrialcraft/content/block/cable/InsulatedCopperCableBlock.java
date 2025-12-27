package com.nick.industrialcraft.content.block.cable;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Insulated Copper Cable (IC2 Type 0)
 * - Transfer rate: 40 EU/t
 * - Capacity: 40 EU
 * - Better insulation (no power loss to adjacent blocks)
 * - 6-way connectivity
 */
public class InsulatedCopperCableBlock extends BaseCableBlock {

    public InsulatedCopperCableBlock(BlockBehaviour.Properties props) {
        super(props);
    }
}
