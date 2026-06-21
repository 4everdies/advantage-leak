/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import cc.advantage.utils.Util;
import cc.advantage.utils.render.GlUtils;
import cc.advantage.utils.render.shaders.RoundedShader;
import cc.advantage.utils.render.shaders.ShaderUtils;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderUtils
extends Util {
    public static RoundedShader roundedShader = new RoundedShader("roundedRect");
    public static RoundedShader roundedOutlineShader = new RoundedShader("roundRectOutline");
    private static final Map<ResourceLocation, int[]> IMAGE_DIMENSION_CACHE = new ConcurrentHashMap<ResourceLocation, int[]>();
    private static int cachedDisplayWidth = -1;
    private static int cachedDisplayHeight = -1;
    private static int cachedScaleFactor = 1;

    private static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, RoundedShader roundedTexturedShader) {
        Minecraft minecraft = Minecraft.getMinecraft();
        int scaleFactor = RenderUtils.getScaleFactor(minecraft);
        roundedTexturedShader.setUniformf("location", x * (float)scaleFactor, (float)minecraft.displayHeight - height * (float)scaleFactor - y * (float)scaleFactor);
        roundedTexturedShader.setUniformf("rectSize", width * (float)scaleFactor, height * (float)scaleFactor);
        roundedTexturedShader.setUniformf("radius", radius * (float)scaleFactor);
    }

    private static int getScaleFactor(Minecraft minecraft) {
        if (cachedDisplayWidth != minecraft.displayWidth || cachedDisplayHeight != minecraft.displayHeight) {
            ScaledResolution sr = new ScaledResolution(minecraft);
            cachedDisplayWidth = minecraft.displayWidth;
            cachedDisplayHeight = minecraft.displayHeight;
            cachedScaleFactor = sr.getScaleFactor();
        }
        return cachedScaleFactor;
    }

    public static void drawImage(ResourceLocation resourceLocation, float x, float y, float imgWidth, float imgHeight) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(resourceLocation);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, imgWidth, imgHeight, imgWidth, imgHeight);
    }

    public static int[] getImageDimensions(ResourceLocation resource) {
        int[] cached = IMAGE_DIMENSION_CACHE.get(resource);
        if (cached != null) {
            return cached;
        }
        try (InputStream stream = mc.getResourceManager().getResource(resource).getInputStream();){
            BufferedImage image = ImageIO.read(stream);
            int[] dimensions = new int[]{image.getWidth(), image.getHeight()};
            IMAGE_DIMENSION_CACHE.put(resource, dimensions);
            int[] nArray = dimensions;
            return nArray;
        }
        catch (Exception e) {
            e.printStackTrace();
            int[] fallback = new int[]{200, 200};
            IMAGE_DIMENSION_CACHE.put(resource, fallback);
            return fallback;
        }
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, Color color) {
        RenderUtils.drawRoundedRect(x, y, width, height, radius, false, color);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, boolean blur, Color color) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        roundedShader.init();
        RenderUtils.setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        RoundedShader.drawQuads(x - 1.0f, y - 1.0f, width + 2.0f, height + 2.0f);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        RenderUtils.drawRoundedRect(x, y, width, height, radius, false, color);
    }

    public static void drawRoundedRect(double x, double y, double width, double height, double radius, boolean blur, Color color) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        roundedShader.init();
        RenderUtils.setupRoundedRectUniforms((float)x, (float)y, (float)width, (float)height, (float)radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        RoundedShader.drawQuads((float)x - 1.0f, (float)y - 1.0f, (float)width + 2.0f, (float)height + 2.0f);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    public static void drawRoundOutline(float x, float y, float width, float height, float radius, float outlineThickness, Color color, Color outlineColor) {
        RenderUtils.resetColor();
        GlUtils.startBlend();
        GL11.glBlendFunc(770, 771);
        RenderUtils.setAlphaLimit(0.0f);
        roundedOutlineShader.init();
        RenderUtils.setupRoundedRectUniforms(x, y, width, height, radius, roundedOutlineShader);
        roundedOutlineShader.setUniformf("outlineThickness", outlineThickness * (float)RenderUtils.getScaleFactor(Minecraft.getMinecraft()));
        roundedOutlineShader.setUniformf("color", (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        roundedOutlineShader.setUniformf("outlineColor", (float)outlineColor.getRed() / 255.0f, (float)outlineColor.getGreen() / 255.0f, (float)outlineColor.getBlue() / 255.0f, (float)outlineColor.getAlpha() / 255.0f);
        ShaderUtils.drawQuads(x - (2.0f + outlineThickness), y - (2.0f + outlineThickness), width + (4.0f + outlineThickness * 2.0f), height + (4.0f + outlineThickness * 2.0f));
        roundedOutlineShader.unload();
        GlUtils.endBlend();
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture(3553, texture);
    }

    public static void drawGradientRect(double v, double v1, double v2, double v3, boolean b, int rgb, int rgb1) {
    }

    public static void drawOutline(float x, float y, float width, float height, float outlineThickness, int outlineColor) {
        GL11.glEnable(2848);
        RenderUtils.color(outlineColor);
        GlUtils.setup2DRendering();
        GL11.glLineWidth(outlineThickness);
        GL11.glBegin(2);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();
        GlUtils.end2DRendering();
        GL11.glDisable(2848);
    }

    public static void drawFilledCircleNoGL(int x, int y, double r, int c, int quality) {
        float f = (float)(c >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(c >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(c >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(c & 0xFF) / 255.0f;
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(6);
        for (int i = 0; i <= 360 / quality; ++i) {
            double x2 = Math.sin((double)(i * quality) * Math.PI / 180.0) * r;
            double y2 = Math.cos((double)(i * quality) * Math.PI / 180.0) * r;
            GL11.glVertex2d((double)x + x2, (double)y + y2);
        }
        GL11.glEnd();
    }

    public static int darker(int color, float factor) {
        int r = (int)((float)(color >> 16 & 0xFF) * factor);
        int g = (int)((float)(color >> 8 & 0xFF) * factor);
        int b = (int)((float)(color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public static void drawRect(float x, float y, float width, float height, Color color) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int argb = color.getRGB();
        int alpha = argb >> 24 & 0xFF;
        int red = argb >> 16 & 0xFF;
        int green = argb >> 8 & 0xFF;
        int blue = argb & 0xFF;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y + height, 0.0).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(x + width, y, 0.0).color(red, green, blue, alpha).endVertex();
        worldrenderer.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlUtils.resetColor();
    }

    public static void drawArrow(float x, float y, float size, ArrowDirection direction, int color) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(2.0f);
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(4);
        switch (direction.ordinal()) {
            case 0: {
                GL11.glVertex2f(x, y + size);
                GL11.glVertex2f(x + size, y + size);
                GL11.glVertex2f(x + size / 2.0f, y);
                break;
            }
            case 1: {
                GL11.glVertex2f(x, y);
                GL11.glVertex2f(x + size, y);
                GL11.glVertex2f(x + size / 2.0f, y + size);
            }
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
        GL11.glPopMatrix();
    }

    public static void drawCircle(double x, double y, double radius, int color) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(6);
        for (int i = 0; i <= 360; ++i) {
            GL11.glVertex2d(x + Math.sin((double)i * Math.PI / 180.0) * radius, y + Math.cos((double)i * Math.PI / 180.0) * radius);
        }
        GL11.glEnd();
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float outlineThickness, int rectColor, int outlineColor, boolean top, boolean right, boolean bottom, boolean left) {
        Gui.drawRect(x, y, width, height, rectColor);
        GL11.glEnable(2848);
        RenderUtils.color(outlineColor);
        GlUtils.startBlend();
        GlUtils.end2DRendering();
        GL11.glLineWidth(outlineThickness);
        float cornerValue = (float)((double)outlineThickness * 0.19);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, left ? (double)(y + height + cornerValue) : (double)y);
        GL11.glVertex2d(x + width, y + height + cornerValue);
        GL11.glVertex2d(x + width, right ? (double)(y - cornerValue) : (double)(y + height + cornerValue));
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(top ? (double)(x + width) : (double)x, y);
        GL11.glVertex2d(x, y + height);
        GL11.glVertex2d(bottom ? (double)(x + width) : (double)x, y + height);
        GL11.glEnd();
        GlUtils.setup2DRendering();
        GlUtils.endBlend();
        GlUtils.resetColor();
        GL11.glDisable(2848);
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.minY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.pos((float)a.minX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.maxZ).endVertex();
        worldrenderer.pos((float)a.maxX, (float)a.maxY, (float)a.minZ).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }

    public static void renderBoundingBox(AxisAlignedBB aabb, Color color, int alpha) {
        AxisAlignedBB bb = aabb;
        GlStateManager.pushMatrix();
        GlUtils.startBlend();
        GlUtils.end2DRendering();
        GlUtils.enableCaps(3042, 2832, 2881, 2848);
        GL11.glLineWidth(5.0f);
        float actualAlpha = 0.3f * (float)alpha;
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), actualAlpha);
        RenderUtils.color(color.getRGB(), actualAlpha);
        RenderGlobal.drawOutlinedBoundingBox(bb, color.getRed(), color.getGreen(), color.getBlue(), alpha);
        GlUtils.disableCaps();
        GlUtils.setup2DRendering();
        GlUtils.endBlend();
        GlStateManager.popMatrix();
    }

    public static void drawBlockESP(BlockPos blockPos, float red, float green, float blue, float alpha, float lineAlpha, float lineWidth) {
        GlStateManager.color(red, green, blue, alpha);
        float x = (float)((double)blockPos.getX() - mc.getRenderManager().getRenderPosX());
        float y = (float)((double)blockPos.getY() - mc.getRenderManager().getRenderPosY());
        float z = (float)((double)blockPos.getZ() - mc.getRenderManager().getRenderPosZ());
        Block block = RenderUtils.mc.theWorld.getBlockState(blockPos).getBlock();
        RenderUtils.drawBoundingBox(new AxisAlignedBB(x, y, z, (double)x + block.getBlockBoundsMaxX(), (double)y + block.getBlockBoundsMaxY(), (double)z + block.getBlockBoundsMaxZ()));
        if (lineWidth > 0.0f) {
            GL11.glLineWidth(lineWidth);
            GlStateManager.color(red, green, blue, lineAlpha);
            RenderUtils.drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, (double)x + block.getBlockBoundsMaxX(), (double)y + block.getBlockBoundsMaxY(), (double)z + block.getBlockBoundsMaxZ()));
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
    }

    public static void color(int color, float alpha) {
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void color(int color) {
        RenderUtils.color(color, (float)(color >> 24 & 0xFF) / 255.0f);
    }

    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    public static void startScissor(float x, float y, float width, float height) {
        GL11.glEnable(3089);
        Minecraft mc = Minecraft.getMinecraft();
        int scaleFactor = 1;
        try {
            scaleFactor = new ScaledResolution(mc).getScaleFactor();
        }
        catch (Exception exception) {
            // empty catch block
        }
        int scissorX = (int)(x * (float)scaleFactor);
        int scissorY = (int)((float)mc.displayHeight - (y + height) * (float)scaleFactor);
        int scissorWidth = (int)(width * (float)scaleFactor);
        int scissorHeight = (int)(height * (float)scaleFactor);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    public static void endScissor() {
        GL11.glDisable(3089);
    }

    public static float interpolate(float old, float now, float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    public static int getRainbowFromEntity(long currentMillis, int speed, int offset, boolean invert, float alpha) {
        float time = (float)((currentMillis + (long)offset * 300L) % (long)speed) / (float)speed;
        int rainbow = Color.HSBtoRGB(invert ? 1.0f - time : time, 0.9f, 0.9f);
        int r = rainbow >> 16 & 0xFF;
        int g = rainbow >> 8 & 0xFF;
        int b = rainbow & 0xFF;
        int a = (int)(alpha * 255.0f);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static int fadeBetween(int startColor, int endColor, float progress) {
        if (progress > 1.0f) {
            progress = 1.0f - progress % 1.0f;
        }
        return RenderUtils.fadeTo(startColor, endColor, progress);
    }

    public static int fadeBetween(int startColor, int endColor) {
        return RenderUtils.fadeBetween(startColor, endColor, (float)(System.currentTimeMillis() % 2000L) / 1000.0f);
    }

    public static int fadeTo(int startColor, int endColor, float progress) {
        float invert = 1.0f - progress;
        int r = (int)((float)(startColor >> 16 & 0xFF) * invert + (float)(endColor >> 16 & 0xFF) * progress);
        int g = (int)((float)(startColor >> 8 & 0xFF) * invert + (float)(endColor >> 8 & 0xFF) * progress);
        int b = (int)((float)(startColor & 0xFF) * invert + (float)(endColor & 0xFF) * progress);
        int a = (int)((float)(startColor >> 24 & 0xFF) * invert + (float)(endColor >> 24 & 0xFF) * progress);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF;
    }

    public static void start3D() {
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDepthMask(false);
        GlStateManager.disableCull();
    }

    public static void stop3D() {
        GlStateManager.enableCull();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return RenderUtils.interpolate(oldValue, newValue, (float)interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int)RenderUtils.interpolate(oldValue, newValue, (float)interpolationValue);
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1.0f, Math.max(0.0f, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)((float)color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return RenderUtils.applyOpacity(old, opacity).getRGB();
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        return new Color(RenderUtils.interpolateInt(color1.getRed(), color2.getRed(), amount), RenderUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount), RenderUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount), RenderUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        Color resultColor = Color.getHSBColor(RenderUtils.interpolateFloat(color1HSB[0], color2HSB[0], amount), RenderUtils.interpolateFloat(color1HSB[1], color2HSB[1], amount), RenderUtils.interpolateFloat(color1HSB[2], color2HSB[2], amount));
        return RenderUtils.applyOpacity(resultColor, (float)RenderUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount) / 255.0f);
    }

    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? RenderUtils.interpolateColorHue(start, end, (float)angle / 360.0f) : RenderUtils.interpolateColorC(start, end, (float)angle / 360.0f);
    }

    public static int blendColors(Color c1, Color c2, float ratio) {
        ratio = Math.max(0.0f, Math.min(1.0f, ratio));
        int r = (int)((float)c1.getRed() * (1.0f - ratio) + (float)c2.getRed() * ratio);
        int g = (int)((float)c1.getGreen() * (1.0f - ratio) + (float)c2.getGreen() * ratio);
        int b = (int)((float)c1.getBlue() * (1.0f - ratio) + (float)c2.getBlue() * ratio);
        int a = (int)((float)c1.getAlpha() * (1.0f - ratio) + (float)c2.getAlpha() * ratio);
        return new Color(r, g, b, a).getRGB();
    }

    public static Color astolfoColors(int yOffset, int yTotal) {
        float hue;
        float speed = 2900.0f;
        for (hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9); hue > speed; hue -= speed) {
        }
        if ((double)(hue /= speed) > 0.5) {
            hue = 0.5f - (hue - 0.5f);
        }
        return new Color(Color.HSBtoRGB(hue += 0.5f, 0.5f, 1.0f));
    }

    public static Color rainbowColors(int yOffset, int yTotal) {
        float hue;
        float speed = 2900.0f;
        for (hue = (float)(System.currentTimeMillis() % (long)((int)speed)) + (float)((yTotal - yOffset) * 9); hue > speed; hue -= speed) {
        }
        if ((double)(hue /= speed) > 0.5) {
            hue = 0.5f - (hue - 0.5f);
        }
        return new Color(Color.HSBtoRGB(hue += 0.5f, 0.9f, 1.0f));
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return RenderUtils.createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (framebuffer == null || framebuffer.framebufferWidth != RenderUtils.mc.displayWidth || framebuffer.framebufferHeight != RenderUtils.mc.displayHeight) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(RenderUtils.mc.displayWidth, RenderUtils.mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawLine(Entity entity, double[] color, double x, double y, double z) {
        GL11.glEnable(2848);
        if (color.length >= 4) {
            if (color[3] <= 0.1) {
                return;
            }
            GL11.glColor4d(color[0], color[1], color[2], color[3]);
        } else {
            GL11.glColor3d(color[0], color[1], color[2]);
        }
        GL11.glLineWidth(1.5f);
        GL11.glBegin(1);
        GL11.glVertex3d(0.0, RenderUtils.mc.thePlayer.getEyeHeight(), 0.0);
        GL11.glVertex3d(x, y, z);
        GL11.glEnd();
        GL11.glDisable(2848);
    }

    public static void drawLine(double x, double y, double z, double x1, double y1, double z1, Color color, float width) {
        x -= mc.getRenderManager().getRenderPosX();
        x1 -= mc.getRenderManager().getRenderPosX();
        y -= mc.getRenderManager().getRenderPosY();
        y1 -= mc.getRenderManager().getRenderPosY();
        z -= mc.getRenderManager().getRenderPosZ();
        z1 -= mc.getRenderManager().getRenderPosZ();
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(width);
        RenderUtils.color(color.getRGB());
        GL11.glBegin(2);
        GL11.glVertex3d(x, y, z);
        GL11.glVertex3d(x1, y1, z1);
        GL11.glEnd();
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        RenderUtils.color(Color.WHITE.getRGB());
    }

    public static enum ArrowDirection {
        UP,
        DOWN,
        LEFT,
        RIGHT;

    }
}

