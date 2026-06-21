/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

@ModuleInfo(label="Client Spoofer", category=ModuleCategory.CLIENT)
public final class ClientSpooferModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Lunar);
    @EventLink
    public final Listener<PacketSendEvent> packetSendEventListener = e -> {
        if (e.getPacket() instanceof C17PacketCustomPayload) {
            PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer((switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
                case 0 -> "lunarclient:v2.14.5-2411";
                case 1 -> "Feather Forge";
                default -> "";
            }).getBytes()));
            C17PacketCustomPayload spoofedPacket = new C17PacketCustomPayload("MC|Brand", buffer);
            e.setPacket(spoofedPacket);
        }
    };

    private static enum Mode {
        Lunar("Lunar"),
        Feather("Feather");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

