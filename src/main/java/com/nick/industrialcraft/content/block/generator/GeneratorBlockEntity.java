package com.nick.industrialcraft.content.block.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModDataComponents;
import com.nick.industrialcraft.registry.ModItems;
import com.nick.industrialcraft.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IElectricItem;
import com.nick.industrialcraft.api.energy.IEnergyTier;
import com.nick.industrialcraft.api.energy.IVoltageTransformer;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager.MachineConnection;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.content.item.StoredEnergyData;

import java.util.*;

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier, IWrenchable {

    public static final int FUEL_SLOT = 0;
    public static final int CHARGE_SLOT = 1;  // Slot for charging electric items
    public static final int SLOTS = 2;

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

    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int energy = 0;
    private boolean powered = false;

    private static final int MAX_ENERGY = 4000;  // Generator stores 4000 EU max
    private static final int ENERGY_PER_TICK = 10;  // 10 EU/tick generation (400 ticks * 10 EU = 4000 EU per coal)
    private static final int MAX_OUTPUT_RATE = 10;  // Maximum output rate per tick for simultaneous distribution
    private static final int SOUND_INTERVAL = 25;

    private int soundTimer = 0;

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
        return Component.translatable("container.industrialcraft.generator");
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

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.LV;  // Generator is LV tier (outputs at 32V standard)
    }

    // Note: getOutputPacketSize() defaults to tier voltage (32V for LV)
    // The 10 EU/t is the WATTAGE (power generated), not the voltage

    // ========== Energy Transfer Logic (Simultaneous Distribution) ==========

    /**
     * Output energy to machines using simultaneous distribution like original IC2.
     * Uses cached network scanning for optimal performance.
     * Every tick, ALL connected machines draw energy simultaneously from the generator's battery.
     * Generator produces 10 EU/tick, can power ~2.5 furnaces (4 EU/tick each) in steady state.
     */
    private void outputEnergy(Level level, BlockPos pos) {
        // Use cached network manager for O(1) amortized performance
        List<MachineConnection> machines = EnergyNetworkManager.getConnectedMachines(
            level, pos, Direction.values()
        );

        // Also check for direct neighbors in case the network scan misses them
        for (Direction dir : Direction.values()) {
            BlockPos directNeighbor = pos.relative(dir);
            BlockEntity directBe = level.getBlockEntity(directNeighbor);
            if (directBe != null && !(directBe instanceof com.nick.industrialcraft.content.block.cable.CableBlockEntity)) {
                Direction accessSide = dir.getOpposite();
                IEnergyStorage directStorage = level.getCapability(
                    net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                    directNeighbor,
                    accessSide
                );
                if (directStorage != null && directStorage.canReceive()) {
                    boolean alreadyInList = false;
                    for (MachineConnection mc : machines) {
                        if (mc.pos().equals(directNeighbor)) {
                            alreadyInList = true;
                            break;
                        }
                    }
                    if (!alreadyInList) {
                        machines = new ArrayList<>(machines);
                        machines.add(new MachineConnection(directNeighbor, directStorage, directBe, accessSide));
                    }
                }
            }
        }

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
            // For transformers, use side-specific check; for regular machines, use global check
            if (machine.blockEntity() instanceof IVoltageTransformer transformer) {
                // Transformer - check the specific side we're connecting to
                if (!transformer.canSideReceive(machine.accessSide(), packetSize)) {
                    // This side of the transformer can't handle this voltage - EXPLODE!
                    explodeMachine(level, machine.pos());
                    continue;
                }
            } else if (machine.blockEntity() instanceof IEnergyTier tieredMachine) {
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

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;

        // If we're burning, continue
        if (be.burnTime > 0) {
            be.burnTime--;
            be.powered = true;

            // Play operation sound periodically
            be.soundTimer++;
            if (be.soundTimer >= SOUND_INTERVAL) {
                level.playSound(null, pos, ModSounds.GENERATOR.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
                be.soundTimer = 0;
            }

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
            be.soundTimer = 0;

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
                            be.setChanged();
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
                            be.setChanged();
                        }
                    }
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

    // ========== IWrenchable Implementation ==========

    @Override
    public boolean canWrenchRotate(net.minecraft.world.entity.player.Player player, Direction newFacing) {
        return newFacing.getAxis().isHorizontal();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(GeneratorBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(GeneratorBlock.FACING, facing), 3);
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
        ItemStack drop = new ItemStack(ModItems.GENERATOR_ITEM.get());
        if (energy > 0) {
            drop.set(ModDataComponents.STORED_ENERGY.get(), StoredEnergyData.of(energy));
        }
        return drop;
    }
}
