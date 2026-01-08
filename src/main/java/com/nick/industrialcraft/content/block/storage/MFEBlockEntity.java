package com.nick.industrialcraft.content.block.storage;

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
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IElectricItem;
import com.nick.industrialcraft.api.energy.IEnergyTier;
import com.nick.industrialcraft.api.energy.IVoltageTransformer;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager.MachineConnection;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.content.item.StoredEnergyData;

import java.util.*;

/**
 * MFE BlockEntity - Medium Voltage energy storage (MV tier)
 *
 * Specifications from IC2 source (TileEntityElectricMFE: super(2, 128, 600000)):
 * - Storage: 600,000 EU
 * - Max Input: 128 EU/t (MV tier)
 * - Max Output: 128 EU/t (MV tier)
 *
 * Behavior:
 * - Accepts energy from 5 sides (all except output face)
 * - Outputs energy from 1 side (the output face)
 * - Slot 0 (top): Charge items FROM MFE storage
 * - Slot 1 (bottom): Discharge items INTO MFE storage
 */
public class MFEBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier, IWrenchable {

    // Slot indices
    public static final int CHARGE_SLOT = 0;     // Items charged FROM MFE
    public static final int DISCHARGE_SLOT = 1;  // Items discharged INTO MFE
    public static final int SLOTS = 2;

    // Energy specifications (from IC2 TileEntityElectricMFE: super(2, 128, 600000))
    private static final int MAX_ENERGY = 600000;  // 600,000 EU storage
    private static final int MAX_TRANSFER = 128;   // 128 EU/t (MV tier)

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

