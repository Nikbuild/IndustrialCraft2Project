package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.machine.InductionFurnaceMenu;

/**
 * Reimagined Induction Furnace GUI
 *
 * No heat display - instead shows power throughput.
 * The energy bar shows current EU/t flow as a percentage of max (128 EU/t).
 * More power = faster smelting, displayed in real-time.
 */
public class InductionFurnaceScreen extends AbstractContainerScreen<InductionFurnaceMenu> {

    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/induction_furnace.png");
    private static final ResourceLocation TEX_ARROW =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/induction_furnace_arrow.png");
    private static final ResourceLocation TEX_ENERGY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/induction_furnace_energy.png");

    // Energy flow display - shows EU/t as percentage of max input (128 EU/t)
    private float flowLevel = 0.0f;
    private boolean firstFrame = true;
    private static final float FLOW_STEP = 0.05f;
    private static final int MAX_INPUT = 128;  // MV tier max input

    public InductionFurnaceScreen(InductionFurnaceMenu menu, Inventory playerInv, Component component) {
        super(menu, playerInv, component);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw background GUI texture
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BG, this.leftPos, this.topPos,
                         0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Render energy bar overlay (fills from bottom to top)
        renderEnergyOverlay(guiGraphics);

        // Render progress arrow overlay (reveals from left to right)
        renderProgressArrow(guiGraphics);
    }

    /**
     * Render energy bar overlay showing power throughput.
     * Shows current EU/t as percentage of max input (128 EU/t).
     * Energy PNG is 7x13 pixels native.
     */
    private void renderEnergyOverlay(GuiGraphics guiGraphics) {
        int energyReceived = this.menu.getEnergyReceivedLastTick();
        boolean powerAvailable = this.menu.isPowerAvailable();

        // Determine target fill level
        float target;
        if (energyReceived > 0) {
            // Actively receiving energy - show percentage of required energy
            target = Math.min(1.0f, (float)energyReceived / MAX_INPUT);
        } else if (powerAvailable) {
            // Power is available but not being used (idle) - show full bar like an LED
            target = 1.0f;
        } else {
            // No power available
            target = 0.0f;
        }

        if (firstFrame) {
            flowLevel = target;
            firstFrame = false;
        } else {
            if (flowLevel < target) {
                flowLevel = Math.min(flowLevel + FLOW_STEP, target);
            } else if (flowLevel > target) {
                flowLevel = Math.max(flowLevel - FLOW_STEP, target);
            }
        }

        if (flowLevel <= 0.0f) return;

        int maxHeight = 13;
        int fillHeight = (int)(flowLevel * maxHeight);

        if (fillHeight <= 0) return;

        // Position for red energy bar (lightning bolt)
        // +X moves RIGHT, -X moves LEFT | +Y moves DOWN, -Y moves UP
        int overlayX = this.leftPos + 59;  // Horizontal position
        int overlayY = this.topPos + 37;   // Vertical position

        int srcY = maxHeight - fillHeight;
        int dstY = overlayY + srcY;

        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ENERGY,
            overlayX, dstY,
            0.0f, (float) srcY,
            7, fillHeight,
            7, 13
        );
    }

    /**
     * Render progress arrow overlay (reveals left-to-right based on smelting progress).
     * Arrow is 22x16 pixels native (same as Electric Furnace).
     */
    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress <= 0 || progress < 1) return;

        // Calculate arrow width (texture is 22 pixels wide)
        int maxWidth = 22;
        int arrowWidth = ((progress * maxWidth) / maxProgress) + 1;

        if (arrowWidth <= 0) return;

        // Position for white progress arrow
        // +X moves RIGHT, -X moves LEFT | +Y moves DOWN, -Y moves UP
        int overlayX = this.leftPos + 80;  // Horizontal position
        int overlayY = this.topPos + 35;   // Vertical position

        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ARROW,
            overlayX, overlayY,
            0.0f, 0.0f,
            arrowWidth, 16,
            22, 16
        );
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // Draw inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);

        // Draw power throughput label (replaces heat display)
        int energyReceived = this.menu.getEnergyReceivedLastTick();
        String powerLabel = energyReceived + " EU/t";
        guiGraphics.drawString(this.font, powerLabel, 10, 40, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
