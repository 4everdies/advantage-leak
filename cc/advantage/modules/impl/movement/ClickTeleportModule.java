/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.game.MiddleClickEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Click Teleport", category=ModuleCategory.MOVEMENT)
public final class ClickTeleportModule
extends Module {
    private MovingObjectPosition targetBlock = null;
    private final List<Vec3> pathPositions = new ArrayList<Vec3>();
    @EventLink
    public final Listener<MiddleClickEvent> onMiddleClick = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        MovingObjectPosition result = this.rayTraceBlock(1000.0);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Util.mc.theWorld.getBlockState(result.getBlockPos()).getBlock().getMaterial() != Material.air) {
            double x = (double)result.getBlockPos().getX() + 0.5;
            double y = (double)result.getBlockPos().getY() + 1.0;
            double z = (double)result.getBlockPos().getZ() + 0.5;
            Util.mc.thePlayer.setPosition(x, y, z);
            Util.mc.thePlayer.motionX = 0.0;
            Util.mc.thePlayer.motionY = 0.0;
            Util.mc.thePlayer.motionZ = 0.0;
            event.setCancelled();
        }
    };
    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        this.pathPositions.clear();
        this.targetBlock = this.rayTraceBlock(1000.0);
        if (this.targetBlock != null && this.targetBlock.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && Util.mc.theWorld.getBlockState(this.targetBlock.getBlockPos()).getBlock().getMaterial() != Material.air) {
            Vec3 start = Util.mc.thePlayer.getPositionVector().addVector(0.0, Util.mc.thePlayer.getEyeHeight(), 0.0);
            Vec3 end = new Vec3((double)this.targetBlock.getBlockPos().getX() + 0.5, (double)this.targetBlock.getBlockPos().getY() + 1.0, (double)this.targetBlock.getBlockPos().getZ() + 0.5);
            int segments = 20;
            for (int i = 0; i <= segments; ++i) {
                double t = (double)i / (double)segments;
                this.pathPositions.add(new Vec3(start.xCoord + (end.xCoord - start.xCoord) * t, start.yCoord + (end.yCoord - start.yCoord) * t, start.zCoord + (end.zCoord - start.zCoord) * t));
            }
            this.renderPath();
        }
    };

    private MovingObjectPosition rayTraceBlock(double range) {
        Vec3 eyePos = Util.mc.thePlayer.getPositionEyes(Util.mc.timer.renderPartialTicks);
        Vec3 lookVec = Util.mc.thePlayer.getLook(Util.mc.timer.renderPartialTicks);
        Vec3 endPos = eyePos.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);
        return Util.mc.theWorld.rayTraceBlocks(eyePos, endPos, false, false, false);
    }

    private void renderPath() {
        if (this.pathPositions.size() < 2) {
            return;
        }
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GlStateManager.disableCull();
        GL11.glDepthMask(false);
        GL11.glLineWidth(3.0f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        for (Vec3 pos : this.pathPositions) {
            double renderX = pos.xCoord - Util.mc.getRenderManager().renderPosX;
            double renderY = pos.yCoord - Util.mc.getRenderManager().renderPosY;
            double renderZ = pos.zCoord - Util.mc.getRenderManager().renderPosZ;
            worldRenderer.pos(renderX, renderY, renderZ).color((float)ColorProcess.getColor().getRed() / 255.0f, (float)ColorProcess.getColor().getGreen() / 255.0f, (float)ColorProcess.getColor().getBlue() / 255.0f, 0.8f).endVertex();
        }
        tessellator.draw();
        if (this.targetBlock != null) {
            double x = (double)this.targetBlock.getBlockPos().getX() - Util.mc.getRenderManager().renderPosX;
            double y = (double)this.targetBlock.getBlockPos().getY() - Util.mc.getRenderManager().renderPosY;
            double z = (double)this.targetBlock.getBlockPos().getZ() - Util.mc.getRenderManager().renderPosZ;
            GL11.glLineWidth(2.0f);
            worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
            float r = (float)ColorProcess.getColor().getRed() / 255.0f;
            float g = (float)ColorProcess.getColor().getGreen() / 255.0f;
            float b = (float)ColorProcess.getColor().getBlue() / 255.0f;
            worldRenderer.pos(x, y, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y + 1.0, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y + 1.0, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y + 1.0, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y + 1.0, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y + 1.0, z).color(r, g, b, 0.6f).endVertex();
            tessellator.draw();
            worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(x, y, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y + 1.0, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y + 1.0, z).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x + 1.0, y + 1.0, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y, z + 1.0).color(r, g, b, 0.6f).endVertex();
            worldRenderer.pos(x, y + 1.0, z + 1.0).color(r, g, b, 0.6f).endVertex();
            tessellator.draw();
        }
        GL11.glLineWidth(1.0f);
        GL11.glDepthMask(true);
        GlStateManager.enableCull();
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
    }
}

