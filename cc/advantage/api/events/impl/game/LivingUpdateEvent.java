/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.game;

import cc.advantage.api.events.Event;
import lombok.Generated;
import net.minecraft.entity.Entity;

public final class LivingUpdateEvent
implements Event {
    private final Entity entity;

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public LivingUpdateEvent(Entity entity) {
        this.entity = entity;
    }
}
