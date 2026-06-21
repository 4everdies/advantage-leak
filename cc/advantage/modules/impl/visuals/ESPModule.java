/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.client.AntiBotModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.ESPUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="ESP", category=ModuleCategory.VISUALS)
public final class ESPModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Corners);
    public static Property<Boolean> players = new Property<Boolean>("Only Players", true);
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = event -> {
        float red = (float)ColorProcess.getColor().getRed() / 255.0f;
        float green = (float)ColorProcess.getColor().getGreen() / 255.0f;
        float blue = (float)ColorProcess.getColor().getBlue() / 255.0f;
        for (Entity entity : Util.mc.theWorld.loadedEntityList) {
            if (!AntiBotModule.botList.contains(entity)) continue;
            return;
        }
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 1: {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                    if (!(players.getValue() != false ? entity instanceof EntityPlayer && !entity.equals(Util.mc.thePlayer) : entity != null && !entity.equals(Util.mc.thePlayer))) continue;
                    ESPUtils.drawEntityESP(entity, red, green, blue, 0.5f, 1.0f, 6.0f);
                }
                GL11.glDepthMask(true);
                GlStateManager.enableCull();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDisable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2848);
                break;
            }
            case 2: {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                    if (!(players.getValue() != false ? entity instanceof EntityPlayer && !entity.equals(Util.mc.thePlayer) : entity != null && !entity.equals(Util.mc.thePlayer))) continue;
                    ESPUtils.drawEntityESP(entity, red, green, blue, 0.5f, 1.0f, 0.0f);
                }
                GL11.glDepthMask(true);
                GlStateManager.enableCull();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDisable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2848);
                break;
            }
            case 3: {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                    if (!(players.getValue() != false ? entity instanceof EntityPlayer && !entity.equals(Util.mc.thePlayer) : entity != null && !entity.equals(Util.mc.thePlayer))) continue;
                    ESPUtils.drawOutlineEntityESP(entity, red, green, blue, 0.5f, 1.0f, 6.0f);
                }
                GL11.glDepthMask(true);
                GlStateManager.enableCull();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDisable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2848);
                break;
            }
            case 4: {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                    if (!(players.getValue() != false ? entity instanceof EntityPlayer && !entity.equals(Util.mc.thePlayer) : entity != null && !entity.equals(Util.mc.thePlayer))) continue;
                    ESPUtils.drawCornerESP(entity, red, green, blue);
                }
                GL11.glDepthMask(true);
                GlStateManager.enableCull();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDisable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2848);
                break;
            }
            case 5: {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glDisable(2929);
                GL11.glDisable(3553);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                    if (!(players.getValue() != false ? entity instanceof EntityPlayer && !entity.equals(Util.mc.thePlayer) : entity != null && !entity.equals(Util.mc.thePlayer))) continue;
                    ESPUtils.drawFake2DESP(entity, red, green, blue);
                }
                GL11.glDepthMask(true);
                GlStateManager.enableCull();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDisable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2848);
            }
        }
    };
    @EventLink
    public final Listener<Render2DEvent> render2DEventListener = event -> {
        if (this.mode.getValue() != Mode.GameSense || Util.mc.theWorld == null || Util.mc.thePlayer == null) {
            return;
        }
        GL11.glPushAttrib(1048575);
        GlStateManager.pushMatrix();
        try {
            for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                if (entity == null || entity.equals(Util.mc.thePlayer) || AntiBotModule.botList.contains(entity) || players.getValue().booleanValue() && !(entity instanceof EntityPlayer)) continue;
                ESPUtils.render2DESP(entity.getEntityBoundingBox().offset(-entity.posX, -entity.posY, -entity.posZ).offset(ESPUtils.interpolate(entity.lastTickPosX, entity.posX), ESPUtils.interpolate(entity.lastTickPosY, entity.posY), ESPUtils.interpolate(entity.lastTickPosZ, entity.posZ)).offset(-Util.mc.getRenderManager().viewerPosX, -Util.mc.getRenderManager().viewerPosY, -Util.mc.getRenderManager().viewerPosZ), ColorProcess.getColor(), 0.25f);
            }
        }
        finally {
            GlStateManager.resetColor();
            GlStateManager.popMatrix();
            GL11.glPopAttrib();
        }
    };

    public static enum Mode {
        GameSense,
        Full,
        Box,
        Outline,
        Corners,
        Flat;

    }
}

