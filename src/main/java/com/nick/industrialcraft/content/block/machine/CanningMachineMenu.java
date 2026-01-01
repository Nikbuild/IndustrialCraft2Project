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
import com.nick.industrialcraft.registry.ModItems;

public class CanningMachineMenu extends AbstractContainerMenu {

    public final CanningMachineBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Data syncing
    private int progress = 0;
    private int maxProgress = 600;
    private int energyReceivedLastTick = 0;
    private int powerAvailable = 0;

    // Constructor for server-side
    public CanningMachineMenu(int id, Inventory playerInv, CanningMachineBlockEntity blockEntity) {
        super(ModMenus.CANNING_MACHINE.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Slot positions:
        // X: +increase = move RIGHT, -decrease = move LEFT
        // Y: +increase = move DOWN,  -decrease = move UP

        // MIDDLE-TOP slot (food input)
        this.addSlot(new SlotItemHandler(itemHandler, CanningMachineBlockEntity.INPUT_SLOT, 69, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CanningMachineBlockEntity.getFoodValue(stack) > 0;
            }
        });

        // MIDDLE-BOTTOM slot (tin cans)
        this.addSlot(new SlotItemHandler(itemHandler, CanningMachineBlockEntity.CAN_SLOT, 69, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.TIN_CAN.get());
            }
        });

        // LEFT slot (battery - under red energy bar)
        this.addSlot(new SlotItemHandler(itemHandler, CanningMachineBlockEntity.BATTERY_SLOT, 30, 45));

        // RIGHT slot (output)
        this.addSlot(new SlotItemHandler(itemHandler, CanningMachineBlockEntity.OUTPUT_SLOT, 119, 35) {
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

        // Add data slots for syncing
        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getProgress(); }
            @Override public void set(int v) { progress = v; }
        });

        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getMaxProgress(); }
            @Override public void set(int v) { maxProgress = v; }
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
    public CanningMachineMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (CanningMachineBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress > 0 ? maxProgress : 600;
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
