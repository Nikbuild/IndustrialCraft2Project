package com.nick.industrialcraft.content.block.machine;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import com.nick.industrialcraft.registry.ModMenus;

/**
 * Iron Furnace Menu - Container for the Iron Furnace GUI.
 * Similar to vanilla furnace but uses IC2 Iron Furnace's faster operation.
 */
public class IronFurnaceMenu extends AbstractContainerMenu {

    public final IronFurnaceBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Synced data for client
    private int fuel = 0;
    private int maxFuel = 0;
    private int progress = 0;

    // Constructor for server-side
    public IronFurnaceMenu(int id, Inventory playerInv, IronFurnaceBlockEntity blockEntity) {
        super(ModMenus.IRON_FURNACE.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Slot positions match vanilla furnace layout
        // Input slot (top) - slot 0
        this.addSlot(new SlotItemHandler(itemHandler, IronFurnaceBlockEntity.INPUT_SLOT, 56, 17));
        // Fuel slot (bottom left) - slot 1
        this.addSlot(new SlotItemHandler(itemHandler, IronFurnaceBlockEntity.FUEL_SLOT, 56, 53));
        // Output slot (right) - slot 2
        this.addSlot(new SlotItemHandler(itemHandler, IronFurnaceBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Output slot cannot receive items
            }
        });

        // Add player inventory (27 slots)
        final int xStart = 8, yStart = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, xStart + col * 18, yStart + row * 18));
            }
        }

        // Add hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, xStart + col * 18, yStart + 58));
        }

        // Add data slots for syncing
        this.addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getFuel(); }
            @Override public void set(int v) { fuel = v; }
        });

        this.addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getMaxFuel(); }
            @Override public void set(int v) { maxFuel = v; }
        });

        this.addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int v) { progress = v; }
        });
    }

    // Constructor for client-side (called when packet is received from server)
    public IronFurnaceMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (IronFurnaceBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    // Getters for screen rendering
    public int getFuel() { return fuel; }
    public int getMaxFuel() { return maxFuel > 0 ? maxFuel : 1; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return IronFurnaceBlockEntity.OPERATION_LENGTH; }

    public boolean isBurning() {
        return fuel > 0;
    }

    /**
     * Get scaled fuel indicator (for flame animation) - 0 to scale.
     */
    public int getBurnProgress(int scale) {
        if (maxFuel <= 0) return 0;
        return fuel * scale / maxFuel;
    }

    /**
     * Get scaled cook progress (for arrow animation) - 0 to scale.
     */
    public int getCookProgress(int scale) {
        return progress * scale / IronFurnaceBlockEntity.OPERATION_LENGTH;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack returnStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            returnStack = stackInSlot.copy();

            // Machine slots: 0 = input, 1 = fuel, 2 = output
            // Player inventory: 3-29
            // Hotbar: 30-38

            if (index < 3) {
                // From machine to player
                if (!this.moveItemStackTo(stackInSlot, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player to machine
                if (IronFurnaceBlockEntity.getFuelValue(stackInSlot) > 0) {
                    // It's fuel, try fuel slot
                    if (!this.moveItemStackTo(stackInSlot, 1, 2, false)) {
                        // Try input slot
                        if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // Not fuel, try input slot
                    if (!this.moveItemStackTo(stackInSlot, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return returnStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
