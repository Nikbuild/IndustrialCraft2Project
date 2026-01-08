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
 * MFSU Menu - Handles GUI slots and data sync
 *
 * GUI Layout (same as BatBox/MFE, using GUIElectricBlock.png):
 * - Slot 0 (top): Charge items FROM MFSU - position (56, 17)
 * - Slot 1 (bottom): Discharge items INTO MFSU - position (56, 53)
 * - Energy bar on right side showing fill level
 */
public class MFSUMenu extends AbstractContainerMenu {

    public final MFSUBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Synced data - split into high/low for large energy values
    private int energyLow = 0;
    private int energyHigh = 0;

    // Server-side constructor
    public MFSUMenu(int id, Inventory playerInv, MFSUBlockEntity blockEntity) {
        super(ModMenus.MFSU.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Charge slot (top) - items charged FROM MFSU storage
        this.addSlot(new SlotItemHandler(itemHandler, MFSUBlockEntity.CHARGE_SLOT, 56, 17));

        // Discharge slot (bottom) - items discharged INTO MFSU storage
        this.addSlot(new SlotItemHandler(itemHandler, MFSUBlockEntity.DISCHARGE_SLOT, 56, 53));

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

        // Data slots for energy sync (split into high/low for values > 32767)
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getEnergy() & 0xFFFF;  // Low 16 bits
            }

            @Override
            public void set(int value) {
                energyLow = value & 0xFFFF;
            }
        });

        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (blockEntity.getEnergy() >> 16) & 0xFFFF;  // High 16 bits
            }

            @Override
            public void set(int value) {
                energyHigh = value & 0xFFFF;
            }
        });
    }

    // Client-side constructor
    public MFSUMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (MFSUBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getEnergy() {
        return (energyHigh << 16) | energyLow;
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
