/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.processes;

import cc.advantage.api.events.CancellableEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C13PacketPlayerAbilities;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.util.Tuple;

public class LagProcess {
    public static ConcurrentLinkedQueue<PacketUtils.TimedPacket> packets = new ConcurrentLinkedQueue();
    static Timer enabledTimer = new Timer();
    public static boolean enabled;
    static long amount;
    static Tuple<Class[], Boolean> regular;
    static Tuple<Class[], Boolean> velocity;
    static Tuple<Class[], Boolean> teleports;
    static Tuple<Class[], Boolean> players;
    static Tuple<Class[], Boolean> blink;
    static Tuple<Class[], Boolean> movement;
    public static Tuple<Class[], Boolean>[] types;
    private static C0BPacketEntityAction.Action lastEntityAction;
    public static double serverX;
    public static double serverY;
    public static double serverZ;
    public static boolean hasServerPosition;
    @EventLink
    public final Listener<PacketSendEvent> send = event -> event.setCancelled(this.onPacket(event.getPacket(), (CancellableEvent)event).isCancelled());
    @EventLink
    public final Listener<PacketReceiveEvent> receive = event -> event.setCancelled(this.onPacket(event.getPacket(), (CancellableEvent)event).isCancelled());
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> LagProcess.dispatch();
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (event.isPre()) {
            return;
        }
        enabled = !enabledTimer.hasTimeElapsed(100.0) && !(Util.mc.currentScreen instanceof GuiDownloadTerrain);
        if (!enabled) {
            LagProcess.dispatch();
        } else {
            enabled = false;
            Iterator<PacketUtils.TimedPacket> iterator2 = packets.iterator();
            while (iterator2.hasNext()) {
                PacketUtils.TimedPacket packet = iterator2.next();
                if (packet.getTime() + amount >= System.currentTimeMillis()) continue;
                Packet<?> p = packet.getPacket();
                if (p instanceof C0BPacketEntityAction) {
                    lastEntityAction = ((C0BPacketEntityAction)p).getAction();
                }
                PacketUtils.queue(p);
                iterator2.remove();
            }
            enabled = true;
        }
    };

    public CancellableEvent onPacket(Packet<?> packet, CancellableEvent event) {
        if (!event.isCancelled() && enabled && Arrays.stream(types).anyMatch(tuple -> (Boolean)tuple.getSecond() != false && Arrays.stream((Class[])tuple.getFirst()).anyMatch(regularpacket -> regularpacket == packet.getClass()))) {
            C0BPacketEntityAction entityAction;
            if (packet instanceof C0BPacketEntityAction && (entityAction = (C0BPacketEntityAction)packet).getAction() == lastEntityAction) {
                return event;
            }
            event.setCancelled();
            packets.add(new PacketUtils.TimedPacket(packet));
        }
        if (packet instanceof C03PacketPlayer) {
            C03PacketPlayer movement = (C03PacketPlayer)packet;
            if (!event.isCancelled() && movement.isMoving()) {
                serverX = movement.getPositionX();
                serverY = movement.getPositionY();
                serverZ = movement.getPositionZ();
                hasServerPosition = true;
            }
        }
        return event;
    }

    public static void dispatch() {
        if (!packets.isEmpty()) {
            boolean enabled = LagProcess.enabled;
            LagProcess.enabled = false;
            packets.forEach(timedPacket -> PacketUtils.queue(timedPacket.getPacket()));
            LagProcess.enabled = enabled;
            packets.clear();
            lastEntityAction = null;
        }
    }

    public static void disable() {
        enabled = false;
        enabledTimer.setTime(enabledTimer.getTime() - 999999999L);
        lastEntityAction = null;
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players) {
        LagProcess.spoof(amount, regular, velocity, teleports, players, false);
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players, boolean blink, boolean movement) {
        enabledTimer.reset();
        LagProcess.regular.setSecond(regular);
        LagProcess.velocity.setSecond(velocity);
        LagProcess.teleports.setSecond(teleports);
        LagProcess.players.setSecond(players);
        LagProcess.blink.setSecond(blink);
        LagProcess.movement.setSecond(movement);
        LagProcess.amount = amount;
    }

    public static void spoof(int amount, boolean regular, boolean velocity, boolean teleports, boolean players, boolean blink) {
        LagProcess.spoof(amount, regular, velocity, teleports, players, blink, true);
    }

    public static void blink() {
        LagProcess.spoof(9999999, true, false, false, false, true);
    }

    static {
        regular = new Tuple<Class[], Boolean>(new Class[]{C0FPacketConfirmTransaction.class, C00PacketKeepAlive.class, S1CPacketEntityMetadata.class}, false);
        velocity = new Tuple<Class[], Boolean>(new Class[]{S12PacketEntityVelocity.class, S27PacketExplosion.class}, false);
        teleports = new Tuple<Class[], Boolean>(new Class[]{S08PacketPlayerPosLook.class, S39PacketPlayerAbilities.class, S09PacketHeldItemChange.class}, false);
        players = new Tuple<Class[], Boolean>(new Class[]{S13PacketDestroyEntities.class, S14PacketEntity.class, S14PacketEntity.S16PacketEntityLook.class, S14PacketEntity.S15PacketEntityRelMove.class, S14PacketEntity.S17PacketEntityLookMove.class, S18PacketEntityTeleport.class, S20PacketEntityProperties.class, S19PacketEntityHeadLook.class}, false);
        blink = new Tuple<Class[], Boolean>(new Class[]{C02PacketUseEntity.class, C0DPacketCloseWindow.class, C0EPacketClickWindow.class, C0CPacketInput.class, C08PacketPlayerBlockPlacement.class, C07PacketPlayerDigging.class, C09PacketHeldItemChange.class, C13PacketPlayerAbilities.class, C15PacketClientSettings.class, C16PacketClientStatus.class, C17PacketCustomPayload.class, C18PacketSpectate.class, C19PacketResourcePackStatus.class, C03PacketPlayer.class, C03PacketPlayer.C04PacketPlayerPosition.class, C03PacketPlayer.C05PacketPlayerLook.class, C03PacketPlayer.C06PacketPlayerPosLook.class, C0APacketAnimation.class}, false);
        movement = new Tuple<Class[], Boolean>(new Class[]{C03PacketPlayer.class, C03PacketPlayer.C04PacketPlayerPosition.class, C03PacketPlayer.C05PacketPlayerLook.class, C03PacketPlayer.C06PacketPlayerPosLook.class}, false);
        types = new Tuple[]{regular, velocity, teleports, players, blink, movement};
        lastEntityAction = null;
        hasServerPosition = false;
    }
}

