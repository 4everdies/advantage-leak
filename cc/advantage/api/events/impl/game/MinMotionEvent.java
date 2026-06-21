/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.game;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;

public class MinMotionEvent
extends CancellableEvent {
    private double minimumMotion;

    @Generated
    public double getMinimumMotion() {
        return this.minimumMotion;
    }

    @Generated
    public void setMinimumMotion(double minimumMotion) {
        this.minimumMotion = minimumMotion;
    }

    @Generated
    public MinMotionEvent(double minimumMotion) {
        this.minimumMotion = minimumMotion;
    }
}
