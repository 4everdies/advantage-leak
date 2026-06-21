/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.MovePlayerEvent;
import cc.advantage.api.events.impl.player.StrafeEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Speed", category=ModuleCategory.MOVEMENT)
public final class SpeedModule
extends Module {
    private static final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Legit);
    private static final NumberProperty vanillaSpeed = new NumberProperty("Vanilla Speed", 1.0, () -> mode.getValue() == Mode.Vanilla, 0.1, 9.5, 0.1);
    private double speedV;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)mode.getValue()))).toString());
        if (Util.mc.gameSettings.keyBindJump.isKeyDown() && mode.getValue() != Mode.Legit && mode.getValue() != Mode.LegitExploit && mode.getValue() != Mode.OldGrim) {
            Util.mc.gameSettings.keyBindJump.setPressed(false);
        }
        if (!e.isPre()) {
            return;
        }
        switch (((Mode)((Object)((Object)mode.getValue()))).ordinal()) {
            case 3: {
                if (Util.mc.gameSettings.keyBindJump.isKeyDown()) {
                    return;
                }
                PacketUtils.sendSilentPacket(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0.0f, 0.5f, 0.0f));
                if (MovementUtils.isMoving()) {
                    if (Util.mc.thePlayer.onGround) {
                        Util.mc.thePlayer.motionY = 1.0E-5f;
                    }
                } else {
                    Util.mc.thePlayer.motionZ = 0.0;
                    Util.mc.thePlayer.motionX = 0.0;
                }
                MovementUtils.strafe(MovementUtils.getVerusLimit(true));
                if (!((double)Util.mc.thePlayer.fallDistance > 0.2)) break;
                Util.mc.thePlayer.motionY = -0.1f;
                break;
            }
            case 0: {
                if (!MovementUtils.isMoving()) break;
                MovementUtils.setSpeed(((Double)vanillaSpeed.getValue()).floatValue());
                if (!MovementUtils.isOnGround()) break;
                Util.mc.thePlayer.jump();
                break;
            }
            case 4: {
                PacketUtils.sendSilentPacket(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, new ItemStack(Items.water_bucket), 0.0f, 0.5f, 0.0f));
                if (Util.mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    Util.mc.thePlayer.motionY = 0.42;
                    MovementUtils.strafe(0.48f, 0.52f, 0.6f);
                }
                if (Util.mc.thePlayer.offGroundTicks != 1) break;
                Util.mc.thePlayer.motionY = -0.15233518685055714;
                break;
            }
            case 7: {
                if (!MovementUtils.isMoving()) break;
                MovementUtils.strafe();
                if (!MovementUtils.isOnGround()) break;
                Util.mc.thePlayer.jump();
                break;
            }
            case 8: {
                if (!MovementUtils.isMoving() || !MovementUtils.isOnGround()) break;
                Util.mc.thePlayer.jump();
                MovementUtils.strafe();
                break;
            }
            case 5: {
                if (MovementUtils.isMoving() && !Util.mc.gameSettings.keyBindJump.isKeyDown()) {
                    MovementUtils.strafe();
                    Util.mc.timer.timerSpeed = (double)Util.mc.thePlayer.offGroundTicks >= 5.1 ? 1.2f : 1.0f;
                    if (Util.mc.thePlayer.onGround) {
                        Util.mc.thePlayer.jump();
                    }
                }
                if (MovementUtils.isMoving()) break;
                Util.mc.timer.timerSpeed = 1.0f;
                break;
            }
            case 1: 
            case 10: {
                Util.mc.gameSettings.keyBindJump.setPressed(MovementUtils.isMoving() && MovementUtils.isOnGround());
                break;
            }
            case 2: {
                Util.mc.gameSettings.keyBindJump.setPressed(MovementUtils.isMoving() && MovementUtils.isOnGround());
                Util.mc.timer.timerSpeed = 1.0075f;
                break;
            }
            case 9: {
                if (MovementUtils.isOnGround() && MovementUtils.isMoving()) {
                    Util.mc.thePlayer.jump();
                }
                switch (Util.mc.thePlayer.offGroundTicks) {
                    case 1: {
                        Util.mc.thePlayer.motionX *= 1.005;
                        Util.mc.thePlayer.motionZ *= 1.005;
                        break;
                    }
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 6: {
                        Util.mc.thePlayer.motionX *= 1.011;
                        Util.mc.thePlayer.motionZ *= 1.011;
                    }
                }
                if (Util.mc.thePlayer.onGroundTicks == 1) {
                    Util.mc.thePlayer.motionX *= 1.0045;
                    Util.mc.thePlayer.motionZ *= 1.0045;
                }
                Util.mc.timer.timerSpeed = 1.0075f;
                break;
            }
            case 6: {
                if (!MovementUtils.isMoving()) break;
                Util.mc.timer.timerSpeed = (double)Util.mc.thePlayer.fallDistance > 1.0 ? 0.8f : 1.9f;
                if (Util.mc.thePlayer.onGround) {
                    Util.mc.thePlayer.motionY = 0.42f;
                    float speed = 0.031f;
                    if (Util.mc.thePlayer.speedInAir < speed) {
                        if ((double)Util.mc.thePlayer.speedInAir < 0.025) {
                            Util.mc.thePlayer.speedInAir = (float)((double)0.025f + Math.random() / 100.0);
                        }
                        Util.mc.thePlayer.speedInAir += 0.0091f;
                    } else {
                        Util.mc.thePlayer.speedInAir = speed;
                    }
                    if (!((double)Util.mc.thePlayer.jumpMovementFactor > 0.022)) break;
                    Util.mc.thePlayer.jumpMovementFactor -= 0.002f;
                    break;
                }
                Util.mc.thePlayer.motionY *= 1.0;
                Util.mc.thePlayer.motionX *= (double)0.982f;
                Util.mc.thePlayer.motionZ *= (double)0.982f;
                Util.mc.thePlayer.speedInAir -= 1.9E-4f;
                if ((double)Util.mc.thePlayer.fallDistance > 0.4 && (double)Util.mc.thePlayer.fallDistance < 0.41) {
                    Util.mc.thePlayer.motionY -= (double)0.1f;
                }
                if (Util.mc.thePlayer.hurtTime <= 4) break;
                Util.mc.thePlayer.speedInAir += 0.006f;
            }
        }
    };
    @EventLink
    public final Listener<MovePlayerEvent> movePlayerEventListener = e -> {
        if (mode.getValue() == Mode.NCP && Util.mc.thePlayer.onGround) {
            Util.mc.thePlayer.motionY = 0.42f;
            e.setY(0.42f);
            if (this.speedV < 0.2805) {
                this.speedV = 0.2805;
            }
            this.speedV *= 1.949;
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (!MovementUtils.isOnGround() && mode.getValue() == Mode.LegitExploit) {
            RotationProcess.setRotations(new Vector2f(Util.mc.thePlayer.rotationYaw + 45.0f, Util.mc.thePlayer.rotationPitch), 10.0, MovementFix.NORMAL);
        }
    };
    @EventLink(value=4)
    public final Listener<StrafeEvent> strafe = event -> {
        if (mode.getValue() == Mode.OldGrim) {
            Util.mc.theWorld.playerEntities.stream().filter(entityPlayer -> entityPlayer != Util.mc.thePlayer && Util.mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0).intersectsWith(entityPlayer.getEntityBoundingBox())).forEach(entityPlayer -> MovementUtils.moveFlying(0.08));
        }
    };

    @Override
    public void onDisable() {
        Util.mc.timer.timerSpeed = 1.0f;
        if (mode.getValue() == Mode.UpdatedNCP || mode.getValue() == Mode.NCP) {
            Util.mc.thePlayer.speedInAir = 0.02f;
        }
        if (mode.getValue() == Mode.Legit && Util.mc.gameSettings.keyBindJump.isPressed()) {
            Util.mc.gameSettings.keyBindJump.setPressed(false);
        }
        if (mode.getValue() == Mode.LegitExploit && Util.mc.gameSettings.keyBindJump.isPressed()) {
            Util.mc.gameSettings.keyBindJump.setPressed(false);
        }
        if (mode.getValue() == Mode.OldGrim && Util.mc.gameSettings.keyBindJump.isPressed()) {
            Util.mc.gameSettings.keyBindJump.setPressed(false);
        }
    }

    private static enum Mode {
        Vanilla("Vanilla"),
        Legit("Legit"),
        LegitExploit("Legit Exploit"),
        VerusGround("Verus Ground"),
        VerusLowHop("Verus Low Hop"),
        UpdatedNCP("Updated NCP"),
        NCP("NCP"),
        Strafe("Strafe"),
        OldHypixel("Old Hypixel"),
        Intave("Intave"),
        OldGrim("Old Grim");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

