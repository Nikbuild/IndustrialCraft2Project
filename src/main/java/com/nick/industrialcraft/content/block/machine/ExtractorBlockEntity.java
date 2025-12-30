package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;

import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModItems;

import java.util.HashMap;
import java.util.Map;

public class ExtractorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int UPGRADE_SLOT_1 = 3;
    public static final int UPGRADE_SLOT_2 = 4;
    public static final int UPGRADE_SLOT_3 = 5;
    public static final int UPGRADE_SLOT_4 = 6;
    public static final int SLOTS = 7;

    // Extractor recipes: input item -> output ItemStack
    private static final Map<Item, ExtractorRecipe> RECIPES = new HashMap<>();

    static {
        initRecipes();
    }

    private static void initRecipes() {
        // ========== RUBBER EXTRACTION (core IC2 mechanic) ==========
        // Sticky resin -> Rubber (3x)
        addRecipe(ModItems.STICKY_RESIN.get(), ModItems.RUBBER.get(), 3);

        // Rubber wood -> Rubber (1x)
        addRecipe(Items.OAK_LOG, ModItems.RUBBER.get(), 1);  // Placeholder until rubber wood is implemented

        // ========== OTHER EXTRACTIONS ==========
        // Slime ball -> Rubber (can substitute for sticky resin)
        addRecipe(Items.SLIME_BALL, ModItems.RUBBER.get(), 2);

        // Melon -> Melon seeds (better than breaking)
        addRecipe(Items.MELON_SLICE, Items.MELON_SEEDS, 4);

        // Pumpkin -> Pumpkin seeds (double normal yield)
        addRecipe(Items.PUMPKIN, Items.PUMPKIN_SEEDS, 8);

        // Beetroot -> Beetroot seeds (guaranteed good yield)
        addRecipe(Items.BEETROOT, Items.BEETROOT_SEEDS, 5);

        // Wheat -> Wheat seeds (guaranteed seeds from wheat)
        addRecipe(Items.WHEAT, Items.WHEAT_SEEDS, 3);

        // Dandelion -> Yellow dye
        addRecipe(Items.DANDELION, Items.YELLOW_DYE, 3);

        // Rose (poppy) -> Red dye
        addRecipe(Items.POPPY, Items.RED_DYE, 3);

        // Cactus -> Green dye (more efficient than smelting)
        addRecipe(Items.CACTUS, Items.GREEN_DYE, 2);

        // Lapis block -> Lapis lazuli
        addRecipe(Items.LAPIS_BLOCK, Items.LAPIS_LAZULI, 9);

        // Sea pickle -> Lime dye
        addRecipe(Items.SEA_PICKLE, Items.LIME_DYE, 2);

        // ========== IC2 CELL PROCESSING ==========
        // Bio Cell -> Biofuel Cell (IC2 biofuel production)
        addRecipe(ModItems.BIO_CELL.get(), ModItems.BIOFUEL_CELL.get(), 1);

        // Hydrated Coal Cell -> Coalfuel Cell (IC2 coalfuel production)
        addRecipe(ModItems.HYDRATED_COAL_CELL.get(), ModItems.COALFUEL_CELL.get(), 1);

        // Water Cell -> Cooling Cell (IC2 reactor cooling)
        addRecipe(ModItems.WATER_CELL.get(), ModItems.COOLING_CELL.get(), 1);

        // Cooling Cell -> Hydrating Cell (IC2 hydration process)
        addRecipe(ModItems.COOLING_CELL.get(), ModItems.HYDRATING_CELL.get(), 1);
    }

    private static void addRecipe(Item input, Item output, int count) {
        RECIPES.put(input, new ExtractorRecipe(output, count));
    }

    private record ExtractorRecipe(Item output, int count) {
        ItemStack getResult() {
            return new ItemStack(output, count);
        }
    }

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

    // Extractor specifications (same as Macerator for balance):
    // - Energy consumption: 2 EU/tick while extracting
    // - Operation time: 400 ticks per recipe (20 seconds)
    // - Total EU per recipe: 800 EU
    // - Energy storage capacity: 832 EU
    // - Max input: 32 EU/t (LV tier)

    private float progress = 0.0f;
    private int energy = 0;
    private boolean powered = false;
    private ItemStack lastInputItem = ItemStack.EMPTY;
    private boolean lastInputWasValid = false;
    private int energyReceivedThisTick = 0;
    private int energyReceivedLastTick = 0;
    private boolean powerAvailable = false;
    private int powerAvailableThisTick = 0;

    private static final int MAX_PROGRESS = 400;          // 400 ticks = 20 seconds (IC2 accurate)
    private static final int ENERGY_PER_TICK = 2;          // 2 EU/t consumption (IC2 accurate)
    private static final int MAX_ENERGY = 832;             // 800 + 32 buffer
    private static final int ENERGY_PER_OPERATION = 800;   // 2 EU/t * 400 ticks = 800 EU (5 operations per coal)
    private static final int MAX_INPUT = 32;               // LV tier max input

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            // Track that power is being offered (for GUI display even when idle)
            if (simulate && maxReceive > 0) {
                powerAvailableThisTick = maxReceive;
            }

            // Use cached validity check - matches Macerator pattern
            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            if (input.isEmpty() || !lastInputWasValid) {
                return 0;  // No input or invalid item, don't request energy
            }

            // Cap at ENERGY_PER_TICK - machine only needs this much per tick to operate at full speed
            int toAccept = Math.min(maxReceive, ENERGY_PER_TICK);

            if (!simulate && toAccept > 0) {
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

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.EXTRACTOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Extractor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new ExtractorMenu(id, playerInv, this);
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

    public void setProgressClient(int progress) {
        this.progress = progress;
    }

    public void setEnergyClient(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
    }

    public void setPoweredClient(boolean powered) {
        this.powered = powered;
    }

    // ========== Recipe Logic ==========

    public static ExtractorRecipe getRecipe(ItemStack input) {
        if (input.isEmpty()) return null;
        return RECIPES.get(input.getItem());
    }

    private boolean canExtract(ItemStack input) {
        if (input.isEmpty()) return false;

        ExtractorRecipe recipe = getRecipe(input);
        if (recipe == null) return false;

        ItemStack result = recipe.getResult();
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(output, result) &&
               output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void extractItem(ItemStack input) {
        ExtractorRecipe recipe = getRecipe(input);
        if (recipe == null) return;

        ItemStack result = recipe.getResult();

        // Decrease input
        input.shrink(1);

        // Add to output
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExtractorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        ItemStack input = be.inventory.getStackInSlot(INPUT_SLOT);

        // Check if the input item changed - if so, reset progress and revalidate
        if (!ItemStack.matches(be.lastInputItem, input)) {
            be.progress = 0;
            be.lastInputItem = input.copy();
            be.lastInputWasValid = !input.isEmpty() && be.canExtract(input);
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
                    be.extractItem(input);
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
            level.setBlock(pos, state.setValue(ExtractorBlock.POWERED, be.powered), 3);
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
        // Don't load lastInputItem - force revalidation on first tick after world load
        lastInputItem = ItemStack.EMPTY;
        lastInputWasValid = false;
    }
}
