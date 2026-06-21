/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.DragUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;

@ModuleInfo(label="Session Information", category=ModuleCategory.VISUALS)
public final class SessionInformationModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Advantage);
    private static boolean positionInitialized = false;
    private static final int LABEL_HEIGHT = 12;
    private static final int HUD_HEIGHT = 40;
    private static final int VANILLA_BAR_HEIGHT = 45;
    private static final int MIN_WIDTH = 125;
    private String currentServer;
    private long sessionStartTime = 0L;
    private long lastServerTime;
    @EventLink
    public Listener<WorldLoadEvent> worldLoadEventListener = e -> {
        if (this.sessionStartTime == 0L) {
            this.sessionStartTime = System.currentTimeMillis();
        }
        this.updateServerInfo();
    };
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)mode.getValue()))).toString());
        if (System.currentTimeMillis() - this.lastServerTime > 5000L) {
            this.updateServerInfo();
            this.lastServerTime = System.currentTimeMillis();
        }
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 0: {
                this.drawAdvantageSessionInfo();
                break;
            }
            case 1: {
                this.drawModernSessionInfo();
            }
        }
    };
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> {
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 0: {
                this.drawAdvantageSessionInfo();
                break;
            }
            case 1: {
                this.drawModernSessionInfo();
            }
        }
    };

    private void initializePosition(ScaledResolution sr, int width) {
        if (!positionInitialized && !DragUtils.components.containsKey("SessionInformation")) {
            DragUtils.components.put("SessionInformation", new DragUtils.DraggableComponent((double)(sr.getScaledWidth() - width) / 2.0, sr.getScaledHeight() - 40 - 12 - 45 - 5));
            positionInitialized = true;
        }
    }

    private void drawAdvantageSessionInfo() {
        ScaledResolution sr = new ScaledResolution(Util.mc);
        String playingOnText = "Playing on " + this.currentServer;
        String playTimeText = "Play Time: " + this.getFormattedPlayTime();
        int playingOnWidth = FontUtils.getFont("advantage").getStringWidth(playingOnText);
        int playTimeWidth = FontUtils.getFont("advantage").getStringWidth(playTimeText);
        int maxTextWidth = Math.max(playingOnWidth, playTimeWidth);
        int hudWidth = Math.max(125, 38 + maxTextWidth + 5);
        this.initializePosition(sr, hudWidth);
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("SessionInformation");
        draggableComponent.setHeight(52.0);
        draggableComponent.setWidth(hudWidth);
        GlStateManager.pushMatrix();
        GlStateManager.translate(draggableComponent.getX(), draggableComponent.getY(), 0.0);
        Color color = Color.WHITE;
        Gui.drawRect(0, 0, hudWidth, 12, new Color(0, 0, 0, 220).getRGB());
        String labelText = "Session Info";
        int labelTextWidth = FontUtils.getFont("advantage").getStringWidth(labelText);
        FontUtils.getFont("advantage").drawString(labelText, (hudWidth - labelTextWidth) / 2, 2.0f, color.getRGB());
        Gui.drawRect(0, 12, hudWidth, 48, new Color(0, 0, 0, 180).getRGB());
        RenderUtils.resetColor();
        this.renderPlayerSkin(Util.mc.thePlayer, 2, 14);
        FontUtils.getFont("bold").drawString(Util.mc.thePlayer.getName(), 38.0f, 14.0f, color.getRGB());
        FontUtils.getFont("advantage").drawString(playingOnText, 38.0f, 25.0f, color.getRGB());
        FontUtils.getFont("advantage").drawString(playTimeText, 38.0f, 35.0f, color.getRGB());
        RenderUtils.resetColor();
        GlStateManager.popMatrix();
    }

    private void drawModernSessionInfo() {
        ScaledResolution sr = new ScaledResolution(Util.mc);
        String playingOnText = "Playing on " + this.currentServer;
        String playTimeText = "Play Time: " + this.getFormattedPlayTime();
        int playingOnWidth = FontUtils.getFont("advantage").getStringWidth(playingOnText);
        int playTimeWidth = FontUtils.getFont("advantage").getStringWidth(playTimeText);
        int maxTextWidth = Math.max(playingOnWidth, playTimeWidth);
        int hudWidth = Math.max(125, 38 + maxTextWidth + 5);
        this.initializePosition(sr, hudWidth);
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("SessionInformation");
        draggableComponent.setHeight(52.0);
        draggableComponent.setWidth(hudWidth);
        GlStateManager.pushMatrix();
        GlStateManager.translate(draggableComponent.getX(), draggableComponent.getY(), 0.0);
        Color color = Color.WHITE;
        Color gradientStart = new Color(ColorProcess.getColor().getRed(), ColorProcess.getColor().getGreen(), ColorProcess.getColor().getBlue(), 200);
        RenderUtils.drawRoundedRect(0.0f, 0.0f, hudWidth, 48.0f, 6.0f, true, gradientStart);
        RenderUtils.drawRoundedRect(2.0f, 11.0f, hudWidth - 4, 1.0f, 0.5f, gradientStart.brighter());
        String labelText = "Session Info";
        int labelTextWidth = FontUtils.getFont("advantage").getStringWidth(labelText);
        FontUtils.getFont("advantage").drawString(labelText, (hudWidth - labelTextWidth) / 2, 2.0f, color.getRGB());
        RenderUtils.resetColor();
        this.renderPlayerSkin(Util.mc.thePlayer, 2, 14);
        FontUtils.getFont("bold").drawString(Util.mc.thePlayer.getName(), 38.0f, 14.0f, color.getRGB());
        FontUtils.getFont("advantage").drawString(playingOnText, 38.0f, 25.0f, new Color(180, 180, 180).getRGB());
        FontUtils.getFont("advantage").drawString(playTimeText, 38.0f, 35.0f, new Color(180, 180, 180).getRGB());
        RenderUtils.resetColor();
        GlStateManager.popMatrix();
    }

    private String getFormattedPlayTime() {
        if (this.sessionStartTime == 0L) {
            return "00:00:00";
        }
        long playTimeMillis = System.currentTimeMillis() - this.sessionStartTime;
        long seconds = playTimeMillis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        return String.format("%02d:%02d:%02d", hours, minutes %= 60L, seconds %= 60L);
    }

    private void updateServerInfo() {
        this.currentServer = Util.mc.getCurrentServerData() != null ? Util.mc.getCurrentServerData().serverIP : "Singleplayer";
    }

    private void renderPlayerSkin(EntityLivingBase player, int x, int y) {
        List<NetworkPlayerInfo> playerInfoList = GuiPlayerTabOverlay.field_175252_a.sortedCopy(Util.mc.thePlayer.sendQueue.getPlayerInfoMap());
        for (NetworkPlayerInfo info : playerInfoList) {
            if (Util.mc.theWorld.getPlayerEntityByUUID(info.getGameProfile().getId()) != player) continue;
            Util.mc.getTextureManager().bindTexture(info.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(x, y, 8.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
            if (!player.func_175148_a(EnumPlayerModelParts.HAT)) break;
            Gui.drawScaledCustomSizeModalRect(x, y, 40.0f, 8.0f, 8, 8, 32, 32, 64.0f, 64.0f);
            break;
        }
    }

    private static enum Mode {
        Advantage("Advantage"),
        Modern("Modern");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

