/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.processes;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.JumpEvent;
import cc.advantage.api.events.impl.player.LookEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.MoveEvent;
import cc.advantage.api.events.impl.player.StrafeEvent;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.RotationUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.function.Function;
import lombok.Generated;
import net.minecraft.util.MathHelper;
import org.lwjgl.util.vector.Vector2f;

public class RotationProcess {
    private static boolean active;
    private static boolean smoothed;
    public static Vector2f rotations;
    public static Vector2f lastRotations;
    public static Vector2f targetRotations;
    public static Vector2f lastServerRotations;
    private static double rotationSpeed;
    private static MovementFix correctMovement;
    private static Function<Vector2f, Boolean> raycast;
    private static float randomAngle;
    private static final Vector2f offset;
    private static float lastYawDelta;
    @EventLink(value=0)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (!active || rotations == null || lastRotations == null || targetRotations == null || lastServerRotations == null) {
            targetRotations = lastServerRotations = new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch);
            lastRotations = lastServerRotations;
            rotations = lastServerRotations;
        }
        if (active) {
            RotationProcess.smooth();
        }
    };
    @EventLink(value=1)
    public final Listener<MoveEvent> onMove = event -> {
        if (active && correctMovement == MovementFix.NORMAL && rotations != null) {
            float yaw = RotationProcess.rotations.x;
            MovementUtils.fixMovement(event, yaw);
        }
    };
    @EventLink(value=0)
    public final Listener<LookEvent> onLook = event -> {
        if (active && rotations != null) {
            event.setRotation(rotations);
        }
    };
    @EventLink(value=0)
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (active && (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) && rotations != null) {
            event.setYaw(RotationProcess.rotations.x);
        }
    };
    @EventLink(value=0)
    public final Listener<JumpEvent> onJump = event -> {
        if (active && (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) && rotations != null) {
            event.setYaw(RotationProcess.rotations.x);
        }
    };
    @EventLink(value=0)
    public final Listener<MotionEvent> onPreMotionEvent = event -> {
        if (!event.isPre()) {
            return;
        }
        if (active && rotations != null) {
            float yaw = RotationProcess.rotations.x;
            float pitch = RotationProcess.rotations.y;
            event.setYaw(yaw);
            event.setPitch(pitch);
            Util.mc.thePlayer.rotationYawHead = yaw;
            Util.mc.thePlayer.renderPitchHead = pitch;
            lastServerRotations = new Vector2f(yaw, pitch);
            if (Math.abs((RotationProcess.rotations.x - Util.mc.thePlayer.rotationYaw) % 360.0f) < 1.0f && Math.abs(RotationProcess.rotations.y - Util.mc.thePlayer.rotationPitch) < 1.0f) {
                active = false;
                this.correctDisabledRotations();
            }
            lastRotations = rotations;
        } else {
            lastRotations = new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch);
        }
        targetRotations = new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch);
        smoothed = false;
        lastYawDelta = 0.0f;
    };

    public static void setRotations(Vector2f rotations, double rotationSpeed, MovementFix correctMovement) {
        RotationProcess.setRotations(rotations, rotationSpeed, correctMovement, null);
    }

    public static void setRotations(Vector2f rotations, double rotationSpeed, MovementFix correctMovement, Function<Vector2f, Boolean> raycast) {
        targetRotations = rotations;
        RotationProcess.rotationSpeed = rotationSpeed * 36.0;
        RotationProcess.correctMovement = correctMovement;
        RotationProcess.raycast = raycast;
        if (!active) {
            lastRotations = new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch);
            lastYawDelta = 0.0f;
        }
        active = true;
        RotationProcess.smooth();
    }

    private void correctDisabledRotations() {
        Vector2f rotations = new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch);
        Vector2f fixedRotations = RotationUtils.resetRotation(RotationUtils.applySensitivityPatch(rotations, lastRotations));
        float yawDelta = MathHelper.wrapAngleTo180_float(fixedRotations.x - Util.mc.thePlayer.rotationYaw);
        Util.mc.thePlayer.rotationYaw += yawDelta;
        Util.mc.thePlayer.rotationPitch = fixedRotations.y;
    }

    public static void smooth() {
        if (!smoothed) {
            float targetYaw = RotationProcess.targetRotations.x;
            float targetPitch = RotationProcess.targetRotations.y;
            if (raycast != null && (Math.abs(targetYaw - RotationProcess.rotations.x) > 5.0f || Math.abs(targetPitch - RotationProcess.rotations.y) > 5.0f)) {
                Vector2f trueTargetRotations = new Vector2f(targetRotations.getX(), targetRotations.getY());
                double speed = Math.random() * Math.random() * Math.random() * 20.0;
                offset.setX((float)((double)offset.getX() + (double)(-MathHelper.sin((float)Math.toRadians(randomAngle += (float)((20.0 + (double)((float)(Math.random() - 0.5)) * (Math.random() * Math.random() * Math.random() * 360.0)) * (double)(Util.mc.thePlayer.ticksExisted / 10 % 2 == 0 ? -1 : 1))))) * speed));
                offset.setY((float)((double)offset.getY() + (double)MathHelper.cos((float)Math.toRadians(randomAngle)) * speed));
                if (!raycast.apply(new Vector2f(targetYaw += offset.getX(), targetPitch += offset.getY())).booleanValue()) {
                    randomAngle = (float)Math.toDegrees(Math.atan2(trueTargetRotations.getX() - targetYaw, targetPitch - trueTargetRotations.getY())) - 180.0f;
                    targetYaw -= offset.getX();
                    targetPitch -= offset.getY();
                    offset.setX((float)((double)offset.getX() + (double)(-MathHelper.sin((float)Math.toRadians(randomAngle))) * speed));
                    offset.setY((float)((double)offset.getY() + (double)MathHelper.cos((float)Math.toRadians(randomAngle)) * speed));
                    targetYaw += offset.getX();
                    targetPitch += offset.getY();
                }
                if (!raycast.apply(new Vector2f(targetYaw, targetPitch)).booleanValue()) {
                    offset.setX(0.0f);
                    offset.setY(0.0f);
                    targetYaw = (float)((double)RotationProcess.targetRotations.x + Math.random() * 2.0);
                    targetPitch = (float)((double)RotationProcess.targetRotations.y + Math.random() * 2.0);
                }
            }
            float yawDelta = MathHelper.wrapAngleTo180_float(targetYaw - RotationProcess.lastRotations.x);
            float maxDelta = 30.0f;
            if (Math.abs(lastYawDelta) < 30.0f && Math.abs(yawDelta) > 320.0f) {
                yawDelta = Math.signum(yawDelta) * maxDelta;
            }
            targetYaw = RotationProcess.lastRotations.x + yawDelta;
            rotations = RotationUtils.smooth(new Vector2f(targetYaw, targetPitch), rotationSpeed + Math.random());
            lastYawDelta = MathHelper.wrapAngleTo180_float(RotationProcess.rotations.x - RotationProcess.lastRotations.x);
            if (correctMovement == MovementFix.NORMAL || correctMovement == MovementFix.TRADITIONAL) {
                Util.mc.thePlayer.movementYaw = RotationProcess.rotations.x;
            }
            Util.mc.thePlayer.velocityYaw = RotationProcess.rotations.x;
        }
        smoothed = true;
        Util.mc.entityRenderer.getMouseOver(1.0f);
    }

    @Generated
    public static void setActive(boolean active) {
        RotationProcess.active = active;
    }

    @Generated
    public static void setSmoothed(boolean smoothed) {
        RotationProcess.smoothed = smoothed;
    }

    @Generated
    public static boolean isActive() {
        return active;
    }

    @Generated
    public static boolean isSmoothed() {
        return smoothed;
    }

    static {
        lastRotations = new Vector2f(0.0f, 0.0f);
        offset = new Vector2f(0.0f, 0.0f);
        lastYawDelta = 0.0f;
    }
}

