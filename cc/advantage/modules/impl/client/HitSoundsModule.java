/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.game.EntityHurtSoundEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.client.SoundUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Hit Sounds", category=ModuleCategory.CLIENT)
public class HitSoundsModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Skeet);
    @EventLink
    public final Listener<EntityHurtSoundEvent> entityHurtSoundEventListener = event -> {
        event.setCancelled();
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 0: {
                SoundUtils.playSound("skeet.wav");
                break;
            }
            case 1: {
                SoundUtils.playSound("felix.wav");
                break;
            }
            case 2: {
                SoundUtils.playSound("ultrakill.wav");
                break;
            }
            case 3: {
                SoundUtils.playSound("onichan.wav");
            }
        }
    };

    public static enum Mode {
        Skeet("Skeet"),
        Felix("Felix"),
        UltraKill("UltraKill"),
        OniChan("Oni Chan");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

