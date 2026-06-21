/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.processes;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.processes.LagManager;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public class LagManagerSubscriber {
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (LagManager.handlePacket(event.getPacket())) {
            event.setCancelled(true);
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> LagManager.onTick();
    @EventLink
    public final Listener<WorldLoadEvent> onWorldLoad = event -> LagManager.resetDelay();
}

