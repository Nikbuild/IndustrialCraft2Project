package com.nick.industrialcraft.content.block.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.nick.industrialcraft.api.wrench.IWrenchable;
import com.nick.industrialcraft.registry.ModBlockEntity;
import com.nick.industrialcraft.registry.ModItems;

/**
 * Iron Furnace BlockEntity - Fuel-based furnace that smelts 20% faster than vanilla.
 *
 * From original IC2:
 * - Operation time: 160 ticks (8 seconds) vs vanilla's 200 ticks
 * - Uses standard furnace fuels
 * - Uses vanilla smelting recipes
 * - 3 slots: input, fuel, output
 */
public class IronFurnaceBlockEntity extends BlockEntity implements MenuProvider, IWrenchable {

    // Slot indices
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOTS = 3;

    // IC2 Iron Furnace specs: 160 ticks per operation (20% faster than vanilla's 200)
    public static final int OPERATION_LENGTH = 160;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == OUTPUT_SLOT) return false; // Output slot is output-only
            if (slot == FUEL_SLOT) return getFuelValue(stack) > 0;
            return true;
        }
    };

    // Furnace state
    private int fuel = 0;       // Current fuel burn time remaining
    private int maxFuel = 0;    // Max fuel time for current fuel item (for GUI scaling)
    private int progress = 0;   // Current smelting progress (0 to OPERATION_LENGTH)

    public IronFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.IRON_FURNACE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IronFurnaceBlockEntity be) {
        if (level.isClientSide()) return;

        boolean wasLit = be.isBurning();
        boolean needsUpdate = false;

        // Try to start burning fuel if we can operate but aren't burning
        if (be.fuel <= 0 && be.canOperate()) {
            ItemStack fuelStack = be.inventory.getStackInSlot(FUEL_SLOT);
            int fuelValue = getFuelValue(fuelStack);

            if (fuelValue > 0) {
                be.fuel = fuelValue;
                be.maxFuel = fuelValue;

                // Consume fuel item
                fuelStack.shrink(1);
                if (fuelStack.isEmpty()) {
                    be.inventory.setStackInSlot(FUEL_SLOT, ItemStack.EMPTY);
                }
                needsUpdate = true;
            }
        }

        // Process smelting if burning and can operate
        if (be.isBurning() && be.canOperate()) {
            be.progress++;

            if (be.progress >= OPERATION_LENGTH) {
                be.progress = 0;
                be.operate(level);
                needsUpdate = true;
            }
        } else if (!be.canOperate()) {
            // Reset progress if we can't operate (no input or output full)
            be.progress = 0;
        }

        // Consume fuel each tick while burning
        if (be.fuel > 0) {
            be.fuel--;
        }

        // Update block state if lit state changed
        boolean isLit = be.isBurning();
        if (wasLit != isLit) {
            level.setBlock(pos, state.setValue(IronFurnaceBlock.LIT, isLit), 3);
            needsUpdate = true;
        }

        if (needsUpdate) {
            be.setChanged();
        }
    }

    /**
     * Check if the furnace can perform an operation.
     * Requires: valid input with smelting recipe, and output slot can accept result.
     */
    private boolean canOperate() {
        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) return false;

        // Check if there's a smelting recipe for this input
        ItemStack result = getSmeltingResult(input);
        if (result.isEmpty()) return false;

        // Check if output slot can accept the result
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    /**
     * Perform the smelting operation - consume input, produce output.
     */
    private void operate(Level level) {
        if (!canOperate()) return;

        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        ItemStack result = getSmeltingResult(input);
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }

        // Consume input
        input.shrink(1);
        if (input.isEmpty()) {
            inventory.setStackInSlot(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    /**
     * Get the smelting result for an input item using vanilla smelting recipes.
     */
    private ItemStack getSmeltingResult(ItemStack input) {
        if (level == null || input.isEmpty()) return ItemStack.EMPTY;
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return ItemStack.EMPTY;

        var recipeInput = new SingleRecipeInput(input);
        var optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMELTING, recipeInput, serverLevel);

        if (optional.isEmpty()) return ItemStack.EMPTY;

        return optional.get().value().assemble(recipeInput, serverLevel.registryAccess());
    }

    /**
     * Get the fuel burn time for an item.
     * Returns the number of ticks the fuel will burn.
     * Uses standard vanilla fuel values.
     */
    public static int getFuelValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        var item = stack.getItem();

        // Standard fuel values (from vanilla furnace)
        if (item == Items.COAL || item == Items.CHARCOAL) return 1600;
        if (item == Items.COAL_BLOCK) return 16000;
        if (item == Items.LAVA_BUCKET) return 20000;
        if (item == Items.BLAZE_ROD) return 2400;
        if (item == Items.STICK) return 100;
        if (item == Items.BAMBOO) return 50;
        if (item == Items.DRIED_KELP_BLOCK) return 4001;

        // Wood-based items
        if (item == Items.OAK_LOG || item == Items.SPRUCE_LOG || item == Items.BIRCH_LOG ||
            item == Items.JUNGLE_LOG || item == Items.ACACIA_LOG || item == Items.DARK_OAK_LOG ||
            item == Items.MANGROVE_LOG || item == Items.CHERRY_LOG ||
            item == Items.OAK_WOOD || item == Items.SPRUCE_WOOD || item == Items.BIRCH_WOOD ||
            item == Items.JUNGLE_WOOD || item == Items.ACACIA_WOOD || item == Items.DARK_OAK_WOOD ||
            item == Items.MANGROVE_WOOD || item == Items.CHERRY_WOOD ||
            item == Items.STRIPPED_OAK_LOG || item == Items.STRIPPED_SPRUCE_LOG ||
            item == Items.STRIPPED_BIRCH_LOG || item == Items.STRIPPED_JUNGLE_LOG ||
            item == Items.STRIPPED_ACACIA_LOG || item == Items.STRIPPED_DARK_OAK_LOG ||
            item == Items.STRIPPED_MANGROVE_LOG || item == Items.STRIPPED_CHERRY_LOG ||
            item == Items.STRIPPED_OAK_WOOD || item == Items.STRIPPED_SPRUCE_WOOD ||
            item == Items.STRIPPED_BIRCH_WOOD || item == Items.STRIPPED_JUNGLE_WOOD ||
            item == Items.STRIPPED_ACACIA_WOOD || item == Items.STRIPPED_DARK_OAK_WOOD ||
            item == Items.STRIPPED_MANGROVE_WOOD || item == Items.STRIPPED_CHERRY_WOOD) return 300;

        // Planks
        if (item == Items.OAK_PLANKS || item == Items.SPRUCE_PLANKS || item == Items.BIRCH_PLANKS ||
            item == Items.JUNGLE_PLANKS || item == Items.ACACIA_PLANKS || item == Items.DARK_OAK_PLANKS ||
            item == Items.MANGROVE_PLANKS || item == Items.CHERRY_PLANKS || item == Items.BAMBOO_PLANKS ||
            item == Items.CRIMSON_PLANKS || item == Items.WARPED_PLANKS) return 300;

        return 0;
    }

    public boolean isBurning() {
        return fuel > 0;
    }

    // Getters for Menu/Screen
    public ItemStackHandler getInventory() { return inventory; }
    public int getFuel() { return fuel; }
    public int getMaxFuel() { return maxFuel > 0 ? maxFuel : 1; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return OPERATION_LENGTH; }

    // GUI scaling methods (same as original IC2)
    public int gaugeProgressScaled(int scale) {
        return progress * scale / OPERATION_LENGTH;
    }

    public int gaugeFuelScaled(int scale) {
        if (maxFuel == 0) {
            maxFuel = fuel;
            if (maxFuel == 0) maxFuel = OPERATION_LENGTH;
        }
        return fuel * scale / maxFuel;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.industrialcraft.iron_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new IronFurnaceMenu(id, playerInv, this);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putInt("Fuel", fuel);
        out.putInt("MaxFuel", maxFuel);
        out.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        fuel = in.getIntOr("Fuel", 0);
        maxFuel = in.getIntOr("MaxFuel", 0);
        progress = in.getIntOr("Progress", 0);
    }

    // ========== IWrenchable Implementation ==========
    // Iron Furnace is fuel-based, not electric, so no stored energy

    @Override
    public boolean canWrenchRotate(Player player, Direction newFacing) {
        return newFacing.getAxis().isHorizontal();
    }

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(IronFurnaceBlock.FACING);
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && !level.isClientSide) {
            level.setBlock(worldPosition, getBlockState().setValue(IronFurnaceBlock.FACING, facing), 3);
        }
    }

    @Override
    public boolean canWrenchRemove(Player player) {
        return true;
    }

    @Override
    public int getStoredEnergy() {
        return 0;  // Fuel-based, no energy storage
    }

    @Override
    public void setStoredEnergy(int energy) {
        // No-op for fuel-based machine
    }

    @Override
    public int getMaxStoredEnergy() {
        return 0;  // Fuel-based, no energy storage
    }

    @Override
    public ItemStack createWrenchDrop() {
        // Iron Furnace doesn't store energy, just drop it plain
        return new ItemStack(ModItems.IRON_FURNACE_ITEM.get());
    }
}
