/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.TeleportEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(label="No Rotate", category=ModuleCategory.CLIENT)
public class NoRotateModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Edit);
    private float yaw;
    private float pitch;
    private boolean teleport;
    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        if (mode.getValue() == Mode.Packet) {
            this.yaw = event.getYaw();
            this.pitch = event.getPitch();
            event.setYaw(Util.mc.thePlayer.rotationYaw);
            event.setPitch(Util.mc.thePlayer.rotationPitch);
            this.teleport = true;
        }
        if (mode.getValue() == Mode.Edit) {
            event.setYaw(Util.mc.thePlayer.rotationYaw);
            event.setPitch(Util.mc.thePlayer.rotationPitch);
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (mode.getValue() == Mode.Packet) {
            Packet<?> packet = event.getPacket();
            if (this.teleport && packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                C03PacketPlayer.C06PacketPlayerPosLook c06PacketPlayerPosLook = (C03PacketPlayer.C06PacketPlayerPosLook)packet;
                c06PacketPlayerPosLook.yaw = this.yaw;
                c06PacketPlayerPosLook.pitch = this.pitch;
                event.setPacket(c06PacketPlayerPosLook);
                this.teleport = false;
            }
        }
    };

    private static enum Mode {
        Edit,
        Packet;

    }
}

