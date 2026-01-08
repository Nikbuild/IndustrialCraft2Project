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
import com.nick.industrialcraft.Config;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * LV Transformer BlockEntity - Steps voltage between MV (128V) and LV (32V)
 *
 * IC2 Transformer Behavior:
 * - The "front" face (with 3 circles) is the HIGH voltage side
 * - All other 5 faces are the LOW voltage side
 *
 * Step-Down Mode (default):
 * - Receives MV (128V) on the HIGH side (front face)
 * - Outputs LV (32V) on the LOW sides (5 other faces)
 * - One MV packet (128 EU) becomes four LV packets (32 EU each)
 *
 * Step-Up Mode (when redstone powered):
 * - Receives LV (32V) on the LOW sides (5 other faces)
 * - Outputs MV (128V) on the HIGH side (front face)
 * - Four LV packets (32 EU each) become one MV packet (128 EU)
 *
 * Energy Buffer: Small internal buffer (512 EU) for conversion
 */
public class LVTransformerBlockEntity extends BlockEntity implements IEnergyTier, IVoltageTransformer, IWrenchable {

    // Energy buffer for conversion
    private static final int MAX_ENERGY = 512;
    private static final int LV_PACKET = 32;   // LV packet size
    private static final int MV_PACKET = 128;  // MV packet size

    private int energyStored = 0;

    // Track which side type last received energy to determine output direction
    // true = received on HIGH side (step-down mode: output LV on low sides)
    // false = received on LOW side (step-up mode: output MV on high side)
    private boolean stepDownMode = true;

    // High-voltage side energy storage (MV - receives/outputs 128 EU/t)
    private final IEnergyStorage highVoltageStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // High side receives MV packets - this means step-down mode
            int toAccept = Math.min(maxReceive, Math.min(MV_PACKET, MAX_ENERGY - energyStored));
            System.out.println("[LV XFMR DEBUG] highVoltageStorage.receiveEnergy(" + maxReceive + ", " + simulate + ") -> toAccept=" + toAccept + ", energyStored=" + energyStored);
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                stepDownMode = true;  // Energy came in on high side, output on low sides
                System.out.println("[LV XFMR DEBUG] Received " + toAccept + " EU on HIGH side, new energyStored=" + energyStored + ", stepDownMode=true");
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // High side outputs MV packets (step-up mode)
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

