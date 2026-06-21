/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.render;

import cc.advantage.api.events.Event;

public final class Render2DEvent
implements Event {
    private final float partialTicks;

    public Render2DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }
}
