package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.machine.RecyclerMenu;

public class RecyclerScreen extends AbstractContainerScreen<RecyclerMenu> {

    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/recycler.png");
    private static final ResourceLocation TEX_ARROW =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/recycler_arrow.png");
    private static final ResourceLocation TEX_ENERGY =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/recycler_energy.png");
    private static final ResourceLocation TEX_SCRAP_BAR =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/recycler_scrap_bar.png");

    // Energy flow display (shows power availability like a gas pedal)
    private float flowLevel = 0.0f;
    private boolean firstFrame = true;  // Skip animation on first frame after data sync
    private static final float FLOW_STEP = 0.05f;
    private static final int ENERGY_NEEDED_PER_TICK = 1;  // IC2 accurate: 1 EU/t

    // Scrap points bar display (shows accumulated points toward next scrap)
    private float scrapBarLevel = 0.0f;
    private boolean scrapBarFirstFrame = true;  // Separate flag so scrap bar jumps to correct value on GUI open
    private static final float SCRAP_BAR_STEP = 0.016f;  // Speed for smooth animation (must complete before next item)
    private int lastScrapPoints = -1;  // Track previous points to detect overflow (-1 = uninitialized)

    // Overflow animation state machine
    // Phase 0: Normal operation (no overflow)
    // Phase 1: Animating UP toward 100%
    // Phase 2: Pausing at 100%
    // Phase 3: Animating DOWN toward target
    private int overflowPhase = 0;
    private float overflowTarget = 0f;  // Target to animate DOWN to after hitting 100%
    private int overflowPauseFrames = 0;  // Frames remaining in pause
    private static final int OVERFLOW_PAUSE_DURATION = 8;  // Brief pause at top (~0.13 seconds at 60fps)

    public RecyclerScreen(RecyclerMenu menu, Inventory playerInv, Component component) {
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

        // Render scrap points bar overlay (fills from bottom to top)
        renderScrapPointsBar(guiGraphics);
    }

    /**
     * Render energy bar overlay showing power availability like a gas pedal.
     * The bar shows what percentage of required energy (1 EU/t) is currently flowing.
     * When actively processing: shows actual energy received as percentage
     * When idle but power available: shows full bar (power ready to use)
     * When no power: empty bar
     * Energy PNG is 7x13 pixels native.
     */
    private void renderEnergyOverlay(GuiGraphics guiGraphics) {
        int energyReceived = this.menu.getEnergyReceivedLastTick();
        boolean powerAvailable = this.menu.isPowerAvailable();

        float target;
        if (energyReceived > 0) {
            target = Math.min(1.0f, (float)energyReceived / ENERGY_NEEDED_PER_TICK);
        } else if (powerAvailable) {
            target = 1.0f;
        } else {
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

        int overlayX = this.leftPos + 59;
        int overlayY = this.topPos + 37;

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
     * Render progress arrow overlay (reveals left-to-right based on recycling progress).
     * Arrow PNG is 18x15 pixels native.
     */
    private void renderProgressArrow(GuiGraphics guiGraphics) {
        int progress = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();

        if (maxProgress <= 0 || progress < 1) return;

        int maxWidth = 18;
        int arrowWidth = ((progress * maxWidth) / maxProgress) + 1;

        if (arrowWidth <= 0) return;

        int overlayX = this.leftPos + 81;
        int overlayY = this.topPos + 35;

        // Render partial arrow (left-to-right reveal) at native 18x15 size
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ARROW,
            overlayX, overlayY,
            0.0f, 0.0f,
            arrowWidth, 15,
            18, 15
        );
    }

    /**
     * Render scrap points bar overlay (fills from bottom to top based on accumulated points).
     * Shows progress toward next scrap (0-99 points out of 100).
     * Uses same smooth oscillation animation as the energy bar.
     *
     * Overflow animation: When points wrap around (e.g., 70 -> 25 after producing scrap),
     * the bar first animates to 100%, pauses briefly, then animates back down to the new value.
     * This provides visual feedback that scrap was produced.
     *
     * Scrap bar PNG is 10x44 pixels native.
     */
    private void renderScrapPointsBar(GuiGraphics guiGraphics) {
        int scrapPoints = this.menu.getScrapPoints();
        int maxPoints = this.menu.getMaxScrapPoints();

        // Calculate target fill level (0.0 to 1.0)
        float target = maxPoints > 0 ? (float) scrapPoints / maxPoints : 0.0f;

        // First frame: jump directly to correct value, no animation (no "slacking employees")
        if (scrapBarFirstFrame) {
            scrapBarLevel = target;
            lastScrapPoints = scrapPoints;  // Initialize so we don't detect false overflow
            overflowPhase = 0;
            overflowPauseFrames = 0;
            scrapBarFirstFrame = false;
        } else {
            // Detect overflow: if new points < old points AND we're not already in overflow animation,
            // it means scrap was produced and points wrapped around
            if (lastScrapPoints >= 0 && scrapPoints < lastScrapPoints && overflowPhase == 0) {
                // Start overflow animation: remember where we need to end up
                overflowTarget = target;
                overflowPhase = 1;  // Start Phase 1: animate UP
            }
            lastScrapPoints = scrapPoints;

            // Explicit state machine using overflowPhase
            switch (overflowPhase) {
                case 1:
                    // Phase 1: Animate UP toward 100%
                    scrapBarLevel = Math.min(scrapBarLevel + SCRAP_BAR_STEP, 1.0f);
                    if (scrapBarLevel >= 0.999f) {
                        scrapBarLevel = 1.0f;
                        overflowPauseFrames = OVERFLOW_PAUSE_DURATION;
                        overflowPhase = 2;  // Move to Phase 2: pause
                    }
                    break;

                case 2:
                    // Phase 2: Pause at 100%
                    overflowPauseFrames--;
                    if (overflowPauseFrames <= 0) {
                        overflowPhase = 3;  // Move to Phase 3: animate DOWN
                    }
                    break;

                case 3:
                    // Phase 3: Animate DOWN toward overflowTarget
                    scrapBarLevel = Math.max(scrapBarLevel - SCRAP_BAR_STEP, overflowTarget);
                    if (scrapBarLevel <= overflowTarget + 0.001f) {
                        scrapBarLevel = overflowTarget;
                        overflowPhase = 0;  // Done, back to normal
                    }
                    break;

                default:
                    // Phase 0: Normal animation (no overflow) - can go up OR down
                    if (scrapBarLevel < target) {
                        scrapBarLevel = Math.min(scrapBarLevel + SCRAP_BAR_STEP, target);
                    } else if (scrapBarLevel > target) {
                        scrapBarLevel = Math.max(scrapBarLevel - SCRAP_BAR_STEP, target);
                    }
                    break;
            }
        }

        if (scrapBarLevel <= 0.0f) return;

        int maxHeight = 44;
        int fillHeight = (int)(scrapBarLevel * maxHeight);

        if (fillHeight <= 0) return;

        int overlayX = this.leftPos + 139;
        int overlayY = this.topPos + 21;

        // Draw from bottom up: srcY is how far down in the texture to start
        int srcY = maxHeight - fillHeight;
        int dstY = overlayY + srcY;

        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_SCRAP_BAR,
            overlayX, dstY,
            0.0f, (float) srcY,
            10, fillHeight,
            10, 44
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
