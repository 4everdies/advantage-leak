/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.processes.LagProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.render.GlUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.animations.impl.ContinualAnimation;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

@ModuleInfo(label="Back Track", category=ModuleCategory.COMBAT)
public final class BackTrackModule
extends Module {
    public static NumberProperty minDelayProperty = new NumberProperty("Min Delay", 50.0, 0.0, 5000.0, 10.0);
    public static NumberProperty maxDelayProperty = new NumberProperty("Max Delay", 200.0, 0.0, 5000.0, 10.0);
    public static NumberProperty activateDist = new NumberProperty("Activate Distance", 2.0, 2.0, 3.0, 0.1);
    public static NumberProperty deactivateDist = new NumberProperty("Deactivate Distance", 10.0, 3.0, 10.0, 0.1);
    public static ModeProperty<Mode> modeProperty = new ModeProperty<Mode>("Mode", Mode.Constant);
    public Property<Boolean> cancelClientPacketsProperty = new Property<Boolean>("Cancel Client Packets", true);
    public Property<Boolean> swingCheckProperty = new Property<Boolean>("Swing Check", true);
    public Property<Boolean> releaseOnDamageProperty = new Property<Boolean>("Release On Damage", true);
    public EntityPlayer target;
    public Vec3 realPosition = new Vec3(0.0, 0.0, 0.0);
    private final ContinualAnimation animatedX = new ContinualAnimation();
    private final ContinualAnimation animatedY = new ContinualAnimation();
    private final ContinualAnimation animatedZ = new ContinualAnimation();
    private int ping;
    @EventLink
    public Listener<MotionEvent> motionEventListener = event -> {
        double clientDistance;
        boolean on;
        if (event.isPre()) {
            return;
        }
        this.setSuffix(this.ping + " ms");
        if (Util.mc.thePlayer.isDead) {
            LagProcess.dispatch();
            LagProcess.disable();
            return;
        }
        if (!(TargetSelectionProcess.getTarget() instanceof EntityPlayer)) {
            LagProcess.dispatch();
            LagProcess.disable();
            return;
        }
        this.target = (EntityPlayer)TargetSelectionProcess.getTarget();
        if (this.swingCheckProperty.getValue().booleanValue() && !Util.mc.thePlayer.isSwingInProgress) {
            return;
        }
        double realDistance = this.realPosition.distanceTo(Util.mc.thePlayer);
        boolean bl = on = realDistance > (clientDistance = (double)this.target.getDistanceToEntity(Util.mc.thePlayer)) && realDistance >= (Double)activateDist.getValue() && realDistance <= (Double)deactivateDist.getValue() && this.shouldActive(this.target) && (this.releaseOnDamageProperty.getValue() == false || Util.mc.thePlayer.hurtTime == 0);
        if (on) {
            if (this.shouldActive(this.target)) {
                this.ping = (int)MathUtils.getRandom(((Double)minDelayProperty.getValue()).intValue(), ((Double)maxDelayProperty.getValue()).intValue());
                LagProcess.spoof(this.ping, true, true, true, true, this.cancelClientPacketsProperty.getValue(), this.cancelClientPacketsProperty.getValue());
            } else {
                LagProcess.dispatch();
                LagProcess.disable();
            }
        } else {
            LagProcess.dispatch();
            LagProcess.disable();
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        S18PacketEntityTeleport s18PacketEntityTeleport;
        Packet<?> packet = event.getPacket();
        if (this.target == null) {
            return;
        }
        if (packet instanceof S14PacketEntity) {
            S14PacketEntity s14PacketEntity = (S14PacketEntity)packet;
            if (s14PacketEntity.entityId == this.target.getEntityId()) {
                this.realPosition.xCoord += (double)s14PacketEntity.getPosX() / 32.0;
                this.realPosition.yCoord += (double)s14PacketEntity.getPosY() / 32.0;
                this.realPosition.zCoord += (double)s14PacketEntity.getPosZ() / 32.0;
            }
        } else if (packet instanceof S18PacketEntityTeleport && (s18PacketEntityTeleport = (S18PacketEntityTeleport)packet).getEntityId() == this.target.getEntityId()) {
            this.realPosition = new Vec3((double)s18PacketEntityTeleport.getX() / 32.0, (double)s18PacketEntityTeleport.getY() / 32.0, (double)s18PacketEntityTeleport.getZ() / 32.0);
        }
    };
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        if (this.target != null && this.shouldActive(this.target) && (double)Util.mc.thePlayer.getDistanceToEntity(this.target) <= (Double)deactivateDist.getValue() && Util.mc.thePlayer.isSwingInProgress) {
            double x = this.realPosition.xCoord - Util.mc.getRenderManager().viewerPosX;
            double y = this.realPosition.yCoord - Util.mc.getRenderManager().viewerPosY;
            double z = this.realPosition.zCoord - Util.mc.getRenderManager().viewerPosZ;
            this.animatedX.animate((float)x, 5);
            this.animatedY.animate((float)y, 5);
            this.animatedZ.animate((float)z, 5);
            AxisAlignedBB box = Util.mc.thePlayer.getEntityBoundingBox().expand(0.1, 0.1, 0.1);
            AxisAlignedBB axis = new AxisAlignedBB(box.minX - Util.mc.thePlayer.posX + (double)this.animatedX.getOutput(), box.minY - Util.mc.thePlayer.posY + (double)this.animatedY.getOutput(), box.minZ - Util.mc.thePlayer.posZ + (double)this.animatedZ.getOutput(), box.maxX - Util.mc.thePlayer.posX + (double)this.animatedX.getOutput(), box.maxY - Util.mc.thePlayer.posY + (double)this.animatedY.getOutput(), box.maxZ - Util.mc.thePlayer.posZ + (double)this.animatedZ.getOutput());
            Color color = ColorProcess.getColor();
            RenderUtils.start3D();
            GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.627451f);
            RenderUtils.drawBoundingBox(axis);
            RenderUtils.stop3D();
            GlUtils.resetColor();
        }
    };

    public boolean shouldActive(EntityPlayer target) {
        return modeProperty.getValue() == Mode.Constant || modeProperty.getValue() == Mode.Hit && target.hurtTime != 0 || modeProperty.getValue() == Mode.Zero && target.hurtTime == 0;
    }

    public static enum Mode {
        Constant,
        Hit,
        Zero;

    }
}

