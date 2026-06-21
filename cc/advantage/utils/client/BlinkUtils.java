/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.client;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.utils.mc.PacketUtils;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.Vec3;

public class BlinkUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final List<Packet<?>> packets = new CopyOnWriteArrayList();
    public static final List<Packet<?>> packetsReceived = new CopyOnWriteArrayList();
    public static final List<Vec3> positions = new CopyOnWriteArrayList<Vec3>();
    private static EntityOtherPlayerMP fakePlayer = null;

    public static boolean isBlinking() {
        return !packets.isEmpty() || !packetsReceived.isEmpty();
    }

    public static void handleOutgoingPacket(Packet<?> packet, PacketSendEvent event, boolean sentMode, boolean receiveMode) {
        if (event.isCancelled() || BlinkUtils.mc.thePlayer == null || BlinkUtils.mc.thePlayer.isDead || mc.isSingleplayer()) {
            return;
        }
        if (BlinkUtils.shouldIgnorePacket(packet)) {
            return;
        }
        if (sentMode) {
            event.setCancelled(true);
            packets.add(packet);
            if (packet instanceof C03PacketPlayer && ((C03PacketPlayer)packet).isMoving()) {
                positions.add(new Vec3(((C03PacketPlayer)packet).getPositionX(), ((C03PacketPlayer)packet).getPositionY(), ((C03PacketPlayer)packet).getPositionZ()));
            }
        } else {
            BlinkUtils.flushReceivedPackets();
        }
    }

    public static void handleIncomingPacket(Packet<?> packet, PacketReceiveEvent event, boolean sentMode, boolean receiveMode) {
        if (event.isCancelled() || BlinkUtils.mc.thePlayer == null || BlinkUtils.mc.thePlayer.isDead || mc.isSingleplayer()) {
            return;
        }
        if (BlinkUtils.shouldIgnorePacket(packet)) {
            return;
        }
        if (receiveMode && BlinkUtils.mc.thePlayer.ticksExisted > 10) {
            event.setCancelled(true);
            packetsReceived.add(packet);
        } else {
            BlinkUtils.flushSentPackets();
        }
    }

    public static void flushSentPackets() {
        for (Packet<?> p : packets) {
            PacketUtils.sendPacket(p);
        }
        packets.clear();
    }

    public static void flushReceivedPackets() {
        for (Packet<?> p : packetsReceived) {
            BlinkUtils.processPacketSilent(p);
        }
        packetsReceived.clear();
    }

    public static void unblink() {
        BlinkUtils.flushSentPackets();
        BlinkUtils.flushReceivedPackets();
        BlinkUtils.clear();
        BlinkUtils.removeFakePlayer();
    }

    public static void cancel() {
        if (BlinkUtils.mc.thePlayer == null) {
            return;
        }
        if (!positions.isEmpty()) {
            Vec3 firstPos = positions.get(0);
            BlinkUtils.mc.thePlayer.setPositionAndUpdate(firstPos.xCoord, firstPos.yCoord, firstPos.zCoord);
        }
        packets.removeIf(p -> p instanceof C03PacketPlayer);
        BlinkUtils.flushSentPackets();
        positions.clear();
        BlinkUtils.removeFakePlayer();
    }

    public static void clear() {
        packets.clear();
        packetsReceived.clear();
        positions.clear();
    }

    public static void addFakePlayer() {
        if (BlinkUtils.mc.thePlayer == null || BlinkUtils.mc.theWorld == null) {
            return;
        }
        EntityOtherPlayerMP clone = new EntityOtherPlayerMP(BlinkUtils.mc.theWorld, BlinkUtils.mc.thePlayer.getGameProfile());
        clone.copyLocationAndAnglesFrom(BlinkUtils.mc.thePlayer);
        clone.rotationYaw = BlinkUtils.mc.thePlayer.rotationYaw;
        clone.rotationPitch = BlinkUtils.mc.thePlayer.rotationPitch;
        clone.rotationYawHead = BlinkUtils.mc.thePlayer.rotationYawHead;
        clone.renderYawOffset = BlinkUtils.mc.thePlayer.renderYawOffset;
        clone.inventory = BlinkUtils.mc.thePlayer.inventory;
        Random rand = new Random();
        int id = rand.nextInt(0x7FFFFFFE) + 1;
        BlinkUtils.mc.theWorld.addEntityToWorld(id, clone);
        fakePlayer = clone;
    }

    public static void removeFakePlayer() {
        if (fakePlayer != null) {
            BlinkUtils.mc.theWorld.removeEntity(fakePlayer);
            fakePlayer = null;
        }
    }

    private static boolean shouldIgnorePacket(Packet<?> packet) {
        return packet instanceof C00Handshake || packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing || packet instanceof S02PacketChat || packet instanceof C01PacketChatMessage || packet instanceof S29PacketSoundEffect && "game.player.hurt".equals(((S29PacketSoundEffect)packet).getSoundName());
    }

    private static void processPacketSilent(Packet<?> packet) {
        try {
            if (mc.getNetHandler() != null) {
                packet.processPacket(mc.getNetHandler());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

