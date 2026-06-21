/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.mc;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.InventoryUtils;
import cc.advantage.utils.misc.PacketList;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2FPacketSetSlot;

public class PacketUtils
extends Util {
    public static void correctBlockCount(PacketReceiveEvent event) {
        if (PacketUtils.mc.thePlayer == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof S2FPacketSetSlot) {
            S2FPacketSetSlot wrapper = (S2FPacketSetSlot)packet;
            if (wrapper.item() == null) {
                event.setCancelled();
            } else {
                try {
                    int slot = wrapper.id() - 36;
                    if (slot < 0) {
                        return;
                    }
                    ItemStack itemStack = PacketUtils.mc.thePlayer.inventory.getStackInSlot(slot);
                    Item item = wrapper.item().getItem();
                    if (itemStack == null && wrapper.item().stackSize <= 6 && item instanceof ItemBlock && !InventoryUtils.blacklist.contains(((ItemBlock)item).getBlock()) || itemStack != null && Math.abs(Objects.requireNonNull(itemStack).stackSize - wrapper.item().stackSize) <= 6 || wrapper.item() == null) {
                        event.setCancelled();
                    }
                }
                catch (ArrayIndexOutOfBoundsException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public static void sendPacket(Packet<?> p) {
        PacketSendEvent sendEvent = new PacketSendEvent(p);
        Advantage.INSTANCE.getEventBus().post(sendEvent);
        if (sendEvent.isCancelled()) {
            return;
        }
        mc.getNetHandler().getNetworkManager().sendPacket(p);
    }

    public static void sendSilentPacket(Packet<?> p) {
        PacketSendEvent sendEvent = new PacketSendEvent(p);
        Advantage.INSTANCE.getEventBus().post(sendEvent);
        if (sendEvent.isCancelled()) {
            return;
        }
        mc.getNetHandler().getNetworkManager().sendSilentPacket(p);
    }

    public static void receivePacket(Packet p) {
        PacketReceiveEvent sendEvent = new PacketReceiveEvent(p);
        Advantage.INSTANCE.getEventBus().post(sendEvent);
        if (sendEvent.isCancelled()) {
            return;
        }
        mc.getNetHandler().getNetworkManager().receivePacket(p);
    }

    public static void receiveSilentPacket(Packet p) {
        PacketReceiveEvent sendEvent = new PacketReceiveEvent(p);
        Advantage.INSTANCE.getEventBus().post(sendEvent);
        if (sendEvent.isCancelled()) {
            return;
        }
        mc.getNetHandler().getNetworkManager().receiveUnregisteredPacket(p);
    }

    public static boolean isClientPacket(Packet<?> packet) {
        return Arrays.stream(PacketList.serverbound).anyMatch(clazz -> clazz == packet.getClass());
    }

    public static void queue(Packet<?> packet) {
        if (packet == null) {
            System.out.println("Packet is null");
            return;
        }
        if (PacketUtils.isClientPacket(packet)) {
            mc.getNetHandler().addToSendQueue(packet);
        } else {
            mc.getNetHandler().addToReceiveQueue(packet);
        }
    }

    public static class TimedPacket {
        private final Packet<?> packet;
        private final long time;

        public TimedPacket(Packet<?> packet, long time) {
            this.packet = packet;
            this.time = time;
        }

        public TimedPacket(Packet<?> packet) {
            this.packet = packet;
            this.time = System.currentTimeMillis();
        }

        public Packet<?> getPacket() {
            return this.packet;
        }

        public long getTime() {
            return this.time;
        }
    }
}

