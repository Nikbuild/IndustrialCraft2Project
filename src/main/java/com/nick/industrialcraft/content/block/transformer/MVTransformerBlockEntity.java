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
 * MV Transformer BlockEntity - Steps voltage between HV (512V) and MV (128V)
 *
 * IC2 Transformer Behavior:
 * - The "front" face (with 3 circles) is the HIGH voltage side
 * - All other 5 faces are the LOW voltage side
 *
 * Step-Down Mode (default):
 * - Receives HV (512V) on the HIGH side (front face)
 * - Outputs MV (128V) on the LOW sides (5 other faces)
 * - One HV packet (512 EU) becomes four MV packets (128 EU each)
 *
 * Step-Up Mode (when redstone powered):
 * - Receives MV (128V) on the LOW sides (5 other faces)
 * - Outputs HV (512V) on the HIGH side (front face)
 * - Four MV packets (128 EU each) become one HV packet (512 EU)
 *
 * Energy Buffer: 1024 EU for conversion (matches original IC2)
 */
public class MVTransformerBlockEntity extends BlockEntity implements IEnergyTier, IVoltageTransformer, IWrenchable {

    // Energy buffer for conversion (matches original IC2: 1024 EU)
    private static final int MAX_ENERGY = 1024;
    private static final int MV_PACKET = 128;  // MV packet size (low side)
    private static final int HV_PACKET = 512;  // HV packet size (high side)

    private int energyStored = 0;

    // Track which side type last received energy to determine output direction
    // true = received on HIGH side (step-down mode: output MV on low sides)
    // false = received on LOW side (step-up mode: output HV on high side)
    private boolean stepDownMode = true;

    // High-voltage side energy storage (HV - receives/outputs 512 EU/t)
    private final IEnergyStorage highVoltageStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // High side receives HV packets - this means step-down mode
            int toAccept = Math.min(maxReceive, Math.min(HV_PACKET, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                stepDownMode = true;  // Energy came in on high side, output on low sides
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // High side outputs HV packets (step-up mode)
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

    // Low-voltage side energy storage (MV - receives/outputs 128 EU/t)
    private final IEnergyStorage lowVoltageStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Low side receives MV packets - this means step-up mode
            int toAccept = Math.min(maxReceive, Math.min(MV_PACKET, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                stepDownMode = false;  // Energy came in on low side, output on high side
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // Low side outputs MV packets (step-down mode)
            int toExtract = Math.min(maxExtract, Math.min(MV_PACKET, energyStored));
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

    public MVTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MV_TRANSFORMER.get(), pos, state);
    }

    /**
     * Get the appropriate energy storage based on the side being accessed.
     * Front face (FACING direction) = High voltage (HV)
     * All other faces = Low voltage (MV)
     */
    public IEnergyStorage getEnergyStorageForSide(Direction side) {
        if (side == null) {
            return lowVoltageStorage;  // Default to low voltage
        }

        Direction highVoltageFace = getBlockState().getValue(MVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return highVoltageStorage;  // High voltage side (HV)
        } else {
            return lowVoltageStorage;   // Low voltage sides (MV)
        }
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        // For overvoltage checking, transformer can safely receive up to its HIGH side tier
        return EnergyTier.HV;
    }

    /**
     * Transformers can safely receive packets up to their HIGH voltage side's tier.
     * The high side can handle HV (512), low sides can handle MV (128).
     * We return true for HV or below to prevent false explosions during placement checks.
     */
    @Override
    public boolean canSafelyReceive(int packetSize) {
        // Transformer can receive up to HV on its high side
        return packetSize <= HV_PACKET;
    }

    @Override
    public int getOutputPacketSize() {
        // Default output is MV from low voltage sides
        return MV_PACKET;
    }

    /**
     * Get the output packet size for a specific side.
     * High voltage side outputs HV (512), low voltage sides output MV (128).
     */
    public int getOutputPacketSizeForSide(Direction side) {
        Direction highVoltageFace = getBlockState().getValue(MVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return HV_PACKET;  // High voltage output
        } else {
            return MV_PACKET;  // Low voltage output
        }
    }

    // ========== IVoltageTransformer Implementation ==========

    @Override
    public EnergyTier getTierForSide(Direction side) {
        Direction highVoltageFace = getBlockState().getValue(MVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return EnergyTier.HV;  // High voltage side
        } else {
            return EnergyTier.MV;  // Low voltage sides
        }
    }

    @Override
    public boolean canSideReceive(Direction side, int packetSize) {
        Direction highVoltageFace = getBlockState().getValue(MVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            // High voltage side can receive up to HV
            return packetSize <= HV_PACKET;
        } else {
            // Low voltage sides can receive up to MV
            return packetSize <= MV_PACKET;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MVTransformerBlockEntity be) {
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
     * - Step-down mode (energy received on high side): Output MV on low sides only
     * - Step-up mode (energy received on low sides): Output HV on high side only
     *
     * IMPORTANT: We must exclude machines reachable from the input side to prevent
     * energy from looping back through the cable network.
     */
    private boolean outputEnergy(Level level, BlockPos pos, BlockState state) {
        Direction highVoltageFace = state.getValue(MVTransformerBlock.FACING);
        boolean transferred = false;

        if (stepDownMode) {
            // Step-down: Energy came in on HIGH side, output MV on LOW sides
            // Get machines on the input (high) side to exclude them from output
            Set<BlockPos> inputSideMachines = getMachinePositions(
                EnergyNetworkManager.getConnectedMachines(level, pos, highVoltageFace)
            );

            for (Direction dir : Direction.values()) {
                if (dir == highVoltageFace) continue;  // Skip high voltage side
                if (energyStored < MV_PACKET) break;   // Need at least one MV packet

                transferred |= outputToSide(level, pos, dir, MV_PACKET, inputSideMachines);
            }
        } else {
            // Step-up: Energy came in on LOW sides, output HV on HIGH side
            // Get machines on the input (low) sides to exclude them from output
            Set<BlockPos> inputSideMachines = new HashSet<>();
            for (Direction dir : Direction.values()) {
                if (dir == highVoltageFace) continue;
                inputSideMachines.addAll(getMachinePositions(
                    EnergyNetworkManager.getConnectedMachines(level, pos, dir)
                ));
            }

            if (energyStored >= HV_PACKET) {
                transferred |= outputToSide(level, pos, highVoltageFace, HV_PACKET, inputSideMachines);
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
        return getBlockState().getValue(MVTransformerBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(MVTransformerBlock.FACING, facing), 3);
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
        return new ItemStack(ModItems.MV_TRANSFORMER_ITEM.get());
    }
}
