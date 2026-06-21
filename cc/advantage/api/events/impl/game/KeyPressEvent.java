/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.game;

import cc.advantage.api.events.Event;

public final class KeyPressEvent
implements Event {
    private final int key;

    public KeyPressEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }
}
