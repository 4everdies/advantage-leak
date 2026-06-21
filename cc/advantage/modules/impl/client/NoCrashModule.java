/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Logger;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

@ModuleInfo(label="No Crash", category=ModuleCategory.CLIENT)
public final class NoCrashModule
extends Module {
    @EventLink
    public final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        Packet<?> packet = event.getPacket();
        if (packet instanceof S2BPacketChangeGameState && ((S2BPacketChangeGameState)packet).getGameState() == 5 && !Util.mc.isDemo()) {
            event.setCancelled();
            Logger.chatPrint("Blocked a demo crash packet");
        }
        if (packet instanceof S27PacketExplosion && (((S27PacketExplosion)packet).getX() > 1.0E9 || ((S27PacketExplosion)packet).getY() > 1.0E9 || ((S27PacketExplosion)packet).getZ() > 1.0E9 || ((S27PacketExplosion)packet).getStrength() == 2.1474836E9f)) {
            event.setCancelled();
            Logger.chatPrint("The server tried to crash your client with explosion");
        }
        if (packet instanceof S2APacketParticles && (((S2APacketParticles)packet).getXCoordinate() > 1.0E9 || ((S2APacketParticles)packet).getYCoordinate() > 1.0E9 || ((S2APacketParticles)packet).getZCoordinate() > 1.0E9 || (double)((S2APacketParticles)packet).getParticleSpeed() > 1.0E9 || (double)((S2APacketParticles)packet).getXOffset() > 1.0E9 || (double)((S2APacketParticles)packet).getYOffset() > 1.0E9 || (double)((S2APacketParticles)packet).getZOffset() > 1.0E9)) {
            event.setCancelled();
            Logger.chatPrint("The server tried to crash your client with particles");
        }
        if (packet instanceof S0EPacketSpawnObject && ((double)Math.abs(((S0EPacketSpawnObject)packet).getYaw()) > 10000.0 || (double)Math.abs(((S0EPacketSpawnObject)packet).getPitch()) > 10000.0)) {
            event.setCancelled();
            Logger.chatPrint("Blocked bogus spawn packet (yaw/pitch overflow)");
        }
        if (packet instanceof S3FPacketCustomPayload) {
            String channel = ((S3FPacketCustomPayload)packet).getChannelName();
            if (((S3FPacketCustomPayload)packet).getBufferData() == null || ((S3FPacketCustomPayload)packet).getBufferData().array().length > 51200) {
                event.setCancelled();
                Logger.chatPrint("Blocked custom payload packet from channel: " + channel);
            }
        }
        if (packet instanceof S09PacketHeldItemChange && ((S09PacketHeldItemChange)packet).getHeldItemHotbarIndex() == Integer.MAX_VALUE) {
            event.setCancelled();
            Logger.chatPrint("The server tried to crash your client with hotbar switch");
        }
    };
}

