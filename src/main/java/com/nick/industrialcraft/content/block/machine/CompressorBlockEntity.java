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
import net.minecraft.world.level.block.Block;
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

public class CompressorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int UPGRADE_SLOT_1 = 3;
    public static final int UPGRADE_SLOT_2 = 4;
    public static final int UPGRADE_SLOT_3 = 5;
    public static final int UPGRADE_SLOT_4 = 6;
    public static final int SLOTS = 7;

    // Compressor recipes: input item -> output ItemStack
    private static final Map<Item, CompressorRecipe> RECIPES = new HashMap<>();

    static {
        // Initialize compressor recipes
        initRecipes();
    }

    private static void initRecipes() {
        // ========== IC2 Classic Compressor Recipes ==========

        // Coal compression chain (IC2 classic)
        addRecipe(Items.COAL, ModItems.COAL_BALL.get(), 1, 8);           // 8 coal -> coal ball
        addRecipe(ModItems.COAL_BALL.get(), ModItems.COMPRESSED_COAL_BALL.get(), 1); // coal ball -> compressed coal ball
        addRecipe(ModItems.CARBON_MESH.get(), ModItems.CARBON_PLATE.get(), 1);   // carbon mesh -> carbon plate

        // Diamond creation (IC2 classic: coal chunk -> diamond)
        addRecipe(ModItems.COAL_CHUNK.get(), Items.DIAMOND, 1);          // coal chunk -> diamond

        // Plant matter compression
        addRecipe(ModItems.PLANT_BALL.get(), ModItems.COMPRESSED_PLANT_BALL.get(), 1);

        // Hydrated coal processing (IC2 classic)
        addRecipe(ModItems.HYDRATED_COAL_DUST.get(), ModItems.HYDRATED_COAL_CLUMP.get(), 1);

        // Metal processing (IC2 classic) - Mixed Metal Ingot -> Advanced Alloy
        addRecipe(ModItems.ALLOY_INGOT.get(), ModItems.ADVANCED_ALLOY.get(), 1);

        // IC2 specific recipes
        addRecipe(Items.GUNPOWDER, Items.FLINT, 1);                        // IC2: Gunpowder -> Flint
        addRecipe(ModItems.WATER_CELL.get(), Items.SNOWBALL, 1);           // IC2: Water Cell -> Snowball

        // Stone/material compression
        addRecipe(Items.SAND, Items.SANDSTONE, 1, 4);                    // 4 sand -> sandstone
        addRecipe(Items.RED_SAND, Items.RED_SANDSTONE, 1, 4);            // 4 red sand -> red sandstone
        addRecipe(Items.SNOWBALL, Items.SNOW_BLOCK, 1, 4);               // 4 snowballs -> snow block
        addRecipe(Items.SNOW_BLOCK, Items.ICE, 1);                       // snow block -> ice
        addRecipe(Items.ICE, Items.PACKED_ICE, 1);                       // ice -> packed ice
        addRecipe(Items.PACKED_ICE, Items.BLUE_ICE, 1);                  // packed ice -> blue ice

        // Clay compression
        addRecipe(Items.CLAY_BALL, Items.CLAY, 1, 4);                    // 4 clay balls -> clay block

        // Glowstone compression
        addRecipe(Items.GLOWSTONE_DUST, Items.GLOWSTONE, 1, 4);          // 4 glowstone dust -> glowstone

        // Brick compression
        addRecipe(Items.BRICK, Items.BRICKS, 1, 4);                      // 4 bricks -> brick block
        addRecipe(Items.NETHER_BRICK, Items.NETHER_BRICKS, 1, 4);        // 4 nether bricks -> nether brick block

        // Quartz compression
        addRecipe(Items.QUARTZ, Items.QUARTZ_BLOCK, 1, 4);               // 4 quartz -> quartz block

        // Copper block
        addRecipe(Items.COPPER_INGOT, Items.COPPER_BLOCK, 1, 9);         // 9 copper -> copper block

        // Iron block
        addRecipe(Items.IRON_INGOT, Items.IRON_BLOCK, 1, 9);             // 9 iron -> iron block

        // Gold block
        addRecipe(Items.GOLD_INGOT, Items.GOLD_BLOCK, 1, 9);             // 9 gold -> gold block

        // Diamond block
        addRecipe(Items.DIAMOND, Items.DIAMOND_BLOCK, 1, 9);             // 9 diamonds -> diamond block

        // Lapis block
        addRecipe(Items.LAPIS_LAZULI, Items.LAPIS_BLOCK, 1, 9);          // 9 lapis -> lapis block

        // Redstone block
        addRecipe(Items.REDSTONE, Items.REDSTONE_BLOCK, 1, 9);           // 9 redstone -> redstone block

        // Emerald block
        addRecipe(Items.EMERALD, Items.EMERALD_BLOCK, 1, 9);             // 9 emeralds -> emerald block

        // Netherite block
        addRecipe(Items.NETHERITE_INGOT, Items.NETHERITE_BLOCK, 1, 9);   // 9 netherite -> netherite block

        // Coal block
        addRecipe(Items.COAL, Items.COAL_BLOCK, 1, 9);                   // 9 coal -> coal block (alternative recipe)
    }

    private static void addRecipe(Item input, Item output, int outputCount) {
        RECIPES.put(input, new CompressorRecipe(output, outputCount, 1));
    }

    private static void addRecipe(Item input, Item output, int outputCount, int inputCount) {
        RECIPES.put(input, new CompressorRecipe(output, outputCount, inputCount));
    }

    private static void addRecipe(Block input, Item output, int outputCount) {
        addRecipe(input.asItem(), output, outputCount);
    }

    private record CompressorRecipe(Item output, int outputCount, int inputCount) {
        ItemStack getResult() {
            return new ItemStack(output, outputCount);
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

    // Compressor specifications (same as Macerator for balance - IC2 accurate):
    // - Energy consumption: 2 EU/tick while compressing
    // - Operation time: 400 ticks (20 seconds)
    // - Total EU per recipe: 800 EU
    // - Energy storage capacity: 832 EU (800 + 32 buffer)
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

    private static final int MAX_PROGRESS = 400;
    private static final int ENERGY_PER_TICK = 2;
    private static final int MAX_ENERGY = 832;
    private static final int ENERGY_PER_OPERATION = 800;
    private static final int MAX_INPUT = 32;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (simulate && maxReceive > 0) {
                powerAvailableThisTick = maxReceive;
            }

            ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
            if (input.isEmpty() || !lastInputWasValid) {
                return 0;
            }

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

    public CompressorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.COMPRESSOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Compressor");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new CompressorMenu(id, playerInv, this);
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

    public static CompressorRecipe getRecipe(ItemStack input) {
        if (input.isEmpty()) return null;
        return RECIPES.get(input.getItem());
    }

    private boolean canCompress(ItemStack input) {
        if (input.isEmpty()) return false;

        CompressorRecipe recipe = getRecipe(input);
        if (recipe == null) return false;

        // Check if we have enough input items
        if (input.getCount() < recipe.inputCount()) return false;

        ItemStack result = recipe.getResult();
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(output, result) &&
               output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void compressItem(ItemStack input) {
        CompressorRecipe recipe = getRecipe(input);
        if (recipe == null) return;

        ItemStack result = recipe.getResult();

        // Decrease input by required amount
        input.shrink(recipe.inputCount());

        // Add to output
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
    }

    // ========== Server Tick ==========

    public static void serverTick(Level level, BlockPos pos, BlockState state, CompressorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        ItemStack input = be.inventory.getStackInSlot(INPUT_SLOT);

        // Check if the input item changed - if so, reset progress and revalidate
        if (!ItemStack.matches(be.lastInputItem, input)) {
            be.progress = 0;
            be.lastInputItem = input.copy();
            be.lastInputWasValid = !input.isEmpty() && be.canCompress(input);
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
                    be.compressItem(input);
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
            level.setBlock(pos, state.setValue(CompressorBlock.POWERED, be.powered), 3);
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
        lastInputItem = ItemStack.EMPTY;
        lastInputWasValid = false;
    }
}
