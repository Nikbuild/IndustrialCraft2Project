package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModDataComponents;
import com.nick.industrialcraft.registry.ModItems;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IEnergyTier;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.content.item.StoredEnergyData;

/**
 * Reimagined Induction Furnace
 *
 * Unlike the original IC2 design with slow heat buildup, this version uses
 * realistic induction heating physics:
 *
 * - INSTANT operation (no warmup time)
 * - Speed scales with power input (more EU/t = faster smelting)
 * - Only works on ferrous/conductive materials (metals)
 * - Dual slots for parallel processing
 * - MV tier (up to 128 EU/t input)
 * - 25% more efficient than Electric Furnace (300 EU vs 400 EU per item)
 *
 * Energy efficiency comparison:
 * - Electric Furnace: 4 EU/t, 100 ticks, 400 EU per item
 * - Induction Furnace (1 item): 3 EU/t base, 100 ticks, 300 EU per item
 * - Induction Furnace (2 items): 6 EU/t base, 100 ticks, 300 EU per item each
 */
public class InductionFurnaceBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier, IWrenchable {

    // Tag for items that can be induction smelted (ferrous/conductive materials)
    public static final TagKey<Item> INDUCTION_SMELTABLE = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "induction_smeltable")
    );

    // Slot indices
    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int BATTERY_SLOT = 2;
    public static final int OUTPUT_SLOT_1 = 3;
    public static final int OUTPUT_SLOT_2 = 4;
    public static final int SLOTS = 5;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == OUTPUT_SLOT_1 || slot == OUTPUT_SLOT_2) return false;
            // Input slots only accept induction-smeltable items
            if (slot == INPUT_SLOT_1 || slot == INPUT_SLOT_2) {
                return stack.is(INDUCTION_SMELTABLE);
            }
            return true;
        }
    };

    // Reimagined Induction Furnace specifications:
    // - 25% more efficient than Electric Furnace
    // - Electric Furnace: 400 EU per item (4 EU/t * 100 ticks)
    // - Induction Furnace: 300 EU per item (3 EU/t * 100 ticks for 1 item)
    // - When 2 items: 6 EU/t for both (still 300 EU each)
    // - Speed scales with power input beyond base requirement

    private int progress = 0;                     // Current smelting progress (0-300)
    private boolean powered = false;              // Active smelting state
    private int energyReceivedThisTick = 0;       // Energy received in the current tick
    private int energyReceivedLastTick = 0;       // Energy received in the last tick (for GUI display)
    private boolean powerAvailable = false;       // True when power is being offered
    private int powerAvailableThisTick = 0;       // Tracks power offered this tick

    // Track input items for validity caching
    private ItemStack lastInput1 = ItemStack.EMPTY;
    private ItemStack lastInput2 = ItemStack.EMPTY;
    private boolean lastInput1WasValid = false;
    private boolean lastInput2WasValid = false;

    // Energy constants - 25% more efficient than Electric Furnace
    // Electric Furnace: 400 EU per item (4 EU/t * 100 ticks)
    // Induction Furnace: 300 EU per item (3 EU/t * 100 ticks)
    private static final int MAX_PROGRESS = 300;          // 300 EU worth of progress per item (25% less than 400)
    private static final int BASE_EU_PER_ITEM = 3;        // 3 EU/t for 1 item (vs Electric Furnace's 4 EU/t)
    private static final int MAX_ENERGY = 256;            // Small buffer (2 ticks at max input)
    private static final int MAX_INPUT = 128;             // Max input per packet (MV tier)

    // NeoForge Energy Capability
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Always track power being offered for LED indicator (even when idle)
            // Induction Furnace can accept up to MAX_INPUT (128 EU/t MV tier)
            if (simulate && maxReceive > 0) {
                powerAvailableThisTick = Math.min(maxReceive, MAX_INPUT);
                return powerAvailableThisTick;  // Report we COULD accept this much
            }

            // For actual energy transfer, only accept if we have valid work to do
            boolean hasValidInputs = lastInput1WasValid || lastInput2WasValid;
            if (!hasValidInputs) {
                return 0;  // Nothing to smelt, don't actually consume energy
            }

            // Cap input at MV tier
            int toAccept = Math.min(maxReceive, MAX_INPUT);

            if (toAccept > 0) {
                energyReceivedThisTick += toAccept;
                setChanged();
            }

            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0; // Induction Furnace doesn't output energy
        }

        @Override
        public int getEnergyStored() {
            // We use flow-based system, no storage
            return 0;
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

    public InductionFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.INDUCTION_FURNACE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.industrialcraft.induction_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new InductionFurnaceMenu(id, playerInv, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return MAX_PROGRESS;
    }

    public int getEnergyReceivedLastTick() {
        return energyReceivedLastTick;
    }

    public int getMaxInput() {
        return MAX_INPUT;
    }

    public boolean isPowerAvailable() {
        return powerAvailable;
    }

    /**
     * Get the number of active items being smelted (1 or 2)
     */
    public int getActiveItemCount() {
        int count = 0;
        if (lastInput1WasValid) count++;
        if (lastInput2WasValid) count++;
        return count;
    }

    /**
     * Get the base EU/t requirement based on number of items
     * 1 item = 3 EU/t, 2 items = 6 EU/t
     */
    public int getBaseEnergyRequirement() {
        return BASE_EU_PER_ITEM * Math.max(1, getActiveItemCount());
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.MV;  // Induction Furnace is MV tier (max 128 EU/t input)
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, InductionFurnaceBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        // Check if input items changed and validate them
        ItemStack input1 = be.inventory.getStackInSlot(INPUT_SLOT_1);
        ItemStack input2 = be.inventory.getStackInSlot(INPUT_SLOT_2);

        if (!ItemStack.matches(be.lastInput1, input1)) {
            be.lastInput1 = input1.copy();
            be.lastInput1WasValid = be.isValidInput(input1) && be.canSmelt(level, input1, OUTPUT_SLOT_1);
            if (!be.lastInput1WasValid) be.progress = 0; // Reset if input changed to invalid
            needsUpdate = true;
        }
        if (!ItemStack.matches(be.lastInput2, input2)) {
            be.lastInput2 = input2.copy();
            be.lastInput2WasValid = be.isValidInput(input2) && be.canSmelt(level, input2, OUTPUT_SLOT_2);
            needsUpdate = true;
        }

        boolean canOperate = be.lastInput1WasValid || be.lastInput2WasValid;
        int activeItems = be.getActiveItemCount();

        // Process energy received this tick
        if (canOperate && be.energyReceivedThisTick > 0) {
            // Calculate progress based on energy received
            // Base requirement: 3 EU/t per item (so 3 EU/t for 1 item, 6 EU/t for 2 items)
            // At base power: 100 ticks to complete (same as Electric Furnace)
            // At higher power: faster completion
            //
            // Progress formula: progress += energyReceived / activeItems
            // This means energy is split between items, maintaining efficiency
            // 1 item at 3 EU/t: 3 progress/tick, 300/3 = 100 ticks
            // 2 items at 6 EU/t: 6/2 = 3 progress/tick each, 300/3 = 100 ticks
            // 1 item at 128 EU/t: 128 progress/tick, 300/128 = ~2.3 ticks (super fast!)
            // 2 items at 128 EU/t: 128/2 = 64 progress/tick each, 300/64 = ~4.7 ticks

            int progressGain = be.energyReceivedThisTick / activeItems;
            be.progress += progressGain;
            be.powered = true;
            needsUpdate = true;

            // Check if operation is complete
            if (be.progress >= MAX_PROGRESS) {
                // Smelt both slots if possible
                if (be.lastInput1WasValid && be.canSmelt(level, input1, OUTPUT_SLOT_1)) {
                    be.smeltItem(level, INPUT_SLOT_1, OUTPUT_SLOT_1);
                }
                if (be.lastInput2WasValid && be.canSmelt(level, input2, OUTPUT_SLOT_2)) {
                    be.smeltItem(level, INPUT_SLOT_2, OUTPUT_SLOT_2);
                }
                be.progress = 0;
                be.lastInput1 = ItemStack.EMPTY;
                be.lastInput2 = ItemStack.EMPTY;
                be.lastInput1WasValid = false;
                be.lastInput2WasValid = false;
                needsUpdate = true;
            }
        } else {
            be.powered = false;
            // No energy = no progress, but don't reset progress (pause, don't lose work)
        }

        // If no valid inputs, reset progress
        if (!canOperate) {
            be.progress = 0;
            be.powered = false;
        }

        // Update energy tracking for GUI
        be.energyReceivedLastTick = be.energyReceivedThisTick;
        be.energyReceivedThisTick = 0;
        be.powerAvailable = be.powerAvailableThisTick > 0;
        be.powerAvailableThisTick = 0;

        // Update blockstate if powered state changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(InductionFurnaceBlock.POWERED, be.powered), 3);
            needsUpdate = true;
        }

        if (needsUpdate) {
            be.setChanged();
        }
    }

    /**
     * Check if an item is valid for induction smelting (must be ferrous/conductive)
     */
    private boolean isValidInput(ItemStack input) {
        if (input.isEmpty()) return false;
        return input.is(INDUCTION_SMELTABLE);
    }

    private boolean canSmelt(Level level, ItemStack input, int outputSlot) {
        if (input.isEmpty() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return false;

        // Must be induction-smeltable
        if (!input.is(INDUCTION_SMELTABLE)) return false;

        var recipeInput = new net.minecraft.world.item.crafting.SingleRecipeInput(input);
        var optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel);

        if (optional.isEmpty()) return false;

        ItemStack result = optional.get().value().assemble(recipeInput, serverLevel.registryAccess());
        if (result.isEmpty()) return false;

        ItemStack output = inventory.getStackInSlot(outputSlot);
        if (output.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(output, result) &&
               output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void smeltItem(Level level, int inputSlot, int outputSlot) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        ItemStack input = inventory.getStackInSlot(inputSlot);
        if (input.isEmpty()) return;

        var recipeInput = new net.minecraft.world.item.crafting.SingleRecipeInput(input);
        var optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel);

        if (optional.isEmpty()) return;

        ItemStack result = optional.get().value().assemble(recipeInput, serverLevel.registryAccess()).copy();
        if (result.isEmpty()) return;

        // Decrease input
        input.shrink(1);

        // Add to output
        ItemStack output = inventory.getStackInSlot(outputSlot);
        if (output.isEmpty()) {
            inventory.setStackInSlot(outputSlot, result);
        } else {
            output.grow(result.getCount());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putInt("Progress", progress);
        out.putBoolean("Powered", powered);
        out.putInt("EnergyReceivedLastTick", energyReceivedLastTick);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        progress = in.getIntOr("Progress", 0);
        powered = in.getBooleanOr("Powered", false);
        energyReceivedLastTick = in.getIntOr("EnergyReceivedLastTick", 0);
        // Force revalidation on load
        lastInput1 = ItemStack.EMPTY;
        lastInput2 = ItemStack.EMPTY;
        lastInput1WasValid = false;
        lastInput2WasValid = false;
    }

    // ========== IWrenchable Implementation ==========

    @Override
    public boolean canWrenchRotate(Player player, Direction newFacing) {
        return newFacing.getAxis().isHorizontal();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(InductionFurnaceBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(InductionFurnaceBlock.FACING, facing), 3);
        }
    }

    @Override
    public boolean canWrenchRemove(Player player) {
        return true;
    }

    @Override
    public int getStoredEnergy() {
        return 0;  // Flow-based system, no internal storage
    }

    @Override
    public void setStoredEnergy(int energy) {
        // Flow-based system, no internal storage
    }

    @Override
    public int getMaxStoredEnergy() {
        return 0;  // Flow-based system, no internal storage
    }

    @Override
    public ItemStack createWrenchDrop() {
        ItemStack drop = new ItemStack(ModItems.INDUCTION_FURNACE_ITEM.get());
        // No energy storage to preserve
        return drop;
    }
}
