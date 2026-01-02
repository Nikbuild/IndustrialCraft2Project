package com.nick.industrialcraft.content.block.storage;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import com.nick.industrialcraft.registry.ModMenus;

/**
 * BatBox Menu - Handles GUI slots and data sync
 *
 * GUI Layout (from GUIElectricBlock.png):
 * - Slot 0 (top): Charge items FROM BatBox - position (80, 17)
 * - Slot 1 (bottom): Discharge items INTO BatBox - position (80, 53)
 * - Energy bar on right side showing fill level
 */
public class BatBoxMenu extends AbstractContainerMenu {

    public final BatBoxBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Synced data
    private int energy = 0;

    // Server-side constructor
    public BatBoxMenu(int id, Inventory playerInv, BatBoxBlockEntity blockEntity) {
        super(ModMenus.BATBOX.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Charge slot (top) - items charged FROM BatBox storage
        this.addSlot(new SlotItemHandler(itemHandler, BatBoxBlockEntity.CHARGE_SLOT, 56, 17));

        // Discharge slot (bottom) - items discharged INTO BatBox storage
        this.addSlot(new SlotItemHandler(itemHandler, BatBoxBlockEntity.DISCHARGE_SLOT, 56, 53));

        // Player inventory (27 slots)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar (9 slots)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        // Data slot for energy sync
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getEnergy();
            }

            @Override
            public void set(int value) {
                energy = value;
            }
        });
    }

    // Client-side constructor
    public BatBoxMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (BatBoxBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return blockEntity.getMaxEnergy();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // TODO: Implement shift-click logic for battery items
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
