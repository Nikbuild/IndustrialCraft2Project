package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
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

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModDataComponents;
import com.nick.industrialcraft.registry.ModItems;
import com.nick.industrialcraft.api.energy.EnergyTier;
import com.nick.industrialcraft.api.energy.IEnergyTier;
import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.content.item.StoredEnergyData;

/**
 * Recycler Block Entity
 *
 * The Recycler converts crafted items into Scrap using a deterministic points system.
 * Unlike IC2's RNG-based 1/8 chance, this implementation gives predictable output
 * based on item crafting complexity.
 *
 * Points System:
 * - Each item has a point value (0-100) based on crafting complexity
 * - Points accumulate in an internal counter
 * - When counter reaches 100 points, one scrap is produced
 * - Excess points carry over to the next scrap
 * - Items with 0 points (raw materials, unregistered) are EJECTED, not consumed
 *
 * Machine Specifications (IC2 accurate):
 * - Energy consumption: 1 EU/tick while recycling
 * - Operation time: 45 ticks (2.25 seconds)
 * - Max input: 32 EU/t (LV tier)
 */
public class RecyclerBlockEntity extends BlockEntity implements MenuProvider, IEnergyTier, IWrenchable {

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
            if (slot == OUTPUT_SLOT) return false;
            return true;
        }
    };

    // Recycler specifications (IC2 accurate from TileEntityRecycler.java line 24):
    // super(3, 1, 45, 32) = tier 3, 1 EU/t consumption, 45 ticks operation, 32 EU/t max input
    private static final int MAX_PROGRESS = 45;           // 45 ticks = 2.25 seconds (IC2 accurate)
    private static final int ENERGY_PER_TICK = 1;          // 1 EU/t consumption (IC2 accurate)
    private static final int MAX_ENERGY = 77;              // 45 + 32 buffer
    private static final int ENERGY_PER_OPERATION = 45;    // 1 EU/t * 45 ticks = 45 EU
    private static final int MAX_INPUT = 32;               // LV tier max input

    // Processing state
    private float progress = 0.0f;
    private int energy = 0;
    private boolean powered = false;
    private ItemStack lastInputItem = ItemStack.EMPTY;
    private boolean lastInputWasValid = false;
    private int energyReceivedThisTick = 0;
    private int energyReceivedLastTick = 0;
    private boolean powerAvailable = false;
    private int powerAvailableThisTick = 0;

    // Points-based scrap system
    private long scrapPoints = 0;           // Accumulated points (use long to prevent overflow)
    private int pendingScrap = 0;           // Scrap waiting to be placed in output slot

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Always track power being offered for LED indicator (even when idle)
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
                energyReceivedThisTick += toAccept;
                setChanged();
            }

            return toAccept;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
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
            return false;
        }

        @Override
        public boolean canReceive() {
            return energy < MAX_ENERGY;
        }
    };

    public RecyclerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.RECYCLER.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.industrialcraft.recycler");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new RecyclerMenu(id, playerInv, this);
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

    public long getScrapPoints() {
        return scrapPoints;
    }

    public int getPendingScrap() {
        return pendingScrap;
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

    // ========== Recycling Logic ==========

    /**
     * Check if an item has scrap value (can be recycled for points).
     * Items with 0 points will be ejected instead.
     */
    private boolean hasScrapValue(ItemStack input) {
        if (input.isEmpty()) return false;
        return RecyclerRecipes.getScrapValue(input) > 0;
    }

    /**
     * Check if we can recycle the input item.
     * - Input must not be empty
     * - Input must have scrap value > 0 (otherwise it will be ejected)
     * - Output slot must have room for scrap (or be empty)
     */
    private boolean canRecycle(ItemStack input) {
        if (input.isEmpty()) return false;

        // Items with 0 scrap value cannot be recycled - they get ejected
        if (!hasScrapValue(input)) return false;

        // Check if we need output space
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        ItemStack scrapStack = new ItemStack(ModItems.SCRAP.get(), 1);

        if (output.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(output, scrapStack) &&
               output.getCount() < output.getMaxStackSize();
    }

    /**
     * Eject an item from the recycler (for items with 0 scrap value).
     * Spawns the item as an entity above the machine.
     */
    private void ejectItem(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty() || level.isClientSide) return;

        // Spawn item entity above the recycler
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.0;
        double z = pos.getZ() + 0.5;

        ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack.copy());
        // Give it a small upward velocity so it pops out
        itemEntity.setDeltaMovement(
            (level.random.nextDouble() - 0.5) * 0.1,
            0.2,
            (level.random.nextDouble() - 0.5) * 0.1
        );
        level.addFreshEntity(itemEntity);
    }

    /**
     * Try to deposit any pending scrap into the output slot.
     * Returns true if all pending scrap was deposited (or there was none).
     */
    private boolean tryDepositPendingScrap() {
        if (pendingScrap <= 0) return true;

        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) {
            int toDeposit = Math.min(pendingScrap, 64);
            inventory.setStackInSlot(OUTPUT_SLOT, new ItemStack(ModItems.SCRAP.get(), toDeposit));
            pendingScrap -= toDeposit;
            return pendingScrap <= 0;
        }

        if (ItemStack.isSameItemSameComponents(output, new ItemStack(ModItems.SCRAP.get()))) {
            int space = output.getMaxStackSize() - output.getCount();
            int toDeposit = Math.min(pendingScrap, space);
            if (toDeposit > 0) {
                output.grow(toDeposit);
                pendingScrap -= toDeposit;
            }
            return pendingScrap <= 0;
        }

        return false; // Output slot has something else
    }

    /**
     * Perform the recycling operation.
     * Consumes 1 input item and adds its point value to the counter.
     * If counter reaches threshold, produces scrap.
     */
    private void recycleItem(ItemStack input) {
        // Get point value for this item
        int pointValue = RecyclerRecipes.getScrapValue(input);

        // Consume 1 input item
        input.shrink(1);

        // Add points to counter (even if 0)
        if (pointValue > 0) {
            scrapPoints += pointValue;

            // Cap points to prevent overflow exploits
            if (scrapPoints > RecyclerRecipes.MAX_STORED_POINTS) {
                scrapPoints = RecyclerRecipes.MAX_STORED_POINTS;
            }

            // Check if we've earned scrap
            while (scrapPoints >= RecyclerRecipes.POINTS_PER_SCRAP) {
                scrapPoints -= RecyclerRecipes.POINTS_PER_SCRAP;
                pendingScrap++;

                // Safety cap on pending scrap
                if (pendingScrap > 640) {
                    pendingScrap = 640;
                    break;
                }
            }
        }

        // Try to deposit any pending scrap
        tryDepositPendingScrap();
    }

    // ========== Energy Tier Implementation ==========

    @Override
    public EnergyTier getEnergyTier() {
        return EnergyTier.LV;  // Recycler is LV tier (max 32 EU/t input)
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, RecyclerBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        // First, try to deposit any pending scrap from previous operations
        if (be.pendingScrap > 0) {
            be.tryDepositPendingScrap();
            needsUpdate = true;
        }

        ItemStack input = be.inventory.getStackInSlot(INPUT_SLOT);

        // Check if input item has 0 scrap value - if so, eject it immediately
        if (!input.isEmpty() && !be.hasScrapValue(input)) {
            // Remove from slot and eject
            ItemStack toEject = be.inventory.extractItem(INPUT_SLOT, 1, false);
            be.ejectItem(level, pos, toEject);
            be.progress = 0;
            be.lastInputItem = ItemStack.EMPTY;
            be.lastInputWasValid = false;
            needsUpdate = true;
            // Re-fetch input after ejection
            input = be.inventory.getStackInSlot(INPUT_SLOT);
        }

        // Check if the input item changed - if so, reset progress and revalidate
        if (!ItemStack.matches(be.lastInputItem, input)) {
            be.progress = 0;
            be.lastInputItem = input.copy();
            be.lastInputWasValid = !input.isEmpty() && be.canRecycle(input);
            needsUpdate = true;
        }

        // Re-check validity if we have pending scrap (output slot may have freed up)
        if (!be.lastInputWasValid && !input.isEmpty() && be.canRecycle(input)) {
            be.lastInputWasValid = true;
            needsUpdate = true;
        }

        if (!input.isEmpty() && be.lastInputWasValid) {
            // Proportional progress system
            if (be.energyReceivedThisTick > 0) {
                float progressIncrease = (float)be.energyReceivedThisTick / ENERGY_PER_TICK;
                be.progress += progressIncrease;
                be.powered = true;
                needsUpdate = true;

                // Complete the operation
                if (be.progress >= MAX_PROGRESS) {
                    be.recycleItem(input);
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

        // Copy accumulated energy to last tick for GUI display
        be.energyReceivedLastTick = be.energyReceivedThisTick;
        be.energyReceivedThisTick = 0;

        // Update power available state (shows in GUI even when idle)
        be.powerAvailable = be.powerAvailableThisTick > 0;
        be.powerAvailableThisTick = 0;

        // Update blockstate if powered state changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(RecyclerBlock.POWERED, be.powered), 3);
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
        out.putInt("Energy", energy);
        out.putBoolean("Powered", powered);
        out.putInt("EnergyReceivedThisTick", energyReceivedThisTick);
        out.putInt("EnergyReceivedLastTick", energyReceivedLastTick);
        out.putLong("ScrapPoints", scrapPoints);
        out.putInt("PendingScrap", pendingScrap);
        out.storeNullable("LastInputItem", ItemStack.OPTIONAL_CODEC, lastInputItem.isEmpty() ? null : lastInputItem);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        progress = in.getFloatOr("Progress", 0.0f);
        energy = in.getIntOr("Energy", 0);
        powered = in.getBooleanOr("Powered", false);
        energyReceivedThisTick = in.getIntOr("EnergyReceivedThisTick", 0);
        energyReceivedLastTick = in.getIntOr("EnergyReceivedLastTick", 0);

        // Load points system with validation
        scrapPoints = in.getLongOr("ScrapPoints", 0L);
        pendingScrap = in.getIntOr("PendingScrap", 0);

        // Validate loaded values (safeguard against NBT corruption)
        if (scrapPoints < 0) scrapPoints = 0;
        if (scrapPoints > RecyclerRecipes.MAX_STORED_POINTS) scrapPoints = RecyclerRecipes.MAX_STORED_POINTS;
        if (pendingScrap < 0) pendingScrap = 0;
        if (pendingScrap > 640) pendingScrap = 640;

        // Force revalidation on first tick after world load
        lastInputItem = ItemStack.EMPTY;
        lastInputWasValid = false;
    }

    // ========== IWrenchable Implementation ==========

    @Override
    public boolean canWrenchRotate(Player player, Direction newFacing) {
        return newFacing.getAxis().isHorizontal();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(RecyclerBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(RecyclerBlock.FACING, facing), 3);
        }
    }

    @Override
    public boolean canWrenchRemove(Player player) {
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
        ItemStack drop = new ItemStack(ModItems.RECYCLER_ITEM.get());
        if (energy > 0) {
            drop.set(ModDataComponents.STORED_ENERGY.get(), StoredEnergyData.of(energy));
        }
        return drop;
    }
}
