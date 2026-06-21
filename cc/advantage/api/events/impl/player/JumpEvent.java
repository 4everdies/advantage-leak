/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;

public class JumpEvent
extends CancellableEvent {
    private float jumpMotion;
    private float yaw;

    @Generated
    public float getJumpMotion() {
        return this.jumpMotion;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public void setJumpMotion(float jumpMotion) {
        this.jumpMotion = jumpMotion;
    }

    @Generated
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public JumpEvent(float jumpMotion, float yaw) {
        this.jumpMotion = jumpMotion;
        this.yaw = yaw;
    }
}
