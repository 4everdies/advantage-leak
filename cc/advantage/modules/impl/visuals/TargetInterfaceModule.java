/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.DragUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;

@ModuleInfo(label="Target Interface", category=ModuleCategory.VISUALS)
public final class TargetInterfaceModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Advantage);
    private static boolean positionInitialized = false;
    public static EntityLivingBase target;
    private static final int HUD_WIDTH = 150;
    private static final int HUD_HEIGHT = 40;
    private static final int VANILLA_BAR_HEIGHT = 45;
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)mode.getValue()))).toString());
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 1: {
                this.drawAstolfoTargetInterface();
                break;
            }
            case 2: {
                this.drawMyauTargetInterface();
                break;
            }
            case 3: {
                this.drawRavenB4TargetInterface();
                break;
            }
            case 0: {
                this.drawAdvantageTargetInterface();
                break;
            }
            case 4: {
                this.drawExhibitionTargetInterface();
                break;
            }
            case 6: {
                this.drawRiseTargetInterface();
                break;
            }
            case 5: {
                this.drawBlueArchiveTargetInterface();
                break;
            }
            case 7: {
                this.drawNovolineTargetInterface();
                break;
            }
            case 9: {
                this.drawAdjustTargetInterface();
                break;
            }
            case 8: {
                this.drawAstralisTargetInterface();
            }
        }
    };
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> {
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 1: {
                this.drawAstolfoTargetInterface();
                break;
            }
            case 0: {
                this.drawAdvantageTargetInterface();
                break;
            }
            case 2: {
                this.drawMyauTargetInterface();
                break;
            }
            case 3: {
                this.drawRavenB4TargetInterface();
                break;
            }
            case 4: {
                this.drawExhibitionTargetInterface();
                break;
            }
            case 6: {
                this.drawRiseTargetInterface();
                break;
            }
            case 5: {
                this.drawBlueArchiveTargetInterface();
                break;
            }
            case 7: {
                this.drawNovolineTargetInterface();
                break;
            }
            case 9: {
                this.drawAdjustTargetInterface();
                break;
            }
            case 8: {
                this.drawAstralisTargetInterface();
            }
        }
    };

    private void initializePosition(ScaledResolution sr) {
        if (!positionInitialized && !DragUtils.components.containsKey("TargetInterface")) {
            DragUtils.components.put("TargetInterface", new DragUtils.DraggableComponent((double)(sr.getScaledWidth() - 150) / 2.0, sr.getScaledHeight() - 40 - 45 - 5));
            positionInitialized = true;
        }
    }

    private void drawAstolfoTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(40.0);
        draggableComponent.setWidth(150.0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(draggableComponent.getX(), draggableComponent.getY(), 0.0);
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPercentage = health / maxHealth;
        Color color = ColorProcess.getColor();
        Gui.drawRect(0, 0, 125, 36, new Color(0, 0, 0, 150).getRGB());
        Gui.drawRect(37, 26, 89, 32, new Color(0, 0, 0, 255).getRGB());
        int healthWidth = (int)(52.0f * healthPercentage);
        Gui.drawRect(37, 26, 37 + healthWidth, 32, color.getRGB());
        RenderUtils.resetColor();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GuiInventory.drawEntityOnScreen(15, 32, 16, -TargetInterfaceModule.target.rotationYaw, TargetInterfaceModule.target.rotationPitch, target);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
        Util.mc.fontRendererObj.drawString(target.getName(), 38.0f, 2.0f, -1, true);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        Util.mc.fontRendererObj.drawStringWithShadow(((float)Math.round(target.getHealth() * 10.0f) / 10.0f + "\u2764").replace(".0", ""), 19.0f, 5.0f, color.getRGB());
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private void drawRavenB4TargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(40.0);
        draggableComponent.setWidth(150.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        Object playerInfo = target.getDisplayName().getFormattedText();
        double health = target.getHealth() / target.getMaxHealth();
        if (TargetInterfaceModule.target.isDead) {
            health = 0.0;
        }
        playerInfo = (String)playerInfo + " " + String.format("%.1f\u2764", Float.valueOf(target.getHealth()));
        int padding = 8;
        int targetStrWithPadding = Util.mc.fontRendererObj.getStringWidth((String)playerInfo) + 8;
        int x1 = (int)x - 8;
        int y1 = (int)y - 8;
        int x2 = (int)x + targetStrWithPadding;
        int y2 = (int)y + (Util.mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + 8;
        draggableComponent.setWidth(Math.abs(x1 - x2));
        draggableComponent.setHeight(Math.abs(y1 - (y2 + 13)));
        Color mainColor = ColorProcess.getColor();
        Color brighterColor = mainColor.brighter();
        RenderUtils.drawRoundOutline(x1, y1, x2 - x1, y2 + 13 - y1, 10.0f, 0.5f, new Color(0, 0, 0, 110), ColorProcess.getColor());
        int barX1 = x1 + 6;
        int barX2 = x2 - 6;
        int barY = y2;
        RenderUtils.drawRoundedRect(barX1, barY, barX2 - barX1, 5.0f, 4.0f, new Color(0, 0, 0, 110));
        float healthBar = (float)((double)barX2 + (double)(barX1 - barX2) * (1.0 - health));
        RenderUtils.drawRoundedRect(barX1, barY, healthBar - (float)barX1, 5.0f, 4.0f, ColorProcess.getColor());
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        Util.mc.fontRendererObj.drawString((String)playerInfo, x, y, new Color(220, 220, 220, 255).getRGB(), true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawMyauTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(27.0);
        draggableComponent.setWidth(150.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        float health = target.getHealth() + target.getAbsorptionAmount();
        float maxHealth = target.getMaxHealth() + target.getAbsorptionAmount();
        float healthRatio = Math.min(Math.max(health / maxHealth, 0.0f), 1.0f);
        Color targetColor = ColorProcess.getColor();
        Color healthBarColor = this.interpolateHealthColor(healthRatio);
        String targetName = target.getName();
        int targetNameWidth = Util.mc.fontRendererObj.getStringWidth(targetName);
        float abs = target.getAbsorptionAmount() / 2.0f;
        String healthText = String.format("%.1f%s\u2764", Float.valueOf(health / 2.0f), abs > 0.0f ? "" : "");
        int healthTextWidth = Util.mc.fontRendererObj.getStringWidth(healthText);
        float headIconOffset = 25.0f;
        float barContentWidth = Math.max(targetNameWidth, healthTextWidth);
        float barTotalWidth = Math.max(headIconOffset + 70.0f, headIconOffset + 2.0f + barContentWidth + 2.0f);
        draggableComponent.setWidth((int)barTotalWidth);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        int backgroundColor = new Color(0, 0, 0, 150).getRGB();
        RenderUtils.drawRoundedRect(0.0f, 0.0f, barTotalWidth, 27.0f, 2.0f, new Color(backgroundColor));
        Gui.drawRect((int)(headIconOffset + 2.0f), 22, (int)(barTotalWidth - 2.0f), 25, new Color(0, 0, 0, 200).getRGB());
        float healthBarWidth = healthRatio * (barTotalWidth - 2.0f - headIconOffset - 2.0f);
        Gui.drawRect((int)(headIconOffset + 2.0f), 22, (int)(headIconOffset + 2.0f + healthBarWidth), 25, healthBarColor.getRGB());
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        if (target instanceof AbstractClientPlayer) {
            Util.mc.getTextureManager().bindTexture(((AbstractClientPlayer)target).getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(2, 2, 8.0f, 8.0f, 8, 8, 23, 23, 64.0f, 64.0f);
            if (((EntityPlayer)target).func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect(2, 2, 40.0f, 8.0f, 8, 8, 23, 23, 64.0f, 64.0f);
            }
        } else {
            Util.mc.getTextureManager().bindTexture(Util.mc.thePlayer.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(2, 2, 8.0f, 8.0f, 8, 8, 23, 23, 64.0f, 64.0f);
            if (Util.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect(2, 2, 40.0f, 8.0f, 8, 8, 23, 23, 64.0f, 64.0f);
            }
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
        Util.mc.fontRendererObj.drawString(targetName, headIconOffset + 2.0f, 2.0f, -1, true);
        Util.mc.fontRendererObj.drawString(healthText, headIconOffset + 2.0f, 12.0f, abs > 0.0f ? new Color(255, 170, 0).getRGB() : new Color(255, 85, 85).getRGB(), true);
        GlStateManager.popMatrix();
    }

    private Color interpolateHealthColor(float healthRatio) {
        if (healthRatio > 0.5f) {
            return RenderUtils.interpolateColorC(new Color(255, 255, 85), new Color(85, 255, 85), (healthRatio - 0.5f) / 0.5f);
        }
        return RenderUtils.interpolateColorC(new Color(255, 85, 85), new Color(255, 255, 85), healthRatio * 2.0f);
    }

    private void drawAdvantageTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(40.0);
        draggableComponent.setWidth(150.0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(draggableComponent.getX(), draggableComponent.getY(), 0.0);
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPercentage = health / maxHealth;
        Color color = Color.WHITE;
        Gui.drawRect(0, 0, 125, 36, new Color(0, 0, 0, 180).getRGB());
        Gui.drawRect(37, 26, 89, 32, new Color(0, 0, 0, 255).getRGB());
        int healthWidth = (int)(52.0f * healthPercentage);
        Gui.drawRect(37, 26, 37 + healthWidth, 32, ColorProcess.getColor().getRGB());
        RenderUtils.resetColor();
        if (target instanceof EntityPlayer) {
            this.renderPlayerSkin(target, 2, 2);
        } else {
            this.renderPlayerSkin(Util.mc.thePlayer, 2, 2);
        }
        FontUtils.getFont("bold").drawString(target.getName(), 38.0f, 2.0f, color.getRGB());
        FontUtils.getFont("bold").drawString(("" + (float)Math.round(target.getHealth() * 10.0f) / 10.0f).replace(".0", ""), 38.0f, 13.0f, color.getRGB());
        GlStateManager.popMatrix();
    }

    private void drawExhibitionTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(42.0);
        draggableComponent.setWidth(150.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        int MIN_WIDTH = 135;
        int HEIGHT = 42;
        if (target == null) {
            return;
        }
        String name = target.getName();
        int width = Math.max(135, Util.mc.fontRendererObj.getStringWidth("Name: " + name) + 60);
        Color darkest = new Color(10, 10, 10, 180);
        Color secondDarkest = new Color(22, 22, 22, 180);
        Color lightest = new Color(44, 44, 44, 180);
        Color middleColor = new Color(34, 34, 34, 180);
        Color textColor = Color.WHITE;
        Gui.drawRect((int)((double)x - 3.5), (int)((double)y - 3.5), (int)((double)(x + (float)width) + 3.5), (int)((double)(y + 42.0f) + 3.5), darkest.getRGB());
        Gui.drawRect((int)(x - 3.0f), (int)(y - 3.0f), (int)(x + (float)width + 3.0f), (int)(y + 42.0f + 3.0f), middleColor.getRGB());
        Gui.drawRect((int)(x - 1.0f), (int)(y - 1.0f), (int)(x + (float)width + 1.0f), (int)(y + 42.0f + 1.0f), lightest.getRGB());
        Gui.drawRect((int)x, (int)y, (int)(x + (float)width), (int)(y + 42.0f), secondDarkest.getRGB());
        float size = 36.0f;
        Gui.drawRect((int)(x + 3.0f), (int)(y + 3.0f), (int)((double)x + 3.5), (int)(y + 3.0f + size), lightest.getRGB());
        Gui.drawRect((int)(x + 3.0f), (int)(y + 3.0f + size), (int)(x + 3.0f + size), (int)((double)(y + 3.0f + size) + 0.5), lightest.getRGB());
        Gui.drawRect((int)(x + 3.0f + size), (int)(y + 3.0f), (int)((double)x + 3.5 + (double)size), (int)((double)(y + 3.0f + size) + 0.5), lightest.getRGB());
        Gui.drawRect((int)(x + 3.0f), (int)(y + 3.0f), (int)(x + 3.0f + size), (int)((double)y + 3.5), lightest.getRGB());
        FontUtils.getFont("bold").drawString(name, x + 8.0f + size, y + 6.0f, textColor.getRGB());
        float health = target.getHealth() + target.getAbsorptionAmount();
        float maxHealth = target.getMaxHealth() + target.getAbsorptionAmount();
        float healthValue = health / maxHealth;
        Color healthColor = healthValue > 0.5f ? RenderUtils.interpolateColorC(new Color(255, 255, 10), new Color(10, 255, 10), (healthValue - 0.5f) / 0.5f) : RenderUtils.interpolateColorC(new Color(255, 10, 10), new Color(255, 255, 10), healthValue * 2.0f);
        float healthBarWidth = (float)width - (size + 12.0f);
        Gui.drawRect((int)(x + 8.0f + size), (int)(y + 15.0f), (int)(x + 8.0f + size + healthBarWidth), (int)(y + 20.0f), darkest.getRGB());
        Gui.drawRect((int)((double)(x + 8.0f + size) + 0.5), (int)((double)y + 15.5), (int)((double)(x + 8.0f + size + healthBarWidth) - 0.5), (int)((double)y + 19.5), RenderUtils.blendColors(darkest, healthColor, 0.2f));
        float healthBarActualWidth = healthBarWidth - 1.0f;
        Gui.drawRect((int)((double)(x + 8.0f + size) + 0.5), (int)((double)y + 15.5), (int)((double)(x + 8.0f + size) + 0.5 + (double)(healthBarActualWidth * healthValue)), (int)((double)y + 19.5), healthColor.getRGB());
        float increment = healthBarActualWidth / 11.0f;
        for (int i = 1; i < 11; ++i) {
            Gui.drawRect((int)(x + 8.0f + size + increment * (float)i), (int)((double)y + 15.5), (int)((double)(x + 8.0f + size + increment * (float)i) + 0.5), (int)((double)y + 19.5), darkest.getRGB());
        }
        float distance = Util.mc.thePlayer.getDistanceToEntity(target);
        String statsText = String.format("HP: %.1f | Dist: %.1f", Float.valueOf(health), Float.valueOf(distance));
        FontUtils.getFont("advantage").drawString(statsText, x + 8.0f + size, y + 25.0f, textColor.getRGB());
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GuiInventory.drawEntityOnScreen((int)(x + 3.0f + size / 2.0f), (int)(y + size + 1.0f), 18, TargetInterfaceModule.target.rotationYaw, -TargetInterfaceModule.target.rotationPitch, target);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    private void drawBlueArchiveTargetInterface() {
        Color healthColorEnd;
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(55.0);
        draggableComponent.setWidth(180.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        float health = target.getHealth();
        float maxHealth = target.getMaxHealth();
        float healthPercentage = health / maxHealth;
        Color baBlue = new Color(41, 182, 246);
        Color baLightBlue = new Color(129, 212, 250);
        Color baWhite = new Color(255, 255, 255);
        Color baBackground = new Color(15, 20, 35, 200);
        Color baAccent = new Color(255, 213, 79);
        RenderUtils.drawRoundedRect(x, y, 180.0f, 55.0f, 4.0f, baBackground);
        Gui.drawRect((int)x, (int)y, (int)(x + 180.0f), (int)(y + 3.0f), baBlue.getRGB());
        Gui.drawRect((int)x, (int)(y + 3.0f), (int)(x + 2.0f), (int)(y + 55.0f), baLightBlue.getRGB());
        RenderUtils.drawRoundedRect(x + 5.0f, y + 8.0f, 40.0f, 40.0f, 3.0f, new Color(25, 35, 55, 220));
        RenderUtils.drawRoundedRect(x + 4.0f, y + 7.0f, 42.0f, 42.0f, 3.0f, new Color(baBlue.getRed(), baBlue.getGreen(), baBlue.getBlue(), 100));
        if (target instanceof EntityPlayer) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(((AbstractClientPlayer)target).getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + 7.0f), (int)(y + 10.0f), 8.0f, 8.0f, 8, 8, 36, 36, 64.0f, 64.0f);
            if (((EntityPlayer)target).func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + 7.0f), (int)(y + 10.0f), 40.0f, 8.0f, 8, 8, 36, 36, 64.0f, 64.0f);
            }
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(Util.mc.thePlayer.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + 7.0f), (int)(y + 10.0f), 8.0f, 8.0f, 8, 8, 36, 36, 64.0f, 64.0f);
            if (Util.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + 7.0f), (int)(y + 10.0f), 40.0f, 8.0f, 8, 8, 36, 36, 64.0f, 64.0f);
            }
            GlStateManager.popMatrix();
        }
        RenderUtils.drawRoundedRect(x + 50.0f, y + 8.0f, 123.0f, 18.0f, 2.0f, new Color(30, 40, 60, 180));
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        FontUtils.getFont("bold").drawString(target.getName(), x + 55.0f, y + 12.0f, baWhite.getRGB());
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        FontUtils.getFont("bold").drawString("HP", x + 55.0f, y + 25.0f, baAccent.getRGB());
        GlStateManager.popMatrix();
        RenderUtils.drawRoundedRect(x + 50.0f, y + 35.0f, 120.0f, 10.0f, 3.0f, new Color(20, 30, 50, 200));
        RenderUtils.drawRoundedRect(x + 51.0f, y + 36.0f, 118.0f, 8.0f, 2.0f, new Color(10, 15, 25, 150));
        float healthBarWidth = 116.0f * healthPercentage;
        Color healthColorStart = healthPercentage > 0.5f ? baLightBlue : new Color(255, 82, 82);
        Color color = healthColorEnd = healthPercentage > 0.5f ? baBlue : new Color(244, 67, 54);
        if (healthBarWidth > 0.0f) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            this.drawHorizontalGradient((int)(x + 52.0f), (int)(y + 37.0f), (int)(x + 52.0f + healthBarWidth), (int)(y + 43.0f), healthColorStart.getRGB(), healthColorEnd.getRGB());
            GlStateManager.popMatrix();
            Gui.drawRect((int)(x + 52.0f), (int)(y + 37.0f), (int)(x + 52.0f + healthBarWidth), (int)(y + 39.0f), new Color(255, 255, 255, 60).getRGB());
        }
        int segments = 10;
        float segmentWidth = 116.0f / (float)segments;
        for (int i = 1; i < segments; ++i) {
            Gui.drawRect((int)(x + 52.0f + segmentWidth * (float)i), (int)(y + 37.0f), (int)(x + 52.0f + segmentWidth * (float)i + 1.0f), (int)(y + 43.0f), new Color(20, 30, 50, 180).getRGB());
        }
        String healthText = String.format("%.1f / %.1f", Float.valueOf(health), Float.valueOf(maxHealth));
        int healthTextWidth = FontUtils.getFont("noto").getStringWidth(healthText);
        RenderUtils.drawRoundedRect(x + 110.0f - (float)healthTextWidth / 2.0f - 2.0f, y + 47.0f, healthTextWidth + 4, 7.0f, 2.0f, new Color(15, 20, 35, 200));
        GlStateManager.pushMatrix();
        FontUtils.getFont("noto").drawString(healthText, x + 110.0f - (float)healthTextWidth / 2.0f, y + 48.0f, baWhite.getRGB());
        GlStateManager.popMatrix();
        float distance = Util.mc.thePlayer.getDistanceToEntity(target);
        String distText = String.format("%.1fm", Float.valueOf(distance));
        RenderUtils.drawRoundedRect(x + 148.0f, y + 10.0f, 25.0f, 10.0f, 2.0f, new Color(30, 40, 60, 180));
        GlStateManager.pushMatrix();
        FontUtils.getFont("noto").drawString(distText, x + 152.0f, y + 12.0f, baLightBlue.getRGB());
        GlStateManager.popMatrix();
        if (TargetInterfaceModule.target.hurtTime > 0) {
            float hurtAlpha = (float)TargetInterfaceModule.target.hurtTime / 10.0f * 0.3f;
            RenderUtils.drawRoundedRect(x, y, 180.0f, 55.0f, 4.0f, new Color(255, 50, 50, (int)(hurtAlpha * 255.0f)));
        }
        GlStateManager.resetColor();
    }

    private void drawHorizontalGradient(int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float startRed = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float startGreen = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float startBlue = (float)(startColor & 0xFF) / 255.0f;
        float endAlpha = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float endRed = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float endGreen = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float endBlue = (float)(endColor & 0xFF) / 255.0f;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0.0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        worldrenderer.pos(left, top, 0.0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos(left, bottom, 0.0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos(right, bottom, 0.0).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void drawRiseTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(50.0);
        draggableComponent.setWidth(150.0);
        RiseTargetHUD.render(draggableComponent.getX(), draggableComponent.getY(), target);
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

    private void drawNovolineTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(40.0);
        draggableComponent.setWidth(150.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        int headSize = 24;
        int maxL = 100;
        String name = target.getName();
        Color backgroundColor = new Color(45, 45, 45);
        Color backgroundColor2 = new Color(21, 21, 21);
        Gui.drawRect((int)(x - 1.0f), (int)(y - 1.0f), (int)(x + 2.0f + (float)headSize + (float)maxL + 1.0f), (int)(y + 2.0f + (float)headSize + 2.0f + 1.0f), backgroundColor2.getRGB());
        Gui.drawRect((int)x, (int)y, (int)(x + 2.0f + (float)headSize + (float)maxL), (int)(y + 2.0f + (float)headSize + 2.0f), backgroundColor.getRGB());
        if (target instanceof AbstractClientPlayer) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(((AbstractClientPlayer)target).getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + 2.0f), (int)(y + 2.0f), 8.0f, 8.0f, 8, 8, headSize, headSize, 64.0f, 64.0f);
            if (((EntityPlayer)target).func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + 2.0f), (int)(y + 2.0f), 40.0f, 8.0f, 8, 8, headSize, headSize, 64.0f, 64.0f);
            }
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(Util.mc.thePlayer.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + 2.0f), (int)(y + 2.0f), 8.0f, 8.0f, 8, 8, headSize, headSize, 64.0f, 64.0f);
            if (Util.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + 2.0f), (int)(y + 2.0f), 40.0f, 8.0f, 8, 8, headSize, headSize, 64.0f, 64.0f);
            }
            GlStateManager.popMatrix();
        }
        Util.mc.fontRendererObj.drawString(name, (int)(x + 2.0f + (float)headSize + 2.0f), (int)(y + 4.0f), -1, true);
        int healthBarStartX = (int)(x + 2.0f + (float)headSize + 2.0f);
        int healthBarStartY = (int)(y + 4.0f + (float)Util.mc.fontRendererObj.FONT_HEIGHT + 2.0f);
        int healthBarEndX = (int)(x + 2.0f + (float)headSize + (float)maxL - 2.0f);
        int healthBarEndY = healthBarStartY + Util.mc.fontRendererObj.FONT_HEIGHT + 2;
        double health = target.getHealth();
        double maxHealth = target.getMaxHealth();
        double healthPercentage = health / maxHealth;
        String healthPercentageText = String.format("%.1f%%", healthPercentage * 100.0);
        Gui.drawRect(healthBarStartX, healthBarStartY, healthBarEndX, healthBarEndY, backgroundColor.darker().getRGB());
        int filledHealthBarEndX = (int)((double)healthBarStartX + (double)(healthBarEndX - healthBarStartX) * healthPercentage);
        Gui.drawRect(healthBarStartX, healthBarStartY, filledHealthBarEndX, healthBarEndY, ColorProcess.getColor().getRGB());
        int textWidth = Util.mc.fontRendererObj.getStringWidth(healthPercentageText);
        int textHeight = Util.mc.fontRendererObj.FONT_HEIGHT;
        double rectCenterX = (double)(healthBarStartX + healthBarEndX) / 2.0;
        double rectCenterY = (double)(healthBarStartY + healthBarEndY) / 2.0;
        double textX = rectCenterX - (double)textWidth / 2.0;
        double textY = rectCenterY - (double)textHeight / 2.0;
        Util.mc.fontRendererObj.drawString(healthPercentageText, (int)textX, (int)textY + 1, -1, true);
        draggableComponent.setWidth(2 + headSize + maxL);
        draggableComponent.setHeight(2 + headSize + 2);
    }

    private void drawAstralisTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(40.0);
        draggableComponent.setWidth(140.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        float width = 140.0f;
        float height = 40.0f;
        float padding = 6.0f;
        float faceSize = height - padding * 2.0f;
        float health = Math.max(0.0f, target.getHealth());
        float maxHealth = Math.max(1.0f, target.getMaxHealth());
        float healthPercent = Math.min(health / maxHealth, 1.0f);
        float astralisHealthAnimation = 0.0f;
        astralisHealthAnimation += (healthPercent - astralisHealthAnimation) * 0.18f;
        Color accent = ColorProcess.getColor();
        Color background = new Color(5, 5, 10, 145);
        Color panel = new Color(30, 30, 35, 150);
        RenderUtils.drawRoundedRect(x, y, width, height, 8.0f, background);
        RenderUtils.drawRoundOutline(x, y, width, height, 8.0f, 0.5f, new Color(0, 0, 0, 0), new Color(255, 255, 255, 25));
        if (target instanceof AbstractClientPlayer) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(((AbstractClientPlayer)target).getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + padding), (int)(y + padding), 8.0f, 8.0f, 8, 8, (int)faceSize, (int)faceSize, 64.0f, 64.0f);
            if (((EntityPlayer)target).func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + padding), (int)(y + padding), 40.0f, 8.0f, 8, 8, (int)faceSize, (int)faceSize, 64.0f, 64.0f);
            }
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(Util.mc.thePlayer.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + padding), (int)(y + padding), 8.0f, 8.0f, 8, 8, (int)faceSize, (int)faceSize, 64.0f, 64.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
        float textX = x + faceSize + padding * 2.0f;
        float contentWidth = width - faceSize - padding * 3.0f;
        Object targetName = target.getName();
        if ((float)FontUtils.getFont("bold").getStringWidth((String)targetName) > contentWidth) {
            targetName = ((String)targetName).substring(0, Math.min(((String)targetName).length(), 12)) + "...";
        }
        FontUtils.getFont("bold").drawString((String)targetName, textX, y + padding, Color.WHITE.getRGB());
        String healthText = String.format("%.1f", Float.valueOf(health));
        FontUtils.getFont("advantage").drawString(healthText, textX, y + padding + 12.0f, new Color(220, 220, 230).getRGB());
        float barHeight = 6.0f;
        float barY = y + height - padding - barHeight;
        RenderUtils.drawRoundedRect(textX, barY, contentWidth, barHeight, barHeight / 2.0f, panel);
        float barWidth = contentWidth * Math.min(Math.max(astralisHealthAnimation, 0.0f), 1.0f);
        if (barWidth > 0.0f) {
            RenderUtils.drawRoundedRect(textX, barY, barWidth, barHeight, 3.0f, accent);
        }
        if (TargetInterfaceModule.target.hurtTime > 0) {
            RenderUtils.drawRoundedRect(x, y, width, height, 8.0f, new Color(255, 60, 60, TargetInterfaceModule.target.hurtTime * 8));
        }
        draggableComponent.setWidth((int)width);
        draggableComponent.setHeight((int)height);
    }

    private void drawAdjustTargetInterface() {
        target = KillAuraModule.target;
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        if (Util.mc.currentScreen instanceof GuiChat) {
            target = Util.mc.thePlayer;
        } else if (target == null) {
            return;
        }
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("TargetInterface");
        draggableComponent.setHeight(40.0);
        draggableComponent.setWidth(150.0);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        String name = target.getName();
        int maxL = 100;
        int totalWidth = 30 + maxL + 3;
        int totalHeight = 38;
        Gui.drawRect((int)x, (int)y, (int)(x + (float)totalWidth), (int)(y + (float)totalHeight), new Color(43, 43, 43, 200).getRGB());
        if (target instanceof AbstractClientPlayer) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(((AbstractClientPlayer)target).getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + 3.0f), (int)(y + 3.0f), 8.0f, 8.0f, 8, 8, 24, 24, 64.0f, 64.0f);
            if (((EntityPlayer)target).func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + 3.0f), (int)(y + 3.0f), 40.0f, 8.0f, 8, 8, 24, 24, 64.0f, 64.0f);
            }
            GlStateManager.popMatrix();
        } else {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            Util.mc.getTextureManager().bindTexture(Util.mc.thePlayer.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect((int)(x + 3.0f), (int)(y + 3.0f), 8.0f, 8.0f, 8, 8, 24, 24, 64.0f, 64.0f);
            if (Util.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)(x + 3.0f), (int)(y + 3.0f), 40.0f, 8.0f, 8, 8, 24, 24, 64.0f, 64.0f);
            }
            GlStateManager.popMatrix();
        }
        Color gray = Color.WHITE.darker();
        FontUtils.getFont("bold").drawString(name, x + 3.0f + 24.0f + 3.0f, y + 2.0f, gray.getRGB());
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i <= 3; ++i) {
            if (target.getCurrentArmor(i) == null) continue;
            Util.mc.getRenderItem().renderItemAndEffectIntoGUI(target.getCurrentArmor(i), (int)(x + 3.0f + 24.0f + 3.0f + (float)(i * 16)), (int)(y + 3.0f + (float)Util.mc.fontRendererObj.FONT_HEIGHT + 2.0f));
        }
        RenderHelper.disableStandardItemLighting();
        int healthBarStartX = (int)(x + 3.0f);
        int healthBarStartY = (int)(y + 3.0f + 24.0f + 3.0f);
        int healthBarEndX = (int)(x + 3.0f + 24.0f + 3.0f + (float)maxL);
        int healthBarEndY = (int)(y + 3.0f + 24.0f + 8.0f);
        Gui.drawRect(healthBarStartX, healthBarStartY, healthBarEndX, healthBarEndY, new Color(0, 0, 0, 80).getRGB());
        double health = target.getHealth();
        double maxHealth = target.getMaxHealth();
        double healthPercentage = health / maxHealth;
        int filledHealthBarEndX = (int)((double)healthBarStartX + (double)(healthBarEndX - healthBarStartX) * healthPercentage);
        Gui.drawRect(healthBarStartX, healthBarStartY, filledHealthBarEndX, healthBarEndY, ColorProcess.getColor().getRGB());
        RenderUtils.drawOutline(healthBarStartX, healthBarStartY, filledHealthBarEndX - healthBarStartX, healthBarEndY - healthBarStartY, 0.5f, Color.DARK_GRAY.getRGB());
        String healthDiffStr = String.format("%.1f", Float.valueOf(Math.abs(Util.mc.thePlayer.getHealth() - target.getHealth())));
        String healthDiff = Util.mc.thePlayer.getHealth() < target.getHealth() ? "-" + healthDiffStr : "+" + healthDiffStr;
        int healthDiffWidth = FontUtils.getFont("advantage").getStringWidth(healthDiff);
        int healthDiffHeight = FontUtils.getFont("advantage").getHeight();
        FontUtils.getFont("advantage").drawString(healthDiff, x + 3.0f + 24.0f + 3.0f + (float)maxL - (float)healthDiffWidth, y + 3.0f + 24.0f - 9.0f - (float)healthDiffHeight, gray.getRGB());
        draggableComponent.setWidth(totalWidth);
        draggableComponent.setHeight(totalHeight);
    }

    public static class RiseTargetHUD {
        private static final int MIN_WIDTH = 128;
        private static final int HEIGHT = 50;
        private static boolean sentParticles = false;
        private static final List<Particle> particles = new ArrayList<Particle>();
        private static long lastUpdate = System.currentTimeMillis();
        private static float animatedHealthBar = 0.0f;

        public static void render(double x, double y, EntityLivingBase target) {
            if (target == null) {
                return;
            }
            String name = target.getName();
            int width = Math.max(128, Util.mc.fontRendererObj.getStringWidth("Name: " + name) + 60);
            Color backgroundColor = new Color(0, 0, 0, 110);
            int textColor = Color.WHITE.getRGB();
            RenderUtils.drawRoundedRect(x, y, (double)width, 50.0, 6.0, backgroundColor);
            int scaleOffset = (int)((float)target.hurtTime * 0.35f);
            float health = target.getHealth() + target.getAbsorptionAmount();
            float maxHealth = target.getMaxHealth() + target.getAbsorptionAmount();
            float healthPercent = health / maxHealth;
            float targetHealthBar = (float)(width - 28) * healthPercent;
            animatedHealthBar = RiseTargetHUD.lerp(animatedHealthBar, targetHealthBar, 0.18f);
            GlStateManager.pushMatrix();
            Color color1 = ColorProcess.getColor();
            Color color2 = ColorProcess.getColor();
            RiseTargetHUD.drawGradientRect((int)(x + 5.0), (int)(y + 40.0), (int)(x + 5.0 + (double)animatedHealthBar), (int)(y + 45.0), color1.getRGB(), color2.getRGB());
            GlStateManager.popMatrix();
            for (Particle p : particles) {
                p.x = (float)(x + 20.0);
                p.y = (float)(y + 20.0);
                if (!(p.opacity > 4.0f)) continue;
                p.render2D();
            }
            if (target instanceof AbstractClientPlayer) {
                offset = -(target.hurtTime * 23);
                playerTint = new Color(255, (int)(255.0 + offset), (int)(255.0 + offset));
                GlStateManager.color((float)playerTint.getRed() / 255.0f, (float)playerTint.getGreen() / 255.0f, (float)playerTint.getBlue() / 255.0f, 1.0f);
                Util.mc.getTextureManager().bindTexture(((AbstractClientPlayer)target).getLocationSkin());
                Gui.drawScaledCustomSizeModalRect((int)(x + 5.0 + (double)((float)scaleOffset / 2.0f)), (int)(y + 5.0 + (double)((float)scaleOffset / 2.0f)), 8.0f, 8.0f, 8, 8, 30 - scaleOffset, 30 - scaleOffset, 64.0f, 64.0f);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            } else {
                offset = -(target.hurtTime * 23);
                playerTint = new Color(255, (int)(255.0 + offset), (int)(255.0 + offset));
                GlStateManager.color((float)playerTint.getRed() / 255.0f, (float)playerTint.getGreen() / 255.0f, (float)playerTint.getBlue() / 255.0f, 1.0f);
                Util.mc.getTextureManager().bindTexture(Util.mc.thePlayer.getLocationSkin());
                Gui.drawScaledCustomSizeModalRect((int)(x + 5.0 + (double)((float)scaleOffset / 2.0f)), (int)(y + 5.0 + (double)((float)scaleOffset / 2.0f)), 8.0f, 8.0f, 8, 8, 30 - scaleOffset, 30 - scaleOffset, 64.0f, 64.0f);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            }
            if (System.currentTimeMillis() - lastUpdate >= 16L) {
                for (int i = particles.size() - 1; i >= 0; --i) {
                    Particle p;
                    p = particles.get(i);
                    p.updatePosition();
                    if (!(p.opacity < 1.0f)) continue;
                    particles.remove(i);
                }
                lastUpdate = System.currentTimeMillis();
            }
            double healthNum = (double)Math.round((double)health * 10.0) / 10.0;
            FontUtils.getFont("advantage").drawString(String.valueOf(healthNum), (float)(x + (double)animatedHealthBar + 8.0), (float)(y + 38.0), textColor);
            FontUtils.getFont("bold").drawString("Name: " + name, (float)(x + 40.0), (float)(y + 10.0), textColor);
            float distance = Util.mc.thePlayer.getDistanceToEntity(target);
            String statsText = String.format("Distance: %.1f Hurt: %d", Float.valueOf(distance), target.hurtTime);
            FontUtils.getFont("advantage").drawString(statsText, (float)(x + 40.0), (float)(y + 22.0), textColor);
            if (target.hurtTime == 9 && !sentParticles) {
                for (int i = 0; i <= 15; ++i) {
                    Particle particle = new Particle();
                    particle.init((float)(x + 20.0), (float)(y + 20.0), (float)((Math.random() - 0.5) * 2.0 * 1.4), (float)((Math.random() - 0.5) * 2.0 * 1.4), (float)(Math.random() * 4.0), i % 2 == 0 ? color1 : color2);
                    particles.add(particle);
                }
                sentParticles = true;
            }
            if (target.hurtTime == 8) {
                sentParticles = false;
            }
        }

        private static float lerp(float current, float target, float speed) {
            return current + (target - current) * speed;
        }

        private static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
            Gui.drawRect(left, top, right, bottom, startColor);
        }

        public static class Particle {
            public float x;
            public float y;
            public float adjustedX;
            public float adjustedY;
            public float deltaX;
            public float deltaY;
            public float size;
            public float opacity;
            public Color color;

            public void render2D() {
                Color renderColor = new Color(this.color.getRed(), this.color.getGreen(), this.color.getBlue(), (int)this.opacity);
                RenderUtils.drawRoundedRect(this.x + this.adjustedX, this.y + this.adjustedY, this.size, this.size, this.size / 2.0f - 0.5f, renderColor);
            }

            public void updatePosition() {
                for (int i = 1; i <= 2; ++i) {
                    this.adjustedX += this.deltaX;
                    this.adjustedY += this.deltaY;
                    this.deltaY = (float)((double)this.deltaY * 0.97);
                    this.deltaX = (float)((double)this.deltaX * 0.97);
                    this.opacity -= 1.0f;
                    if (!(this.opacity < 1.0f)) continue;
                    this.opacity = 1.0f;
                }
            }

            public void init(float x, float y, float deltaX, float deltaY, float size, Color color) {
                this.x = x;
                this.y = y;
                this.deltaX = deltaX;
                this.deltaY = deltaY;
                this.size = size;
                this.opacity = 254.0f;
                this.color = color;
            }
        }
    }

    private static enum Mode {
        Advantage("Advantage"),
        Astolfo("Astolfo"),
        Myau("Myau"),
        RavenB4("Raven B4"),
        Exhibition("Exhibition"),
        BlueArchive("Blue Archive"),
        Rise("Rise"),
        Novoline("Novoline"),
        Astralis("Astralis"),
        Adjust("Adjust");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

