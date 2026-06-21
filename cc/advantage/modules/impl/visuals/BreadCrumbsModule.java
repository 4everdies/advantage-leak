/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Bread Crumbs", category=ModuleCategory.VISUALS)
public final class BreadCrumbsModule
extends Module {
    private final List<Vec3> path = new ArrayList<Vec3>();
    @EventLink
    private final Listener<MotionEvent> motionEventListener = event -> {
        if (Util.mc.thePlayer.lastTickPosX != Util.mc.thePlayer.posX || Util.mc.thePlayer.lastTickPosY != Util.mc.thePlayer.posY || Util.mc.thePlayer.lastTickPosZ != Util.mc.thePlayer.posZ) {
            this.path.add(new Vec3(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ));
        }
        while (this.path.size() > 40) {
            this.path.remove(0);
        }
    };
    @EventLink
    private final Listener<Render3DEvent> render3DEventListener = event -> this.renderLine(this.path);

    @Override
    public void onEnable() {
        this.path.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.path.clear();
        super.onDisable();
    }

    public void renderLine(List<Vec3> path) {
        GlStateManager.disableDepth();
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glBlendFunc(770, 771);
        int i = 0;
        try {
            for (Vec3 v : path) {
                ++i;
                boolean draw = true;
                double x = v.xCoord - Util.mc.getRenderManager().renderPosX;
                double y = v.yCoord - Util.mc.getRenderManager().renderPosY;
                double z = v.zCoord - Util.mc.getRenderManager().renderPosZ;
                double distanceFromPlayer = Util.mc.thePlayer.getDistance(v.xCoord, v.yCoord - 1.0, v.zCoord);
                int quality = (int)(distanceFromPlayer * 4.0 + 10.0);
                if (quality > 350) {
                    quality = 350;
                }
                if (i % 10 != 0 && distanceFromPlayer > 25.0) {
                    draw = false;
                }
                if (i % 3 == 0 && distanceFromPlayer > 15.0) {
                    draw = false;
                }
                if (!draw) continue;
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, z);
                float scale = 0.04f;
                GL11.glScalef(-0.04f, -0.04f, -0.04f);
                GL11.glRotated(-Util.mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
                GL11.glRotated(Util.mc.getRenderManager().playerViewX, 1.0, 0.0, 0.0);
                Color c = ColorProcess.getColor();
                RenderUtils.drawFilledCircleNoGL(0, 0, 0.7, c.hashCode(), quality);
                if (distanceFromPlayer < 4.0) {
                    RenderUtils.drawFilledCircleNoGL(0, 0, 1.4, new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).hashCode(), quality);
                }
                if (distanceFromPlayer < 20.0) {
                    RenderUtils.drawFilledCircleNoGL(0, 0, 2.3, new Color(c.getRed(), c.getGreen(), c.getBlue(), 30).hashCode(), quality);
                }
                GL11.glScalef(0.8f, 0.8f, 0.8f);
                GL11.glPopMatrix();
            }
        }
        catch (ConcurrentModificationException concurrentModificationException) {
            // empty catch block
        }
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GlStateManager.enableDepth();
        GL11.glColor3d(255.0, 255.0, 255.0);
    }
}

