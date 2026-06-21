/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Step", category=ModuleCategory.MOVEMENT)
public class StepModule
extends Module {
    public static NumberProperty stepHeight = new NumberProperty("Step Height", 1.0, 1.0, 10.0, 0.5);
    @EventLink
    private final Listener<MotionEvent> motionEventListener = event -> {
        this.setSuffix(((Double)stepHeight.getValue()).toString());
        Util.mc.thePlayer.stepHeight = ((Double)stepHeight.getValue()).floatValue();
    };

    @Override
    public void onDisable() {
        Util.mc.thePlayer.stepHeight = 0.5f;
        super.onDisable();
    }
}

