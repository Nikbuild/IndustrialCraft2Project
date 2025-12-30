package com.nick.industrialcraft.content.block.machine;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import com.nick.industrialcraft.registry.ModMenus;

public class CompressorMenu extends AbstractContainerMenu {

    public final CompressorBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Data syncing
    private int progress = 0;
    private int energy = 0;
    private int energyReceivedLastTick = 0;
    private int powerAvailable = 0;  // 0 = false, 1 = true (DataSlot uses int)

    // Constructor for server-side
    public CompressorMenu(int id, Inventory playerInv, CompressorBlockEntity blockEntity) {
        super(ModMenus.COMPRESSOR.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Add input slot (left side, top)
        this.addSlot(new SlotItemHandler(itemHandler, CompressorBlockEntity.INPUT_SLOT, 56, 17));

        // Add battery/charge slot (left side, bottom)
        this.addSlot(new SlotItemHandler(itemHandler, CompressorBlockEntity.BATTERY_SLOT, 56, 53));

        // Add output slot (right side, middle) - result-only slot
        this.addSlot(new SlotItemHandler(itemHandler, CompressorBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Output slot cannot accept items from player
            }
        });

        // Add upgrade slots (4 slots on the far right)
        for (int i = 0; i < 4; i++) {
            this.addSlot(new SlotItemHandler(itemHandler, CompressorBlockEntity.UPGRADE_SLOT_1 + i, 152, 8 + i * 18));
        }

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
        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int v) { progress = v; }
        });

        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getEnergy(); }
            @Override public void set(int v) { energy = v; }
        });

        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getEnergyReceivedLastTick(); }
            @Override public void set(int v) { energyReceivedLastTick = v; }
        });

        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.isPowerAvailable() ? 1 : 0; }
            @Override public void set(int v) { powerAvailable = v; }
        });
    }

    // Constructor for client-side (called when packet is received from server)
    public CompressorMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (CompressorBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return blockEntity.getMaxProgress();
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return blockEntity.getMaxEnergy();
    }

    public int getEnergyReceivedLastTick() {
        return energyReceivedLastTick;
    }

    public boolean isPowerAvailable() {
        return powerAvailable != 0;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        // TODO: Implement shift-click logic
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
