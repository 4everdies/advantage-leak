/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Aim Assist", category=ModuleCategory.COMBAT)
public final class AimAssistModule
extends Module {
    private final NumberProperty searchRange = new NumberProperty("Search Range", 4.0, 1.0, 8.0, 0.1);
    private final Property<Boolean> onlyOnClick = new Property<Boolean>("Only On Click", true);
    private final NumberProperty resetTime = new NumberProperty("Reset Time", 500.0, () -> this.onlyOnClick.getValue(), 0.0, 1000.0, 1.0);
    private final Property<Boolean> teamCheck = new Property<Boolean>("Team Check", false);
    private final ModeProperty<RotationMode> rotationMode = new ModeProperty<RotationMode>("Rotation Mode", RotationMode.Server);
    private final NumberProperty horizontalSpeed = new NumberProperty("Horizontal Speed", 3.5, () -> this.rotationMode.getValue() == RotationMode.Player, 0.1, 10.0, 0.1);
    private final NumberProperty verticalSpeed = new NumberProperty("Vertical Speed", 3.0, () -> this.rotationMode.getValue() == RotationMode.Player, 0.1, 10.0, 0.1);
    private final NumberProperty maxAngle = new NumberProperty("Max Angle", 90.0, 10.0, 180.0, 1.0);
    private final Property<Boolean> smoothing = new Property<Boolean>("Smoothing", true);
    private EntityLivingBase target;
    private boolean angleCalled;
    private long lastClickTime;
    private float smoothYaw;
    private float smoothPitch;
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        float pitchDiff;
        TargetSelectionProcess.setSeekRange(((Double)this.searchRange.getValue()).floatValue());
        TargetSelectionProcess.setDontTargetTeams(this.teamCheck.getValue());
        this.angleCalled = true;
        if (this.onlyOnClick.getValue().booleanValue() && Mouse.isButtonDown(0) && this.angleCalled) {
            this.lastClickTime = System.currentTimeMillis();
        }
        this.target = this.onlyOnClick.getValue() == false || (double)(System.currentTimeMillis() - this.lastClickTime) <= (Double)this.resetTime.getValue() ? TargetSelectionProcess.getTarget() : null;
        if (this.target == null) {
            return;
        }
        float[] rotations = this.getRotationsToEntity(this.target);
        float yawDiff = MathHelper.wrapAngleTo180_float(rotations[0] - Util.mc.thePlayer.rotationYaw);
        float angleDist = (float)Math.sqrt(yawDiff * yawDiff + (pitchDiff = rotations[1] - Util.mc.thePlayer.rotationPitch) * pitchDiff);
        if (angleDist > ((Double)this.maxAngle.getValue()).floatValue()) {
            return;
        }
        if (this.rotationMode.getValue() == RotationMode.Player) {
            float smoothFactor = this.smoothing.getValue() != false ? 0.6f : 1.0f;
            float yawSpeed = ((Double)this.horizontalSpeed.getValue()).floatValue();
            float pitchSpeed = ((Double)this.verticalSpeed.getValue()).floatValue();
            this.smoothYaw = this.smoothYaw * smoothFactor + yawDiff * (1.0f - smoothFactor);
            this.smoothPitch = this.smoothPitch * smoothFactor + pitchDiff * (1.0f - smoothFactor);
            float yawChange = Math.signum(this.smoothYaw) * Math.min(Math.abs(this.smoothYaw), yawSpeed);
            float pitchChange = Math.signum(this.smoothPitch) * Math.min(Math.abs(this.smoothPitch), pitchSpeed);
            Util.mc.thePlayer.rotationYaw += yawChange;
            Util.mc.thePlayer.rotationPitch = MathHelper.clamp_float(Util.mc.thePlayer.rotationPitch + pitchChange, -90.0f, 90.0f);
        } else {
            RotationProcess.setRotations(new Vector2f(rotations[0], rotations[1]), 5.0, MovementFix.NORMAL);
        }
        this.angleCalled = false;
    };

    private float[] getRotationsToEntity(EntityLivingBase entity) {
        double x = entity.posX - Util.mc.thePlayer.posX;
        double y = entity.posY + (double)entity.getEyeHeight() - (Util.mc.thePlayer.posY + (double)Util.mc.thePlayer.getEyeHeight());
        double z = entity.posZ - Util.mc.thePlayer.posZ;
        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    @Override
    public void onEnable() {
        this.target = null;
        this.angleCalled = false;
        this.lastClickTime = 0L;
        this.smoothYaw = 0.0f;
        this.smoothPitch = 0.0f;
    }

    public static enum RotationMode {
        Server,
        Player;

    }
}

