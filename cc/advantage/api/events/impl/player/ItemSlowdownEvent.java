/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;

public class ItemSlowdownEvent
extends CancellableEvent {
    private float strafeMultiplier;
    private float forwardMultiplier;
    private boolean useItem;

    @Generated
    public float getStrafeMultiplier() {
        return this.strafeMultiplier;
    }

    @Generated
    public float getForwardMultiplier() {
        return this.forwardMultiplier;
    }

    @Generated
    public boolean isUseItem() {
        return this.useItem;
    }

    @Generated
    public void setStrafeMultiplier(float strafeMultiplier) {
        this.strafeMultiplier = strafeMultiplier;
    }

    @Generated
    public void setForwardMultiplier(float forwardMultiplier) {
        this.forwardMultiplier = forwardMultiplier;
    }

    @Generated
    public void setUseItem(boolean useItem) {
        this.useItem = useItem;
    }

    @Generated
    public ItemSlowdownEvent(float strafeMultiplier, float forwardMultiplier, boolean useItem) {
        this.strafeMultiplier = strafeMultiplier;
        this.forwardMultiplier = forwardMultiplier;
        this.useItem = useItem;
    }
}
