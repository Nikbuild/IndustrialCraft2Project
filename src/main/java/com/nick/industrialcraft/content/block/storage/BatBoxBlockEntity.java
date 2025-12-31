package com.nick.industrialcraft.content.block.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.content.block.cable.BaseCableBlock;
import com.nick.industrialcraft.content.block.cable.CableBlockEntity;

import java.util.*;

/**
 * BatBox BlockEntity - Basic energy storage (LV tier)
 *
 * Specifications from IC2 source:
 * - Storage: 40,000 EU
 * - Max Input: 32 EU/t (LV tier)
 * - Max Output: 32 EU/t (LV tier)
 *
 * Behavior:
 * - Accepts energy from 5 sides (all except output face)
 * - Outputs energy from 1 side (the output face with orange dot)
 * - Slot 0 (top): Charge items FROM BatBox storage
 * - Slot 1 (bottom): Discharge items INTO BatBox storage
 */
public class BatBoxBlockEntity extends BlockEntity implements MenuProvider {

    // Slot indices
    public static final int CHARGE_SLOT = 0;     // Items charged FROM BatBox
    public static final int DISCHARGE_SLOT = 1;  // Items discharged INTO BatBox
    public static final int SLOTS = 2;

    // Energy specifications (from IC2 TileEntityElectricBatBox: super(1, 32, 40000))
    private static final int MAX_ENERGY = 40000;  // 40,000 EU storage
    private static final int MAX_TRANSFER = 32;   // 32 EU/t (LV tier)

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int energyStored = 0;

