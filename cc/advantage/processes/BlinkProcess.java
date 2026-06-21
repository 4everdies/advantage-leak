/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.processes;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.modules.impl.client.ClientSettingsModule;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.Packet;

public class BlinkProcess {
    public static boolean enabled;
    private static boolean disable;
    public static final List<Packet<?>> blinkedSendPackets;
    public static final List<Packet<?>> blinkedReceivePackets;
    @EventLink
    public final Listener<PacketSendEvent> packetSendEventListener = event -> {
        if (enabled) {
            Packet<?> packet = event.getPacket();
            blinkedSendPackets.add(packet);
            event.setCancelled();
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        if (enabled && ClientSettingsModule.blinkCancelsIncoming.getValue().booleanValue()) {
            Packet<?> packet = event.getPacket();
            blinkedReceivePackets.add(packet);
            event.setCancelled();
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        if (enabled) {
            BlinkProcess.disable();
        }
    };
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (disable) {
            if (event.isPre()) {
                return;
            }
            enabled = false;
            disable = false;
            ArrayList sendPacketsCopy = new ArrayList(blinkedSendPackets);
            ArrayList receivePacketsCopy = new ArrayList(blinkedReceivePackets);
            blinkedSendPackets.clear();
            blinkedReceivePackets.clear();
            for (Packet packet : sendPacketsCopy) {
                PacketUtils.sendPacket(packet);
            }
            for (Packet packet : receivePacketsCopy) {
                PacketUtils.receivePacket(packet);
            }
        }
    };

    public static void enable() {
        blinkedSendPackets.clear();
        blinkedReceivePackets.clear();
        enabled = true;
    }

    public static void disable() {
        disable = true;
    }

    static {
        disable = false;
        blinkedSendPackets = new ArrayList();
        blinkedReceivePackets = new ArrayList();
    }
}

