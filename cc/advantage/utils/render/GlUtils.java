/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class GlUtils {
    private static final FloatBuffer windowPosition = GLAllocation.createDirectFloatBuffer(4);
    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final float[] BUFFER = new float[3];
    public static int[] enabledCaps = new int[32];

    public static void enableDepth() {
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
    }

    public static void disableDepth() {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }

    public static void enableCaps(int ... caps) {
        for (int cap : caps) {
            GL11.glEnable(cap);
        }
        enabledCaps = caps;
    }

    public static void disableCaps() {
        for (int cap : enabledCaps) {
            GL11.glDisable(cap);
        }
    }

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void color(int color) {
        GL11.glColor4ub((byte)(color >> 16 & 0xFF), (byte)(color >> 8 & 0xFF), (byte)(color & 0xFF), (byte)(color >> 24 & 0xFF));
    }

    public static void setup2DRendering() {
        GlStateManager.enableTexture2D();
    }

    public static void end2DRendering() {
        GlStateManager.disableTexture2D();
    }

    public static float[] project2D(float x, float y, float z, int scaleFactor) {
        GL11.glGetFloatv(2982, modelMatrix);
        GL11.glGetFloatv(2983, projectionMatrix);
        GL11.glGetIntegerv(2978, viewport);
        if (GLU.gluProject(x, y, z, modelMatrix, projectionMatrix, viewport, windowPosition)) {
            GlUtils.BUFFER[0] = windowPosition.get(0) / (float)scaleFactor;
            GlUtils.BUFFER[1] = ((float)Display.getHeight() - windowPosition.get(1)) / (float)scaleFactor;
            GlUtils.BUFFER[2] = windowPosition.get(2);
            return BUFFER;
        }
        return null;
    }
}

