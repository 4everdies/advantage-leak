/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.mc;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.MoveEvent;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.mc.PlayerUtils;
import java.util.Arrays;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;

public class MovementUtils
extends Util {
    public static final double WALK_SPEED = 0.221;
    public static final double BUNNY_SLOPE = 0.66;
    public static final double MOD_SPRINTING = (double)1.3f;
    public static final double MOD_SNEAK = (double)0.3f;
    public static final double MOD_ICE = 2.5;
    public static final double MOD_WEB = 0.4751131221719457;
    public static final double JUMP_HEIGHT = (double)0.42f;
    public static final double BUNNY_FRICTION = (double)159.9f;
    public static final double Y_ON_GROUND_MIN = 1.0E-5;
    public static final double Y_ON_GROUND_MAX = 0.0626;
    public static final double LILYPAD_HEIGHT = 0.015625;
    public static final double AIR_FRICTION = (double)0.98f;
    public static final double WATER_FRICTION = (double)0.8f;
    public static final double LAVA_FRICTION = 0.5;
    public static final double MOD_SWIM = 0.5203620003898759;
    public static final double[] MOD_DEPTH_STRIDER = new double[]{1.0, 1.4304347400741908, 1.7347825295420372, 1.9217390955733897};
    public static final double UNLOADED_CHUNK_MOTION = -0.09800000190735147;
    public static final double HEAD_HITTER_MOTION = -0.0784000015258789;

    public static double getBaseMoveSpeed() {
        double baseSpeed = 0.2873;
        if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.16 * (double)(MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    public static double getBaseMoveSpeed(double base) {
        double baseSpeed = base;
        if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            baseSpeed *= 1.0 + 0.2 * (double)(MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
            baseSpeed /= 1.0 + 0.2 * (double)(MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSlowdown).getAmplifier() + 1);
        }
        return baseSpeed;
    }

    private static boolean isMovingEnoughForSprint() {
        MovementInput movementInput = MovementUtils.mc.thePlayer.movementInput;
        return movementInput.moveForward > 0.8f || movementInput.moveForward < -0.8f || movementInput.moveStrafe > 0.8f || movementInput.moveStrafe < -0.8f;
    }

    public static boolean canSprint(boolean omni) {
        EntityPlayerSP player = MovementUtils.mc.thePlayer;
        return (omni ? MovementUtils.isMovingEnoughForSprint() : player.movementInput.moveForward >= 0.8f) && !player.isCollidedHorizontally && (player.getFoodStats().getFoodLevel() > 6 || player.capabilities.allowFlying) && !player.isSneaking() && !player.isUsingItem() && !player.isPotionActive(Potion.moveSlowdown.id);
    }

    public static boolean isMoving() {
        return MovementUtils.mc.thePlayer.movementInput.moveForward != 0.0f || MovementUtils.mc.thePlayer.movementInput.moveStrafe != 0.0f;
    }

    public static boolean isOnGround() {
        return MovementUtils.mc.thePlayer.onGround && MovementUtils.mc.thePlayer.isCollidedVertically;
    }

    public static void setSpeed(double moveSpeed) {
        MovementUtils.setSpeed(moveSpeed, MovementUtils.mc.thePlayer.rotationYaw, MovementUtils.mc.thePlayer.movementInput.moveStrafe, MovementUtils.mc.thePlayer.movementInput.moveForward);
    }

    public static void setSpeed(MotionEvent e, double speed) {
        EntityPlayerSP player = MovementUtils.mc.thePlayer;
        MovementUtils.setSpeed(e, speed, player.moveForward, player.moveStrafing, player.rotationYaw);
    }

    public static void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (float)(forward > 0.0 ? -45 : 45);
            } else if (strafe < 0.0) {
                yaw += (float)(forward > 0.0 ? 45 : -45);
            }
            strafe = 0.0;
            if (forward > 0.0) {
                forward = 1.0;
            } else if (forward < 0.0) {
                forward = -1.0;
            }
        }
        if (strafe > 0.0) {
            strafe = 1.0;
        } else if (strafe < 0.0) {
            strafe = -1.0;
        }
        double mx = Math.cos(Math.toRadians(yaw + 90.0f));
        double mz = Math.sin(Math.toRadians(yaw + 90.0f));
        MovementUtils.mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        MovementUtils.mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
    }

    public static void setSpeed(MotionEvent e, double speed, float forward, float strafing, float yaw) {
        boolean reversed;
        if (forward == 0.0f && strafing == 0.0f) {
            return;
        }
        boolean bl = reversed = forward < 0.0f;
        float strafingYaw = 90.0f * (forward > 0.0f ? 0.5f : (reversed ? -0.5f : 1.0f));
        if (reversed) {
            yaw += 180.0f;
        }
        if (strafing > 0.0f) {
            yaw -= strafingYaw;
        } else if (strafing < 0.0f) {
            yaw += strafingYaw;
        }
        double x = StrictMath.cos(StrictMath.toRadians(yaw + 90.0f));
        double z = StrictMath.cos(StrictMath.toRadians(yaw));
        e.setPosX(x * speed);
        e.setPosZ(z * speed);
    }

    public static double predictedMotion(double motion) {
        return (motion - 0.08) * (double)0.98f;
    }

    public static double getJumpBoostModifier(double baseJumpHeight) {
        if (MovementUtils.mc.thePlayer.isPotionActive(Potion.jump)) {
            int amplifier = MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
            baseJumpHeight += (double)((float)(amplifier + 1) * 0.1f);
        }
        return baseJumpHeight;
    }

    public static void strafe(float v, float v1, float v2) {
        float speedValue = 0.0f;
        speedValue = MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSpeed) ? (MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0 ? v1 : v2) : v;
        MovementUtils.strafe(speedValue);
    }

    public static double getVerusLimit(boolean dif) {
        if (dif && (double)MovementUtils.mc.thePlayer.fallDistance > 0.2) {
            return MovementUtils.getBaseMoveSpeed();
        }
        if ((double)MovementUtils.mc.thePlayer.fallDistance < 0.2) {
            if (MovementUtils.mc.thePlayer.isSprinting()) {
                if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    for (PotionEffect effect : MovementUtils.mc.thePlayer.getActivePotionEffects()) {
                        if (effect.getPotionID() != 1) continue;
                        return MovementUtils.mc.thePlayer.onGround ? (double)(effect.getAmplifier() == 1 ? 0.7f : 0.62f) : (double)(effect.getAmplifier() == 1 ? 0.81f : 0.62f);
                    }
                }
                return MovementUtils.mc.thePlayer.onGround ? (double)0.54f : (double)0.46f;
            }
            return MovementUtils.getBaseMoveSpeed() * (double)1.02f;
        }
        return 0.0;
    }

    public static double getAllowedHorizontalDistance() {
        double horizontalDistance;
        boolean useBaseModifiers = false;
        if (MovementUtils.mc.thePlayer.isInWeb) {
            horizontalDistance = 0.105;
        } else if (PlayerUtils.onLiquid()) {
            horizontalDistance = 0.11500000208616258;
            int depthStriderLevel = EnchantmentHelper.getDepthStriderModifier(MovementUtils.mc.thePlayer);
            if (depthStriderLevel > 0) {
                horizontalDistance *= MOD_DEPTH_STRIDER[depthStriderLevel];
                useBaseModifiers = true;
            }
        } else if (MovementUtils.mc.thePlayer.isSneaking()) {
            horizontalDistance = 0.0663000026345253;
        } else {
            horizontalDistance = 0.221;
            useBaseModifiers = true;
        }
        if (useBaseModifiers) {
            if (MovementUtils.canSprint(false)) {
                horizontalDistance *= (double)1.3f;
            }
            ScaffoldModule scaffold = Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class);
            if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSpeed) && MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getDuration() > 0 && !scaffold.isEnabled()) {
                horizontalDistance *= 1.0 + 0.2 * (double)(MovementUtils.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
            }
            if (MovementUtils.mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
                horizontalDistance = 0.29;
            }
        }
        return horizontalDistance;
    }

    public void strafe(MotionEvent event) {
        MovementUtils.strafe(event, MovementUtils.getSpeed());
    }

    public static void strafe(double movementSpeed) {
        MovementUtils.strafe(null, movementSpeed);
    }

    public static void strafe(MotionEvent motionEvent, double movementSpeed) {
        if ((double)MovementUtils.mc.thePlayer.movementInput.moveForward > 0.0) {
            MovementUtils.mc.thePlayer.movementInput.moveForward = 1.0f;
        } else if ((double)MovementUtils.mc.thePlayer.movementInput.moveForward < 0.0) {
            MovementUtils.mc.thePlayer.movementInput.moveForward = -1.0f;
        }
        if ((double)MovementUtils.mc.thePlayer.movementInput.moveStrafe > 0.0) {
            MovementUtils.mc.thePlayer.movementInput.moveStrafe = 1.0f;
        } else if ((double)MovementUtils.mc.thePlayer.movementInput.moveStrafe < 0.0) {
            MovementUtils.mc.thePlayer.movementInput.moveStrafe = -1.0f;
        }
        if ((double)MovementUtils.mc.thePlayer.movementInput.moveForward == 0.0 && (double)MovementUtils.mc.thePlayer.movementInput.moveStrafe == 0.0) {
            MovementUtils.mc.thePlayer.motionX = 0.0;
            MovementUtils.mc.thePlayer.motionZ = 0.0;
        }
        if ((double)MovementUtils.mc.thePlayer.movementInput.moveForward != 0.0 && (double)MovementUtils.mc.thePlayer.movementInput.moveStrafe != 0.0) {
            MovementUtils.mc.thePlayer.movementInput.moveForward *= (float)Math.sin(0.6398355709958845);
            MovementUtils.mc.thePlayer.movementInput.moveStrafe *= (float)Math.cos(0.6398355709958845);
        }
        if (motionEvent != null) {
            MovementUtils.mc.thePlayer.motionX = (double)MovementUtils.mc.thePlayer.movementInput.moveForward * movementSpeed * -Math.sin(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw)) + (double)MovementUtils.mc.thePlayer.movementInput.moveStrafe * movementSpeed * Math.cos(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw));
            motionEvent.setPosX(MovementUtils.mc.thePlayer.motionX);
            MovementUtils.mc.thePlayer.motionZ = (double)MovementUtils.mc.thePlayer.movementInput.moveForward * movementSpeed * Math.cos(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw)) - (double)MovementUtils.mc.thePlayer.movementInput.moveStrafe * movementSpeed * -Math.sin(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw));
            motionEvent.setPosZ(MovementUtils.mc.thePlayer.motionZ);
        } else {
            MovementUtils.mc.thePlayer.motionX = (double)MovementUtils.mc.thePlayer.movementInput.moveForward * movementSpeed * -Math.sin(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw)) + (double)MovementUtils.mc.thePlayer.movementInput.moveStrafe * movementSpeed * Math.cos(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw));
            MovementUtils.mc.thePlayer.motionZ = (double)MovementUtils.mc.thePlayer.movementInput.moveForward * movementSpeed * Math.cos(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw)) - (double)MovementUtils.mc.thePlayer.movementInput.moveStrafe * movementSpeed * -Math.sin(Math.toRadians(MovementUtils.mc.thePlayer.rotationYaw));
        }
    }

    public static void strafe() {
        MovementUtils.strafe(MovementUtils.getSpeed());
    }

    public static double getSpeed() {
        return MovementUtils.mc.thePlayer == null ? 0.0 : Math.sqrt(MovementUtils.mc.thePlayer.motionX * MovementUtils.mc.thePlayer.motionX + MovementUtils.mc.thePlayer.motionZ * MovementUtils.mc.thePlayer.motionZ);
    }

    public static void fixMovement(MoveEvent event, float yaw) {
        float forward = event.getForward();
        float strafe = event.getStrafe();
        double angle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtils.direction(MovementUtils.mc.thePlayer.rotationYaw, forward, strafe)));
        if (forward == 0.0f && strafe == 0.0f) {
            return;
        }
        float closestForward = 0.0f;
        float closestStrafe = 0.0f;
        float closestDifference = Float.MAX_VALUE;
        for (float predictedForward = -1.0f; predictedForward <= 1.0f; predictedForward += 1.0f) {
            for (float predictedStrafe = -1.0f; predictedStrafe <= 1.0f; predictedStrafe += 1.0f) {
                double predictedAngle;
                double difference;
                if (predictedStrafe == 0.0f && predictedForward == 0.0f || !((difference = MathUtils.wrappedDifference(angle, predictedAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MovementUtils.direction(yaw, predictedForward, predictedStrafe))))) < (double)closestDifference)) continue;
                closestDifference = (float)difference;
                closestForward = predictedForward;
                closestStrafe = predictedStrafe;
            }
        }
        event.setForward(closestForward);
        event.setStrafe(closestStrafe);
    }

    public static double direction() {
        float rotationYaw = MovementUtils.mc.thePlayer.movementYaw;
        if (MovementUtils.mc.thePlayer.moveForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (MovementUtils.mc.thePlayer.moveForward < 0.0f) {
            forward = -0.5f;
        } else if (MovementUtils.mc.thePlayer.moveForward > 0.0f) {
            forward = 0.5f;
        }
        if (MovementUtils.mc.thePlayer.moveStrafing > 0.0f) {
            rotationYaw -= 90.0f * forward;
        }
        if (MovementUtils.mc.thePlayer.moveStrafing < 0.0f) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double direction(MoveEvent moveInputEvent) {
        float rotationYaw = MovementUtils.mc.thePlayer.movementYaw;
        if (moveInputEvent.getForward() < 0.0f) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (moveInputEvent.getForward() < 0.0f) {
            forward = -0.5f;
        } else if (moveInputEvent.getForward() > 0.0f) {
            forward = 0.5f;
        }
        if (moveInputEvent.getStrafe() > 0.0f) {
            rotationYaw -= 70.0f * forward;
        }
        if (moveInputEvent.getStrafe() < 0.0f) {
            rotationYaw += 70.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double direction(float inputForward, float inputStrafe) {
        float rotationYaw = MovementUtils.mc.thePlayer.movementYaw;
        if (inputForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (inputForward < 0.0f) {
            forward = -0.5f;
        } else if (inputForward > 0.0f) {
            forward = 0.5f;
        }
        if (inputStrafe > 0.0f) {
            rotationYaw -= 70.0f * forward;
        }
        if (inputStrafe < 0.0f) {
            rotationYaw += 70.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double direction(float rotationYaw, double moveForward, double moveStrafing) {
        if (moveForward < 0.0) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (moveForward < 0.0) {
            forward = -0.5f;
        } else if (moveForward > 0.0) {
            forward = 0.5f;
        }
        if (moveStrafing > 0.0) {
            rotationYaw -= 90.0f * forward;
        }
        if (moveStrafing < 0.0) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static void stop() {
        MovementUtils.mc.thePlayer.motionX = 0.0;
        MovementUtils.mc.thePlayer.motionZ = 0.0;
    }

    public static void useDiagonalSpeed() {
        boolean active;
        KeyBinding[] gameSettings = new KeyBinding[]{MovementUtils.mc.gameSettings.keyBindForward, MovementUtils.mc.gameSettings.keyBindRight, MovementUtils.mc.gameSettings.keyBindBack, MovementUtils.mc.gameSettings.keyBindLeft};
        int[] down = new int[]{0};
        Arrays.stream(gameSettings).forEach(keyBinding -> {
            down[0] = down[0] + (keyBinding.isKeyDown() ? 1 : 0);
        });
        boolean bl = active = down[0] == 1;
        if (!active) {
            return;
        }
        double groundIncrease = 0.0026000750109401644;
        double airIncrease = 5.199896488849598E-4;
        double increase = MovementUtils.mc.thePlayer.onGround ? 0.0026000750109401644 : 5.199896488849598E-4;
        MovementUtils.moveFlying(increase);
    }

    public static void moveFlying(double increase) {
        if (!MovementUtils.isMoving()) {
            return;
        }
        double yaw = MovementUtils.direction();
        MovementUtils.mc.thePlayer.motionX += (double)(-MathHelper.sin((float)yaw)) * increase;
        MovementUtils.mc.thePlayer.motionZ += (double)MathHelper.cos((float)yaw) * increase;
    }
}

