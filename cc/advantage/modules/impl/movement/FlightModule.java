/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.BlockCollideEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

@ModuleInfo(label="Flight", category=ModuleCategory.MOVEMENT)
public final class FlightModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Motion);
    private final NumberProperty motionSpeed = new NumberProperty("Motion Speed", 0.9, () -> this.mode.getValue() == Mode.Motion, 0.1, 5.0, 0.1);
    private final NumberProperty teleportDelay = new NumberProperty("Teleport Delay", 5.0, () -> this.mode.getValue() == Mode.Packet, 1.0, 20.0, 1.0);
    private final NumberProperty teleportLength = new NumberProperty("Teleport Length", 5.0, () -> this.mode.getValue() == Mode.Packet, 1.0, 20.0, 1.0);
    private final NumberProperty timerAmount = new NumberProperty("Timer Amount", 1.0, 0.1, 3.0, 0.1);
    private final Property<Boolean> fullStop = new Property<Boolean>("Stop on Disable", true);
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        if (!e.isPre()) {
            return;
        }
        Util.mc.timer.timerSpeed = ((Double)this.timerAmount.getValue()).floatValue();
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 0: {
                MovementUtils.setSpeed((Double)this.motionSpeed.getValue());
                Util.mc.thePlayer.motionY = Util.mc.gameSettings.keyBindJump.isKeyDown() ? 0.5 : (Util.mc.gameSettings.keyBindSneak.isKeyDown() ? -0.5 : 0.0);
                break;
            }
            case 1: {
                if (Util.mc.gameSettings.keyBindJump.isKeyDown()) {
                    return;
                }
                PacketUtils.sendSilentPacket(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0.0f, 0.5f, 0.0f));
                MovementUtils.strafe(MovementUtils.getVerusLimit(true));
                Util.mc.thePlayer.onGround = true;
                MovementUtils.setSpeed(0.32);
                e.setOnGround(Util.mc.thePlayer.ticksExisted % 2 == 0);
                Util.mc.thePlayer.motionY = 0.0;
                e.setPosY(Math.round(Util.mc.thePlayer.posY));
                break;
            }
            case 3: {
                Util.mc.thePlayer.motionY = 0.0;
                if (!MovementUtils.isMoving() || Util.mc.thePlayer.ticksExisted % ((Double)this.teleportDelay.getValue()).intValue() != 0) break;
                double x = e.getPosX() + -Math.sin(Math.toRadians(Util.mc.thePlayer.rotationYaw)) * (double)((Double)this.teleportLength.getValue()).intValue();
                double z = e.getPosZ() + Math.cos(Math.toRadians(Util.mc.thePlayer.rotationYaw)) * (double)((Double)this.teleportLength.getValue()).intValue();
                PacketUtils.sendSilentPacket(new C03PacketPlayer.C04PacketPlayerPosition(x, Util.mc.thePlayer.posY, z, false));
                Util.mc.thePlayer.setPosition(x, Util.mc.thePlayer.posY, z);
            }
        }
    };
    @EventLink
    public final Listener<BlockCollideEvent> blockCollideEventListener = e -> {
        if (this.mode.getValue() == Mode.Collide && e.getBlock() instanceof BlockAir && !Util.mc.thePlayer.isSneaking()) {
            double x = e.getX();
            double y = e.getY();
            double z = e.getZ();
            if (y < Util.mc.thePlayer.posY) {
                e.setCollisionBoundingBox(AxisAlignedBB.fromBounds(-15.0, -1.0, -15.0, 15.0, 1.0, 15.0).offset(x, y, z));
            }
        }
    };

    @Override
    public void onDisable() {
        Util.mc.timer.timerSpeed = 1.0f;
        if (this.fullStop.getValue().booleanValue()) {
            Util.mc.thePlayer.motionX = 0.0;
            Util.mc.thePlayer.motionZ = 0.0;
        }
    }

    private static enum Mode {
        Motion("Motion"),
        Verus("Verus"),
        Collide("Collide"),
        Packet("Packet");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

