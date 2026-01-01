package com.nick.industrialcraft.content.block.machine;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

import com.nick.industrialcraft.registry.ModBlockEntity;

/**
 * Induction Furnace - Advanced smelting with dual input slots and heat system
 *
 * Extends BaseMachineBlock for common functionality.
 */
public class InductionFurnaceBlock extends BaseMachineBlock {

    public InductionFurnaceBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InductionFurnaceBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return ModBlockEntity.INDUCTION_FURNACE.get();
    }

    @Nullable
    @Override
    protected IItemHandler getInventory(BlockEntity blockEntity) {
        if (blockEntity instanceof InductionFurnaceBlockEntity furnace) {
            return furnace.getInventory();
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != getBlockEntityType()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> InductionFurnaceBlockEntity.serverTick(lvl, pos, st, (InductionFurnaceBlockEntity) be);
    }

    // ========== Capability Registration ==========

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof InductionFurnaceBlockEntity furnace) {
                    return furnace.getEnergyStorage();
                }
                return null;
            },
            com.nick.industrialcraft.registry.ModBlocks.INDUCTION_FURNACE.get()
        );
    }
}
