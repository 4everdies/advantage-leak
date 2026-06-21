/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.shaders;

import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.shaders.ShaderUtils;
import cc.advantage.utils.render.shaders.StencilUtils;
import java.nio.FloatBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

public class Blur
extends Util {
    private static final ShaderUtils GAUSSIAN_BLUR_SHADER = new ShaderUtils("gaussianBlur");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, false);
    private static final int MAX_RADIUS = 128;
    private static final int CACHE_LIMIT = 16;
    private static final Map<Float, FloatBuffer> gaussianWeightCache = new LinkedHashMap<Float, FloatBuffer>(16, 0.75f, true){

        @Override
        protected boolean removeEldestEntry(Map.Entry<Float, FloatBuffer> eldest) {
            return this.size() > 16;
        }
    };

    private static FloatBuffer getGaussianWeights(float radius) {
        radius = Math.min(radius, 128.0f);
        return gaussianWeightCache.computeIfAbsent(Float.valueOf(radius), r -> {
            float weight;
            int i;
            FloatBuffer buffer = BufferUtils.createFloatBuffer(128);
            float sigma = r.floatValue() / 2.0f;
            float sum = 0.0f;
            for (i = 0; i < 128 && !((weight = MathUtils.calculateGaussianValue(i, sigma)) < 0.001f); ++i) {
                buffer.put(weight);
                sum += i == 0 ? weight : 2.0f * weight;
            }
            buffer.rewind();
            for (i = 0; i < buffer.limit(); ++i) {
                buffer.put(i, buffer.get(i) / sum);
            }
            return buffer;
        });
    }

    private static void setupUniforms(float dirX, float dirY, float radius, float strength) {
        GAUSSIAN_BLUR_SHADER.setUniformi("textureIn", 0);
        GAUSSIAN_BLUR_SHADER.setUniformf("texelSize", 1.0f / (float)Blur.mc.displayWidth, 1.0f / (float)Blur.mc.displayHeight);
        GAUSSIAN_BLUR_SHADER.setUniformf("direction", dirX, dirY);
        GAUSSIAN_BLUR_SHADER.setUniformf("radius", radius);
        GAUSSIAN_BLUR_SHADER.setUniformf("strength", strength);
        GL20.glUniform1fv(GAUSSIAN_BLUR_SHADER.getUniform("weights"), Blur.getGaussianWeights(radius));
    }

    public static void startBlur() {
        StencilUtils.initStencilToWrite();
    }

    public static void endBlur(float radius, float compression, float strength) {
        if (radius <= 0.0f || compression <= 0.0f) {
            StencilUtils.uninitStencilBuffer();
            return;
        }
        StencilUtils.readStencilBuffer(1);
        framebuffer = RenderUtils.createFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        Blur.applyBlurPass(compression, 0.0f, radius, strength, Blur.mc.getFramebuffer().framebufferTexture);
        framebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(false);
        Blur.applyBlurPass(0.0f, compression, radius, strength, Blur.framebuffer.framebufferTexture);
        StencilUtils.uninitStencilBuffer();
        RenderUtils.resetColor();
        GlStateManager.bindTexture(0);
    }

    private static void applyBlurPass(float dirX, float dirY, float radius, float strength, int texture) {
        GAUSSIAN_BLUR_SHADER.init();
        Blur.setupUniforms(dirX, dirY, radius, strength);
        GlStateManager.bindTexture(texture);
        ShaderUtils.drawQuads();
        GAUSSIAN_BLUR_SHADER.unload();
    }
}

