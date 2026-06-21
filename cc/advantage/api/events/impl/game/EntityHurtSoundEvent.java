/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.game;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;
import net.minecraft.entity.EntityLivingBase;

public class EntityHurtSoundEvent
extends CancellableEvent {
    private final EntityLivingBase entity;

    @Generated
    public EntityLivingBase getEntity() {
        return this.entity;
    }

    @Generated
    public EntityHurtSoundEvent(EntityLivingBase entity) {
        this.entity = entity;
    }
}
