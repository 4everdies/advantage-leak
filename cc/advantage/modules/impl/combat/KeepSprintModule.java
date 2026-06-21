/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.player.HitSlowDownEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="KeepSprint", category=ModuleCategory.COMBAT)
public final class KeepSprintModule
extends Module {
    public final Property<Integer> slowdown = new Property<Integer>("Slowdown %", 0);
    public final Property<Boolean> groundOnly = new Property<Boolean>("Ground Only", false);
    public final Property<Boolean> reachOnly = new Property<Boolean>("Reach Only", false);
    @EventLink
    public final Listener<HitSlowDownEvent> hitSlowDownEventListener = e -> {
        boolean keepSprint;
        if (Util.mc.thePlayer == null) {
            return;
        }
        boolean bl = keepSprint = !(this.groundOnly.getValue() != false && !Util.mc.thePlayer.onGround || this.reachOnly.getValue() != false && !(Util.mc.objectMouseOver.hitVec.distanceTo(Util.mc.getRenderViewEntity().getPositionEyes(1.0f)) > 3.0));
        if (keepSprint) {
            e.setSprint(true);
            double factor = 1.0 - (double)this.slowdown.getValue().intValue() / 100.0;
            e.setSlowDown(factor);
        }
    };
}

