/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Timer", category=ModuleCategory.CLIENT)
public class TimerModule
extends Module {
    public ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Constant);
    public NumberProperty time1 = new NumberProperty("Time", 1.0, 0.0, 5.0, 0.1);
    public NumberProperty time2 = new NumberProperty("Time 2", 1.0, () -> this.mode.getValue() == Mode.Pulse, 0.0, 5.0, 0.1);
    Timer timer = new Timer();
    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        this.setSuffix(String.valueOf(((Double)this.time1.getValue()).intValue()));
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 0: {
                Util.mc.timer.timerSpeed = ((Double)this.time1.getValue()).floatValue();
                break;
            }
            case 1: {
                if (this.timer.getTime() < 100L) {
                    Util.mc.timer.timerSpeed = ((Double)this.time2.getValue()).floatValue();
                    break;
                }
                if (this.timer.getTime() > 200L) {
                    this.timer.reset();
                    break;
                }
                Util.mc.timer.timerSpeed = ((Double)this.time1.getValue()).floatValue();
                break;
            }
        }
    };

    @Override
    public void onDisable() {
        Util.mc.timer.timerSpeed = 1.0f;
        super.onDisable();
    }

    public static enum Mode {
        Constant,
        Pulse;

    }
}

