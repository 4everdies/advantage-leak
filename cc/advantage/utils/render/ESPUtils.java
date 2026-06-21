/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import cc.advantage.utils.Util;
import java.awt.Color;
import java.nio.FloatBuffer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class ESPUtils
extends Util {
    public static final FloatBuffer windPos = GLAllocation.createDirectFloatBuffer(4);

    public static void draw3DRect(float x1, float y1, float x2, float y2) {
        GL11.glBegin(7);
        GL11.glVertex2d(x2, y1);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x1, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }

    public static void drawCornerESP(Entity entity, float red, float green, float blue) {
        float x = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosX());
        float y = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosY());
        float z = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosZ());
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0f, z);
        GlStateManager.rotate(-ESPUtils.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(-0.098, -0.098, 0.098);
        float width = (float)(26.6 * (double)entity.width / 2.0);
        float height = entity instanceof EntityPlayer ? 12.0f : (float)(11.98 * (double)(entity.height / 2.0f));
        GlStateManager.color(red, green, blue);
        ESPUtils.draw3DRect(width, height - 1.0f, width - 4.0f, height);
        ESPUtils.draw3DRect(-width, height - 1.0f, -width + 4.0f, height);
        ESPUtils.draw3DRect(-width, height, -width + 1.0f, height - 4.0f);
        ESPUtils.draw3DRect(width, height, width - 1.0f, height - 4.0f);
        ESPUtils.draw3DRect(width, -height, width - 4.0f, -height + 1.0f);
        ESPUtils.draw3DRect(-width, -height, -width + 4.0f, -height + 1.0f);
        ESPUtils.draw3DRect(-width, -height + 1.0f, -width + 1.0f, -height + 4.0f);
        ESPUtils.draw3DRect(width, -height + 1.0f, width - 1.0f, -height + 4.0f);
        GlStateManager.color(0.0f, 0.0f, 0.0f);
        ESPUtils.draw3DRect(width, height, width - 4.0f, height + 0.2f);
        ESPUtils.draw3DRect(-width, height, -width + 4.0f, height + 0.2f);
        ESPUtils.draw3DRect(-width - 0.2f, height + 0.2f, -width, height - 4.0f);
        ESPUtils.draw3DRect(width + 0.2f, height + 0.2f, width, height - 4.0f);
        ESPUtils.draw3DRect(width + 0.2f, -height, width - 4.0f, -height - 0.2f);
        ESPUtils.draw3DRect(-width - 0.2f, -height, -width + 4.0f, -height - 0.2f);
        ESPUtils.draw3DRect(-width - 0.2f, -height, -width, -height + 4.0f);
        ESPUtils.draw3DRect(width + 0.2f, -height, width, -height + 4.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public static void drawEntityESP(Entity entity, float red, float green, float blue, float alpha, float lineAlpha, float lineWidth) {
        float x = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosX());
        float y = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosY());
        float z = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosZ());
        GL11.glColor4f(red, green, blue, alpha);
        ESPUtils.otherDrawBoundingBox(entity, x, y, z, entity.width - 0.2f, entity.height + 0.1f);
        if (lineWidth > 0.0f) {
            GL11.glLineWidth(lineWidth);
            GL11.glColor4f(red, green, blue, lineAlpha);
            ESPUtils.otherDrawOutlinedBoundingBox(entity, x, y, z, entity.width - 0.2f, entity.height + 0.1f);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawOutlineEntityESP(Entity entity, float red, float green, float blue, float alpha, float lineAlpha, float lineWidth) {
        float x = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosX());
        float y = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosY());
        float z = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosZ());
        GL11.glColor4f(red, green, blue, alpha);
        if (lineWidth > 0.0f) {
            GL11.glLineWidth(lineWidth);
            GL11.glColor4f(red, green, blue, lineAlpha);
            ESPUtils.otherDrawOutlinedBoundingBox(entity, x, y, z, entity.width - 0.2f, entity.height + 0.1f);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawFake2DESP(Entity entity, float red, float green, float blue) {
        float x = (float)(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosX());
        float y = (float)(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosY());
        float z = (float)(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)ESPUtils.mc.timer.renderPartialTicks - mc.getRenderManager().getRenderPosZ());
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + entity.height / 2.0f, z);
        GlStateManager.rotate(-ESPUtils.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(-0.1, -0.1, 0.1);
        GlStateManager.color(red, green, blue);
        float width = (float)(23.3 * (double)entity.width / 2.0);
        float height = entity instanceof EntityPlayer ? 12.0f : (float)(11.98 * (double)(entity.height / 2.0f));
        ESPUtils.draw3DRect(width, height, -width, height + 0.4f);
        ESPUtils.draw3DRect(width, -height, -width, -height + 0.4f);
        ESPUtils.draw3DRect(width, -height + 0.4f, width - 0.4f, height + 0.4f);
        ESPUtils.draw3DRect(-width, -height + 0.4f, -width + 0.4f, height + 0.4f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public static void render2DESP(AxisAlignedBB axisAlignedBB, Color color, float lineWidth) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        windPos.clear();
        for (int x = 0; x < 2; ++x) {
            for (int z = 0; z < 2; ++z) {
                for (int y = 0; y < 2; ++y) {
                    if (!GLU.gluProject((float)(x == 1 ? axisAlignedBB.minX : axisAlignedBB.maxX), (float)(y == 1 ? axisAlignedBB.minY : axisAlignedBB.maxY), (float)(z == 1 ? axisAlignedBB.minZ : axisAlignedBB.maxZ), ActiveRenderInfo.MODELVIEW, ActiveRenderInfo.PROJECTION, ActiveRenderInfo.VIEWPORT, windPos) || windPos.get(2) > 1.0f) continue;
                    double screenX = windPos.get(0) / (float)scaledResolution.getScaleFactor();
                    double screenY = windPos.get(1) / (float)scaledResolution.getScaleFactor();
                    minX = Math.min(screenX, minX);
                    minY = Math.min(screenY, minY);
                    maxX = Math.max(screenX, maxX);
                    maxY = Math.max(screenY, maxY);
                }
            }
        }
        if (minX != Double.MAX_VALUE) {
            minX = Math.max(0.0, minX);
            minY = Math.max(0.0, minY);
            maxX = Math.min((double)scaledResolution.getScaledWidth(), maxX);
            maxY = Math.min((double)scaledResolution.getScaledHeight(), maxY);
            double margin = 3.0;
            minX -= margin;
            minY -= margin;
            maxX += margin;
            maxY += margin;
            minY = (double)scaledResolution.getScaledHeight() - minY;
            maxY = (double)scaledResolution.getScaledHeight() - maxY;
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(3553);
            GL11.glColor4d(0.0, 0.0, 0.0, 0.2);
            GL11.glBegin(7);
            GL11.glVertex2d(minX, minY);
            GL11.glVertex2d(maxX, minY);
            GL11.glVertex2d(maxX, maxY);
            GL11.glVertex2d(minX, maxY);
            GL11.glEnd();
            GL11.glColor4d((double)color.getRed() / 255.0, (double)color.getGreen() / 255.0, (double)color.getBlue() / 255.0, (double)color.getAlpha() / 255.0);
            GL11.glLineWidth(lineWidth);
            GL11.glBegin(2);
            GL11.glVertex2d(minX, minY);
            GL11.glVertex2d(minX, maxY);
            GL11.glVertex2d(maxX, maxY);
            GL11.glVertex2d(maxX, minY);
            GL11.glEnd();
            GL11.glEnable(3553);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }
    }

    public static double interpolate(double lastPos, double pos) {
        return lastPos + (pos - lastPos) * (double)ESPUtils.mc.timer.renderPartialTicks;
    }

    public static void otherDrawOutlinedBoundingBox(Entity entity, float x, float y, float z, double width, double height) {
        float newYaw4;
        float newYaw3;
        float newYaw2;
        float newYaw1;
        width *= 1.5;
        float yaw1 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 45.0f;
        if (yaw1 < 0.0f) {
            newYaw1 = 0.0f;
            newYaw1 += 360.0f - Math.abs(yaw1);
        } else {
            newYaw1 = yaw1;
        }
        newYaw1 *= -1.0f;
        newYaw1 *= (float)Math.PI / 180;
        float yaw2 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 135.0f;
        if (yaw2 < 0.0f) {
            newYaw2 = 0.0f;
            newYaw2 += 360.0f - Math.abs(yaw2);
        } else {
            newYaw2 = yaw2;
        }
        newYaw2 *= -1.0f;
        newYaw2 *= (float)Math.PI / 180;
        float yaw3 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 225.0f;
        if (yaw3 < 0.0f) {
            newYaw3 = 0.0f;
            newYaw3 += 360.0f - Math.abs(yaw3);
        } else {
            newYaw3 = yaw3;
        }
        newYaw3 *= -1.0f;
        newYaw3 *= (float)Math.PI / 180;
        float yaw4 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 315.0f;
        if (yaw4 < 0.0f) {
            newYaw4 = 0.0f;
            newYaw4 += 360.0f - Math.abs(yaw4);
        } else {
            newYaw4 = yaw4;
        }
        newYaw4 *= -1.0f;
        newYaw4 *= (float)Math.PI / 180;
        float x2 = (float)(Math.sin(newYaw1) * width + (double)x);
        float z2 = (float)(Math.cos(newYaw1) * width + (double)z);
        float x3 = (float)(Math.sin(newYaw2) * width + (double)x);
        float z3 = (float)(Math.cos(newYaw2) * width + (double)z);
        float x4 = (float)(Math.sin(newYaw3) * width + (double)x);
        float z4 = (float)(Math.cos(newYaw3) * width + (double)z);
        float x5 = (float)(Math.sin(newYaw4) * width + (double)x);
        float z5 = (float)(Math.cos(newYaw4) * width + (double)z);
        float y2 = (float)((double)y + height);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x2, y, z2).endVertex();
        worldrenderer.pos(x2, y2, z2).endVertex();
        worldrenderer.pos(x3, y2, z3).endVertex();
        worldrenderer.pos(x3, y, z3).endVertex();
        worldrenderer.pos(x2, y, z2).endVertex();
        worldrenderer.pos(x5, y, z5).endVertex();
        worldrenderer.pos(x4, y, z4).endVertex();
        worldrenderer.pos(x4, y2, z4).endVertex();
        worldrenderer.pos(x5, y2, z5).endVertex();
        worldrenderer.pos(x5, y, z5).endVertex();
        worldrenderer.pos(x5, y2, z5).endVertex();
        worldrenderer.pos(x4, y2, z4).endVertex();
        worldrenderer.pos(x3, y2, z3).endVertex();
        worldrenderer.pos(x3, y, z3).endVertex();
        worldrenderer.pos(x4, y, z4).endVertex();
        worldrenderer.pos(x5, y, z5).endVertex();
        worldrenderer.pos(x5, y2, z5).endVertex();
        worldrenderer.pos(x2, y2, z2).endVertex();
        worldrenderer.pos(x2, y, z2).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }

    public static void otherDrawBoundingBox(Entity entity, float x, float y, float z, double width, double height) {
        float newYaw4;
        float newYaw3;
        float newYaw2;
        float newYaw1;
        width *= 1.5;
        float yaw1 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 45.0f;
        if (yaw1 < 0.0f) {
            newYaw1 = 0.0f;
            newYaw1 += 360.0f - Math.abs(yaw1);
        } else {
            newYaw1 = yaw1;
        }
        newYaw1 *= -1.0f;
        newYaw1 *= (float)Math.PI / 180;
        float yaw2 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 135.0f;
        if (yaw2 < 0.0f) {
            newYaw2 = 0.0f;
            newYaw2 += 360.0f - Math.abs(yaw2);
        } else {
            newYaw2 = yaw2;
        }
        newYaw2 *= -1.0f;
        newYaw2 *= (float)Math.PI / 180;
        float yaw3 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 225.0f;
        if (yaw3 < 0.0f) {
            newYaw3 = 0.0f;
            newYaw3 += 360.0f - Math.abs(yaw3);
        } else {
            newYaw3 = yaw3;
        }
        newYaw3 *= -1.0f;
        newYaw3 *= (float)Math.PI / 180;
        float yaw4 = MathHelper.wrapAngleTo180_float(entity.getRotationYawHead()) + 315.0f;
        if (yaw4 < 0.0f) {
            newYaw4 = 0.0f;
            newYaw4 += 360.0f - Math.abs(yaw4);
        } else {
            newYaw4 = yaw4;
        }
        newYaw4 *= -1.0f;
        newYaw4 *= (float)Math.PI / 180;
        float x2 = (float)(Math.sin(newYaw1) * width + (double)x);
        float z2 = (float)(Math.cos(newYaw1) * width + (double)z);
        float x3 = (float)(Math.sin(newYaw2) * width + (double)x);
        float z3 = (float)(Math.cos(newYaw2) * width + (double)z);
        float x4 = (float)(Math.sin(newYaw3) * width + (double)x);
        float z4 = (float)(Math.cos(newYaw3) * width + (double)z);
        float x5 = (float)(Math.sin(newYaw4) * width + (double)x);
        float z5 = (float)(Math.cos(newYaw4) * width + (double)z);
        float y2 = (float)((double)y + height);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x2, y, z2).endVertex();
        worldrenderer.pos(x2, y2, z2).endVertex();
        worldrenderer.pos(x3, y2, z3).endVertex();
        worldrenderer.pos(x3, y, z3).endVertex();
        worldrenderer.pos(x3, y, z3).endVertex();
        worldrenderer.pos(x3, y2, z3).endVertex();
        worldrenderer.pos(x4, y2, z4).endVertex();
        worldrenderer.pos(x4, y, z4).endVertex();
        worldrenderer.pos(x4, y, z4).endVertex();
        worldrenderer.pos(x4, y2, z4).endVertex();
        worldrenderer.pos(x5, y2, z5).endVertex();
        worldrenderer.pos(x5, y, z5).endVertex();
        worldrenderer.pos(x5, y, z5).endVertex();
        worldrenderer.pos(x5, y2, z5).endVertex();
        worldrenderer.pos(x2, y2, z2).endVertex();
        worldrenderer.pos(x2, y, z2).endVertex();
        worldrenderer.pos(x2, y, z2).endVertex();
        worldrenderer.pos(x3, y, z3).endVertex();
        worldrenderer.pos(x4, y, z4).endVertex();
        worldrenderer.pos(x5, y, z5).endVertex();
        worldrenderer.pos(x2, y2, z2).endVertex();
        worldrenderer.pos(x3, y2, z3).endVertex();
        worldrenderer.pos(x4, y2, z4).endVertex();
        worldrenderer.pos(x5, y2, z5).endVertex();
        worldrenderer.endVertex();
        tessellator.draw();
    }
}

