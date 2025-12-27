package com.nick.industrialcraft.content.block.cable;

import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Uninsulated High Voltage Cable (IC2 Iron Cable Type 5)
 * - Transfer rate: 512 EU/t
 * - Capacity: 512 EU
 * - 6-way connectivity
 * - Thickness: 6 pixels (5-11)
 */
public class HighVoltageCableBlock extends BaseCableBlock {

    public HighVoltageCableBlock(BlockBehaviour.Properties props) {
        super(props);
    }
}
