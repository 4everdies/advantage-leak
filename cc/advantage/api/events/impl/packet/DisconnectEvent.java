/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.packet;

import cc.advantage.api.events.Event;

public final class DisconnectEvent
implements Event {
    private final String reason;

    public DisconnectEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return this.reason;
    }
}
