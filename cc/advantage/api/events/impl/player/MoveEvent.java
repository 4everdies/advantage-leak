/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.Event;
import lombok.Generated;

public class MoveEvent
implements Event {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private double sneakSlowDownMultiplier;

    @Generated
    public float getForward() {
        return this.forward;
    }

    @Generated
    public float getStrafe() {
        return this.strafe;
    }

    @Generated
    public boolean isJump() {
        return this.jump;
    }

    @Generated
    public boolean isSneak() {
        return this.sneak;
    }

    @Generated
    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
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
    public void setJump(boolean jump) {
        this.jump = jump;
    }

    @Generated
    public void setSneak(boolean sneak) {
        this.sneak = sneak;
    }

    @Generated
    public void setSneakSlowDownMultiplier(double sneakSlowDownMultiplier) {
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }

    @Generated
    public MoveEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }
}
