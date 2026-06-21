/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class TeleportEvent
extends CancellableEvent {
    private C03PacketPlayer response;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;

    @Generated
    public C03PacketPlayer getResponse() {
        return this.response;
    }

    @Generated
    public double getPosX() {
        return this.posX;
    }

    @Generated
    public double getPosY() {
        return this.posY;
    }

    @Generated
    public double getPosZ() {
        return this.posZ;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public void setResponse(C03PacketPlayer response) {
        this.response = response;
    }

    @Generated
    public void setPosX(double posX) {
        this.posX = posX;
    }

    @Generated
    public void setPosY(double posY) {
        this.posY = posY;
    }

    @Generated
    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    @Generated
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Generated
    public TeleportEvent(C03PacketPlayer response, double posX, double posY, double posZ, float yaw, float pitch) {
        this.response = response;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