    // Input-side energy storage (accepts energy from non-output sides)
    private final IEnergyStorage inputEnergyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int toAccept = Math.min(maxReceive, Math.min(MAX_TRANSFER, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;  // Input sides don't output
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_ENERGY;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    // Output-side energy storage (provides energy to adjacent machines)
    private final IEnergyStorage outputEnergyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Output side can also receive (for flexibility)
            int toAccept = Math.min(maxReceive, Math.min(MAX_TRANSFER, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int toExtract = Math.min(maxExtract, Math.min(MAX_TRANSFER, energyStored));
            if (!simulate && toExtract > 0) {
                energyStored -= toExtract;
                setChanged();
            }
            return toExtract;
        }

        @Override
        public int getEnergyStored() {
            return energyStored;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_ENERGY;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    public BatBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.BATBOX.get(), pos, state);
    }

    /**
     * Get the appropriate energy storage based on the side being accessed
     */
    public IEnergyStorage getEnergyStorageForSide(Direction side) {
        if (side == null) {
            return outputEnergyStorage;  // Default to output for null side
        }

        Direction outputFace = getBlockState().getValue(BatBoxBlock.FACING);
        if (side == outputFace) {
            return outputEnergyStorage;  // Output face can extract
        } else {
            return inputEnergyStorage;   // Input faces can only receive
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BatBoxBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        // Output energy through the output face (supports cable networks)
        if (be.energyStored > 0) {
            changed |= be.outputEnergy(level, pos, state);
        }

        // Handle charging items in CHARGE_SLOT (transfer energy FROM BatBox TO item)
        var chargeStack = be.inventory.getStackInSlot(CHARGE_SLOT);
        if (!chargeStack.isEmpty() && be.energyStored > 0) {
            IEnergyStorage itemStorage = chargeStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemStorage != null && itemStorage.canReceive()) {
                int toTransfer = Math.min(be.energyStored, MAX_TRANSFER);
                int transferred = itemStorage.receiveEnergy(toTransfer, false);
                if (transferred > 0) {
                    be.energyStored -= transferred;
                    changed = true;
                }
            }
        }

        // Handle discharging items in DISCHARGE_SLOT (transfer energy FROM item TO BatBox)
        var dischargeStack = be.inventory.getStackInSlot(DISCHARGE_SLOT);
        if (!dischargeStack.isEmpty() && be.energyStored < MAX_ENERGY) {
            IEnergyStorage itemStorage = dischargeStack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemStorage != null && itemStorage.canExtract()) {
                int spaceLeft = MAX_ENERGY - be.energyStored;
                int toExtract = Math.min(spaceLeft, MAX_TRANSFER);
                int extracted = itemStorage.extractEnergy(toExtract, false);
                if (extracted > 0) {
                    be.energyStored += extracted;
                    changed = true;
                }
            }
        }

        if (changed) {
            be.setChanged();
        }
    }

    // ========== Energy Output Logic (Cable Network Scanning) ==========

    /**
     * Output energy to machines on the output face, scanning through cable networks.
     * Similar to Generator's outputEnergy but only from the output face direction.
     */
    private boolean outputEnergy(Level level, BlockPos pos, BlockState state) {
        Direction outputFace = state.getValue(BatBoxBlock.FACING);
        BlockPos startPos = pos.relative(outputFace);

        // Scan the cable network starting from the output face
        Set<BlockPos> visitedCables = new HashSet<>();
        List<MachineConnection> machines = new ArrayList<>();

        scanNetwork(level, startPos, visitedCables, machines, pos);

        if (machines.isEmpty() || energyStored <= 0) return false;

        // Filter to only machines that actually want energy
        List<MachineConnection> needyMachines = new ArrayList<>();
        for (MachineConnection machine : machines) {
            int wants = machine.storage.receiveEnergy(Integer.MAX_VALUE, true);
            if (wants > 0) {
                needyMachines.add(machine);
            }
        }

        if (needyMachines.isEmpty()) return false;

        // Fair distribution: split available energy equally among all machines
        int availableEnergy = Math.min(energyStored, MAX_TRANSFER);
        int energyPerMachine = availableEnergy / needyMachines.size();
        if (energyPerMachine <= 0) energyPerMachine = 1;

        int totalTransferred = 0;

        for (MachineConnection machine : needyMachines) {
            int toTransfer = Math.min(energyPerMachine, energyStored);
            int transferred = machine.storage.receiveEnergy(toTransfer, false);
            if (transferred > 0) {
                energyStored -= transferred;
                totalTransferred += transferred;
            }
        }

        return totalTransferred > 0;
    }

    /**
     * Recursively scan the cable network to find all connected machines.
     */
    private void scanNetwork(Level level, BlockPos pos, Set<BlockPos> visitedCables, List<MachineConnection> machines, BlockPos sourcePos) {
        // Don't scan back to the BatBox itself
        if (pos.equals(sourcePos)) return;

        BlockState state = level.getBlockState(pos);

        // If this is a cable, explore its connections
        if (state.getBlock() instanceof BaseCableBlock) {
            if (visitedCables.contains(pos)) return;
            visitedCables.add(pos);

            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof CableBlockEntity cable)) return;

            // Check all 6 directions from this cable
            for (Direction dir : Direction.values()) {
                if (!cable.isConnectedInDirection(state, dir)) continue;
                BlockPos neighborPos = pos.relative(dir);
                scanNetwork(level, neighborPos, visitedCables, machines, sourcePos);
            }
        }
        // If this is a machine (not a cable), check if it can receive energy
        else {
            // Check for duplicates
            for (MachineConnection existing : machines) {
                if (existing.pos.equals(pos)) return;
            }

            // Try to get energy capability
            IEnergyStorage neighborEnergy = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                pos,
                null
            );

            if (neighborEnergy != null && neighborEnergy.canReceive()) {
                machines.add(new MachineConnection(pos, neighborEnergy));
            }
        }
    }

    /**
     * Helper class to store information about connected machines.
     */
    private static class MachineConnection {
        final BlockPos pos;
        final IEnergyStorage storage;

        MachineConnection(BlockPos pos, IEnergyStorage storage) {
            this.pos = pos;
            this.storage = storage;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putInt("Energy", energyStored);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        energyStored = in.getIntOr("Energy", 0);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcraft.batbox");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new BatBoxMenu(id, playerInv, this);
    }

    // Getters for Menu/Screen
    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getEnergy() {
        return energyStored;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public int getMaxTransfer() {
        return MAX_TRANSFER;
    }
}
