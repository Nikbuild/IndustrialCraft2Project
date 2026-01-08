package com.nick.industrialcraft.content.block.transformer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IEnergyTier;
import com.nick.industrialcraft.api.energy.IVoltageTransformer;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager;
import com.nick.industrialcraft.api.energy.EnergyNetworkManager.MachineConnection;
import com.nick.industrialcraft.api.energy.OvervoltageHandler;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.registry.ModItems;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * HV Transformer BlockEntity - Steps voltage between EV (2048V) and HV (512V)
 *
 * IC2 Transformer Behavior:
 * - The "front" face (with 3 circles) is the HIGH voltage side
 * - All other 5 faces are the LOW voltage side
 *
 * Step-Down Mode (default):
 * - Receives EV (2048V) on the HIGH side (front face)
 * - Outputs HV (512V) on the LOW sides (5 other faces)
 * - One EV packet (2048 EU) becomes four HV packets (512 EU each)
 *
 * Step-Up Mode (when redstone powered):
 * - Receives HV (512V) on the LOW sides (5 other faces)
 * - Outputs EV (2048V) on the HIGH side (front face)
 * - Four HV packets (512 EU each) become one EV packet (2048 EU)
 *
 * Energy Buffer: 4096 EU for conversion (matches original IC2)
 */
public class HVTransformerBlockEntity extends BlockEntity implements IEnergyTier, IVoltageTransformer, IWrenchable {

    // Energy buffer for conversion (matches original IC2: 4096 EU)
    private static final int MAX_ENERGY = 4096;
    private static final int HV_PACKET = 512;   // HV packet size (low side)
    private static final int EV_PACKET = 2048;  // EV packet size (high side)

    private int energyStored = 0;

    // Track which side type last received energy to determine output direction
    // true = received on HIGH side (step-down mode: output HV on low sides)
    // false = received on LOW side (step-up mode: output EV on high side)
    private boolean stepDownMode = true;

