package com.nick.industrialcraft.content.block.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.nick.industrialcraft.registry.ModBlockEntity;

public class GeneratorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int FUEL_SLOT = 0;
    public static final int SLOTS = 1;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int burnTime = 0;
    private int maxBurnTime = 0;
    private int energy = 0;
    private boolean powered = false;
    private static final int MAX_ENERGY = 4000;  // IC2 Generator stores 4000 EU max
    private static final int ENERGY_PER_TICK = 4;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.GENERATOR.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new GeneratorMenu(id, playerInv, this);
    }

    public ItemStackHandler getInventory() {
        return inventory;
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
        return MAX_ENERGY;
    }

    public void setBurnTimeClient(int time) {
        this.burnTime = time;
    }

    public void setEnergyClient(int energy) {
        this.energy = Math.min(energy, MAX_ENERGY);
    }

    public boolean isPowered() {
        return powered;
    }

    public void setPoweredClient(boolean powered) {
        this.powered = powered;
    }

    public static void serverTick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, GeneratorBlockEntity be) {
        if (level.isClientSide) return;

        boolean wasPowered = be.powered;

        // If we're burning, continue
        if (be.burnTime > 0) {
            be.burnTime--;
            be.powered = true;

            // Generate energy while burning (if not already full)
            if (be.energy < MAX_ENERGY) {
                be.energy += ENERGY_PER_TICK;
                if (be.energy > MAX_ENERGY) {
                    be.energy = MAX_ENERGY;
                }
            }

            be.setChanged();
        } else {
            be.powered = false;

            // Only try to get fuel if storage is NOT full
            if (be.energy < MAX_ENERGY && !be.inventory.getStackInSlot(FUEL_SLOT).isEmpty()) {
                var fuelStack = be.inventory.getStackInSlot(FUEL_SLOT);

                // Simple fuel check - accept coal and charcoal
                if (isFuel(fuelStack)) {
                    be.maxBurnTime = 1600; // 80 seconds
                    be.burnTime = be.maxBurnTime;
                    be.inventory.extractItem(FUEL_SLOT, 1, false);
                    be.powered = true;
                    be.setChanged();
                }
            }
        }

        // Update blockstate if powered changed
        if (wasPowered != be.powered) {
            level.setBlock(pos, state.setValue(com.nick.industrialcraft.content.block.generator.GeneratorBlock.POWERED, be.powered), 3);
        }
    }

    private static boolean isFuel(net.minecraft.world.item.ItemStack stack) {
        if (stack.isEmpty()) return false;
        var item = stack.getItem();
        return item == net.minecraft.world.item.Items.COAL ||
               item == net.minecraft.world.item.Items.CHARCOAL;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        inventory.serialize(out.child("Inventory"));
        out.putInt("BurnTime", burnTime);
        out.putInt("MaxBurnTime", maxBurnTime);
        out.putInt("Energy", energy);
        out.putBoolean("Powered", powered);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        in.child("Inventory").ifPresent(inventory::deserialize);
        burnTime = in.getIntOr("BurnTime", 0);
        maxBurnTime = in.getIntOr("MaxBurnTime", 0);
        energy = in.getIntOr("Energy", 0);
        powered = in.getBooleanOr("Powered", false);
    }
}
