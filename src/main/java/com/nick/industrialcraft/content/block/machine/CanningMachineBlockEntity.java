package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModDataComponents;
import com.nick.industrialcraft.registry.ModItems;
import com.nick.industrialcraft.content.item.CannedFoodData;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IEnergyTier;

/**
 * Canning Machine - an appliance that consumes power directly (no internal storage).
 * Converts food + tin cans into filled tin cans.
 *
 * Specs (from IC2):
 * - Energy consumption: 1 EU/tick while operating
 * - Operation time: 50 ticks per food value point
 * - Max input: 32 EU/t (LV tier)
 * - Recipe: 1 food + 1 tin can = 1 filled tin can
 */
public class CanningMachineBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier {

    public static final int INPUT_SLOT = 0;      // Food input
    public static final int CAN_SLOT = 1;        // Empty tin cans
    public static final int BATTERY_SLOT = 2;    // Battery (unused for now)
    public static final int OUTPUT_SLOT = 3;     // Filled tin cans output
    public static final int SLOTS = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == OUTPUT_SLOT) return false;
            if (slot == CAN_SLOT) return stack.is(ModItems.TIN_CAN.get());
            if (slot == INPUT_SLOT) return getFoodValue(stack) > 0;
            return true;
        }
    };

    // Operation state
    private float progress = 0.0f;
    private boolean powered = false;
    private ItemStack lastInputItem = ItemStack.EMPTY;
    private boolean lastInputWasValid = false;
    private int currentOperationLength = 100;  // Default, recalculated based on food

    // Power tracking (appliance - no storage, just tracks what's received each tick)
    private int energyReceivedThisTick = 0;
    private int energyReceivedLastTick = 0;
    private boolean powerAvailable = false;
    private int powerAvailableThisTick = 0;

    private static final int ENERGY_PER_TICK = 1;      // 1 EU/tick consumption
    private static final int MAX_INPUT = 32;           // LV tier max input
    private static final int TICKS_PER_FOOD_POINT = 50; // IC2: 50 ticks per food value

    // IEnergyStorage for receiving power (appliance - receives and uses immediately)
    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Track power being offered for LED indicator (even when idle)
            if (simulate && maxReceive > 0) {
                powerAvailableThisTick = Math.min(maxReceive, ENERGY_PER_TICK);
                return powerAvailableThisTick;
            }

            // Only accept power if we have valid work to do
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            if (input.isEmpty() || !lastInputWasValid) {
                return 0;
            }

            // Accept up to 1 EU/tick (consumed immediately, not stored)
            int toAccept = Math.min(maxReceive, ENERGY_PER_TICK);
            if (toAccept > 0) {
                energyReceivedThisTick += toAccept;
                setChanged();
            }
            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0; // Cannot extract - appliance only
        }

        @Override
        public int getEnergyStored() {
            return 0; // No storage - appliance
        }

        @Override
        public int getMaxEnergyStored() {
            return 0; // No storage - appliance
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true; // Always ready to receive (will reject if no work)
        }
    };

    public CanningMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.CANNING_MACHINE.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.industrialcraft.canning_machine");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new CanningMachineMenu(id, playerInv, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public int getProgress() {
        return (int)progress;
    }

    public float getProgressFloat() {
        return progress;
    }

    public int getMaxProgress() {
        return currentOperationLength;
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

    public void setPoweredClient(boolean powered) {
        this.powered = powered;
    }

    public void setMaxProgressClient(int maxProgress) {
        this.currentOperationLength = maxProgress;
    }

    // ========== Food Value Calculation (IC2 accurate) ==========

    /**
     * Get the food value for canning purposes.
     * IC2 uses ceil(nutrition / 2) as the food value.
     */
    public static int getFoodValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        FoodProperties food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food != null) {
            return (int) Math.ceil(food.nutrition() / 2.0);
        }
        return 0;
    }

    // ========== Recipe Logic ==========

    private boolean canCan(ItemStack input) {
        if (input.isEmpty()) return false;

        int foodValue = getFoodValue(input);
        if (foodValue <= 0) return false;

        // Check if we have at least 1 tin can (1 food + 1 tin = 1 filled tin)
        ItemStack cans = inventory.getStackInSlot(CAN_SLOT);
        if (cans.isEmpty() || !cans.is(ModItems.TIN_CAN.get())) return false;
        if (cans.getCount() < 1) return false;

        // Check output slot
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;

        // Check if we can add 1 more filled can to output
        if (!output.is(ModItems.FILLED_TIN_CAN.get())) return false;
        if (output.getCount() + 1 > output.getMaxStackSize()) return false;

        // Check if the source food matches (only stack same source foods)
        CannedFoodData existingData = output.get(ModDataComponents.CANNED_FOOD.get());
        if (existingData == null) return true;

        ResourceLocation inputId = BuiltInRegistries.ITEM.getKey(input.getItem());
        return existingData.sourceItem().equals(inputId);
    }

    private void canItem(ItemStack input) {
        int foodValue = getFoodValue(input);
        if (foodValue <= 0) return;

        // Get food properties for saturation
        FoodProperties food = input.get(net.minecraft.core.component.DataComponents.FOOD);
        float saturation = food != null ? food.saturation() : 0.3f;

        // Get source item ID
        ResourceLocation sourceId = BuiltInRegistries.ITEM.getKey(input.getItem());

        // Consume 1 input food
        input.shrink(1);

        // Consume 1 tin can
        ItemStack cans = inventory.getStackInSlot(CAN_SLOT);
        cans.shrink(1);

        // Create canned food data (foodValue stored for when eaten)
        CannedFoodData cannedData = new CannedFoodData(sourceId, foodValue, saturation);

        // Produce 1 filled tin can with data
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            ItemStack newOutput = new ItemStack(ModItems.FILLED_TIN_CAN.get(), 1);
            newOutput.set(ModDataComponents.CANNED_FOOD.get(), cannedData);
            inventory.setStackInSlot(OUTPUT_SLOT, newOutput);
        } else {
            output.grow(1);
        }
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.LV;  // Canning Machine is LV tier (max 32 EU/t input)
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, CanningMachineBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        ItemStack input = be.inventory.getStackInSlot(INPUT_SLOT);

        // Check if the input item changed - if so, reset progress and revalidate
        if (!ItemStack.matches(be.lastInputItem, input)) {
            be.progress = 0;
            be.lastInputItem = input.copy();
            be.lastInputWasValid = !input.isEmpty() && be.canCan(input);

            // Calculate operation length based on food value
            if (be.lastInputWasValid) {
                int foodValue = getFoodValue(input);
                be.currentOperationLength = foodValue * TICKS_PER_FOOD_POINT;
            }
            needsUpdate = true;
        }

        if (!input.isEmpty() && be.lastInputWasValid) {
            // Progress based on energy received this tick
            if (be.energyReceivedThisTick > 0) {
                float progressIncrease = (float)be.energyReceivedThisTick / ENERGY_PER_TICK;
                be.progress += progressIncrease;
                be.powered = true;
                needsUpdate = true;

                // Complete the operation
                if (be.progress >= be.currentOperationLength) {
                    be.canItem(input);
                    be.progress = 0;
                    be.lastInputItem = ItemStack.EMPTY;
                    needsUpdate = true;
                }
            } else {
                be.powered = false;
            }
        } else {
            be.progress = 0;
            be.powered = false;
        }

        // Track energy for GUI display
        be.energyReceivedLastTick = be.energyReceivedThisTick;
        be.energyReceivedThisTick = 0;

        // Update power available state (shows in GUI even when idle)
        be.powerAvailable = be.powerAvailableThisTick > 0;
        be.powerAvailableThisTick = 0;

        // Update blockstate if powered state changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(CanningMachineBlock.POWERED, be.powered), 3);
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
        out.putFloat("Progress", progress);
        out.putBoolean("Powered", powered);
        out.putInt("CurrentOperationLength", currentOperationLength);
        out.storeNullable("LastInputItem", ItemStack.OPTIONAL_CODEC, lastInputItem.isEmpty() ? null : lastInputItem);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        progress = in.getFloatOr("Progress", 0.0f);
        powered = in.getBooleanOr("Powered", false);
        currentOperationLength = in.getIntOr("CurrentOperationLength", 100);
        lastInputItem = ItemStack.EMPTY;
        lastInputWasValid = false;
    }
}
