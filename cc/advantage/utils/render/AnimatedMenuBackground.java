/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import java.awt.Color;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public final class AnimatedMenuBackground {
    private static final int DARK_PINK = AnimatedMenuBackground.rgb(94, 12, 59);
    private static final int DARK_PURPLE = AnimatedMenuBackground.rgb(45, 14, 86);
    private static final int DARK_BLUE = AnimatedMenuBackground.rgb(8, 23, 71);
    private static final int DEEP_NAVY = AnimatedMenuBackground.rgb(3, 5, 22);
    private static final int TOP_VIGNETTE = AnimatedMenuBackground.argb(105, 0, 0, 0);
    private static final int BOTTOM_VIGNETTE = AnimatedMenuBackground.argb(145, 0, 0, 0);
    private static final int SIDE_VIGNETTE = AnimatedMenuBackground.argb(95, 0, 0, 0);
    private static final int TRANSPARENT_BLACK = AnimatedMenuBackground.argb(0, 0, 0, 0);

    private AnimatedMenuBackground() {
    }

    public static void draw(int width, int height, long startTime) {
        AnimatedMenuBackground.draw(width, height, startTime, 0);
    }

    public static void draw(int width, int height, long startTime, int variant) {
        long elapsed = System.currentTimeMillis() - startTime;
        Gui.drawRect(0, 0, width, height, DEEP_NAVY);
        AnimatedMenuBackground.drawGradientStrips(width, height, elapsed, variant);
        AnimatedMenuBackground.drawFlowingRibbons(width, height, elapsed, variant);
        AnimatedMenuBackground.drawVignette(width, height);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static Color getAccentColor(long startTime, int alpha) {
        long elapsed = System.currentTimeMillis() - startTime;
        return new Color(AnimatedMenuBackground.withAlpha(AnimatedMenuBackground.palette((double)elapsed * 2.2E-4), alpha), true);
    }

    private static void drawGradientStrips(int width, int height, long elapsed, int variant) {
        int strips = Math.max(36, Math.min(96, width / 8));
        double stripWidth = (double)width / (double)strips;
        double time = (double)elapsed * 1.8E-4 + (double)variant * 0.7;
        AnimatedMenuBackground.beginColorDrawing(true);
        WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < strips; ++i) {
            double x1 = (double)i * stripWidth;
            double x2 = Math.ceil((double)(i + 1) * stripWidth) + 1.0;
            double phase = (double)i / (double)strips + time;
            int top = AnimatedMenuBackground.withAlpha(AnimatedMenuBackground.palette(phase), 255);
            int bottom = AnimatedMenuBackground.withAlpha(AnimatedMenuBackground.palette(phase + 0.34 + Math.sin(time + (double)i * 0.035) * 0.08), 255);
            AnimatedMenuBackground.addVertex(renderer, x2, 0.0, top);
            AnimatedMenuBackground.addVertex(renderer, x1, 0.0, top);
            AnimatedMenuBackground.addVertex(renderer, x1, height, bottom);
            AnimatedMenuBackground.addVertex(renderer, x2, height, bottom);
        }
        Tessellator.getInstance().draw();
        AnimatedMenuBackground.endColorDrawing();
    }

    private static void drawFlowingRibbons(int width, int height, long elapsed, int variant) {
        double time = (double)elapsed * 1.4E-4 + (double)variant * 0.32;
        AnimatedMenuBackground.beginColorDrawing(true);
        WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 5; ++i) {
            double bandWidth = (double)width * (0.22 + (double)i * 0.035);
            double speed = 0.18 + (double)i * 0.045;
            double progress = (time * speed + (double)i * 0.23) % 1.55;
            double x = (progress - 0.35) * (double)width;
            double skew = (double)height * (i % 2 == 0 ? 0.36 : -0.42);
            int color = AnimatedMenuBackground.withAlpha(AnimatedMenuBackground.palette(time + (double)i * 0.27), 34 + i * 5);
            int fade = AnimatedMenuBackground.withAlpha(color, 0);
            AnimatedMenuBackground.addVertex(renderer, x + bandWidth, 0.0, fade);
            AnimatedMenuBackground.addVertex(renderer, x, 0.0, color);
            AnimatedMenuBackground.addVertex(renderer, x + skew, height, fade);
            AnimatedMenuBackground.addVertex(renderer, x + bandWidth + skew, height, color);
        }
        Tessellator.getInstance().draw();
        AnimatedMenuBackground.endColorDrawing();
    }

    private static void drawVignette(int width, int height) {
        int verticalSize = Math.max(40, height / 4);
        int horizontalSize = Math.max(40, width / 5);
        AnimatedMenuBackground.drawVerticalGradient(0, 0, width, verticalSize, TOP_VIGNETTE, TRANSPARENT_BLACK);
        AnimatedMenuBackground.drawVerticalGradient(0, height - verticalSize, width, height, TRANSPARENT_BLACK, BOTTOM_VIGNETTE);
        AnimatedMenuBackground.drawHorizontalGradient(0, 0, horizontalSize, height, SIDE_VIGNETTE, TRANSPARENT_BLACK);
        AnimatedMenuBackground.drawHorizontalGradient(width - horizontalSize, 0, width, height, TRANSPARENT_BLACK, SIDE_VIGNETTE);
    }

    private static void drawVerticalGradient(int left, int top, int right, int bottom, int start, int end) {
        AnimatedMenuBackground.beginColorDrawing(true);
        WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        AnimatedMenuBackground.addVertex(renderer, right, top, start);
        AnimatedMenuBackground.addVertex(renderer, left, top, start);
        AnimatedMenuBackground.addVertex(renderer, left, bottom, end);
        AnimatedMenuBackground.addVertex(renderer, right, bottom, end);
        Tessellator.getInstance().draw();
        AnimatedMenuBackground.endColorDrawing();
    }

    private static void drawHorizontalGradient(int left, int top, int right, int bottom, int start, int end) {
        AnimatedMenuBackground.beginColorDrawing(true);
        WorldRenderer renderer = Tessellator.getInstance().getWorldRenderer();
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        AnimatedMenuBackground.addVertex(renderer, right, top, end);
        AnimatedMenuBackground.addVertex(renderer, left, top, start);
        AnimatedMenuBackground.addVertex(renderer, left, bottom, start);
        AnimatedMenuBackground.addVertex(renderer, right, bottom, end);
        Tessellator.getInstance().draw();
        AnimatedMenuBackground.endColorDrawing();
    }

    private static void beginColorDrawing(boolean smooth) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (smooth) {
            GlStateManager.shadeModel(7425);
        }
    }

    private static void endColorDrawing() {
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private static void addVertex(WorldRenderer renderer, double x, double y, int color) {
        renderer.pos(x, y, 0.0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >>> 24).endVertex();
    }

    private static int palette(double phase) {
        double normalized = phase - Math.floor(phase);
        double section = normalized * 3.0;
        if (section < 1.0) {
            return AnimatedMenuBackground.blend(DARK_PINK, DARK_PURPLE, AnimatedMenuBackground.smooth(section));
        }
        if (section < 2.0) {
            return AnimatedMenuBackground.blend(DARK_PURPLE, DARK_BLUE, AnimatedMenuBackground.smooth(section - 1.0));
        }
        return AnimatedMenuBackground.blend(DARK_BLUE, DARK_PINK, AnimatedMenuBackground.smooth(section - 2.0));
    }

    private static double smooth(double value) {
        return value * value * (3.0 - 2.0 * value);
    }

    private static int blend(int first, int second, double amount) {
        int red = (int)((double)(first >> 16 & 0xFF) + (double)((second >> 16 & 0xFF) - (first >> 16 & 0xFF)) * amount);
        int green = (int)((double)(first >> 8 & 0xFF) + (double)((second >> 8 & 0xFF) - (first >> 8 & 0xFF)) * amount);
        int blue = (int)((double)(first & 0xFF) + (double)((second & 0xFF) - (first & 0xFF)) * amount);
        return AnimatedMenuBackground.rgb(red, green, blue);
    }

    private static int withAlpha(int color, int alpha) {
        return Math.max(0, Math.min(255, alpha)) << 24 | color & 0xFFFFFF;
    }

    private static int rgb(int red, int green, int blue) {
        return AnimatedMenuBackground.argb(255, red, green, blue);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
}

