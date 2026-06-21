/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

@ModuleInfo(label="Full Bright", category=ModuleCategory.VISUALS)
public final class FullBrightModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.GAMMA);
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 0: {
                Util.mc.gameSettings.gammaSetting = 100000.0f;
                break;
            }
            case 1: {
                Util.mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 5200, 1));
            }
        }
    };

    @Override
    public void onDisable() {
        if (Util.mc.thePlayer.isPotionActive(Potion.nightVision) && this.mode.getValue() == Mode.POTION) {
            Util.mc.thePlayer.removePotionEffect(Potion.nightVision.id);
        }
        Util.mc.gameSettings.gammaSetting = 1.0f;
    }

    private static enum Mode {
        GAMMA("Gamma"),
        POTION("Potion");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

