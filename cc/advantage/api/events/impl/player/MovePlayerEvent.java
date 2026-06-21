/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;

public final class MovePlayerEvent
extends CancellableEvent {
    private double x;
    private double y;
    private double z;

    @Generated
    public double getX() {
        return this.x;
    }

    @Generated
    public double getY() {
        return this.y;
    }

    @Generated
    public double getZ() {
        return this.z;
    }

    @Generated
    public void setX(double x) {
        this.x = x;
    }

    @Generated
    public void setY(double y) {
        this.y = y;
    }

    @Generated
    public void setZ(double z) {
        this.z = z;
    }

    @Generated
    public MovePlayerEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
