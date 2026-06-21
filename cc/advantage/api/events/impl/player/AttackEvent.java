/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.player;

import cc.advantage.api.events.CancellableEvent;
import net.minecraft.entity.EntityLivingBase;

public class AttackEvent
extends CancellableEvent {
    public EntityLivingBase target;

    public AttackEvent(EntityLivingBase target) {
        this.target = target;
    }
}
