/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.PostStrafeEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vector3d;

@ModuleInfo(label="Stop Movement", category=ModuleCategory.MOVEMENT)
public final class StopMovementModule
extends Module {
    private Vector3d motion;
    @EventLink
    public final Listener<PostStrafeEvent> onPostStrafe = event -> {
        MovementUtils.stop();
        Util.mc.thePlayer.motionY = 0.0;
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C03PacketPlayer) {
            event.setCancelled();
        }
    };

    @Override
    public void onEnable() {
        this.motion = new Vector3d(Util.mc.thePlayer.motionX, Util.mc.thePlayer.motionY, Util.mc.thePlayer.motionZ);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Util.mc.thePlayer.motionX = this.motion.x;
        Util.mc.thePlayer.motionY = this.motion.y;
        Util.mc.thePlayer.motionZ = this.motion.z;
        super.onDisable();
    }
}

