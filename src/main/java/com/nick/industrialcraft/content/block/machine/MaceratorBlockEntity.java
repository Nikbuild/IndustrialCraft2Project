package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

public class MaceratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int BATTERY_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int UPGRADE_SLOT_1 = 3;
    public static final int UPGRADE_SLOT_2 = 4;
    public static final int UPGRADE_SLOT_3 = 5;
    public static final int UPGRADE_SLOT_4 = 6;
    public static final int SLOTS = 7;

    // Macerator recipes: input item -> output ItemStack
    private static final Map<Item, MaceratorRecipe> RECIPES = new HashMap<>();

    static {
        // Initialize macerator recipes
        initRecipes();
    }

    private static void initRecipes() {
        // ========== ORE DOUBLING (core IC2 mechanic) ==========
        // Raw ores -> 2x Dust
        addRecipe(Items.RAW_IRON, ModItems.IRON_DUST.get(), 2);
        addRecipe(Items.RAW_GOLD, ModItems.GOLD_DUST.get(), 2);
        addRecipe(Items.RAW_COPPER, ModItems.COPPER_DUST.get(), 2);

        // Ore blocks -> 2x Dust (same as raw ores)
        addRecipe(Items.IRON_ORE, ModItems.IRON_DUST.get(), 2);
        addRecipe(Items.DEEPSLATE_IRON_ORE, ModItems.IRON_DUST.get(), 2);
        addRecipe(Items.GOLD_ORE, ModItems.GOLD_DUST.get(), 2);
        addRecipe(Items.DEEPSLATE_GOLD_ORE, ModItems.GOLD_DUST.get(), 2);
        addRecipe(Items.COPPER_ORE, ModItems.COPPER_DUST.get(), 2);
        addRecipe(Items.DEEPSLATE_COPPER_ORE, ModItems.COPPER_DUST.get(), 2);

        // ========== INGOTS -> 1x Dust (recycling) ==========
        addRecipe(Items.IRON_INGOT, ModItems.IRON_DUST.get(), 1);
        addRecipe(Items.GOLD_INGOT, ModItems.GOLD_DUST.get(), 1);
        addRecipe(Items.COPPER_INGOT, ModItems.COPPER_DUST.get(), 1);

        // ========== STONE/COBBLESTONE (IC2 accurate) ==========
        addRecipe(Items.STONE, Items.COBBLESTONE, 1);
        addRecipe(Items.COBBLESTONE, Items.SAND, 1);       // IC2: Cobblestone -> Sand (NOT Gravel)
        addRecipe(Items.GRAVEL, Items.FLINT, 1);           // IC2: Gravel -> Flint (NOT Sand)
        addRecipe(Items.FLINT, Items.GUNPOWDER, 1);        // IC2: Flint -> Gunpowder

        // ========== COAL ==========
        addRecipe(Items.COAL, ModItems.COAL_DUST.get(), 1);
        addRecipe(Items.CHARCOAL, ModItems.COAL_DUST.get(), 1);
        addRecipe(Items.COAL_ORE, Items.COAL, 2);
        addRecipe(Items.DEEPSLATE_COAL_ORE, Items.COAL, 2);

        // ========== OTHER ORES ==========
        addRecipe(Items.LAPIS_ORE, Items.LAPIS_LAZULI, 12);
        addRecipe(Items.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI, 12);
        addRecipe(Items.REDSTONE_ORE, Items.REDSTONE, 6);
        addRecipe(Items.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE, 6);
        addRecipe(Items.DIAMOND_ORE, Items.DIAMOND, 2);
        addRecipe(Items.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND, 2);
        addRecipe(Items.EMERALD_ORE, Items.EMERALD, 2);
        addRecipe(Items.DEEPSLATE_EMERALD_ORE, Items.EMERALD, 2);
        addRecipe(Items.NETHER_QUARTZ_ORE, Items.QUARTZ, 3);
        addRecipe(Items.NETHER_GOLD_ORE, ModItems.GOLD_DUST.get(), 4);

        // ========== MISC ==========
        addRecipe(Items.GLOWSTONE, Items.GLOWSTONE_DUST, 4);
        addRecipe(Items.BONE, Items.BONE_MEAL, 6);
        addRecipe(Items.BLAZE_ROD, Items.BLAZE_POWDER, 5);  // IC2: 5 powder (was 4)
        addRecipe(Items.CLAY, ModItems.CLAY_DUST.get(), 2); // IC2: Clay Block -> 2x Clay Dust
        addRecipe(Items.SANDSTONE, Items.SAND, 4);
        addRecipe(Items.RED_SANDSTONE, Items.RED_SAND, 4);
        addRecipe(Items.NETHERRACK, Items.NETHER_BRICK, 1);
        addRecipe(Items.WHITE_WOOL, Items.STRING, 4);
        addRecipe(Items.ICE, Items.SAND, 1);               // IC2: Ice -> Sand (line 52: pb.Q -> pb.E)
        addRecipe(Items.SNOW_BLOCK, Items.SNOWBALL, 1);    // IC2: Snow Block -> Snowball (line 53: pb.aT -> yr.aD)

        // ========== IC2 SPECIFIC ==========
        addRecipe(ModItems.PLANT_BALL.get(), Items.DIRT, 8);           // IC2: Plant Ball -> 8x Dirt
        addRecipe(ModItems.COFFEE_BEANS.get(), ModItems.COFFEE_POWDER.get(), 3);  // IC2: Coffee Beans -> 3x Coffee Powder
        addRecipe(Items.NETHER_WART, ModItems.GRIN_POWDER.get(), 2);   // IC2: Nether Wart -> 2x Grin Powder
    }

    private static void addRecipe(Item input, Item output, int count) {
        RECIPES.put(input, new MaceratorRecipe(output, count));
    }

    private static void addRecipe(Block input, Item output, int count) {
        addRecipe(input.asItem(), output, count);
    }

    private record MaceratorRecipe(Item output, int count) {
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

    // Macerator specifications (same as Electric Furnace for balance):
    // - Energy consumption: 4 EU/tick while macerating
    // - Operation time: 130 ticks per recipe (6.5 seconds)
    // - Total EU per recipe: 520 EU
    // - Energy storage capacity: 540 EU
    // - Max input: 13 EU/t (matches generator output)

    private float progress = 0.0f;
    private int energy = 0;
    private boolean powered = false;
    private ItemStack lastInputItem = ItemStack.EMPTY;
    private boolean lastInputWasValid = false;    // Cached validity check - matches Electric Furnace pattern
    private int energyReceivedThisTick = 0;
    private int energyReceivedLastTick = 0;
    private boolean powerAvailable = false;       // True when power is being offered (for GUI, even when idle)
    private int powerAvailableThisTick = 0;       // Tracks power offered this tick (via simulate calls)

    private static final int MAX_PROGRESS = 400;          // 400 ticks = 20 seconds (IC2 accurate)
    private static final int ENERGY_PER_TICK = 2;          // 2 EU/t consumption (IC2 accurate)
    private static final int MAX_ENERGY = 832;             // 800 + 32 buffer
    private static final int ENERGY_PER_OPERATION = 800;   // 2 EU/t * 400 ticks = 800 EU (5 ores per coal)
    private static final int MAX_INPUT = 32;               // LV tier max input

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

    public MaceratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MACERATOR.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Macerator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new MaceratorMenu(id, playerInv, this);
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

    public static MaceratorRecipe getRecipe(ItemStack input) {
        if (input.isEmpty()) return null;
        return RECIPES.get(input.getItem());
    }

    private boolean canMacerate(ItemStack input) {
        if (input.isEmpty()) return false;

        MaceratorRecipe recipe = getRecipe(input);
        if (recipe == null) return false;

        ItemStack result = recipe.getResult();
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) return true;

        return ItemStack.isSameItemSameComponents(output, result) &&
               output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void macerateItem(ItemStack input) {
        MaceratorRecipe recipe = getRecipe(input);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, MaceratorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;
        boolean needsUpdate = false;

        ItemStack input = be.inventory.getStackInSlot(INPUT_SLOT);

        // Check if the input item changed - if so, reset progress and revalidate
        if (!ItemStack.matches(be.lastInputItem, input)) {
            be.progress = 0;
            be.lastInputItem = input.copy();
            be.lastInputWasValid = !input.isEmpty() && be.canMacerate(input);
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
                    be.macerateItem(input);
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
        be.powerAvailableThisTick = 0;  // Reset for next tick

        // Update blockstate if powered state changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(MaceratorBlock.POWERED, be.powered), 3);
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
        // This ensures lastInputWasValid gets set correctly even after mod updates
        lastInputItem = ItemStack.EMPTY;
        lastInputWasValid = false;
    }
}
