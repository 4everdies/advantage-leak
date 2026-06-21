/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.SprintEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Sprint", category=ModuleCategory.MOVEMENT)
public final class SprintModule
extends Module {
    public static Property<Boolean> omni = new Property<Boolean>("Omni", false);
    @EventLink
    public final Listener<SprintEvent> onSprintEvent = event -> {
        if (!event.isSprinting() && MovementUtils.isMoving() && !omni.getValue().booleanValue()) {
            Util.mc.gameSettings.keyBindSprint.setPressed(true);
        }
    };
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (MovementUtils.isMoving() && omni.getValue().booleanValue()) {
            Util.mc.thePlayer.setSprinting(MovementUtils.canSprint(true));
        }
    };

    public SprintModule() {
        this.toggle();
    }
}

