/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events;

import cc.advantage.api.events.Event;

public class CancellableEvent
implements Event {
    private boolean cancelled;

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setCancelled() {
        this.cancelled = true;
    }
}
