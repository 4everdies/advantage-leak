/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.Event;

public final class SprintEvent
implements Event {
    private boolean sprinting;

    public SprintEvent(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public boolean isSprinting() {
        return this.sprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }
}
