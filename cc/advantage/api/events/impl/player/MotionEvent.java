/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;

public final class MotionEvent
extends CancellableEvent {
    private final float prevYaw;
    private final float prevPitch;
    private final double prevPosX;
    private final double prevPosY;
    private final double prevPosZ;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
    private boolean ground;
    private boolean pre;
    private boolean rotating;

    public MotionEvent(float prevYaw, float prevPitch, double posX, double posY, double posZ, double prevPosX, double prevPosY, double prevPosZ, float yaw, float pitch, boolean ground) {
        this.prevYaw = prevYaw;
        this.prevPitch = prevPitch;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.prevPosX = prevPosX;
        this.prevPosY = prevPosY;
        this.prevPosZ = prevPosZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ground = ground;
        this.pre = true;
    }

    public double getPrevPosX() {
        return this.prevPosX;
    }

    public double getPrevPosY() {
        return this.prevPosY;
    }

    public double getPrevPosZ() {
        return this.prevPosZ;
    }

    public boolean isRotating() {
        return this.rotating;
    }

    public float getPrevYaw() {
        return this.prevYaw;
    }

    public float getPrevPitch() {
        return this.prevPitch;
    }

    public boolean isPre() {
        return this.pre;
    }

    public void setPost() {
        this.pre = false;
    }

    public double getPosX() {
        return this.posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return this.posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return this.posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public float getYaw() {
        return this.yaw;
    }

    public boolean hasMoved() {
        double xDif = this.prevPosX - this.posX;
        double yDif = this.prevPosY - this.posY;
        double zDif = this.prevPosZ - this.posZ;
        return Math.sqrt(xDif * xDif + yDif * yDif + zDif * zDif) > 1.0E-5;
    }

    public void setYaw(float yaw) {
        this.rotating = this.yaw - yaw != 0.0f;
        this.yaw = yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.rotating = this.pitch - pitch != 0.0f;
        this.pitch = pitch;
    }

    public boolean isOnGround() {
        return this.ground;
    }

    public void setOnGround(boolean ground) {
        this.ground = ground;
    }
}
