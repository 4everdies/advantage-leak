/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.game;

import cc.advantage.api.events.Event;
import lombok.Generated;
import net.minecraft.util.MovingObjectPosition;

public class MouseOverEvent
implements Event {
    private double range;
    private float expand;
    private MovingObjectPosition movingObjectPosition;

    public MouseOverEvent(double range, float expand) {
        this.range = range;
        this.expand = expand;
    }

    @Generated
    public double getRange() {
        return this.range;
    }

    @Generated
    public float getExpand() {
        return this.expand;
    }

    @Generated
    public MovingObjectPosition getMovingObjectPosition() {
        return this.movingObjectPosition;
    }

    @Generated
    public void setRange(double range) {
        this.range = range;
    }

    @Generated
    public void setExpand(float expand) {
        this.expand = expand;
    }

    @Generated
    public void setMovingObjectPosition(MovingObjectPosition movingObjectPosition) {
        this.movingObjectPosition = movingObjectPosition;
    }
}
