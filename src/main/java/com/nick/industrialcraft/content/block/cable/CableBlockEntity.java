package com.nick.industrialcraft.content.block.cable;

import com.nick.industrialcraft.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * BlockEntity for energy cables.
 * Cables are simple pass-through conductors - they don't handle distribution logic.
 * Energy distribution is managed by generators that scan the cable network.
 */
public class CableBlockEntity extends BlockEntity {

    // Cables don't store energy - they just provide connections
    private static final int TRANSFER_BUFFER = 0;

    // Simple energy capability - cables just indicate they exist in the network
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Cables don't receive energy directly anymore
            // Generators handle all distribution
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return TRANSFER_BUFFER;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    };

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.CABLE.get(), pos, state);
    }

    /**
     * Check if cable is connected in a specific direction based on blockstate properties.
     */
    public boolean isConnectedInDirection(BlockState state, Direction dir) {
        return switch (dir) {
            case NORTH -> state.getValue(BaseCableBlock.NORTH);
            case SOUTH -> state.getValue(BaseCableBlock.SOUTH);
            case EAST -> state.getValue(BaseCableBlock.EAST);
            case WEST -> state.getValue(BaseCableBlock.WEST);
            case UP -> state.getValue(BaseCableBlock.UP);
            case DOWN -> state.getValue(BaseCableBlock.DOWN);
        };
    }

    /**
     * Expose energy capability to other blocks/mods.
     */
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    /**
     * Server tick - no longer needed, generators handle everything.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, CableBlockEntity cable) {
        // Cables are now passive - they don't do anything on tick
    }
}
