package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.generator.GeneratorMenu;

/**
 * Generator GUI with background texture, energy bar overlay, and flame animation.
 * Flame animation based on BuildCraft's Stirling Engine implementation.
 */
public class GeneratorScreen extends AbstractContainerScreen<GeneratorMenu> {

    // ---- Background GUI texture ----
    private static final ResourceLocation TEX_BG =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/guigenerator.png");
    private static final int BG_U = 0, BG_V = 0, BG_W = 176, BG_H = 166;
    private static final int TEX_W = 256, TEX_H = 256;

    // ---- Energy bar positioning (adjustable for testing) ----
    private static final int ENERGY_BAR_X = 94;   // Adjust as needed
    private static final int ENERGY_BAR_Y = 39;    // Adjust as needed

    // ---- Energy bar texture (red bar from test6.png) - no stretching ----
    private static final ResourceLocation TEX_ENERGY_BAR =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/block/technical/test6.png");
    // test6.png is 24x9 pixels, render at native size
    private static final int ENERGY_BAR_TEX_W = 24;
    private static final int ENERGY_BAR_TEX_H = 9;

    // ---- Flame animation filmstrip (28 frames stacked vertically, each 112x112) ----
    private static final ResourceLocation TEX_FLAME_STRIP =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/generator_flame_strip.png");
    private static final int FRAME_SRC_W = 112;
    private static final int FRAME_SRC_H = 112;
    private static final int FRAMES = 28;

    // ---- 14×14 window on GUI (matching Stirling Engine original proportions) ----
    private static final int FLAME_W = 14, FLAME_H = 14;
    private static final int FLAME_X = (int) 65.8, FLAME_Y = 36;

    // ---- Split one 112px frame into three source columns ----
    private static final int[] SRC_U  = {  6,  40,  74 }; // source X within the 112px frame
    private static final int[] SRC_WC = { 30,  32,  30 }; // source width of each flame column

    // ---- Place three narrow flames inside the 14×14 slot ----
    private static final int[] DST_OX = { 1,   5,   9 };  // destination X within the slot
    private static final int[] DST_W  = { 4,   4,   4 };  // destination width for each flame strip

    // ---- Liveliness model: mean-reverting offsets per flame ----
    private static final double DEV_MAX = 0.10;
    private static final int    NOISE_PERIOD_TICKS = 9;
    private static final int    SYNC_PERIOD_TICKS  = 80;
    private static final double LERP_TO_TARGET     = 0.22;
    private static final double PASSIVE_DECAY      = 0.990;

    // State: per-flame offset and target
    private final double[] offset = new double[3];
    private final double[] target = new double[3];
    private long lastTick = Long.MIN_VALUE;

    public GeneratorScreen(GeneratorMenu menu, Inventory playerInv, Component component) {
        super(menu, playerInv, component);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // LAYER 1: Render background GUI
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEX_BG, this.leftPos, this.topPos,
                         BG_U, BG_V, BG_W, BG_H, TEX_W, TEX_H);

        // LAYER 2: Render flame animation (if burning)
        if (this.menu.getBurnTime() > 0) {
            renderFlameAnimation(guiGraphics);
        }

