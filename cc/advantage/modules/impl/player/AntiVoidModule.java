/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.LagProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.mc.PlayerUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.util.Vector3d;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Anti Void", category=ModuleCategory.PLAYER)
public final class AntiVoidModule
extends Module {
    public ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Position);
    private final NumberProperty distance = new NumberProperty("Distance", 5.0, () -> this.mode.getValue() != Mode.Blink, 0.0, 10.0, 1.0);
    private Vector3d position;
    private Vector3d motion;
    private Vector2f rotation;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (this.mode.getValue() == Mode.Blink) {
            if (event.isPre()) {
                return;
            }
            if (Util.mc.thePlayer.ticksExisted <= 60) {
                return;
            }
            if (this.position != null && this.motion != null && this.rotation != null && !PlayerUtils.isBlockUnder(50.0, true)) {
                if (Util.mc.thePlayer.fallDistance > 4.0f) {
                    Util.mc.thePlayer.setPosition(this.position.x, this.position.y, this.position.z);
                    Util.mc.thePlayer.motionX = 0.0;
                    Util.mc.thePlayer.motionY = MovementUtils.predictedMotion(this.motion.y);
                    Util.mc.thePlayer.motionZ = 0.0;
                    Util.mc.thePlayer.rotationYaw = this.rotation.x;
                    Util.mc.thePlayer.rotationPitch = this.rotation.y;
                    Util.mc.thePlayer.fallDistance = 0.0f;
                    LagProcess.packets.removeIf(timedPacket -> !(timedPacket.getPacket() instanceof C0FPacketConfirmTransaction) && !(timedPacket.getPacket() instanceof C00PacketKeepAlive));
                    LagProcess.disable();
                    LagProcess.dispatch();
                }
            } else {
                this.position = new Vector3d(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ);
                this.motion = new Vector3d(Util.mc.thePlayer.motionX, Util.mc.thePlayer.motionY, Util.mc.thePlayer.motionZ);
                this.rotation = new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch);
            }
        }
        if (this.mode.getValue() == Mode.Position) {
            if (!event.isPre()) {
                return;
            }
            if (Util.mc.thePlayer.fallDistance > ((Double)this.distance.getValue()).floatValue() && !PlayerUtils.isBlockUnder()) {
                event.setPosY(event.getPosY() + (double)Util.mc.thePlayer.fallDistance);
            }
        }
        if (this.mode.getValue() == Mode.Packet) {
            if (!event.isPre()) {
                return;
            }
            if (Util.mc.thePlayer.fallDistance > ((Double)this.distance.getValue()).floatValue() && !PlayerUtils.isBlockUnder()) {
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition());
            }
        }
        if (this.mode.getValue() == Mode.Collision) {
            if (!event.isPre()) {
                return;
            }
            if (Util.mc.thePlayer.fallDistance > (float)((Double)this.distance.getValue()).intValue() && !PlayerUtils.isBlockUnder() && Util.mc.thePlayer.posY + Util.mc.thePlayer.motionY < Math.floor(Util.mc.thePlayer.posY)) {
                Util.mc.thePlayer.motionY = Math.floor(Util.mc.thePlayer.posY) - Util.mc.thePlayer.posY;
                if (Util.mc.thePlayer.motionY == 0.0) {
                    Util.mc.thePlayer.onGround = true;
                    event.setOnGround(true);
                }
            }
        }
    };

    public static enum Mode {
        Position,
        Packet,
        Collision,
        Blink;

    }
}

