/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.shaders;

import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.shaders.ShaderUtils;
import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public class Shadow
extends Util {
    private static final ShaderUtils bloomShader = new ShaderUtils("shadow");
    private static Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);
    private static final FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
    private static final int MAX_RADIUS = 128;
    private static final int CACHE_LIMIT = 16;
    private static final Map<Integer, float[]> gaussianCache = new LinkedHashMap<Integer, float[]>(16, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, float[]> eldest) {
            return this.size() > 16;
        }
    };

    public static void renderShadow(int sourceTexture, int radius, int offset, float strength) {
        if (radius < 0) {
            return;
        }
        bloomFramebuffer = RenderUtils.createFrameBuffer(bloomFramebuffer, true);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        GlStateManager.enableBlend();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(true);
        bloomShader.init();
        Shadow.setUniforms(radius, offset, 0, strength);
        GL13.glActiveTexture(33984);
        GL11.glBindTexture(3553, sourceTexture);
        ShaderUtils.drawQuads();
        mc.getFramebuffer().bindFramebuffer(true);
        Shadow.setUniforms(radius, 0, offset, strength);
        GL13.glActiveTexture(34000);
        GL11.glBindTexture(3553, sourceTexture);
        GL13.glActiveTexture(33984);
        GL11.glBindTexture(3553, Shadow.bloomFramebuffer.framebufferTexture);
        ShaderUtils.drawQuads();
        bloomShader.unload();
        GlStateManager.bindTexture(0);
    }

    private static void setUniforms(int radius, int dirX, int dirY, float strength) {
        float[] weights = Shadow.getGaussianWeights(radius);
        weightBuffer.clear();
        for (float w : weights) {
            weightBuffer.put(w);
        }
        weightBuffer.flip();
        bloomShader.setUniformi("inTexture", 0);
        bloomShader.setUniformi("textureToCheck", 16);
        bloomShader.setUniformf("texelSize", 1.0f / (float)Shadow.mc.displayWidth, 1.0f / (float)Shadow.mc.displayHeight);
        bloomShader.setUniformf("radius", radius);
        bloomShader.setUniformf("direction", dirX, dirY);
        bloomShader.setUniformf("strength", strength);
        GL20.glUniform1fv(bloomShader.getUniform("weights"), weightBuffer);
    }

    private static float[] getGaussianWeights(int radius) {
        radius = Math.min(radius, 127);
        return gaussianCache.computeIfAbsent(radius, r -> {
            float[] weights = new float[256];
            float sigma = (float)r.intValue() / 2.0f;
            float twoSigmaSq = 2.0f * sigma * sigma;
            float sum = 0.0f;
            for (int i = 0; i <= r; ++i) {
                float weight;
                weights[i] = weight = (float)Math.exp((float)(-(i * i)) / twoSigmaSq);
                sum += i == 0 ? weight : 2.0f * weight;
            }
            float inv = 1.0f / sum;
            int i = 0;
            while (i <= r) {
                int n = i++;
                weights[n] = weights[n] * inv;
            }
            return weights;
        });
    }
}

