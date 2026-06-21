/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.shaders;

import cc.advantage.utils.Util;
import cc.advantage.utils.render.GlUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.shaders.ShaderUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

public class Bloom {
    public static ShaderUtils kawaseDown = new ShaderUtils("kawaseDownBloom");
    public static ShaderUtils kawaseUp = new ShaderUtils("kawaseUpBloom");
    public static Framebuffer framebuffer = new Framebuffer(1, 1, true);
    private static int currentIterations;
    private static final List<Framebuffer> framebufferList;

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();
        framebuffer = RenderUtils.createFrameBuffer(null, true);
        framebufferList.add(framebuffer);
        int i = 1;
        while ((float)i <= iterations) {
            Framebuffer currentBuffer = new Framebuffer((int)((double)Util.mc.displayWidth / Math.pow(2.0, i)), (int)((double)Util.mc.displayHeight / Math.pow(2.0, i)), true);
            currentBuffer.setFramebufferFilter(9729);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(3553, 10242, 33648);
            GL11.glTexParameteri(3553, 10243, 33648);
            GlStateManager.bindTexture(0);
            framebufferList.add(currentBuffer);
            ++i;
        }
    }

    public static void renderBloom(int framebufferTexture, int iterations, int offset, float strength) {
        int i;
        if (currentIterations != iterations || Bloom.framebuffer.framebufferWidth != Util.mc.displayWidth || Bloom.framebuffer.framebufferHeight != Util.mc.displayHeight) {
            Bloom.initFramebuffers(iterations);
            currentIterations = iterations;
        }
        RenderUtils.setAlphaLimit(0.0f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(1, 1);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Bloom.renderFBO(framebufferList.get(1), framebufferTexture, kawaseDown, offset, strength);
        for (i = 1; i < iterations; ++i) {
            Bloom.renderFBO(framebufferList.get(i + 1), Bloom.framebufferList.get((int)i).framebufferTexture, kawaseDown, offset, strength);
        }
        for (i = iterations; i > 1; --i) {
            Bloom.renderFBO(framebufferList.get(i - 1), Bloom.framebufferList.get((int)i).framebufferTexture, kawaseUp, offset, strength);
        }
        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / (float)lastBuffer.framebufferWidth, 1.0f / (float)lastBuffer.framebufferHeight);
        kawaseUp.setUniformf("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);
        kawaseUp.setUniformf("strength", strength);
        GlStateManager.setActiveTexture(34000);
        RenderUtils.bindTexture(framebufferTexture);
        GlStateManager.setActiveTexture(33984);
        RenderUtils.bindTexture(Bloom.framebufferList.get((int)1).framebufferTexture);
        ShaderUtils.drawQuads();
        kawaseUp.unload();
        GlStateManager.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Util.mc.getFramebuffer().bindFramebuffer(false);
        RenderUtils.bindTexture(Bloom.framebufferList.get((int)0).framebufferTexture);
        RenderUtils.setAlphaLimit(0.0f);
        GlUtils.startBlend();
        ShaderUtils.drawQuads();
        GlStateManager.bindTexture(0);
        RenderUtils.setAlphaLimit(0.0f);
        GlUtils.startBlend();
    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtils shader, float offset, float strength) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.init();
        RenderUtils.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / (float)framebuffer.framebufferWidth, 1.0f / (float)framebuffer.framebufferHeight);
        shader.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        shader.setUniformf("strength", strength);
        ShaderUtils.drawQuads();
        shader.unload();
    }

    static {
        framebufferList = new ArrayList<Framebuffer>();
    }
}

