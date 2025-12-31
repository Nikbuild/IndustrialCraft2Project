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

public class InductionFurnaceMenu extends AbstractContainerMenu {

    public final InductionFurnaceBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Data syncing (no heat in reimagined version)
    private int progress = 0;
    private int energyReceivedLastTick = 0;
    private int powerAvailable = 0;  // 0 = false, 1 = true (DataSlot uses int)

    // Constructor for server-side
    public InductionFurnaceMenu(int id, Inventory playerInv, InductionFurnaceBlockEntity blockEntity) {
        super(ModMenus.INDUCTION_FURNACE.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Add input slots (2 slots side by side on left)
        this.addSlot(new SlotItemHandler(itemHandler, InductionFurnaceBlockEntity.INPUT_SLOT_1, 47, 17));
        this.addSlot(new SlotItemHandler(itemHandler, InductionFurnaceBlockEntity.INPUT_SLOT_2, 63, 17));

        // Add battery/charge slot (center bottom)
        this.addSlot(new SlotItemHandler(itemHandler, InductionFurnaceBlockEntity.BATTERY_SLOT, 56, 53));

        // Add output slots (2 slots side by side on right) - result-only slots
        this.addSlot(new SlotItemHandler(itemHandler, InductionFurnaceBlockEntity.OUTPUT_SLOT_1, 113, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Output slot cannot accept items from player
            }
        });
        this.addSlot(new SlotItemHandler(itemHandler, InductionFurnaceBlockEntity.OUTPUT_SLOT_2, 131, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Output slot cannot accept items from player
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

        // Add data slots for syncing (simplified - no heat)
        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int v) { progress = v; }
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
    public InductionFurnaceMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (InductionFurnaceBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return 300;  // MAX_PROGRESS constant (300 EU per item, 25% more efficient than Electric Furnace)
    }

    public int getEnergyReceivedLastTick() {
        return energyReceivedLastTick;
    }

    public int getMaxInput() {
        return 128;  // MAX_INPUT constant (MV tier)
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
