/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.render;

import cc.advantage.api.events.CancellableEvent;
import net.minecraft.entity.EntityLivingBase;

public final class RenderNameTagEvent
extends CancellableEvent {
    private final EntityLivingBase entityLivingBase;

    public RenderNameTagEvent(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    public EntityLivingBase getEntityLivingBase() {
        return this.entityLivingBase;
    }
}
