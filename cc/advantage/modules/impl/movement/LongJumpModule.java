/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.player.BlockCollideEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.StrafeEvent;
import cc.advantage.api.events.impl.player.TeleportEvent;
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
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(label="Long Jump", category=ModuleCategory.MOVEMENT)
public final class LongJumpModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Vanilla);
    private final NumberProperty height = new NumberProperty("Height", 0.5, () -> this.mode.getValue() == Mode.Vanilla, 0.1, 1.0, 0.01);
    private final NumberProperty speed = new NumberProperty("Speed", 1.0, () -> this.mode.getValue() == Mode.Vanilla, 0.1, 9.5, 0.1);
    private final NumberProperty groundSpeed = new NumberProperty("Ground Speed", 0.4, () -> this.mode.getValue() == Mode.NCP, 0.1, 3.0, 0.1);
    private final NumberProperty jumpSpeed = new NumberProperty("Jump Speed", 1.4, () -> this.mode.getValue() == Mode.NCP, 0.0, 3.0, 0.1);
    private final NumberProperty glide = new NumberProperty("Glide", 0.0, () -> this.mode.getValue() == Mode.NCP, 0.0, 3.0, 0.5);
    private final NumberProperty timer = new NumberProperty("Timer", 1.0, () -> this.mode.getValue() == Mode.NCP, 0.1, 10.0, 0.1);
    private final NumberProperty mushJumpHeight = new NumberProperty("Mush Jump Height", 0.42, () -> this.mode.getValue() == Mode.Mush, 0.1, 1.0, 0.01);
    private final NumberProperty mushSpeed = new NumberProperty("Mush Speed", 0.28, () -> this.mode.getValue() == Mode.Mush, 0.1, 1.0, 0.01);
    private boolean reset;
    private double nSpeed;
    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (this.mode.getValue() == Mode.Vanilla) {
            if (Util.mc.thePlayer.onGround) {
                Util.mc.thePlayer.motionY = ((Double)this.height.getValue()).floatValue();
            }
            event.setSpeed(((Double)this.speed.getValue()).floatValue());
        }
        if (this.mode.getValue() == Mode.NCP) {
            double base = MovementUtils.getAllowedHorizontalDistance();
            if (MovementUtils.isMoving()) {
                switch (Util.mc.thePlayer.offGroundTicks) {
                    case 0: {
                        Util.mc.thePlayer.motionY = 0.42f;
                        this.nSpeed = (Double)this.groundSpeed.getValue();
                        break;
                    }
                    case 1: {
                        this.nSpeed = (Double)this.jumpSpeed.getValue();
                        break;
                    }
                    default: {
                        this.nSpeed -= this.nSpeed / (double)159.9f;
                    }
                }
                Util.mc.timer.timerSpeed = ((Double)this.timer.getValue()).floatValue();
                this.reset = false;
            } else if (!this.reset) {
                this.nSpeed = MovementUtils.getAllowedHorizontalDistance();
                Util.mc.timer.timerSpeed = 1.0f;
                this.reset = true;
            }
            if (Util.mc.thePlayer.fallDistance > 0.0f) {
                Util.mc.thePlayer.motionY += (double)(((Double)this.glide.getValue()).floatValue() / 100.0f);
            }
            if (Util.mc.thePlayer.isCollidedHorizontally) {
                this.nSpeed = MovementUtils.getAllowedHorizontalDistance();
            }
            event.setSpeed(Math.max(this.nSpeed, base), Math.random() / 2000.0);
        }
        if (this.mode.getValue() == Mode.DoubleJump && Util.mc.thePlayer.onGround) {
            Util.mc.thePlayer.jump();
            Util.mc.thePlayer.jump();
            this.toggle();
        }
    };
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        if (!e.isPre()) {
            return;
        }
        if (this.mode.getValue() == Mode.OldIntaveBoat && Util.mc.thePlayer.isRiding()) {
            PacketUtils.sendSilentPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
            PacketUtils.sendSilentPacket(new C03PacketPlayer.C04PacketPlayerPosition(Util.mc.thePlayer.posX - Math.sin(Math.toRadians(Util.mc.thePlayer.rotationYaw)) * 3.0, Util.mc.thePlayer.posY + 2.0, Util.mc.thePlayer.posZ + Math.cos(Math.toRadians(Util.mc.thePlayer.rotationYaw)) * 3.0, false));
            this.toggle();
        }
        if (this.mode.getValue() == Mode.Mush) {
            if (!MovementUtils.isMoving() || !Util.mc.gameSettings.keyBindForward.isKeyDown()) {
                return;
            }
            if (Util.mc.thePlayer.onGround) {
                Util.mc.thePlayer.motionY = ((Double)this.mushJumpHeight.getValue()).floatValue();
                MovementUtils.setSpeed(((Double)this.mushSpeed.getValue()).floatValue());
            } else {
                MovementUtils.setSpeed(((Double)this.mushSpeed.getValue()).floatValue());
            }
        }
    };
    @EventLink
    public final Listener<BlockCollideEvent> blockCollideEventListener = e -> {
        if (this.mode.getValue() == Mode.Mush && !Util.mc.thePlayer.onGround && !Util.mc.thePlayer.isSneaking() && e.getBlock() instanceof BlockAir) {
            double x = e.getX();
            double y = e.getY();
            double z = e.getZ();
            if (y < Util.mc.thePlayer.posY) {
                e.setCollisionBoundingBox(AxisAlignedBB.fromBounds(-15.0, -1.0, -15.0, 15.0, 1.0, 15.0).offset(x, y, z));
            }
        }
    };
    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        this.nSpeed = 0.0;
    };

    @Override
    public void onDisable() {
        if (this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.NCP) {
            MovementUtils.stop();
            Util.mc.timer.timerSpeed = 1.0f;
        }
        this.nSpeed = 0.0;
        super.onDisable();
    }

    private static enum Mode {
        Vanilla("Vanilla"),
        OldIntaveBoat("Old Intave Boat"),
        NCP("NCP"),
        DoubleJump("Double Jump"),
        Mush("Mush");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

