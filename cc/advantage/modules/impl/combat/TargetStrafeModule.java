/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.JumpEvent;
import cc.advantage.api.events.impl.player.StrafeEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.modules.impl.movement.FlightModule;
import cc.advantage.modules.impl.movement.SpeedModule;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.PlayerUtils;
import cc.advantage.utils.mc.RotationUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vector3d;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Target Strafe", category=ModuleCategory.COMBAT)
public final class TargetStrafeModule
extends Module {
    private final NumberProperty range = new NumberProperty("Range", 3.0, 0.5, 6.0, 0.1);
    private final NumberProperty speed = new NumberProperty("Strafe Speed", 1.0, 0.1, 2.0, 0.05);
    private final Property<Boolean> holdJump = new Property<Boolean>("Hold Jump", false);
    private final Property<Boolean> strictMode = new Property<Boolean>("Strict Mode", false);
    private final Property<Boolean> adaptiveRange = new Property<Boolean>("Adaptive Range", true);
    private final Property<Boolean> autoDirection = new Property<Boolean>("Auto Direction", true);
    private final Property<Boolean> renderPath = new Property<Boolean>("Render Path", true);
    private final NumberProperty pathPoints = new NumberProperty("Path Points", 32.0, () -> this.renderPath.getValue(), 8.0, 64.0, 4.0);
    private final Property<Boolean> renderTarget = new Property<Boolean>("Render Target", true);
    private float yaw;
    private Entity target;
    private boolean left;
    private boolean colliding;
    private boolean active;
    private final List<Vector3d> pathTrail = new ArrayList<Vector3d>();
    private int directionSwitchTicks = 0;
    @EventLink(value=3)
    public final Listener<JumpEvent> onJump = event -> {
        if (this.target != null && this.active) {
            event.setYaw(this.yaw);
        }
    };
    @EventLink(value=3)
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (this.target != null && this.active) {
            event.setYaw(this.yaw);
            if (this.strictMode.getValue().booleanValue()) {
                event.setForward(1.0f);
                event.setStrafe(0.0f);
            }
        }
    };
    @EventLink(value=3)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        double currentDist;
        EntityLivingBase auraTarget;
        boolean canStrafe;
        ScaffoldModule scaffold = Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class);
        KillAuraModule killaura = Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class);
        if (scaffold != null && scaffold.isEnabled()) {
            this.active = false;
            this.target = null;
            return;
        }
        if (killaura == null || !killaura.isEnabled()) {
            this.active = false;
            this.target = null;
            return;
        }
        SpeedModule speedModule = Advantage.INSTANCE.getModuleManager().getModule(SpeedModule.class);
        FlightModule flight = Advantage.INSTANCE.getModuleManager().getModule(FlightModule.class);
        boolean bl = canStrafe = flight != null && flight.isEnabled() || speedModule != null && speedModule.isEnabled();
        if (!this.strictMode.getValue().booleanValue()) {
            if (this.holdJump.getValue().booleanValue() && !Util.mc.gameSettings.keyBindJump.isKeyDown()) {
                this.active = false;
                this.target = null;
                return;
            }
            if (!canStrafe || !Util.mc.gameSettings.keyBindForward.isKeyDown()) {
                this.active = false;
                this.target = null;
                return;
            }
        }
        if ((auraTarget = KillAuraModule.target) == null) {
            this.active = false;
            this.target = null;
            return;
        }
        this.active = true;
        this.target = auraTarget;
        ++this.directionSwitchTicks;
        boolean shouldSwitchDirection = false;
        if (Util.mc.thePlayer.isCollidedHorizontally) {
            shouldSwitchDirection = true;
        }
        if (!this.strictMode.getValue().booleanValue() && !PlayerUtils.isBlockUnder(5.0, false)) {
            shouldSwitchDirection = true;
        }
        if (this.autoDirection.getValue().booleanValue() && this.directionSwitchTicks >= 40 && (currentDist = (double)Util.mc.thePlayer.getDistanceToEntity(this.target)) < (Double)this.range.getValue() - 0.5) {
            shouldSwitchDirection = true;
            this.directionSwitchTicks = 0;
        }
        if (shouldSwitchDirection && !this.colliding) {
            this.left = !this.left;
            this.colliding = true;
        } else if (!shouldSwitchDirection) {
            this.colliding = false;
        }
        double effectiveRange = (Double)this.range.getValue();
        if (this.adaptiveRange.getValue().booleanValue()) {
            double distance = Util.mc.thePlayer.getDistanceToEntity(this.target);
            effectiveRange = MathHelper.clamp_double(distance, (Double)this.range.getValue() * 0.5, (Double)this.range.getValue());
        }
        float baseYaw = RotationUtils.calculate(this.target).getX();
        float offset = 135 * (this.left ? -1 : 1);
        this.yaw = baseYaw + offset;
        double strafeRange = effectiveRange + Math.random() / 100.0;
        double posX = (double)(-MathHelper.sin((float)Math.toRadians(this.yaw))) * strafeRange + this.target.posX;
        double posZ = (double)MathHelper.cos((float)Math.toRadians(this.yaw)) * strafeRange + this.target.posZ;
        Util.mc.thePlayer.movementYaw = this.yaw = (this.yaw = RotationUtils.calculate(new Vector3d(posX, this.target.posY, posZ)).getX());
        if (this.strictMode.getValue().booleanValue()) {
            double currentSpeed = (Double)this.speed.getValue() * MovementUtils.getBaseMoveSpeed();
            MovementUtils.setSpeed(currentSpeed, this.yaw, 0.0, 1.0);
        }
        this.updatePathTrail();
    };
    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (!this.active || this.target == null) {
            return;
        }
        if (this.renderPath.getValue().booleanValue()) {
            this.renderStrafeCircle();
        }
        if (this.renderTarget.getValue().booleanValue()) {
            this.renderTargetIndicator();
        }
    };

    private void updatePathTrail() {
        if (!this.renderPath.getValue().booleanValue() || this.target == null) {
            return;
        }
        this.pathTrail.clear();
        int points = ((Double)this.pathPoints.getValue()).intValue();
        for (int i = 0; i < points; ++i) {
            float angle = (float)(Math.PI * 2 * (double)i / (double)points);
            float yawAngle = RotationUtils.calculate(this.target).getX() + (float)Math.toDegrees(angle);
            double x = (double)(-MathHelper.sin((float)Math.toRadians(yawAngle))) * (Double)this.range.getValue() + this.target.posX;
            double z = (double)MathHelper.cos((float)Math.toRadians(yawAngle)) * (Double)this.range.getValue() + this.target.posZ;
            this.pathTrail.add(new Vector3d(x, this.target.posY, z));
        }
    }

    private void renderStrafeCircle() {
        if (this.pathTrail.isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableDepth();
        GL11.glLineWidth(2.0f);
        GL11.glBegin(2);
        for (Vector3d point : this.pathTrail) {
            double x = point.getX() - Util.mc.getRenderManager().viewerPosX;
            double y = point.getY() - Util.mc.getRenderManager().viewerPosY;
            double z = point.getZ() - Util.mc.getRenderManager().viewerPosZ;
            Color color = new Color(0, 255, 255, 150);
            GL11.glColor4f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
            GL11.glVertex3d(x, y, z);
        }
        GL11.glEnd();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderTargetIndicator() {
        double x = this.target.posX - Util.mc.getRenderManager().viewerPosX;
        double y = this.target.posY - Util.mc.getRenderManager().viewerPosY;
        double z = this.target.posZ - Util.mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y + (double)this.target.height + 0.5, z);
        GlStateManager.rotate(-Util.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(Util.mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GL11.glLineWidth(2.0f);
        GL11.glBegin(1);
        GL11.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        GL11.glVertex3d(-0.2, 0.0, 0.0);
        GL11.glVertex3d(0.2, 0.0, 0.0);
        GL11.glVertex3d(0.0, -0.2, 0.0);
        GL11.glVertex3d(0.0, 0.2, 0.0);
        GL11.glEnd();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void onDisable() {
        this.active = false;
        this.target = null;
        this.pathTrail.clear();
        this.directionSwitchTicks = 0;
    }
}

