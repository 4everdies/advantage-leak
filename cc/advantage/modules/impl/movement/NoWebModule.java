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
import cc.advantage.utils.mc.MovementUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="No Web", category=ModuleCategory.MOVEMENT)
public final class NoWebModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Vanilla);
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 0: {
                if (!Util.mc.thePlayer.isInWeb || !MovementUtils.isMoving()) break;
                Util.mc.thePlayer.isInWeb = false;
                break;
            }
            case 1: {
                if (!Util.mc.thePlayer.isInWeb || !MovementUtils.isMoving() || !Util.mc.thePlayer.onGround) break;
                if (Util.mc.thePlayer.ticksExisted % 3 == 0) {
                    MovementUtils.strafe(0.734);
                    break;
                }
                Util.mc.thePlayer.jump();
                MovementUtils.strafe(0.346);
            }
        }
    };

    private static enum Mode {
        Vanilla,
        Intave;

    }
}

