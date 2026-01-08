package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.storage.MFEMenu;

/**
 * MFE Screen - Renders the GUI for the MFE energy storage
 *
 * GUI Features:
 * - Energy bar showing current storage level
 * - 2 slots for charging/discharging items
 * - Uses same layout as BatBox (GUIElectricBlock.png style)
 */
public class MFEScreen extends AbstractContainerScreen<MFEMenu> {

    private static final ResourceLocation TEX_BG =
            ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/mfe.png");

    private static final ResourceLocation TEX_ENERGY =
            ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/mfe_energy.png");

    // Energy bar texture native dimensions (24x9 pixels)
    private static final int ENERGY_TEX_W = 24;
    private static final int ENERGY_TEX_H = 9;

    // Position in GUI where to paste it (aligned with charge slot at x=56)
    private static final int ENERGY_X = 79;   // Increase = move RIGHT, Decrease = move LEFT
    private static final int ENERGY_Y = 38;   // Increase = move DOWN, Decrease = move UP

    public MFEScreen(MFEMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw main GUI background
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BG, this.leftPos, this.topPos,
                0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Draw energy bar overlay
        renderEnergyBar(guiGraphics);
    }

    /**
     * Render energy bar texture revealing from left to right based on stored energy.
     */
    private void renderEnergyBar(GuiGraphics guiGraphics) {
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();

        if (maxEnergy <= 0 || energy <= 0) return;  // Don't render if no energy

        // Calculate energy percentage (0.0 to 1.0)
        float energyPercent = (float) energy / maxEnergy;

        // Calculate how much of the texture to reveal (left to right)
        int revealWidth = Math.round(ENERGY_TEX_W * energyPercent);
        if (revealWidth <= 0) return;

        // Screen coordinates
        int screenX = this.leftPos + ENERGY_X;
        int screenY = this.topPos + ENERGY_Y;

        // Use scissor clipping to reveal from left to right
        guiGraphics.enableScissor(screenX, screenY, screenX + revealWidth, screenY + ENERGY_TEX_H);

        // Render at native size
        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEX_ENERGY,
                screenX, screenY,
                0.0f, 0.0f,
                ENERGY_TEX_W, ENERGY_TEX_H,
                ENERGY_TEX_W, ENERGY_TEX_H
        );

        guiGraphics.disableScissor();
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
