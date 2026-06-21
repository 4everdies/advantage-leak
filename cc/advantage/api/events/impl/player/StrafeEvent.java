/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import lombok.Generated;

public final class StrafeEvent
extends CancellableEvent {
    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public void setSpeed(double speed, double motionMultiplier) {
        this.setFriction((float)(this.getForward() != 0.0f && this.getStrafe() != 0.0f ? speed * (double)0.98f : speed));
        Util.mc.thePlayer.motionX *= motionMultiplier;
        Util.mc.thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(double speed) {
        this.setFriction((float)(this.getForward() != 0.0f && this.getStrafe() != 0.0f ? speed * (double)0.98f : speed));
        MovementUtils.stop();
    }

    @Generated
    public float getForward() {
        return this.forward;
    }

    @Generated
    public float getStrafe() {
        return this.strafe;
    }

    @Generated
    public float getFriction() {
        return this.friction;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public void setForward(float forward) {
        this.forward = forward;
    }

    @Generated
    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    @Generated
    public void setFriction(float friction) {
        this.friction = friction;
    }

    @Generated
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public StrafeEvent(float forward, float strafe, float friction, float yaw) {
        this.forward = forward;
        this.strafe = strafe;
        this.friction = friction;
        this.yaw = yaw;
    }
}
