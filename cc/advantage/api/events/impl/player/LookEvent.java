/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.Event;
import lombok.Generated;
import org.lwjgl.util.vector.Vector2f;

public final class LookEvent
implements Event {
    private Vector2f rotation;

    @Generated
    public Vector2f getRotation() {
        return this.rotation;
    }

    @Generated
    public void setRotation(Vector2f rotation) {
        this.rotation = rotation;
    }

    @Generated
    public LookEvent(Vector2f rotation) {
        this.rotation = rotation;
    }
}
