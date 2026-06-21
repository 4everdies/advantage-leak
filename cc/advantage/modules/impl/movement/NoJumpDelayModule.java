/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="No Jump Delay", category=ModuleCategory.MOVEMENT)
public final class NoJumpDelayModule
extends Module {
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        Util.mc.thePlayer.jumpTicks = 0;
    };
}

