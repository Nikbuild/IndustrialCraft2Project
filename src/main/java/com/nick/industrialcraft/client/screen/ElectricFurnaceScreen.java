package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.machine.ElectricFurnaceMenu;

public class ElectricFurnaceScreen extends AbstractContainerScreen<ElectricFurnaceMenu> {

    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/electric_furnace.png");
    private static final ResourceLocation TEX_ARROW =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/electric_furnace_arrow.png");
    private static final ResourceLocation TEX_ENERGY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/electric_furnace_energy.png");

    // Energy flow display (shows power availability like a gas pedal)
    private float flowLevel = 0.0f;       // Current display level (0.0 to 1.0)
    private static final float FLOW_STEP = 0.05f;  // 5% per frame (20 total frames)
    private static final int ENERGY_NEEDED_PER_TICK = 4;  // Energy required to continue operation

    public ElectricFurnaceScreen(ElectricFurnaceMenu menu, Inventory playerInv, Component component) {
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
     * The bar shows what percentage of required energy (4 EU/t) is currently flowing.
     * 100% = receiving full 4+ EU/t (can operate)
     * 50% = receiving 2 EU/t (underpowered)
     * 0% = receiving no energy
     * Transitions use discrete 5% steps (20 frames) for smooth, visible animation.
     */
    private void renderEnergyOverlay(GuiGraphics guiGraphics) {
        int energyReceived = this.menu.getEnergyReceivedLastTick();

        // Target shows percentage of required energy being received
        // Cap at 100% if receiving more than needed
        float target = Math.min(1.0f, (float)energyReceived / ENERGY_NEEDED_PER_TICK);

        // Smoothly animate toward target in 5% steps
        if (flowLevel < target) {
            // Gas pedal pressed - fill up smoothly
            flowLevel = Math.min(flowLevel + FLOW_STEP, target);
        } else if (flowLevel > target) {
            // Gas pedal released - drain down smoothly
            flowLevel = Math.max(flowLevel - FLOW_STEP, target);
        }

        if (flowLevel <= 0.0f) return;  // Don't render if empty

        // Calculate fill height based on current flow level
        // Our texture is 7x13, animation uses discrete 5% steps
        int maxHeight = 13;
        int fillHeight = (int)(flowLevel * maxHeight);

        if (fillHeight <= 0) return;

        // Position (from your manual adjustment)
        int overlayX = this.leftPos + 59;
        int overlayY = this.topPos + 37;

        // Calculate source Y to render from bottom up (IC2 pattern)
        int srcY = maxHeight - fillHeight;  // Start from this Y in texture
        int dstY = overlayY + srcY;          // Draw starting from this screen Y

        // Render only the filled portion (bottom-to-top reveal)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ENERGY,
            overlayX, dstY,              // Screen position (adjusted Y)
            0.0f, (float) srcY,          // Texture position (offset Y to skip unfilled top)
            7, fillHeight,               // Size to render (width=7, partial height)
            7, 13                        // Full texture size
        );
    }

    /**
     * Render progress arrow overlay (reveals left-to-right based on smelting progress).
     * IC2 pattern: int progress = (int)(24.0F * getProgress());
     * if (progress > 0) b(j + 79, k + 34, 176, 14, progress + 1, 16);
     */
    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress <= 0 || progress < 1) return;  // Don't render if not smelting (progress must be at least 1)

        // Calculate arrow width (IC2 uses 24 pixels max, our texture is 22 pixels)
        int maxWidth = 22;
        // IC2 adds +1 for smoother animation and to show progress immediately
        int arrowWidth = ((progress * maxWidth) / maxProgress) + 1;

        if (arrowWidth <= 0) return;

        // Position (from your manual adjustment)
        int overlayX = this.leftPos + 80;
        int overlayY = this.topPos + 35;

        // Render only the visible portion (left-to-right reveal)
        // IC2 adds +1 to progress width for smoother animation
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ARROW,
            overlayX, overlayY,          // Screen position
            0.0f, 0.0f,                  // Texture position (start from left)
            arrowWidth, 16,              // Size to render (partial width, full height=16)
            22, 16                       // Full texture size
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