    // Low-voltage side energy storage (LV - receives/outputs 32 EU/t)
    private final IEnergyStorage lowVoltageStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Low side receives LV packets - this means step-up mode
            int toAccept = Math.min(maxReceive, Math.min(LV_PACKET, MAX_ENERGY - energyStored));
            if (!simulate && toAccept > 0) {
                energyStored += toAccept;
                stepDownMode = false;  // Energy came in on low side, output on high side
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // Low side outputs LV packets (step-down mode)
            int toExtract = Math.min(maxExtract, Math.min(LV_PACKET, energyStored));
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

    public LVTransformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.LV_TRANSFORMER.get(), pos, state);
    }

    /**
     * Get the appropriate energy storage based on the side being accessed.
     * Front face (FACING direction) = High voltage (MV)
     * All other faces = Low voltage (LV)
     */
    public IEnergyStorage getEnergyStorageForSide(Direction side) {
        if (side == null) {
            return lowVoltageStorage;  // Default to low voltage
        }

        Direction highVoltageFace = getBlockState().getValue(LVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return highVoltageStorage;  // High voltage side (MV)
        } else {
            return lowVoltageStorage;   // Low voltage sides (LV)
        }
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        // For overvoltage checking, transformer can safely receive up to its HIGH side tier
        // This prevents false explosions when the placement check runs
        return EnergyTier.MV;
    }

    /**
     * Transformers can safely receive packets up to their HIGH voltage side's tier.
     * The high side can handle MV (128), low sides can handle LV (32).
     * We return true for MV or below to prevent false explosions during placement checks.
     */
    @Override
    public boolean canSafelyReceive(int packetSize) {
        // Transformer can receive up to MV on its high side
        return packetSize <= MV_PACKET;
    }

    @Override
    public int getOutputPacketSize() {
        // Default output is LV from low voltage sides
        return LV_PACKET;
    }

    /**
     * Get the output packet size for a specific side.
     * High voltage side outputs MV (128), low voltage sides output LV (32).
     */
    public int getOutputPacketSizeForSide(Direction side) {
        Direction highVoltageFace = getBlockState().getValue(LVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return MV_PACKET;  // High voltage output
        } else {
            return LV_PACKET;  // Low voltage output
        }
    }

    // ========== IVoltageTransformer Implementation ==========

    @Override
    public EnergyTier getTierForSide(Direction side) {
        Direction highVoltageFace = getBlockState().getValue(LVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            return EnergyTier.MV;  // High voltage side
        } else {
            return EnergyTier.LV;  // Low voltage sides
        }
    }

    @Override
    public boolean canSideReceive(Direction side, int packetSize) {
        Direction highVoltageFace = getBlockState().getValue(LVTransformerBlock.FACING);
        if (side == highVoltageFace) {
            // High voltage side can receive up to MV
            return packetSize <= MV_PACKET;
        } else {
            // Low voltage sides can receive up to LV
            return packetSize <= LV_PACKET;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LVTransformerBlockEntity be) {
        if (level.isClientSide()) return;

        boolean changed = false;

        // Output energy from both sides
        if (be.energyStored > 0) {
            System.out.println("[LV XFMR DEBUG] serverTick at " + pos + ", energyStored=" + be.energyStored + ", stepDownMode=" + be.stepDownMode);
            changed |= be.outputEnergy(level, pos, state);
        }

        if (changed) {
            be.setChanged();
        }
    }

    /**
     * Output energy based on the current mode.
     * - Step-down mode (energy received on high side): Output LV on low sides only
     * - Step-up mode (energy received on low sides): Output MV on high side only
     *
     * IMPORTANT: We must exclude machines reachable from the input side to prevent
     * energy from looping back through the cable network.
     */
    private boolean outputEnergy(Level level, BlockPos pos, BlockState state) {
        Direction highVoltageFace = state.getValue(LVTransformerBlock.FACING);
        boolean transferred = false;

        System.out.println("[LV XFMR DEBUG] outputEnergy: stepDownMode=" + stepDownMode + ", highVoltageFace=" + highVoltageFace + ", energyStored=" + energyStored);

        if (stepDownMode) {
            // Step-down: Energy came in on HIGH side, output LV on LOW sides
            // Get machines on the input (high) side to exclude them from output
            Set<BlockPos> inputSideMachines = getMachinePositions(
                EnergyNetworkManager.getConnectedMachines(level, pos, highVoltageFace)
            );

            System.out.println("[LV XFMR DEBUG] Step-down mode: inputSideMachines (to exclude) = " + inputSideMachines);

            for (Direction dir : Direction.values()) {
                if (dir == highVoltageFace) {
                    System.out.println("[LV XFMR DEBUG]   Skipping dir=" + dir + " (high voltage face)");
                    continue;  // Skip high voltage side
                }
                if (energyStored < LV_PACKET) {
                    System.out.println("[LV XFMR DEBUG]   Breaking: energyStored=" + energyStored + " < LV_PACKET=" + LV_PACKET);
                    break;   // Need at least one LV packet
                }

                System.out.println("[LV XFMR DEBUG]   Trying to output LV (" + LV_PACKET + " EU) on direction " + dir);
                boolean result = outputToSide(level, pos, dir, LV_PACKET, inputSideMachines);
                System.out.println("[LV XFMR DEBUG]   outputToSide result: " + result);
                transferred |= result;
            }
        } else {
            // Step-up: Energy came in on LOW sides, output MV on HIGH side
            // Get machines on the input (low) sides to exclude them from output
            Set<BlockPos> inputSideMachines = new HashSet<>();
            for (Direction dir : Direction.values()) {
                if (dir == highVoltageFace) continue;
                inputSideMachines.addAll(getMachinePositions(
                    EnergyNetworkManager.getConnectedMachines(level, pos, dir)
                ));
            }

            System.out.println("[LV XFMR DEBUG] Step-up mode: inputSideMachines (to exclude) = " + inputSideMachines);

            if (energyStored >= MV_PACKET) {
                System.out.println("[LV XFMR DEBUG]   Trying to output MV (" + MV_PACKET + " EU) on direction " + highVoltageFace);
                transferred |= outputToSide(level, pos, highVoltageFace, MV_PACKET, inputSideMachines);
            }
        }

        System.out.println("[LV XFMR DEBUG] outputEnergy returning: transferred=" + transferred);
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
        System.out.println("[LV XFMR DEBUG] outputToSide: dir=" + outputDir + ", packetSize=" + packetSize);

        // First, try the network scan to find machines through cables
        List<MachineConnection> machines = EnergyNetworkManager.getConnectedMachines(
            level, pos, outputDir
        );

        System.out.println("[LV XFMR DEBUG]   Network scan found " + machines.size() + " machines");

        // Also check for direct neighbor in case the network scan misses it
        // This handles edge cases where the cable blockstate might not have updated yet
        BlockPos directNeighbor = pos.relative(outputDir);
        BlockEntity directBe = level.getBlockEntity(directNeighbor);
        System.out.println("[LV XFMR DEBUG]   Direct neighbor at " + directNeighbor + " is " + (directBe != null ? directBe.getClass().getSimpleName() : "null"));

        if (directBe != null && !(directBe instanceof com.nick.industrialcraft.content.block.cable.CableBlockEntity)) {
            // Direct neighbor is not a cable, check if it can receive energy
            Direction accessSide = outputDir.getOpposite();
            IEnergyStorage directStorage = level.getCapability(
                net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                directNeighbor,
                accessSide
            );
            System.out.println("[LV XFMR DEBUG]   Direct neighbor storage: " + (directStorage != null ? "found, canReceive=" + directStorage.canReceive() : "null"));

            if (directStorage != null && directStorage.canReceive()) {
                // Check if this machine is already in the list
                boolean alreadyInList = false;
                for (MachineConnection mc : machines) {
                    if (mc.pos().equals(directNeighbor)) {
                        alreadyInList = true;
                        break;
                    }
                }
                if (!alreadyInList) {
                    machines = new ArrayList<>(machines); // Make mutable copy
                    machines.add(new MachineConnection(directNeighbor, directStorage, directBe, accessSide));
                    System.out.println("[LV XFMR DEBUG]   Added direct neighbor to machine list (wasn't found by BFS)");
                }
            }
        }

        System.out.println("[LV XFMR DEBUG]   Total machines after direct check: " + machines.size());

        if (machines.isEmpty() || energyStored < packetSize) {
            System.out.println("[LV XFMR DEBUG]   Early return: machines.isEmpty()=" + machines.isEmpty() + ", energyStored=" + energyStored);
            return false;
        }

        // Filter machines that want energy AND are not on the input side
        List<MachineConnection> needyMachines = new ArrayList<>();
        for (MachineConnection machine : machines) {
            // Skip machines that are on the input side (prevents energy looping back)
            if (excludePositions.contains(machine.pos())) {
                System.out.println("[LV XFMR DEBUG]   EXCLUDED machine at " + machine.pos() + " (in input side exclusion list)");
                continue;
            }
            int wants = machine.storage().receiveEnergy(Integer.MAX_VALUE, true);
            System.out.println("[LV XFMR DEBUG]   Machine at " + machine.pos() + " (" + (machine.blockEntity() != null ? machine.blockEntity().getClass().getSimpleName() : "null") + ") wants " + wants + " EU");
            if (wants > 0) {
                needyMachines.add(machine);
            }
        }

        System.out.println("[LV XFMR DEBUG]   After filtering: " + needyMachines.size() + " needy machines");

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
                    Config.debugLog("      OVERVOLTAGE! Transformer at {} (side {} tier={}) can't handle {} EU packet. TierGap={}",
                        machine.pos(), machine.accessSide(), machineTier, packetSize, tierGap);
                    OvervoltageHandler.applyConsequence(level, machine.pos(), tierGap);
                    continue;
                }
            } else if (machine.blockEntity() instanceof IEnergyTier tieredMachine) {
                if (!tieredMachine.canSafelyReceive(packetSize)) {
                    // Machine can't handle this voltage - use graduated consequence system
                    EnergyTier outputTier = EnergyTier.fromPacketSize(packetSize);
                    EnergyTier machineTier = tieredMachine.getEnergyTier();
                    int tierGap = EnergyTier.getTierGap(outputTier, machineTier);
                    Config.debugLog("      OVERVOLTAGE! Machine at {} (tier={}) can't handle {} EU packet. TierGap={}",
                        machine.pos(), machineTier, packetSize, tierGap);
                    OvervoltageHandler.applyConsequence(level, machine.pos(), tierGap);
                    continue;
                }
            }

            // Transfer energy
            int toTransfer = Math.min(packetSize, energyStored);
            System.out.println("[LV XFMR DEBUG]   Attempting to transfer " + toTransfer + " EU to " + machine.pos());
            int received = machine.storage().receiveEnergy(toTransfer, false);
            System.out.println("[LV XFMR DEBUG]   Actually transferred: " + received + " EU");
            if (received > 0) {
                energyStored -= received;
                totalTransferred += received;
            }
        }

        System.out.println("[LV XFMR DEBUG]   Total transferred in outputToSide: " + totalTransferred);
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
        return getBlockState().getValue(LVTransformerBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(LVTransformerBlock.FACING, facing), 3);
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
        return new ItemStack(ModItems.LV_TRANSFORMER_ITEM.get());
    }
}
