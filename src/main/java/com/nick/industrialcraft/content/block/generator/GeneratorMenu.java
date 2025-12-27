package com.nick.industrialcraft.content.block.generator;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import com.nick.industrialcraft.registry.ModMenus;

public class GeneratorMenu extends AbstractContainerMenu {
    public static final int RESULT_SLOT = 0;

    public final GeneratorBlockEntity blockEntity;
    private final ItemStackHandler itemHandler;

    // Data syncing
    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int energy = 0;

    // Constructor for server-side
    public GeneratorMenu(int id, Inventory playerInv, GeneratorBlockEntity blockEntity) {
        super(ModMenus.GENERATOR.get(), id);
        this.blockEntity = blockEntity;
        this.itemHandler = blockEntity.getInventory();

        // Add fuel slot
        this.addSlot(new SlotItemHandler(itemHandler, GeneratorBlockEntity.FUEL_SLOT, 65, 53));

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
            @Override public int get() { return blockEntity.getBurnTime(); }
            @Override public void set(int v) { burnTime = v; }
        });

        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getMaxBurnTime(); }
            @Override public void set(int v) { maxBurnTime = v; }
        });

        this.addDataSlot(new net.minecraft.world.inventory.DataSlot() {
            @Override public int get() { return blockEntity.getEnergy(); }
            @Override public void set(int v) { energy = v; }
        });
    }

    // Constructor for client-side (called when packet is received from server)
    public GeneratorMenu(int id, Inventory playerInv, RegistryFriendlyByteBuf buf) {
        this(id, playerInv, (GeneratorBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return blockEntity.getMaxEnergy();
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        // No quick move support for now
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
