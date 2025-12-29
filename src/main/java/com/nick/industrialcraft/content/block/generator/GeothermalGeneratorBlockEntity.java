package com.nick.industrialcraft.content.block.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import com.nick.industrialcraft.registry.ModItems;
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

public class GeothermalGeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOTS = 2;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    // IC2 Geothermal Generator specifications:
    // - 10 EU/t generation rate
    // - 1000 EU per lava bucket/cell
    // - Max capacity: 24,000 EU (24 buckets)

    private int fuel = 0;                         // Current stored fuel/energy (in EU)
    private int energy = 0;                       // Current output buffer energy
    private boolean powered = false;              // Active/burning state

    private static final int MAX_FUEL = 24000;    // 24 buckets worth of lava
    private static final int FUEL_PER_BUCKET = 1000;  // Each lava bucket/cell = 1000 EU
    private static final int MAX_ENERGY = 1000;   // Buffer for smooth operation
    private static final int ENERGY_PER_TICK = 20;    // 20 EU/t generation (IC2-accurate)
    private static final int MAX_OUTPUT = 20;     // 20 EU/t output (IC2-accurate)

    // NeoForge Energy Capability (for compatibility with other mods)
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0; // Geothermal Generator doesn't receive energy
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (energy <= 0) return 0;

            int toExtract = Math.min(maxExtract, Math.min(energy, MAX_OUTPUT));

            if (!simulate) {
                energy -= toExtract;
                setChanged();
            }

            return toExtract;
        }

        @Override
        public int getEnergyStored() {
            return energy;
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
            return false; // Geothermal Generator only outputs energy
        }
    };

    public GeothermalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.GEOTHERMAL_GENERATOR.get(), pos, state);
    }

    // Expose energy capability to other mods
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Geothermal Generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new GeothermalGeneratorMenu(id, playerInv, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getFuel() {
        return fuel;
    }

    public int getMaxFuel() {
        return MAX_FUEL;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    // Legacy compatibility for GUI (burnTime used for progress bar)
    public int getBurnTime() {
        return fuel;
    }

    public int getMaxBurnTime() {
        return MAX_FUEL;
    }

    public void setFuelClient(int fuel) {
        this.fuel = Math.min(fuel, MAX_FUEL);
    }

    public void setEnergyClient(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPoweredClient(boolean powered) {
        this.powered = powered;
    }

    // ========== Energy Transfer Logic (Simultaneous Distribution) ==========

    /**
     * Output energy to machines using simultaneous fair distribution.
     * Every tick, ALL connected machines draw energy simultaneously from the generator's battery.
     * Geothermal Generator produces 20 EU/tick, can power ~5 furnaces (4 EU/tick each) in steady state.
     */
    private void outputEnergy(Level level, BlockPos pos) {
        // Scan the cable network to find all connected machines
        Set<BlockPos> visitedCables = new HashSet<>();
        List<MachineConnection> machines = new ArrayList<>();

        // Start scanning from all 6 adjacent positions
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.relative(dir);
            scanNetwork(level, neighborPos, visitedCables, machines);
        }

        if (machines.isEmpty()) return;
        if (energy <= 0) return;

        // Filter to only machines that actually want energy
        List<MachineConnection> needyMachines = new ArrayList<>();
        for (MachineConnection machine : machines) {
            int wants = machine.storage.receiveEnergy(Integer.MAX_VALUE, true);
            if (wants > 0) {
                needyMachines.add(machine);
            }
        }

        if (needyMachines.isEmpty()) return;

        // Fair distribution: split available energy equally among all machines that want it
        // Example: 20 EU battery, 4 furnaces → each gets 5 EU (20/4 = 5)
        int energyPerMachine = energy / needyMachines.size();
        int totalTransferred = 0;

        // Transfer to each machine
        for (MachineConnection machine : needyMachines) {
            // Give each machine its fair share
            int transferred = machine.storage.receiveEnergy(energyPerMachine, false);

            if (transferred > 0) {
                energy -= transferred;
                totalTransferred += transferred;
            }
        }

        if (totalTransferred > 0) {
            setChanged();
        }
    }

    /**
     * Recursively scan the cable network to find all connected machines.
     */
    private void scanNetwork(Level level, BlockPos pos, Set<BlockPos> visitedCables, List<MachineConnection> machines) {
        BlockState state = level.getBlockState(pos);

        // If this is a cable, explore its connections
        if (state.getBlock() instanceof BaseCableBlock) {
            // Skip if we've already visited this cable
            if (visitedCables.contains(pos)) return;
            visitedCables.add(pos);

            // Get the cable block entity to check connections
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof CableBlockEntity cable)) return;

            // Check all 6 directions from this cable
            for (Direction dir : Direction.values()) {
                if (!cable.isConnectedInDirection(state, dir)) continue;

                BlockPos neighborPos = pos.relative(dir);
                scanNetwork(level, neighborPos, visitedCables, machines);
            }
        }
        // If this is a machine (not a cable), check if it can receive energy
        else {
            // Check if we've already added this machine position (prevent duplicates)
            for (MachineConnection existing : machines) {
                if (existing.pos.equals(pos)) {
                    return;  // Already in the list, skip
                }
            }

            // Try to get energy capability
            IEnergyStorage neighborEnergy = level.getCapability(
                Capabilities.EnergyStorage.BLOCK,
                pos,
                null
            );

            if (neighborEnergy != null && neighborEnergy.canReceive()) {
                // Add this machine to the list (guaranteed unique now)
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

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, GeothermalGeneratorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        // Try to consume lava from inventory if we have space
        if (be.fuel < MAX_FUEL && !be.inventory.getStackInSlot(FUEL_SLOT).isEmpty()) {
            ItemStack fuelStack = be.inventory.getStackInSlot(FUEL_SLOT);

            if (fuelStack.is(Items.LAVA_BUCKET)) {
                // Add lava fuel from lava bucket
                int toAdd = Math.min(FUEL_PER_BUCKET, MAX_FUEL - be.fuel);
                be.fuel += toAdd;

                // Replace lava bucket with empty bucket
                be.inventory.setStackInSlot(FUEL_SLOT, new ItemStack(Items.BUCKET));
                needsUpdate = true;
            } else if (fuelStack.is(ModItems.LAVA_CELL.get())) {
                // Add lava fuel from lava cell
                int toAdd = Math.min(FUEL_PER_BUCKET, MAX_FUEL - be.fuel);
                be.fuel += toAdd;

                // Return empty cell to output slot
                fuelStack.shrink(1);
                ItemStack outputStack = be.inventory.getStackInSlot(OUTPUT_SLOT);
                if (outputStack.isEmpty()) {
                    be.inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.CELL.get()));
                } else if (outputStack.is(ModItems.CELL.get()) && outputStack.getCount() < outputStack.getMaxStackSize()) {
                    outputStack.grow(1);
                }
                needsUpdate = true;
            }
        }

        // Generate energy if we have fuel and storage isn't full
        if (be.fuel > 0 && be.energy + ENERGY_PER_TICK <= MAX_ENERGY) {
            be.fuel--;
            be.energy += ENERGY_PER_TICK;
            be.powered = true;
            needsUpdate = true;
        } else if (be.fuel <= 0) {
            be.powered = false;
        }

        // Clamp energy to max
        if (be.energy > MAX_ENERGY) {
            be.energy = MAX_ENERGY;
        }

        // Output energy to adjacent machines
        if (be.energy > 0) {
            be.outputEnergy(level, pos);
        }

        // Update blockstate if powered state changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(com.nick.industrialcraft.content.block.generator.GeothermalGeneratorBlock.POWERED, be.powered), 3);
            needsUpdate = true;
        }

        if (needsUpdate) {
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putInt("Fuel", fuel);
        out.putInt("Energy", energy);
        out.putBoolean("Powered", powered);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        fuel = in.getIntOr("Fuel", 0);
        energy = in.getIntOr("Energy", 0);
        powered = in.getBooleanOr("Powered", false);
    }
}
