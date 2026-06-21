/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

@ModuleInfo(label="Ambience", category=ModuleCategory.VISUALS)
public final class AmbienceModule
extends Module {
    private final ModeProperty<TimeEnum> timeProperty = new ModeProperty<TimeEnum>("Time", TimeEnum.Night);
    private final Property<Boolean> rainProperty = new Property<Boolean>("Rain", false);
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (Util.mc.theWorld == null) {
            return;
        }
        switch (((TimeEnum)((Object)((Object)this.timeProperty.getValue()))).ordinal()) {
            case 0: {
                Util.mc.theWorld.setWorldTime(1000L);
                break;
            }
            case 1: {
                Util.mc.theWorld.setWorldTime(18000L);
            }
        }
        if (this.rainProperty.getValue().booleanValue()) {
            Util.mc.theWorld.setRainStrength(1.0f);
            Util.mc.theWorld.setThunderStrength(1.0f);
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCancelled(true);
        }
    };

    public static enum TimeEnum {
        Day,
        Night;

    }
}

