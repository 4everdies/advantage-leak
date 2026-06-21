/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.processes;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

public class LagManager {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Deque<LagPacket> packetQueue = new ConcurrentLinkedDeque<LagPacket>();
    private static int tickDelay = 0;
    private static boolean flushing = false;
    private static Vec3 lastPosition = new Vec3(0.0, 0.0, 0.0);

    public static void setDelay(int ticks) {
        tickDelay = Math.max(0, ticks);
    }

    public static int getDelay() {
        return tickDelay;
    }

    public static Vec3 getLastPosition() {
        return lastPosition;
    }

    public static boolean handlePacket(Packet<?> packet) {
        C03PacketPlayer c03;
        LagManager.flushQueue();
        if (packet instanceof C00PacketKeepAlive || packet instanceof C01PacketChatMessage) {
            return false;
        }
        if (tickDelay > 0) {
            packetQueue.offer(new LagPacket(packet));
            return true;
        }
        if (packet instanceof C03PacketPlayer && (c03 = (C03PacketPlayer)packet).isMoving()) {
            lastPosition = new Vec3(c03.getPositionX(), c03.getPositionY(), c03.getPositionZ());
        }
        return false;
    }

    public static void onTick() {
        if (LagManager.mc.thePlayer != null && LagManager.mc.thePlayer.isDead) {
            LagManager.setDelay(0);
        }
        LagManager.incrementDelays();
        LagManager.flushQueue();
    }

    public static void resetDelay() {
        LagManager.setDelay(0);
    }

    public static boolean isFlushing() {
        return flushing;
    }

    private static void flushQueue() {
        if (mc.getNetHandler() == null) {
            packetQueue.clear();
            return;
        }
        flushing = true;
        while (!packetQueue.isEmpty()) {
            C03PacketPlayer c03;
            LagPacket lp = packetQueue.peek();
            if (tickDelay > 0 && lp.delay <= tickDelay) break;
            packetQueue.poll();
            mc.getNetHandler().addToSendQueue(lp.packet);
            if (!(lp.packet instanceof C03PacketPlayer) || !(c03 = (C03PacketPlayer)lp.packet).isMoving()) continue;
            lastPosition = new Vec3(c03.getPositionX(), c03.getPositionY(), c03.getPositionZ());
        }
        flushing = false;
    }

    private static void incrementDelays() {
        for (LagPacket lp : packetQueue) {
            ++lp.delay;
        }
    }

    private static class LagPacket {
        final Packet<?> packet;
        int delay;

        LagPacket(Packet<?> packet) {
            this.packet = packet;
            this.delay = 0;
        }
    }
}

