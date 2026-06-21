/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Tracers", category=ModuleCategory.VISUALS)
public class TracersModule
extends Module {
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        for (EntityPlayer player : Util.mc.theWorld.playerEntities.stream().toList()) {
            if (!player.isEntityAlive() || player == Util.mc.thePlayer || player.isInvisible()) continue;
            double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)Util.mc.timer.renderPartialTicks - Util.mc.getRenderManager().renderPosX;
            double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)Util.mc.timer.renderPartialTicks - Util.mc.getRenderManager().renderPosY;
            double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)Util.mc.timer.renderPartialTicks - Util.mc.getRenderManager().renderPosZ;
            boolean old = Util.mc.gameSettings.viewBobbing;
            GL11.glEnable(3042);
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(2848);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            Util.mc.entityRenderer.setupCameraTransform(Util.mc.timer.renderPartialTicks, 0);
            Util.mc.gameSettings.viewBobbing = false;
            Util.mc.entityRenderer.setupCameraTransform(Util.mc.timer.renderPartialTicks, 2);
            Util.mc.gameSettings.viewBobbing = old;
            double[] color = new double[]{1.0, 1.0, 1.0};
            RenderUtils.drawLine(player, color, posX, posY + (double)player.getEyeHeight(), posZ);
            GL11.glDisable(3042);
            GL11.glEnable(3553);
            GL11.glDisable(2848);
            GL11.glDisable(3042);
            GL11.glEnable(2929);
            GlStateManager.disableBlend();
        }
    };
}

