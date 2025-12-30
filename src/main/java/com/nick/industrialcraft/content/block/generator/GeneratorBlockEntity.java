package com.nick.industrialcraft.content.block.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int SLOTS = 1;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int energy = 0;
    private boolean powered = false;

    private static final int MAX_ENERGY = 4000;  // Generator stores 4000 EU max
    private static final int ENERGY_PER_TICK = 10;  // 10 EU/tick generation (400 ticks * 10 EU = 4000 EU per coal)
    private static final int MAX_OUTPUT_RATE = 10;  // Maximum output rate per tick for simultaneous distribution

    // NeoForge Energy Capability (for compatibility with other mods)
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0; // Generator doesn't receive energy
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (energy <= 0) return 0;

            // For external mods, allow extracting at max output rate
            int toExtract = Math.min(maxExtract, Math.min(energy, MAX_OUTPUT_RATE));

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
            return false; // Generator only outputs energy
        }
    };

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.GENERATOR.get(), pos, state);
    }

    // Expose energy capability to other mods
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new GeneratorMenu(id, playerInv, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public void setBurnTimeClient(int time) {
        this.burnTime = time;
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
     * Output energy to machines using simultaneous distribution like original IC2.
     * Every tick, ALL connected machines draw energy simultaneously from the generator's battery.
     * Generator produces 13 EU/tick, can power ~3.25 furnaces (4 EU/tick each) in steady state.
     * With 4+ furnaces, battery drains. With 3 or less, battery charges.
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
        // Example: 13 EU battery, 4 furnaces → each gets 3 EU (13/4 = 3.25, rounded down)
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

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;

        // If we're burning, continue
        if (be.burnTime > 0) {
            be.burnTime--;
            be.powered = true;

            // Generate energy while burning (if not already full)
            if (be.energy < MAX_ENERGY) {
                be.energy += ENERGY_PER_TICK;
                if (be.energy > MAX_ENERGY) {
                    be.energy = MAX_ENERGY;
                }
            }

            be.setChanged();
        } else {
            be.powered = false;

            // Only try to get fuel if storage is NOT full
            if (be.energy < MAX_ENERGY && !be.inventory.getStackInSlot(FUEL_SLOT).isEmpty()) {
                var fuelStack = be.inventory.getStackInSlot(FUEL_SLOT);

                // Simple fuel check - accept coal and charcoal
                if (isFuel(fuelStack)) {
                    be.maxBurnTime = 400; // 20 seconds (400 * 10 = 4,000 EU per coal)
                    be.burnTime = be.maxBurnTime;
                    be.inventory.extractItem(FUEL_SLOT, 1, false);
                    be.powered = true;
                    be.setChanged();
                }
            }
        }

        // Output energy to adjacent machines
        if (be.energy > 0) {
            be.outputEnergy(level, pos);
        }

        // Update blockstate if powered changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(com.nick.industrialcraft.content.block.generator.GeneratorBlock.POWERED, be.powered), 3);
        }
    }

    private static boolean isFuel(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return false;
        var item = stack.getItem();
        return item == net.minecraft.world.item.Items.COAL ||
               item == net.minecraft.world.item.Items.CHARCOAL;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putInt("BurnTime", burnTime);
        out.putInt("MaxBurnTime", maxBurnTime);
        out.putInt("Energy", energy);
        out.putBoolean("Powered", powered);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        burnTime = in.getIntOr("BurnTime", 0);
        maxBurnTime = in.getIntOr("MaxBurnTime", 0);
        energy = in.getIntOr("Energy", 0);
        powered = in.getBooleanOr("Powered", false);
    }
}
