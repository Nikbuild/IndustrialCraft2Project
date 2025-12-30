package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.machine.ExtractorMenu;

public class ExtractorScreen extends AbstractContainerScreen<ExtractorMenu> {

    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/extractor.png");
    private static final ResourceLocation TEX_ARROW =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/extractor_arrow.png");
    private static final ResourceLocation TEX_ENERGY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/extractor_energy.png");

    // Energy flow display (shows power availability like a gas pedal)
    private float flowLevel = 0.0f;
    private boolean firstFrame = true;  // Skip animation on first frame after data sync
    private static final float FLOW_STEP = 0.05f;
    private static final int ENERGY_NEEDED_PER_TICK = 2;  // IC2 accurate: 2 EU/t

    public ExtractorScreen(ExtractorMenu menu, Inventory playerInv, Component component) {
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
     * Render energy bar overlay showing power availability like a gas pedal.
     * The bar shows what percentage of required energy (2 EU/t) is currently flowing.
     * When actively processing: shows actual energy received as percentage
     * When idle but power available: shows full bar (power ready to use)
     * When no power: empty bar
     */
    private void renderEnergyOverlay(GuiGraphics guiGraphics) {
        int energyReceived = this.menu.getEnergyReceivedLastTick();
        boolean powerAvailable = this.menu.isPowerAvailable();

        // Determine target fill level
        float target;
        if (energyReceived > 0) {
            // Actively receiving energy - show percentage of required energy
            target = Math.min(1.0f, (float)energyReceived / ENERGY_NEEDED_PER_TICK);
        } else if (powerAvailable) {
            // Power is available but not being used (idle) - show full bar
            target = 1.0f;
        } else {
            // No power available
            target = 0.0f;
        }

        // On first frame, jump directly to target (no animation on GUI open)
        if (firstFrame) {
            flowLevel = target;
            firstFrame = false;
        } else {
            // Smoothly animate toward target in 5% steps
            if (flowLevel < target) {
                flowLevel = Math.min(flowLevel + FLOW_STEP, target);
            } else if (flowLevel > target) {
                flowLevel = Math.max(flowLevel - FLOW_STEP, target);
            }
        }

        if (flowLevel <= 0.0f) return;

        // Energy icon is 7x13 pixels
        int maxHeight = 13;
        int fillHeight = (int)(flowLevel * maxHeight);

        if (fillHeight <= 0) return;

        // Position (aligned)
        int overlayX = this.leftPos + 59;
        int overlayY = this.topPos + 37;

        // Calculate source Y to render from bottom up
        int srcY = maxHeight - fillHeight;
        int dstY = overlayY + srcY;

        // Render only the filled portion (bottom-to-top reveal)
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
     * Render progress arrow overlay (reveals left-to-right based on extracting progress).
     * The extractor uses a droplet-shaped arrow (24x16 pixels like macerator).
     */
    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress <= 0 || progress < 1) return;

        // Extractor arrow is 24x16 pixels (same as macerator)
        int maxWidth = 24;
        int arrowWidth = ((progress * maxWidth) / maxProgress) + 1;

        if (arrowWidth <= 0) return;

        // Position (aligned)
        int overlayX = this.leftPos + 79;
        int overlayY = this.topPos + 34;

        // Render only the visible portion (left-to-right reveal)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ARROW,
            overlayX, overlayY,
            0.0f, 0.0f,
            arrowWidth, 16,
            24, 16
        );
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // Draw inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
