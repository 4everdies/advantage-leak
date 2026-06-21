/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="No Hit Delay", category=ModuleCategory.COMBAT)
public final class NoHitDelayModule
extends Module {
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        if (Util.mc.theWorld != null && Util.mc.thePlayer != null) {
            if (!Util.mc.inGameHasFocus) {
                return;
            }
            Util.mc.leftClickCounter = 0;
        }
    };
}

