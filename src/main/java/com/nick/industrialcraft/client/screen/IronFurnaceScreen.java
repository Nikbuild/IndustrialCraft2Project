package com.nick.industrialcraft.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.nick.industrialcraft.IndustrialCraft;
import com.nick.industrialcraft.content.block.machine.IronFurnaceMenu;

/**
 * Iron Furnace GUI Screen.
 * Similar to vanilla furnace but uses IC2's Iron Furnace textures and faster operation.
 * Uses the same flame animation system as the Generator.
 */
public class IronFurnaceScreen extends AbstractContainerScreen<IronFurnaceMenu> {

    // GUI background texture
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/gui_iron_furnace.png");

    // Arrow overlay texture (reuse electric furnace arrow)
    private static final ResourceLocation TEX_ARROW =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/electric_furnace_arrow.png");

    // Flame animation filmstrip (same as Generator - 28 frames stacked vertically, each 112x112)
    private static final ResourceLocation TEX_FLAME_STRIP =
        ResourceLocation.fromNamespaceAndPath(IndustrialCraft.MODID, "textures/gui/generator_flame_strip.png");

    // Texture dimensions (standard 256x256 Minecraft GUI texture)
    private static final int TEX_W = 256, TEX_H = 256;
    // GUI render dimensions (standard 1x scale)
    private static final int BG_W = 176, BG_H = 166;

    // ---- Flame animation settings (same as Generator) ----
    private static final int FRAME_SRC_W = 112;
    private static final int FRAME_SRC_H = 112;
    private static final int FRAMES = 28;

    // 14×14 window on GUI for flame
    private static final int FLAME_W = 14, FLAME_H = 14;
    private static final int FLAME_X = 56, FLAME_Y = 36;

    // Split one 112px frame into three source columns
    private static final int[] SRC_U  = {  6,  40,  74 };
    private static final int[] SRC_WC = { 30,  32,  30 };

    // Place three narrow flames inside the 14×14 slot
    private static final int[] DST_OX = { 1,   5,   9 };
    private static final int[] DST_W  = { 4,   4,   4 };

    // Liveliness model: mean-reverting offsets per flame
    private static final double DEV_MAX = 0.10;
    private static final int    NOISE_PERIOD_TICKS = 9;
    private static final int    SYNC_PERIOD_TICKS  = 80;
    private static final double LERP_TO_TARGET     = 0.22;
    private static final double PASSIVE_DECAY      = 0.990;

    // State: per-flame offset and target
    private final double[] offset = new double[3];
    private final double[] target = new double[3];
    private long lastTick = Long.MIN_VALUE;

    // Arrow animation position
    private static final int ARROW_X = 80;
    private static final int ARROW_Y = 35;

    public IronFurnaceScreen(IronFurnaceMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = BG_W;
        this.imageHeight = BG_H;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Draw background
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
            this.leftPos, this.topPos,
            0, 0, BG_W, BG_H, TEX_W, TEX_H);

        // Draw flame animation (if burning)
        if (this.menu.isBurning()) {
            renderFlameAnimation(guiGraphics);
        }

        // Draw arrow (progress indicator) - draws from left to right
        int arrowWidth = this.menu.getCookProgress(22);
        if (arrowWidth > 0) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEX_ARROW,
                this.leftPos + ARROW_X, this.topPos + ARROW_Y,
                0.0f, 0.0f,
                arrowWidth, 16,
                22, 16);
        }
    }

    /**
     * Render flame animation with three independently-indexed wandering columns.
     * Same implementation as Generator.
     */
    private void renderFlameAnimation(GuiGraphics g) {
        // Calculate burn progress (0.0 = empty, 1.0 = full burn time remaining)
        double frac = (double) this.menu.getFuel() / this.menu.getMaxFuel();

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

            int srcV = idx * FRAME_SRC_H;
            int srcU = SRC_U[i];
            int srcW = SRC_WC[i];

            int dx = dstBaseX + DST_OX[i];
            int dw = DST_W[i];

            g.blit(
                RenderPipelines.GUI_TEXTURED,
                TEX_FLAME_STRIP,
                dx, dstY,
                (float) srcU, (float) srcV,
                dw, FLAME_H,
                srcW, FRAME_SRC_H,
                FRAME_SRC_W, FRAME_SRC_H * FRAMES
            );
        }
    }

    /**
     * Update flame animation state with mean-reverting wandering offsets.
     */
    private void updateOffsets(long gt, double baseFrac) {
        double edgeScale = Mth.clamp(baseFrac * 1.2, 0.0, 1.0) * Mth.clamp((1.0 - baseFrac) * 1.2, 0.0, 1.0);
        double maxDev = DEV_MAX * (0.6 + 0.4 * edgeScale);

        boolean pick = (gt % NOISE_PERIOD_TICKS) == 0;
        boolean sync = (gt % SYNC_PERIOD_TICKS)  == 0;

        for (int i = 0; i < 3; i++) {
            if (pick) {
                double n = pseudoNoise(gt, i);
                double rnd = (n * 2.0 - 1.0) * maxDev;
                target[i] = sync ? 0.0 : rnd;
            }

            offset[i] += (target[i] - offset[i]) * LERP_TO_TARGET;
            offset[i] *= PASSIVE_DECAY;
            offset[i] = Mth.clamp(offset[i], -maxDev, maxDev);
        }
    }

    private static double pseudoNoise(long t, int i) {
        double x = (t * (17 + i * 13)) * 0.123456789;
        return (Math.sin(x) * 0.5) + 0.5;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw title (centered)
        guiGraphics.drawString(this.font, this.title,
            (this.imageWidth - this.font.width(this.title)) / 2, 6, 0x404040, false);

        // Draw inventory label
        guiGraphics.drawString(this.font, this.playerInventoryTitle,
            8, this.imageHeight - 96 + 2, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
