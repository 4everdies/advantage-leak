/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.processes;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;

public class BadPacketsProcess {
    public boolean C08;
    public boolean C07;
    private boolean C02;
    public boolean C09;
    public static boolean delayAttack;
    public boolean delay;
    public static int playerSlot;
    public int serverSlot = -1;
    @EventLink(value=4)
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPacket() instanceof C02PacketUseEntity) {
            if (this.C07) {
                event.setCancelled(true);
                return;
            }
            this.C02 = true;
        } else if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            this.C08 = true;
        } else if (event.getPacket() instanceof C07PacketPlayerDigging) {
            this.C07 = true;
        } else if (event.getPacket() instanceof C09PacketHeldItemChange) {
            if (((C09PacketHeldItemChange)event.getPacket()).getSlotId() == playerSlot && ((C09PacketHeldItemChange)event.getPacket()).getSlotId() == this.serverSlot) {
                event.setCancelled(true);
                return;
            }
            this.C09 = true;
            this.serverSlot = playerSlot = ((C09PacketHeldItemChange)event.getPacket()).getSlotId();
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> packetInEventListener = event -> {
        if (event.getPacket() instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange packet = (S09PacketHeldItemChange)event.getPacket();
            if (packet.getHeldItemHotbarIndex() >= 0 && packet.getHeldItemHotbarIndex() < InventoryPlayer.getHotbarSize()) {
                this.serverSlot = packet.getHeldItemHotbarIndex();
            }
        } else if (event.getPacket() instanceof S0CPacketSpawnPlayer && Minecraft.getMinecraft().thePlayer != null) {
            if (((S0CPacketSpawnPlayer)event.getPacket()).getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) {
                return;
            }
            playerSlot = -1;
        }
    };
    @EventLink
    public final Listener<MotionEvent> eventMotionListener = e -> {
        if (this.delay) {
            delayAttack = false;
            this.delay = false;
        }
        if (this.C08 || this.C09) {
            this.delay = true;
            delayAttack = true;
        }
        this.C09 = false;
        this.C02 = false;
        this.C07 = false;
        this.C08 = false;
    };

    static {
        playerSlot = -1;
    }
}

