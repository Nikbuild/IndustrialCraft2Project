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
import com.nick.industrialcraft.registry.ModDataComponents;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IElectricItem;
import com.nick.industrialcraft.api.energy.IEnergyTier;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager.MachineConnection;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.content.item.StoredEnergyData;

import java.util.*;

public class GeothermalGeneratorBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier, IWrenchable {

    public static final int FUEL_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int CHARGE_SLOT = 2;  // Slot for charging electric items
    public static final int SLOTS = 3;

    // Transfer rate for charging items (LV tier)
    private static final int CHARGE_TRANSFER_RATE = 32;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public void setSize(int size) {
            // Don't allow deserialization to resize - keep SLOTS size
            // This prevents old saves from shrinking the inventory
            if (size < SLOTS) {
                super.setSize(SLOTS);
            } else {
                super.setSize(size);
            }
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
        return Component.translatable("container.industrialcraft.geothermal_generator");
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

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.LV;  // Geothermal Generator is LV tier (outputs at 32V standard)
    }

    // Note: getOutputPacketSize() defaults to tier voltage (32V for LV)
    // The 20 EU/t is the WATTAGE (power generated), not the voltage

    // ========== Energy Transfer Logic (Simultaneous Distribution) ==========

    /**
     * Output energy to machines using simultaneous fair distribution.
     * Uses cached network scanning for optimal performance.
     * Every tick, ALL connected machines draw energy simultaneously from the generator's battery.
     * Geothermal Generator produces 20 EU/tick, can power ~5 furnaces (4 EU/tick each) in steady state.
     */
    private void outputEnergy(Level level, BlockPos pos) {
        // Use cached network manager for O(1) amortized performance
        List<MachineConnection> machines = EnergyNetworkManager.getConnectedMachines(
            level, pos, Direction.values()
        );

        if (machines.isEmpty() || energy <= 0) return;

        // Filter to only machines that actually want energy
        List<MachineConnection> needyMachines = new ArrayList<>();
        for (MachineConnection machine : machines) {
            int wants = machine.storage().receiveEnergy(Integer.MAX_VALUE, true);
            if (wants > 0) {
                needyMachines.add(machine);
            }
        }

        if (needyMachines.isEmpty()) return;

        // Fair distribution: split available energy equally among all machines that want it
        int energyPerMachine = energy / needyMachines.size();
        int totalTransferred = 0;

        // Get our output packet size (for tier checking)
        int packetSize = getOutputPacketSize();

        // Transfer to each machine
        for (MachineConnection machine : needyMachines) {
            // Check tier compatibility before transferring
            if (machine.blockEntity() instanceof IEnergyTier tieredMachine) {
                if (!tieredMachine.canSafelyReceive(packetSize)) {
                    // Machine can't handle this voltage - EXPLODE!
                    explodeMachine(level, machine.pos());
                    continue;  // Don't transfer to exploded machine
                }
            }

            // Give each machine its fair share
            int transferred = machine.storage().receiveEnergy(energyPerMachine, false);

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
     * Cause an explosion at the given position (machine received packet too large for its tier).
     */
    private void explodeMachine(Level level, BlockPos machinePos) {
        // Small explosion like IC2 (size 0.5-1.5)
        level.explode(
            null,  // No entity caused the explosion
            machinePos.getX() + 0.5,
            machinePos.getY() + 0.5,
            machinePos.getZ() + 0.5,
            1.0f,  // Explosion radius (small, just destroys the machine)
            Level.ExplosionInteraction.BLOCK  // Destroys blocks
        );
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

        // Charge items in the charging slot (transfer energy FROM Generator TO item)
        // Safety check for old saves that may have fewer slots
        if (be.energy > 0 && CHARGE_SLOT < be.inventory.getSlots()) {
            var chargeStack = be.inventory.getStackInSlot(CHARGE_SLOT);
            if (!chargeStack.isEmpty()) {
                // Check if item is an IElectricItem and if Generator tier (LV) is high enough
                if (chargeStack.getItem() instanceof IElectricItem electricItem) {
                    EnergyTier itemTier = electricItem.getTier(chargeStack);
                    if (itemTier.getTierLevel() <= EnergyTier.LV.getTierLevel()) {
                        int toTransfer = Math.min(be.energy, CHARGE_TRANSFER_RATE);
                        int transferred = electricItem.charge(chargeStack, toTransfer, EnergyTier.LV, false, false);
                        if (transferred > 0) {
                            be.energy -= transferred;
                            needsUpdate = true;
                        }
                    }
                } else {
                    // Non-IElectricItem - use capability (for compatibility with other mods)
                    IEnergyStorage itemStorage = chargeStack.getCapability(Capabilities.EnergyStorage.ITEM);
                    if (itemStorage != null && itemStorage.canReceive()) {
                        int toTransfer = Math.min(be.energy, CHARGE_TRANSFER_RATE);
                        int transferred = itemStorage.receiveEnergy(toTransfer, false);
                        if (transferred > 0) {
                            be.energy -= transferred;
                            needsUpdate = true;
                        }
                    }
                }
            }
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

    // ========== IWrenchable Implementation ==========

    @Override
    public boolean canWrenchRotate(net.minecraft.world.entity.player.Player player, Direction newFacing) {
        return newFacing.getAxis().isHorizontal();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(GeothermalGeneratorBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(GeothermalGeneratorBlock.FACING, facing), 3);
        }
    }

    @Override
    public boolean canWrenchRemove(net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @Override
    public int getStoredEnergy() {
        return energy;
    }

    @Override
    public void setStoredEnergy(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
    }

    @Override
    public int getMaxStoredEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public ItemStack createWrenchDrop() {
        ItemStack drop = new ItemStack(ModItems.GEOTHERMAL_GENERATOR_ITEM.get());
        if (energy > 0) {
            drop.set(ModDataComponents.STORED_ENERGY.get(), StoredEnergyData.of(energy));
        }
        return drop;
    }
}
