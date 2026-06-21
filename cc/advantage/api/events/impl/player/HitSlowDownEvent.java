/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;

public final class HitSlowDownEvent
extends CancellableEvent {
    public double slowDown;
    public boolean sprint;

    @Generated
    public double getSlowDown() {
        return this.slowDown;
    }

    @Generated
    public boolean isSprint() {
        return this.sprint;
    }

    @Generated
    public void setSlowDown(double slowDown) {
        this.slowDown = slowDown;
    }

    @Generated
    public void setSprint(boolean sprint) {
        this.sprint = sprint;
    }

    @Generated
    public HitSlowDownEvent(double slowDown, boolean sprint) {
        this.slowDown = slowDown;
        this.sprint = sprint;
    }
}
