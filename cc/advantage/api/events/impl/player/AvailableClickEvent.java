/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;

public class AvailableClickEvent
extends CancellableEvent {
    private boolean shouldRightClick;
    private int slot;

    public AvailableClickEvent(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public boolean isShouldRightClick() {
        return this.shouldRightClick;
    }

    public void setShouldRightClick(boolean shouldRightClick) {
        this.shouldRightClick = shouldRightClick;
    }
}
