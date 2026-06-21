/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.DragUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

@ModuleInfo(label="Radar", category=ModuleCategory.VISUALS)
public class RadarModule
extends Module {
    private final NumberProperty scale = new NumberProperty("Scale", 2.0, 0.1, 5.0, 0.1);
    private final NumberProperty size = new NumberProperty("Size", 125.0, 50.0, 500.0, 5.0);
    private static boolean positionInitialized = false;
    private static final int DEFAULT_SIZE = 125;
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> this.renderRadar();
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> this.renderRadar();

    private void initializePosition(ScaledResolution sr) {
        if (!positionInitialized && !DragUtils.components.containsKey("Radar")) {
            DragUtils.components.put("Radar", new DragUtils.DraggableComponent(125.0, (double)sr.getScaledHeight() / 2.0 - 62.5));
            positionInitialized = true;
        }
    }

    private void renderRadar() {
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr);
        DragUtils.DraggableComponent draggableComponent = DragUtils.components.get("Radar");
        int radarSize = ((Double)this.size.getValue()).intValue();
        draggableComponent.setWidth(radarSize);
        draggableComponent.setHeight(radarSize);
        float x = (float)draggableComponent.getX();
        float y = (float)draggableComponent.getY();
        float pTicks = Util.mc.timer.renderPartialTicks;
        double playerOffsetX = Util.mc.thePlayer.posX + (Util.mc.thePlayer.posX - Util.mc.thePlayer.lastTickPosX) * (double)pTicks;
        double playerOffsetZ = Util.mc.thePlayer.posZ + (Util.mc.thePlayer.posZ - Util.mc.thePlayer.lastTickPosZ) * (double)pTicks;
        Color lightest = new Color(44, 44, 44, 20);
        Color accentColor = ColorProcess.getColor();
        Gui.drawRect((int)((double)x + 2.5), (int)((double)y + 2.5), (int)((double)(x + (float)radarSize) - 2.5), (int)((double)(y + (float)radarSize) - 2.5), lightest.getRGB());
        Gui.drawRect((int)(x + 3.0f), (int)((double)y + 3.5), (int)(x + (float)radarSize - 3.0f), (int)((double)(y + (float)radarSize) - 3.5), new Color(15, 20, 35, 200).getRGB());
        Gui.drawRect((int)((double)(x + (float)radarSize / 2.0f) - 0.5), (int)((double)y + 3.5), (int)((double)(x + (float)radarSize / 2.0f) + 0.5), (int)((double)(y + (float)radarSize) - 3.5), new Color(255, 255, 255, 80).getRGB());
        Gui.drawRect((int)((double)x + 3.5), (int)((double)(y + (float)radarSize / 2.0f) - 0.5), (int)((double)(x + (float)radarSize) - 3.5), (int)((double)(y + (float)radarSize / 2.0f) + 0.5), new Color(255, 255, 255, 80).getRGB());
        Gui.drawRect((int)((double)x + 3.5), (int)((double)y + 3.5), (int)((double)(x + (float)radarSize) - 3.5), (int)((double)y + 4.5), accentColor.getRGB());
        Gui.drawRect((int)((double)x + 3.5), (int)(y + 4.0f), (int)((double)(x + (float)radarSize) - 3.5), (int)((double)y + 4.5), new Color(0, 0, 0, 110).getRGB());
        GlStateManager.pushMatrix();
        for (Entity o : Util.mc.theWorld.getLoadedEntityList()) {
            EntityPlayer entity;
            if (!(o instanceof EntityPlayer) || !(entity = (EntityPlayer)o).isEntityAlive() || entity == Util.mc.thePlayer || entity.isInvisible() || entity.isInvisibleToPlayer(Util.mc.thePlayer)) continue;
            float posX = (float)((entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)pTicks - playerOffsetX) * (Double)this.scale.getValue());
            float posZ = (float)((entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)pTicks - playerOffsetZ) * (Double)this.scale.getValue());
            Color entityColor = Util.mc.thePlayer.canEntityBeSeen(entity) ? new Color(255, 50, 50) : new Color(255, 50, 50, 150);
            float cos = (float)Math.cos((double)Util.mc.thePlayer.rotationYaw * (Math.PI / 180));
            float sin = (float)Math.sin((double)Util.mc.thePlayer.rotationYaw * (Math.PI / 180));
            float rotY = -(posZ * cos - posX * sin);
            float rotX = -(posX * cos + posZ * sin);
            float maxDistance = (float)radarSize / 2.0f - 5.0f;
            rotY = MathHelper.clamp_float(rotY, -maxDistance, maxDistance);
            rotX = MathHelper.clamp_float(rotX, -maxDistance, maxDistance);
            float markerX = x + (float)radarSize / 2.0f + rotX;
            float markerY = y + (float)radarSize / 2.0f + rotY;
            Gui.drawRect((int)((double)markerX - 1.5), (int)((double)markerY - 1.5), (int)((double)markerX + 1.5), (int)((double)markerY + 1.5), entityColor.getRGB());
        }
        GlStateManager.popMatrix();
        Gui.drawRect((int)(x + (float)radarSize / 2.0f - 2.0f), (int)(y + (float)radarSize / 2.0f - 2.0f), (int)(x + (float)radarSize / 2.0f + 2.0f), (int)(y + (float)radarSize / 2.0f + 2.0f), new Color(0, 255, 0).getRGB());
    }
}

