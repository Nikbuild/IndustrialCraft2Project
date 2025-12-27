package com.nick.industrialcraft.content.block.cable;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Insulated High Voltage Cable (IC2 Iron Cable Type 6)
 * - Transfer rate: 512 EU/t
 * - Capacity: 512 EU
 * - 6-way connectivity
 * - Thickness: 8 pixels (4-12)
 */
public class HighVoltageCableInsulatedBlock extends BaseCableBlock {

    public HighVoltageCableInsulatedBlock(BlockBehaviour.Properties props) {
        super(props);
    }
}
