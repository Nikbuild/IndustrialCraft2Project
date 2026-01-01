package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.machine.CanningMachineMenu;

public class CanningMachineScreen extends AbstractContainerScreen<CanningMachineMenu> {

    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/canning_machine.png");
    private static final ResourceLocation TEX_ARROW =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/canning_machine_arrow.png");
    private static final ResourceLocation TEX_ENERGY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/canning_machine_energy.png");

    // Energy flow display (shows power availability like a gas pedal)
    private float flowLevel = 0.0f;
    private boolean firstFrame = true;
    private static final float FLOW_STEP = 0.05f;
    private static final int ENERGY_NEEDED_PER_TICK = 1;  // IC2 accurate: 1 EU/t

    public CanningMachineScreen(CanningMachineMenu menu, Inventory playerInv, Component component) {
        super(menu, playerInv, component);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw background GUI texture
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BG, this.leftPos, this.topPos,
                         0, 0, this.imageWidth, this.imageHeight, 256, 256);

        // Render energy bar overlay
        renderEnergyOverlay(guiGraphics);

        // Render progress arrow overlay
        renderProgressArrow(guiGraphics);
    }

    /**
     * Render energy bar overlay showing power availability.
     */
    private void renderEnergyOverlay(GuiGraphics guiGraphics) {
        int energyReceived = this.menu.getEnergyReceivedLastTick();
        boolean powerAvailable = this.menu.isPowerAvailable();

        // Determine target fill level
        float target;
        if (energyReceived > 0) {
            target = Math.min(1.0f, (float)energyReceived / ENERGY_NEEDED_PER_TICK);
        } else if (powerAvailable) {
            target = 1.0f;
        } else {
            target = 0.0f;
        }

        // On first frame, jump directly to target
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

        // Energy icon is 7x13 pixels
        int maxHeight = 13;
        int fillHeight = (int)(flowLevel * maxHeight);

        if (fillHeight <= 0) return;

        // Position (finalized)
        int overlayX = this.leftPos + 34;
        int overlayY = this.topPos + 28;

        // Calculate source Y to render from bottom up
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
     * Render progress arrow overlay (reveals left-to-right based on canning progress).
     */
    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress <= 0 || progress < 1) return;

        // Arrow dimensions: 34x13 pixels
        int maxWidth = 34;
        int arrowHeight = 13;
        int arrowWidth = ((progress * maxWidth) / maxProgress) + 1;

        if (arrowWidth <= 0) return;

        // Position (finalized)
        int overlayX = this.leftPos + 74;
        int overlayY = this.topPos + 36;

        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ARROW,
            overlayX, overlayY,
            0.0f, 0.0f,
            arrowWidth, arrowHeight,
            34, 13
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
