package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeothermalGeneratorMenu;

/**
 * Geothermal Generator GUI with background texture, energy bar overlay, and flame animation.
 * Flame animation based on BuildCraft's Stirling Engine implementation.
 */
public class GeothermalGeneratorScreen extends AbstractContainerScreen<GeothermalGeneratorMenu> {

    // ---- Background GUI texture ----
    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/guigeogenerator.png");
    private static final int BG_U = 0, BG_V = 0, BG_W = 176, BG_H = 166;
    private static final int TEX_W = 256, TEX_H = 256;

    // ---- Overlay textures ----
    private static final ResourceLocation TEX_BATTERY_OVERLAY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/geothermal_battery_overlay.png");
    private static final ResourceLocation TEX_LAVA_OVERLAY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/geothermal_lava_overlay.png");

    public GeothermalGeneratorScreen(GeothermalGeneratorMenu menu, Inventory playerInv, Component component) {
        super(menu, playerInv, component);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // LAYER 1: Render background GUI
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BG, this.leftPos, this.topPos,
                         BG_U, BG_V, BG_W, BG_H, TEX_W, TEX_H);

        // LAYER 2: Render lava window overlay (fills bottom-to-top based on fuel)
        renderLavaOverlay(guiGraphics);

        // LAYER 3: Render battery overlay (appears when has energy)
        renderBatteryOverlay(guiGraphics);
    }

    /**
     * Render battery overlay texture (shows when actively generating power).
     * Like IC2: shows when outputting to energy network.
     * Currently: shows when generating (has fuel AND buffer not full).
     */
    private void renderBatteryOverlay(GuiGraphics guiGraphics) {
        // Show battery when actively generating (placeholder for "outputting to network")
        // Don't show if: no fuel OR buffer is full (paused generation)
        if (this.menu.getBurnTime() <= 0) return;  // No fuel
        if (this.menu.getEnergy() >= this.menu.getMaxEnergy()) return;  // Buffer full, paused

        int overlayX = this.leftPos + 89;  // X-axis: increase to move RIGHT, decrease to move LEFT
        int overlayY = this.topPos + 39;    // Y-axis: increase to move DOWN, decrease to move UP

        // Render at native texture size: 22x6 pixels (no stretching)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_BATTERY_OVERLAY,
            overlayX, overlayY,
            0.0f, 0.0f,
            22, 6,  // Native size: 22x6 pixels
            22, 6   // Texture size: 22x6 pixels
        );
    }

    /**
     * Render lava window overlay texture (fills bottom-to-top based on fuel level).
     * Like IC2: starts invisible, reveals from bottom upward as lava is added.
     */
    private void renderLavaOverlay(GuiGraphics guiGraphics) {
        int fuel = this.menu.getBurnTime();
        int maxFuel = this.menu.getMaxBurnTime();

        if (maxFuel <= 0 || fuel <= 0) return;  // Don't render if no fuel

        // Calculate fill percentage (0.0 = empty, 1.0 = full)
        float fillPercent = (float) fuel / maxFuel;

        // Lava overlay is 12x12 pixels
        int fullHeight = 12;
        int fillHeight = Math.round(fullHeight * fillPercent);

        if (fillHeight <= 0) return;  // Don't render if nothing to show

        int overlayX = this.leftPos + 67;  // X-axis: increase to move RIGHT, decrease to move LEFT
        int overlayY = this.topPos + 37;   // Y-axis: increase to move DOWN, decrease to move UP

        // Calculate source and destination Y coordinates to fill from bottom up
        int srcY = fullHeight - fillHeight;  // Start from this Y in texture
        int dstY = overlayY + srcY;          // Draw starting from this screen Y

        // Render only the filled portion (bottom-to-top reveal)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_LAVA_OVERLAY,
            overlayX, dstY,              // Screen position (adjusted Y)
            0.0f, (float) srcY,          // Texture position (offset Y to skip unfilled top)
            12, fillHeight,              // Size to render (width, partial height)
            12, 12                       // Full texture size
        );
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040);

        // Draw energy information
        int energy = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        String energyText = energy + "/" + maxEnergy + " EU";
        guiGraphics.drawString(this.font, energyText, 70, 72, 0xFFFFFF);

        // Draw lava fuel level (in buckets)
        int fuel = this.menu.getBurnTime();  // getBurnTime returns fuel units
        int buckets = fuel / 1000;  // 1000 fuel units = 1 bucket
        int maxBuckets = this.menu.getMaxBurnTime() / 1000;  // 24000 / 1000 = 24 buckets
        String fuelText = "Lava: " + buckets + "/" + maxBuckets + " buckets";
        guiGraphics.drawString(this.font, fuelText, 70, 82, 0xFFFFFF);

        // Draw inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x404040);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
