/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render.shaders;

import cc.advantage.utils.Util;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

public class StencilUtils
extends Util {
    private static void setupFBO(Framebuffer fbo) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
        int stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT();
        int stencil_texture_buffer_ID = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, StencilUtils.mc.displayWidth, StencilUtils.mc.displayHeight);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencil_depth_buffer_ID);
        EXTFramebufferObject.glFramebufferTexture2DEXT(36160, 36064, 36161, stencil_texture_buffer_ID, 0);
    }

    public static void checkSetupFBO(Framebuffer framebuffer) {
        if (framebuffer == null) {
            return;
        }
        if (framebuffer.depthBuffer > -1) {
            StencilUtils.setupFBO(framebuffer);
            framebuffer.depthBuffer = -1;
        }
    }

    public static void initStencilToWrite() {
        mc.getFramebuffer().bindFramebuffer(false);
        StencilUtils.checkSetupFBO(mc.getFramebuffer());
        GL11.glClear(1024);
        GL11.glEnable(2960);
        GL11.glStencilFunc(519, 1, 1);
        GL11.glStencilOp(7681, 7681, 7681);
        GL11.glColorMask(false, false, false, false);
    }

    public static void readStencilBuffer(int ref) {
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(514, ref, 1);
        GL11.glStencilOp(7680, 7680, 7680);
    }

    public static void uninitStencilBuffer() {
        GL11.glDisable(2960);
        GlStateManager.bindTexture(0);
    }
}

