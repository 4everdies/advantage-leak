/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.events.impl.packet;

import cc.advantage.api.events.CancellableEvent;
import net.minecraft.network.Packet;

public final class PacketReceiveEvent
extends CancellableEvent {
    private Packet<?> packet;

    public PacketReceiveEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }
}
