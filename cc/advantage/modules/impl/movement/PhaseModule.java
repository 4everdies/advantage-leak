/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.BlockCollisionEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.mc.PlayerUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(label="Phase", category=ModuleCategory.MOVEMENT)
public final class PhaseModule
extends Module {
    private boolean phasing;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        this.phasing = false;
        double rotation = Math.toRadians(Util.mc.thePlayer.rotationYaw);
        double x = Math.sin(rotation);
        double z = Math.cos(rotation);
        if (Util.mc.thePlayer.isCollidedHorizontally) {
            Util.mc.thePlayer.setPosition(Util.mc.thePlayer.posX - x * 0.005, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ + z * 0.005);
            this.phasing = true;
        } else if (PlayerUtils.insideBlock()) {
            PacketUtils.sendSilentPacket(new C03PacketPlayer.C04PacketPlayerPosition(Util.mc.thePlayer.posX - x * 1.5, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ + z * 1.5, false));
            Util.mc.thePlayer.motionX *= 0.3;
            Util.mc.thePlayer.motionZ *= 0.3;
            this.phasing = true;
        }
    };
    @EventLink
    public final Listener<BlockCollisionEvent> blockCollisionEventListener = event -> {
        if (event.getBlock() instanceof BlockAir && this.phasing) {
            double x = event.getBlockPos().getX();
            double y = event.getBlockPos().getY();
            double z = event.getBlockPos().getZ();
            if (y < Util.mc.thePlayer.posY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15.0, -1.0, -15.0, 15.0, 1.0, 15.0).offset(x, y, z));
            }
        }
    };
}

