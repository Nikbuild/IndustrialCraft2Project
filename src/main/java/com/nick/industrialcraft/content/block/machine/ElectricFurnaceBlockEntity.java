package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IEnergyTier;

public class ElectricFurnaceBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier {

    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int UPGRADE_SLOT_1 = 3;
    public static final int UPGRADE_SLOT_2 = 4;
    public static final int UPGRADE_SLOT_3 = 5;
    public static final int UPGRADE_SLOT_4 = 6;
    public static final int SLOTS = 7;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == OUTPUT_SLOT) return false; // Output slot is result-only
            // TODO: In future, check if slot == BATTERY_SLOT, validate it's an energy storage item
            return true;
        }
    };

    // IC2 Electric Furnace specifications:
    // - Energy consumption: 4 EU/tick while smelting
    // - Operation time: 100 ticks per recipe (5 seconds)
    // - Total EU per recipe: 400 EU (10 smelts per coal)
    // - Energy storage capacity: 416 EU
    // - Max input: 10 EU/t (matches generator output)

    private float progress = 0.0f;                // Current smelting progress (0-100, fractional for smooth scaling)
    private int energy = 0;                       // Current stored energy (in EU) - kept for battery slot compatibility
    private boolean powered = false;              // Active smelting state
    private ItemStack lastInputItem = ItemStack.EMPTY; // Track what's being smelted to prevent exploits
    private boolean lastInputWasValid = false;    // Cached validity check - prevents energy waste on invalid items
    private int energyReceivedThisTick = 0;       // Energy received in the current tick (accumulator)
    private int energyReceivedLastTick = 0;       // Energy received in the last tick (for GUI display)
    private boolean powerAvailable = false;       // True when power is being offered (for GUI, even when idle)
    private int powerAvailableThisTick = 0;       // Tracks power offered this tick (via simulate calls)

    private static final int MAX_PROGRESS = 100;        // 100 ticks per operation (5 seconds)
    private static final int ENERGY_PER_TICK = 4;       // 4 EU consumed per operation tick
    private static final int MAX_ENERGY = 416;          // Energy storage: 416 EU (400 + 16 buffer)
    private static final int ENERGY_PER_OPERATION = 400; // Total EU per recipe: 400 EU (10 smelts per coal)
    private static final int MAX_INPUT = 10;            // Max input: 10 EU/t (matches generator output)

    // NeoForge Energy Capability (for compatibility with other mods)
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Always track power being offered for LED indicator (even when idle)
            // This allows the energy bar to show "power available" as an LED
            if (simulate && maxReceive > 0) {
                powerAvailableThisTick = Math.min(maxReceive, ENERGY_PER_TICK);
                return powerAvailableThisTick;  // Report we COULD accept this much
            }

            // For actual energy transfer, only accept if we have valid work to do
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            if (input.isEmpty() || !lastInputWasValid) {
                return 0;  // No input or invalid item, don't actually consume energy
            }

            // Cap at ENERGY_PER_TICK - machine only needs this much per tick to operate at full speed
            int toAccept = Math.min(maxReceive, ENERGY_PER_TICK);

            if (toAccept > 0) {
                // Track energy flowing through for progress calculation
                energyReceivedThisTick += toAccept;
                setChanged();
            }

            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0; // Electric Furnace doesn't output energy
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
            return false; // Electric Furnace only receives energy
        }

        @Override
        public boolean canReceive() {
            return energy < MAX_ENERGY; // Can receive if not full
        }
    };

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.ELECTRIC_FURNACE.get(), pos, state);
    }

    // Expose energy capability to other mods
    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.industrialcraft.electric_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new ElectricFurnaceMenu(id, playerInv, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getProgress() {
        return (int)progress;  // Return as int for GUI display
    }

    public float getProgressFloat() {
        return progress;  // Expose actual float value if needed
    }

    public int getMaxProgress() {
        return MAX_PROGRESS;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return MAX_ENERGY;
    }

    public int getEnergyReceivedLastTick() {
        return energyReceivedLastTick;
    }

    public boolean isPowerAvailable() {
        return powerAvailable;
    }

    public void setProgressClient(int progress) {
        this.progress = progress;
    }

    public void setEnergyClient(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
    }

    public void setPoweredClient(boolean powered) {
        this.powered = powered;
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.LV;  // Electric Furnace is LV tier (max 32 EU/t input)
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        // TODO: In future, charge from batteries in BATTERY_SLOT

        // Check if we can smelt
        ItemStack input = be.inventory.getStackInSlot(INPUT_SLOT);

        // Check if the input item changed - if so, reset progress and revalidate
        if (!ItemStack.matches(be.lastInputItem, input)) {
            be.progress = 0;
            be.lastInputItem = input.copy();
            be.lastInputWasValid = !input.isEmpty() && be.canSmelt(level, input);
            needsUpdate = true;
        }

        if (!input.isEmpty() && be.lastInputWasValid) {
            // Proportional progress system: progress scales with energy received THIS tick
            // No minimum energy requirement - any energy contributes proportionally
            // Formula: progressIncrease = energyReceived / ENERGY_PER_TICK
            // Example: 3.25 EU/t → 3.25/4 = 0.8125 progress/tick (81.25% speed)

            if (be.energyReceivedThisTick > 0) {
                // Calculate proportional progress based on energy flow THIS tick
                float progressIncrease = (float)be.energyReceivedThisTick / ENERGY_PER_TICK;
                be.progress += progressIncrease;
                be.powered = true;
                needsUpdate = true;

                // Complete the operation when progress reaches max
                if (be.progress >= MAX_PROGRESS) {
                    be.smeltItem(level, input);
                    be.progress = 0;
                    be.lastInputItem = ItemStack.EMPTY;
                    needsUpdate = true;
                }
            } else {
                // No energy flowing - no progress
                be.powered = false;
            }
        } else {
            // No valid input or can't smelt, reset progress
            be.progress = 0;
            be.powered = false;
        }

        // Copy accumulated energy to last tick for GUI display (do this at END of tick)
        be.energyReceivedLastTick = be.energyReceivedThisTick;
        be.energyReceivedThisTick = 0;  // Reset accumulator for next tick

        // Update power available state (shows in GUI even when idle)
        be.powerAvailable = be.powerAvailableThisTick > 0;
        be.powerAvailableThisTick = 0;  // Reset for next tick

        // Update blockstate if powered state changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(ElectricFurnaceBlock.POWERED, be.powered), 3);
            needsUpdate = true;
        }

        if (needsUpdate) {
            be.setChanged();
        }
    }

    private boolean canSmelt(Level level, ItemStack input) {
        if (input.isEmpty() || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return false;

        // Get smelting recipe
        var recipeInput = new net.minecraft.world.item.crafting.SingleRecipeInput(input);
        var optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel);

        if (optional.isEmpty()) return false;

        ItemStack result = optional.get().value().assemble(recipeInput, serverLevel.registryAccess());
        if (result.isEmpty()) return false;

        // Check if output slot can accept the result
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;

        // Check if items are same type and won't overflow
        return ItemStack.isSameItemSameComponents(output, result) &&
               output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void smeltItem(Level level, ItemStack input) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        // Get smelting recipe
        var recipeInput = new net.minecraft.world.item.crafting.SingleRecipeInput(input);
        var optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel);

        if (optional.isEmpty()) return;

        ItemStack result = optional.get().value().assemble(recipeInput, serverLevel.registryAccess()).copy();
        if (result.isEmpty()) return;

        // Decrease input
        input.shrink(1);

        // Add to output
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result);
        } else {
            output.grow(result.getCount());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putFloat("Progress", progress);  // Save as float for fractional progress
        out.putInt("Energy", energy);
        out.putBoolean("Powered", powered);
        out.putInt("EnergyReceivedThisTick", energyReceivedThisTick);
        out.putInt("EnergyReceivedLastTick", energyReceivedLastTick);
        out.storeNullable("LastInputItem", ItemStack.OPTIONAL_CODEC, lastInputItem.isEmpty() ? null : lastInputItem);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        progress = in.getFloatOr("Progress", 0.0f);  // Load as float for fractional progress
        energy = in.getIntOr("Energy", 0);
        powered = in.getBooleanOr("Powered", false);
        energyReceivedThisTick = in.getIntOr("EnergyReceivedThisTick", 0);
        energyReceivedLastTick = in.getIntOr("EnergyReceivedLastTick", 0);
        // Don't load lastInputItem - force revalidation on first tick after world load
        // This ensures lastInputWasValid gets set correctly even after mod updates
        lastInputItem = ItemStack.EMPTY;
        lastInputWasValid = false;
    }
}