    public MFEBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MFE.get(), pos, state);
    }

    /**
     * Get the appropriate energy storage based on the side being accessed
     */
    public IEnergyStorage getEnergyStorageForSide(Direction side) {
        if (side == null) {
            return outputEnergyStorage;  // Default to output for null side
        }

        Direction outputFace = getBlockState().getValue(MFEBlock.FACING);
        if (side == outputFace) {
            return outputEnergyStorage;  // Output face can extract
        } else {
            return inputEnergyStorage;   // Input faces can only receive
        }
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.MV;  // MFE is MV tier (128 EU/t max I/O)
    }

    @Override
    public int getOutputPacketSize() {
        // MFE outputs up to 128 EU/t (full MV tier)
        return MAX_TRANSFER;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MFEBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        // Output energy through the output face (supports cable networks)
        if (be.energyStored > 0) {
            changed |= be.outputEnergy(level, pos, state);
        }

        // Handle charging items in CHARGE_SLOT (transfer energy FROM MFE TO item)
        var chargeStack = be.inventory.getStackInSlot(CHARGE_SLOT);
        if (!chargeStack.isEmpty() && be.energyStored > 0) {
            // Check if item is an IElectricItem and if MFE tier is high enough
            if (chargeStack.getItem() instanceof IElectricItem electricItem) {
                // MFE is MV - can charge LV and MV items
                EnergyTier itemTier = electricItem.getTier(chargeStack);
                if (itemTier.getTierLevel() <= EnergyTier.MV.getTierLevel()) {
                    int toTransfer = Math.min(be.energyStored, MAX_TRANSFER);
                    int transferred = electricItem.charge(chargeStack, toTransfer, EnergyTier.MV, false, false);
                    if (transferred > 0) {
                        be.energyStored -= transferred;
                        changed = true;
                    }
                }
            } else {
                // Non-IElectricItem - use capability (for compatibility with other mods)
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
        }

        // Handle discharging items in DISCHARGE_SLOT (transfer energy FROM item TO MFE)
        var dischargeStack = be.inventory.getStackInSlot(DISCHARGE_SLOT);
        if (!dischargeStack.isEmpty() && be.energyStored < MAX_ENERGY) {
            // Check if item is an IElectricItem that can provide energy
            if (dischargeStack.getItem() instanceof IElectricItem electricItem) {
                if (electricItem.canProvideEnergy(dischargeStack)) {
                    int spaceLeft = MAX_ENERGY - be.energyStored;
                    int toExtract = Math.min(spaceLeft, MAX_TRANSFER);
                    int extracted = electricItem.discharge(dischargeStack, toExtract, EnergyTier.MV, false, false);
                    if (extracted > 0) {
                        be.energyStored += extracted;
                        changed = true;
                    }
                }
            } else {
                // Non-IElectricItem - use capability (for compatibility with other mods)
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
        }

        if (changed) {
            be.setChanged();
        }
    }

    // ========== Energy Output Logic (Cable Network Scanning) ==========

    /**
     * Output energy to machines on the output face, scanning through cable networks.
     * Uses cached network scanning for optimal performance.
     */
    private boolean outputEnergy(Level level, BlockPos pos, BlockState state) {
        Direction outputFace = state.getValue(MFEBlock.FACING);

        // DEBUG: Log MFE output attempt
        System.out.println("[MFE DEBUG] outputEnergy called at " + pos + ", outputFace=" + outputFace + ", energyStored=" + energyStored);

        // Use cached network manager for O(1) amortized performance
        // Only scan from output face direction
        List<MachineConnection> machines = EnergyNetworkManager.getConnectedMachines(
            level, pos, outputFace
        );

        System.out.println("[MFE DEBUG] Network scan found " + machines.size() + " machines");

        // Also check for direct neighbor in case the network scan misses it
        BlockPos directNeighbor = pos.relative(outputFace);
        BlockEntity directBe = level.getBlockEntity(directNeighbor);
        System.out.println("[MFE DEBUG] Direct neighbor at " + directNeighbor + " is " + (directBe != null ? directBe.getClass().getSimpleName() : "null"));

        if (directBe != null && !(directBe instanceof com.nick.industrialcraft.content.block.cable.CableBlockEntity)) {
            Direction accessSide = outputFace.getOpposite();
            IEnergyStorage directStorage = level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                directNeighbor,
                accessSide
            );
            System.out.println("[MFE DEBUG] Direct neighbor storage: " + (directStorage != null ? "found, canReceive=" + directStorage.canReceive() : "null"));

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
                    System.out.println("[MFE DEBUG] Added direct neighbor to machine list");
                }
            }
        }

        if (machines.isEmpty() || energyStored <= 0) {
            System.out.println("[MFE DEBUG] Early return: machines.isEmpty()=" + machines.isEmpty() + ", energyStored=" + energyStored);
            return false;
        }

        // Filter to only machines that actually want energy
        List<MachineConnection> needyMachines = new ArrayList<>();
        for (MachineConnection machine : machines) {
            int wants = machine.storage().receiveEnergy(Integer.MAX_VALUE, true);
            System.out.println("[MFE DEBUG] Machine at " + machine.pos() + " (" + (machine.blockEntity() != null ? machine.blockEntity().getClass().getSimpleName() : "null") + ") wants " + wants + " EU, accessSide=" + machine.accessSide());
            if (wants > 0) {
                needyMachines.add(machine);
            }
        }

        if (needyMachines.isEmpty()) {
            System.out.println("[MFE DEBUG] No needy machines found");
            return false;
        }

        // Fair distribution: split available energy equally among all machines
        int availableEnergy = Math.min(energyStored, MAX_TRANSFER);
        int energyPerMachine = availableEnergy / needyMachines.size();
        if (energyPerMachine <= 0) energyPerMachine = 1;

        int totalTransferred = 0;

        // Get our output packet size (for tier checking)
        int packetSize = getOutputPacketSize();
        System.out.println("[MFE DEBUG] packetSize=" + packetSize + ", energyPerMachine=" + energyPerMachine);

        for (MachineConnection machine : needyMachines) {
            // Check tier compatibility before transferring
            // For transformers, use side-specific check; for regular machines, use global check
            if (machine.blockEntity() instanceof IVoltageTransformer transformer) {
                // Transformer - check the specific side we're connecting to
                boolean canReceive = transformer.canSideReceive(machine.accessSide(), packetSize);
                System.out.println("[MFE DEBUG] Transformer at " + machine.pos() + " canSideReceive(" + machine.accessSide() + ", " + packetSize + ")=" + canReceive);
                if (!canReceive) {
                    // This side of the transformer can't handle this voltage - EXPLODE!
                    System.out.println("[MFE DEBUG] EXPLODING transformer at " + machine.pos());
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

            int toTransfer = Math.min(energyPerMachine, energyStored);
            int transferred = machine.storage().receiveEnergy(toTransfer, false);
            System.out.println("[MFE DEBUG] Transferred " + transferred + " EU to " + machine.pos());
            if (transferred > 0) {
                energyStored -= transferred;
                totalTransferred += transferred;
            }
        }

        System.out.println("[MFE DEBUG] Total transferred: " + totalTransferred);
        return totalTransferred > 0;
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
        return Component.translatable("block.industrialcraft.mfe");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new MFEMenu(id, playerInv, this);
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

    // ========== IWrenchable Implementation ==========

    @Override
    public boolean canWrenchRotate(Player player, Direction newFacing) {
        return newFacing.getAxis().isHorizontal();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(MFEBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(MFEBlock.FACING, facing), 3);
        }
    }

    @Override
    public boolean canWrenchRemove(Player player) {
        return true;
    }

    @Override
    public int getStoredEnergy() {
        return energyStored;
    }

    @Override
    public void setStoredEnergy(int energy) {
        this.energyStored = Math.min(energy, MAX_ENERGY);
    }

    @Override
    public int getMaxStoredEnergy() {
        return MAX_ENERGY;
    }

    @Override
    public ItemStack createWrenchDrop() {
        ItemStack drop = new ItemStack(ModItems.MFE_ITEM.get());
        if (energyStored > 0) {
            drop.set(ModDataComponents.STORED_ENERGY.get(), StoredEnergyData.of(energyStored));
        }
        return drop;
    }
}
