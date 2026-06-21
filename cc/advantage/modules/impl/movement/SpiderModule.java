/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Spider", category=ModuleCategory.MOVEMENT)
public final class SpiderModule
extends Module {
    private static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Vanilla);
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 1: {
                if (!Util.mc.thePlayer.isCollidedHorizontally || Util.mc.thePlayer.ticksExisted % 2 != 0) break;
                Util.mc.thePlayer.jump();
                break;
            }
            case 0: {
                if (!Util.mc.thePlayer.isCollidedHorizontally) break;
                Util.mc.thePlayer.jump();
            }
        }
    };

    public static enum Mode {
        Vanilla,
        Verus;

    }
}

