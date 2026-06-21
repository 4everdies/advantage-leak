/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.processes.LagProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(label="Lag Range", category=ModuleCategory.COMBAT)
public final class LagRangeModule
extends Module {
    public static NumberProperty rangeProperty = new NumberProperty("Range", 3.5, 0.1, 6.0, 0.05);
    public static NumberProperty minDelayProperty = new NumberProperty("Min Delay", 50.0, 0.0, 5000.0, 10.0);
    public static NumberProperty maxDelayProperty = new NumberProperty("Max Delay", 200.0, 0.0, 5000.0, 10.0);
    private final Property<Boolean> teleports = new Property<Boolean>("Delay Teleports", true);
    private final Property<Boolean> velocity = new Property<Boolean>("Delay Velocity", true);
    private final Property<Boolean> entities = new Property<Boolean>("Delay Entity Movements", true);
    public static final Property<Boolean> displayProperty = new Property<Boolean>("Display", true);
    public static final Property<Boolean> onlyWithKillaura = new Property<Boolean>("Only With Killaura", true);
    public static final Property<Boolean> renderLagPos = new Property<Boolean>("Render Lag Pos", true);
    private boolean lagging = false;
    private int lagAmount = ((Double)maxDelayProperty.getValue()).intValue();
    @EventLink
    public Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        if (this.lagging) {
            this.disableLag();
        }
    };
    @EventLink
    public Listener<MotionEvent> motionEventListener = event -> {
        if (event.isPre()) {
            return;
        }
        if (onlyWithKillaura.getValue().booleanValue() && !Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled()) {
            if (this.lagging) {
                this.disableLag();
            }
            return;
        }
        if (TargetSelectionProcess.getTarget() == null) {
            if (this.lagging) {
                this.disableLag();
            }
            return;
        }
        if (!MovementUtils.isMoving()) {
            if (this.lagging) {
                this.disableLag();
            }
            return;
        }
        double distance = Util.mc.thePlayer.getDistanceToEntity(TargetSelectionProcess.getTarget());
        if (!(distance <= (Double)KillAuraModule.seekRange.getValue()) || !(distance > (Double)rangeProperty.getValue())) {
            if (this.lagging) {
                this.disableLag();
            }
            return;
        }
        this.lagAmount = (int)MathUtils.getRandom(((Double)minDelayProperty.getValue()).intValue(), ((Double)maxDelayProperty.getValue()).intValue());
        LagProcess.spoof(this.lagAmount, true, this.velocity.getValue(), this.teleports.getValue(), this.entities.getValue());
        this.lagging = true;
        this.setSuffix(String.format("%.1f | %dms", rangeProperty.getValue(), this.lagAmount));
    };
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = event -> {
        if (!displayProperty.getValue().booleanValue() || !this.lagging) {
            return;
        }
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        ScaledResolution sr = new ScaledResolution(Util.mc);
        String text = String.format("Lagging %dms", this.lagAmount);
        fr.drawStringWithShadow(text, (float)sr.getScaledWidth() / 2.0f - (float)fr.getStringWidth(text) / 2.0f, (float)sr.getScaledHeight() / 10.0f - (float)fr.FONT_HEIGHT, Color.YELLOW.getRGB());
    };
    @EventLink
    public Listener<Render3DEvent> render3DEventListener = event -> {
        if (!renderLagPos.getValue().booleanValue() || !this.lagging) {
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
        GlStateManager.color((float)ColorProcess.getColor().getRed() / 255.0f, (float)ColorProcess.getColor().getGreen() / 255.0f, (float)ColorProcess.getColor().getBlue() / 255.0f, (float)ColorProcess.getColor().getAlpha() / 255.0f);
        RenderUtils.drawBoundingBox(bb);
        GlStateManager.popMatrix();
        RenderUtils.stop3D();
    };

    private void disableLag() {
        LagProcess.dispatch();
        LagProcess.disable();
        this.lagging = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.lagging) {
            this.disableLag();
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }
}

