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
 * Extractor - Extracts materials (rubber from sticky resin, etc.)
 *
 * Extends BaseMachineBlock for common functionality.
 */
public class ExtractorBlock extends BaseMachineBlock {

    public ExtractorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ExtractorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<?> getBlockEntityType() {
        return ModBlockEntity.EXTRACTOR.get();
    }

    @Nullable
    @Override
    protected IItemHandler getInventory(BlockEntity blockEntity) {
        if (blockEntity instanceof ExtractorBlockEntity extractor) {
            return extractor.getInventory();
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != getBlockEntityType()) {
            return null;
        }
        return level.isClientSide ? null : (lvl, pos, st, be) -> ExtractorBlockEntity.serverTick(lvl, pos, st, (ExtractorBlockEntity) be);
    }

    // ========== Capability Registration ==========

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
            Capabilities.EnergyStorage.BLOCK,
            (level, pos, state, be, side) -> {
                if (be instanceof ExtractorBlockEntity extractor) {
                    return extractor.getEnergyStorage();
                }
                return null;
            },
            com.nick.industrialcraft.registry.ModBlocks.EXTRACTOR.get()
        );
    }
}