        // LAYER 3: Render energy bar overlay (left-to-right fill)
        renderEnergyBar(guiGraphics);
    }

    /**
     * Render energy bar texture (test6.png) revealing from left to right as coal burns.
     * Starts invisible, reveals left-to-right as coal is consumed.
     */
    private void renderEnergyBar(GuiGraphics guiGraphics) {
        if (this.menu.getMaxBurnTime() <= 0) return;

        // Calculate burn progress: (maxBurnTime - burnTime) / maxBurnTime
        // This gives 0% when coal is fresh, 100% when coal is fully consumed
        int burnedTime = this.menu.getMaxBurnTime() - this.menu.getBurnTime();
        float burnProgress = (float) burnedTime / this.menu.getMaxBurnTime();

        // Calculate how much of the texture to reveal (left to right)
        int revealWidth = Math.round(ENERGY_BAR_TEX_W * burnProgress);
        if (revealWidth <= 0) return;  // Don't render if nothing to show

        // Screen coordinates for the energy bar area
        int screenX = this.leftPos + ENERGY_BAR_X;
        int screenY = this.topPos + ENERGY_BAR_Y;

        // Enable scissor clipping to reveal only the filled portion (left to right)
        guiGraphics.enableScissor(screenX, screenY, screenX + revealWidth, screenY + ENERGY_BAR_TEX_H);

        // Render the red bar texture at native size (24x9 pixels, no stretching)
        guiGraphics.blit(
            RenderPipelines.GUI_TEXTURED,
            TEX_ENERGY_BAR,
            screenX, screenY,
            0.0f, 0.0f,
            ENERGY_BAR_TEX_W, ENERGY_BAR_TEX_H,  // Draw size = texture size (native)
            ENERGY_BAR_TEX_W, ENERGY_BAR_TEX_H   // Texture size
        );

        // Disable scissor clipping
        guiGraphics.disableScissor();
    }

    /**
     * Render flame animation with three independently-indexed wandering columns.
     * Based on BuildCraft's Stirling Engine implementation.
     */
    private void renderFlameAnimation(GuiGraphics g) {
        // Calculate burn progress (0.0 = empty, 1.0 = full burn time remaining)
        double frac = (double) this.menu.getBurnTime() / this.menu.getMaxBurnTime();

        // Update per-flame wandering offsets once per game tick
        long gt = (this.minecraft != null && this.minecraft.level != null) ? this.minecraft.level.getGameTime() : 0L;
        if (gt != lastTick) {
            updateOffsets(gt, frac);
            lastTick = gt;
        }

        int dstBaseX = this.leftPos + FLAME_X;
        int dstY     = this.topPos  + FLAME_Y;

        // Render three flame columns, each with its own frame index
        for (int i = 0; i < 3; i++) {
            // Apply this flame's offset to the burn fraction, then map to a frame index
            double localFrac = Mth.clamp(frac + offset[i], 0.0, 1.0);
            int idx = Mth.clamp((int) Math.floor((FRAMES - 1) * (1.0 - localFrac)), 0, FRAMES - 1);

            int srcV = idx * FRAME_SRC_H;   // vertical frame row
            int srcU = SRC_U[i];
            int srcW = SRC_WC[i];

            int dx = dstBaseX + DST_OX[i];
            int dw = DST_W[i];

            g.blit(
                RenderPipelines.GUI_TEXTURED,
                TEX_FLAME_STRIP,
                dx, dstY,
                (float) srcU, (float) srcV,
                dw, FLAME_H,                   // destination height in the window
                srcW, FRAME_SRC_H,             // one column source size
                FRAME_SRC_W, FRAME_SRC_H * FRAMES  // total texture size
            );
        }
    }

    /**
     * Update flame animation state with mean-reverting wandering offsets.
     * Based on BuildCraft's Stirling Engine implementation.
     */
    private void updateOffsets(long gt, double baseFrac) {
        // Scale amplitude down when nearly empty/full so it doesn't look weird
        double edgeScale = Mth.clamp(baseFrac * 1.2, 0.0, 1.0) * Mth.clamp((1.0 - baseFrac) * 1.2, 0.0, 1.0);
        double maxDev = DEV_MAX * (0.6 + 0.4 * edgeScale); // keep some motion across range

        // Every NOISE_PERIOD_TICKS, pick new targets; every SYNC_PERIOD_TICKS, bias to 0 (catch up)
        boolean pick = (gt % NOISE_PERIOD_TICKS) == 0;
        boolean sync = (gt % SYNC_PERIOD_TICKS)  == 0;

        for (int i = 0; i < 3; i++) {
            if (pick) {
                double n = pseudoNoise(gt, i);              // 0..1
                double rnd = (n * 2.0 - 1.0) * maxDev;      // -maxDev..+maxDev
                target[i] = sync ? 0.0 : rnd;               // periodic catch-up impulse
            }

            // Smoothly chase the target and gently decay toward 0
            offset[i] += (target[i] - offset[i]) * LERP_TO_TARGET;
            offset[i] *= PASSIVE_DECAY;

            // Clamp safety
            offset[i] = Mth.clamp(offset[i], -maxDev, maxDev);
        }
    }

    /**
     * Deterministic, cheap "noise" without allocating Random.
     * Based on BuildCraft's implementation.
     */
    private static double pseudoNoise(long t, int i) {
        double x = (t * (17 + i * 13)) * 0.123456789; // arbitrary irrational-ish factor
        return (Math.sin(x) * 0.5) + 0.5;             // 0..1
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

        // Draw burn time text
        String burnText = "Burn: " + this.menu.getBurnTime() / 20 + "s";
        guiGraphics.drawString(this.font, burnText, 70, 82, 0xFFFFFF);

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