    // High-voltage side energy storage (EV - receives/outputs 2048 EU/t)
    private final IEnergyStorage highVoltageStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // High side receives EV packets - this means step-down mode
            int toAccept = Math.min(maxReceive, Math.min(EV_PACKET, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                stepDownMode = true;  // Energy came in on high side, output on low sides
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // High side outputs EV packets (step-up mode)
            int toExtract = Math.min(maxExtract, Math.min(EV_PACKET, energyStored));
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

    // Low-voltage side energy storage (HV - receives/outputs 512 EU/t)
    private final IEnergyStorage lowVoltageStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Low side receives HV packets - this means step-up mode
            int toAccept = Math.min(maxReceive, Math.min(HV_PACKET, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                stepDownMode = false;  // Energy came in on low side, output on high side
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // Low side outputs HV packets (step-down mode)
            int toExtract = Math.min(maxExtract, Math.min(HV_PACKET, energyStored));
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

    public HVTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.HV_TRANSFORMER.get(), pos, state);
    }

    /**
     * Get the appropriate energy storage based on the side being accessed.
     * Front face (FACING direction) = High voltage (EV)
     * All other faces = Low voltage (HV)
     */
    public IEnergyStorage getEnergyStorageForSide(Direction side) {
        if (side == null) {
            return lowVoltageStorage;  // Default to low voltage
        }

        Direction highVoltageFace = getBlockState().getValue(HVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return highVoltageStorage;  // High voltage side (EV)
        } else {
            return lowVoltageStorage;   // Low voltage sides (HV)
        }
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        // Transformer operates at the higher tier it handles
        return EnergyTier.EV;
    }

    /**
     * Transformers can safely receive packets up to their HIGH voltage side's tier.
     * The high side can handle EV (2048), low sides can handle HV (512).
     * We return true for EV or below to prevent false explosions during placement checks.
     */
    @Override
    public boolean canSafelyReceive(int packetSize) {
        // Transformer can receive up to EV on its high side
        return packetSize <= EV_PACKET;
    }

    @Override
    public int getOutputPacketSize() {
        // Output depends on which side - but for tier checking purposes,
        // we report the HV output since that's what goes to HV machines
        return HV_PACKET;
    }

    /**
     * Get the output packet size for a specific side.
     * High voltage side outputs EV (2048), low voltage sides output HV (512).
     */
    public int getOutputPacketSizeForSide(Direction side) {
        Direction highVoltageFace = getBlockState().getValue(HVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return EV_PACKET;  // High voltage output
        } else {
            return HV_PACKET;  // Low voltage output
        }
    }

    // ========== IVoltageTransformer Implementation ==========

    @Override
    public EnergyTier getTierForSide(Direction side) {
        Direction highVoltageFace = getBlockState().getValue(HVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return EnergyTier.EV;  // High voltage side
        } else {
            return EnergyTier.HV;  // Low voltage sides
        }
    }

    @Override
    public boolean canSideReceive(Direction side, int packetSize) {
        Direction highVoltageFace = getBlockState().getValue(HVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            // High voltage side can receive up to EV
            return packetSize <= EV_PACKET;
        } else {
            // Low voltage sides can receive up to HV
            return packetSize <= HV_PACKET;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HVTransformerBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        // Output energy from both sides
        if (be.energyStored > 0) {
            changed |= be.outputEnergy(level, pos, state);
        }

        if (changed) {
            be.setChanged();
        }
    }

    /**
     * Output energy based on the current mode.
     * - Step-down mode (energy received on high side): Output HV on low sides only
     * - Step-up mode (energy received on low sides): Output EV on high side only
     *
     * IMPORTANT: We must exclude machines reachable from the input side to prevent
     * energy from looping back through the cable network.
     */
    private boolean outputEnergy(Level level, BlockPos pos, BlockState state) {
        Direction highVoltageFace = state.getValue(HVTransformerBlock.FACING);
        boolean transferred = false;

        if (stepDownMode) {
            // Step-down: Energy came in on HIGH side, output HV on LOW sides
            // Get machines on the input (high) side to exclude them from output
            Set<BlockPos> inputSideMachines = getMachinePositions(
                EnergyNetworkManager.getConnectedMachines(level, pos, highVoltageFace)
            );

            for (Direction dir : Direction.values()) {
                if (dir == highVoltageFace) continue;  // Skip high voltage side
                if (energyStored < HV_PACKET) break;   // Need at least one HV packet

                transferred |= outputToSide(level, pos, dir, HV_PACKET, inputSideMachines);
            }
        } else {
            // Step-up: Energy came in on LOW sides, output EV on HIGH side
            // Get machines on the input (low) sides to exclude them from output
            Set<BlockPos> inputSideMachines = new HashSet<>();
            for (Direction dir : Direction.values()) {
                if (dir == highVoltageFace) continue;
                inputSideMachines.addAll(getMachinePositions(
                    EnergyNetworkManager.getConnectedMachines(level, pos, dir)
                ));
            }

            if (energyStored >= EV_PACKET) {
                transferred |= outputToSide(level, pos, highVoltageFace, EV_PACKET, inputSideMachines);
            }
        }

        return transferred;
    }

    /**
     * Helper to extract BlockPos set from machine connections.
     */
    private Set<BlockPos> getMachinePositions(List<MachineConnection> machines) {
        Set<BlockPos> positions = new HashSet<>();
        for (MachineConnection machine : machines) {
            positions.add(machine.pos());
        }
        return positions;
    }

    /**
     * Output energy to a specific side with the given packet size.
     * @param excludePositions Machines at these positions will be skipped (input side machines)
     */
    private boolean outputToSide(Level level, BlockPos pos, Direction outputDir, int packetSize, Set<BlockPos> excludePositions) {
        // First, try the network scan to find machines through cables
        List<MachineConnection> machines = EnergyNetworkManager.getConnectedMachines(
            level, pos, outputDir
        );

        // Also check for direct neighbor in case the network scan misses it
        BlockPos directNeighbor = pos.relative(outputDir);
        BlockEntity directBe = level.getBlockEntity(directNeighbor);
        if (directBe != null && !(directBe instanceof com.nick.industrialcraft.content.block.cable.CableBlockEntity)) {
            Direction accessSide = outputDir.getOpposite();
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

        if (machines.isEmpty() || energyStored < packetSize) return false;

        // Filter machines that want energy AND are not on the input side
        List<MachineConnection> needyMachines = new ArrayList<>();
        for (MachineConnection machine : machines) {
            // Skip machines that are on the input side (prevents energy looping back)
            if (excludePositions.contains(machine.pos())) {
                continue;
            }
            int wants = machine.storage().receiveEnergy(Integer.MAX_VALUE, true);
            if (wants > 0) {
                needyMachines.add(machine);
            }
        }

        if (needyMachines.isEmpty()) return false;

        int totalTransferred = 0;

        for (MachineConnection machine : needyMachines) {
            if (energyStored < packetSize) break;

            // Check tier compatibility - transformer outputs appropriate voltage for each side
            // For other transformers, use side-specific check; for regular machines, use global check
            if (machine.blockEntity() instanceof IVoltageTransformer otherTransformer) {
                // Another transformer - check the specific side we're connecting to
                if (!otherTransformer.canSideReceive(machine.accessSide(), packetSize)) {
                    EnergyTier outputTier = EnergyTier.fromPacketSize(packetSize);
                    EnergyTier machineTier = otherTransformer.getTierForSide(machine.accessSide());
                    int tierGap = EnergyTier.getTierGap(outputTier, machineTier);
                    OvervoltageHandler.applyConsequence(level, machine.pos(), tierGap);
                    continue;
                }
            } else if (machine.blockEntity() instanceof IEnergyTier tieredMachine) {
                if (!tieredMachine.canSafelyReceive(packetSize)) {
                    // Machine can't handle this voltage - use graduated consequence system
                    EnergyTier outputTier = EnergyTier.fromPacketSize(packetSize);
                    EnergyTier machineTier = tieredMachine.getEnergyTier();
                    int tierGap = EnergyTier.getTierGap(outputTier, machineTier);
                    OvervoltageHandler.applyConsequence(level, machine.pos(), tierGap);
                    continue;
                }
            }

            // Transfer energy
            int toTransfer = Math.min(packetSize, energyStored);
            int received = machine.storage().receiveEnergy(toTransfer, false);
            if (received > 0) {
                energyStored -= received;
                totalTransferred += received;
            }
        }

        return totalTransferred > 0;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("Energy", energyStored);
        out.putBoolean("StepDownMode", stepDownMode);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        energyStored = in.getIntOr("Energy", 0);
        stepDownMode = in.getBooleanOr("StepDownMode", true);
    }

    // ========== IWrenchable Implementation ==========

    @Override
    public boolean canWrenchRotate(Player player, Direction newFacing) {
        return true;  // Can rotate to any direction
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(HVTransformerBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(HVTransformerBlock.FACING, facing), 3);
            // Check for overvoltage after rotation - transformer may now connect wrong side to wrong tier
            EnergyNetworkManager.invalidateAt(level, worldPosition);
            OvervoltageHandler.checkOnPlacement(level, worldPosition);
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
        return new ItemStack(ModItems.HV_TRANSFORMER_ITEM.get());
    }
}
