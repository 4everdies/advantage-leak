/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.processes.LagProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.GlUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(label="Fake Lag", category=ModuleCategory.CLIENT)
public class FakeLagModule
extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 200.0, 50.0, 2000.0, 5.0);
    private final Property<Boolean> teleports = new Property<Boolean>("Delay Teleports", true);
    private final Property<Boolean> velocity = new Property<Boolean>("Delay Velocity", true);
    private final Property<Boolean> entities = new Property<Boolean>("Delay Entity Movements", true);
    private final Property<Boolean> renderLagPos = new Property<Boolean>("Render Lag Pos", true);
    @EventLink
    public Listener<MotionEvent> motionEventListener = event -> {
        if (event.isPre()) {
            return;
        }
        LagProcess.spoof(((Double)this.delay.getValue()).intValue(), true, this.velocity.getValue(), this.teleports.getValue(), this.entities.getValue());
    };
    @EventLink
    public Listener<Render3DEvent> render3DEventListener = event -> {
        if (!this.renderLagPos.getValue().booleanValue()) {
            return;
        }
        if (!LagProcess.hasServerPosition) {
            return;
        }
        if (Util.mc.gameSettings.thirdPersonView == 0) {
            return;
        }
        double x = LagProcess.serverX - Util.mc.getRenderManager().viewerPosX;
        double y = LagProcess.serverY - Util.mc.getRenderManager().viewerPosY;
        double z = LagProcess.serverZ - Util.mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = new AxisAlignedBB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3);
        RenderUtils.start3D();
        GlStateManager.pushMatrix();
        Color color = ColorProcess.getColor();
        GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        RenderUtils.drawBoundingBox(bb);
        GlStateManager.popMatrix();
        RenderUtils.stop3D();
        GlUtils.resetColor();
    };
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = event -> {
        if (!this.renderLagPos.getValue().booleanValue()) {
            return;
        }
        if (!LagProcess.hasServerPosition) {
            return;
        }
    };
}

