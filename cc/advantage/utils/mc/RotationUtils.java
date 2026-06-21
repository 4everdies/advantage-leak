/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.mc;

import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.RayCastUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import org.lwjgl.util.vector.Vector2f;

public class RotationUtils
extends Util {
    public static Vector2f calculate(Vector3d from, Vector3d to) {
        Vector3d diff = to.subtract(from);
        double distance = Math.hypot(diff.getX(), diff.getZ());
        float yaw = (float)(MathHelper.atan2(diff.getZ(), diff.getX()) * 57.2957763671875) - 90.0f;
        float pitch = (float)(-(MathHelper.atan2(diff.getY(), distance) * 57.2957763671875));
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f calculate(Entity entity) {
        return RotationUtils.calculate(entity.getCustomPositionVector().add(0.0, Math.max(0.0, Math.min(RotationUtils.mc.thePlayer.posY - entity.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight(), (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * 0.9)), 0.0));
    }

    public static Vector2f calculate(Entity entity, boolean adaptive, double range) {
        Vector2f normalRotations = RotationUtils.calculate(entity);
        if (!adaptive || RayCastUtils.rayCast((Vector2f)normalRotations, (double)range).typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            return normalRotations;
        }
        for (double yPercent = 1.0; yPercent >= 0.0; yPercent -= 0.25 + Math.random() * 0.1) {
            for (double xPercent = 1.0; xPercent >= -0.5; xPercent -= 0.5) {
                for (double zPercent = 1.0; zPercent >= -0.5; zPercent -= 0.5) {
                    Vector2f adaptiveRotations = RotationUtils.calculate(entity.getCustomPositionVector().add((entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) * xPercent, (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * yPercent, (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) * zPercent));
                    if (RayCastUtils.rayCast((Vector2f)adaptiveRotations, (double)range).typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) continue;
                    return adaptiveRotations;
                }
            }
        }
        return normalRotations;
    }

    public Vector2f calculate(Vec3 to, EnumFacing enumFacing) {
        return RotationUtils.calculate(new Vector3d(to.xCoord, to.yCoord, to.zCoord), enumFacing);
    }

    public static Vector2f calculate(Vec3 to) {
        return RotationUtils.calculate(RotationUtils.mc.thePlayer.getCustomPositionVector().add(0.0, RotationUtils.mc.thePlayer.getEyeHeight(), 0.0), new Vector3d(to.xCoord, to.yCoord, to.zCoord));
    }

    public Vector2f calculate(BlockPos to) {
        return RotationUtils.calculate(RotationUtils.mc.thePlayer.getCustomPositionVector().add(0.0, RotationUtils.mc.thePlayer.getEyeHeight(), 0.0), new Vector3d(to.getX(), to.getY(), to.getZ()).add(0.5, 0.5, 0.5));
    }

    public static Vector2f calculate(Vector3d to) {
        return RotationUtils.calculate(RotationUtils.mc.thePlayer.getCustomPositionVector().add(0.0, RotationUtils.mc.thePlayer.getEyeHeight(), 0.0), to);
    }

    public static Vector2f calculate(Vector3d position, EnumFacing enumFacing) {
        double x = position.getX() + 0.5;
        double y = position.getY() + 0.5;
        double z = position.getZ() + 0.5;
        return RotationUtils.calculate(new Vector3d(x += (double)enumFacing.getDirectionVec().getX() * 0.5, y += (double)enumFacing.getDirectionVec().getY() * 0.5, z += (double)enumFacing.getDirectionVec().getZ() * 0.5));
    }

    public static Vector2f puhfyRotations(Entity entity) {
        Vec3 eyePos = new Vec3(RotationUtils.mc.thePlayer.posX, RotationUtils.mc.thePlayer.posY + (double)RotationUtils.mc.thePlayer.getEyeHeight(), RotationUtils.mc.thePlayer.posZ);
        AxisAlignedBB box = entity.getEntityBoundingBox();
        Vec3[] points = new Vec3[]{new Vec3((box.minX + box.maxX) / 2.0, box.minY, (box.minZ + box.maxZ) / 2.0), new Vec3((box.minX + box.maxX) / 2.0, (box.minY + box.maxY) / 2.0, (box.minZ + box.maxZ) / 2.0), new Vec3((box.minX + box.maxX) / 2.0, box.maxY - 0.1, (box.minZ + box.maxZ) / 2.0)};
        Vec3 bestPoint = null;
        double closestDist = Double.MAX_VALUE;
        for (Vec3 point : points) {
            double dist = eyePos.distanceTo(point);
            if (!(dist < closestDist)) continue;
            closestDist = dist;
            bestPoint = point;
        }
        if (bestPoint == null) {
            return null;
        }
        float[] rotations = RotationUtils.getRotationsTo(eyePos, bestPoint);
        float targetYaw = rotations[0];
        float targetPitch = rotations[1];
        return new Vector2f(targetYaw, targetPitch);
    }

    public static float[] getRotationsTo(Vec3 from, Vec3 to) {
        double dx = to.xCoord - from.xCoord;
        double dy = to.yCoord - from.yCoord;
        double dz = to.zCoord - from.zCoord;
        double distHorizontal = MathHelper.sqrt_double(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distHorizontal)));
        yaw = RotationUtils.mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - RotationUtils.mc.thePlayer.rotationYaw);
        pitch = RotationUtils.mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - RotationUtils.mc.thePlayer.rotationPitch);
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationFromPosition(double x, double y, double z) {
        double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        double yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 1.2;
        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float)(Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(yDiff, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getNormalRotationsFromPosition(double x, double y, double z, float currentYaw, float currentPitch, float yawSpeed, float pitchSpeed) {
        if (yawSpeed < 0.0f) {
            yawSpeed *= -1.0f;
        }
        if (pitchSpeed < 0.0f) {
            pitchSpeed *= -1.0f;
        }
        float sYaw = RotationUtils.updateRotation(currentYaw, RotationUtils.getRotationFromPosition(x, y, z)[0], yawSpeed);
        float sPitch = RotationUtils.updateRotation(currentPitch, RotationUtils.getRotationFromPosition(x, y, z)[1], pitchSpeed);
        currentYaw = RotationUtils.updateRotation(currentYaw, sYaw, 360.0f);
        if ((currentPitch = RotationUtils.updateRotation(currentPitch, sPitch, 360.0f)) > 90.0f) {
            currentPitch = 90.0f;
        } else if (currentPitch < -90.0f) {
            currentPitch = -90.0f;
        }
        return new float[]{currentYaw, currentPitch};
    }

    public static float updateRotation(float current, float intended, float factor) {
        float var4 = MathHelper.wrapAngleTo180_float(intended - current);
        if (var4 > factor) {
            var4 = factor;
        }
        if (var4 < -factor) {
            var4 = -factor;
        }
        return current + var4;
    }

    public static Vector2f move(Vector2f targetRotation, double speed) {
        return RotationUtils.move(RotationProcess.lastRotations, targetRotation, speed);
    }

    public static Vector2f move(Vector2f lastRotation, Vector2f targetRotation, double speed) {
        if (speed != 0.0) {
            double deltaYaw = MathHelper.wrapAngleTo180_float(targetRotation.x - lastRotation.x);
            double deltaPitch = targetRotation.y - lastRotation.y;
            double distance = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
            double distributionYaw = Math.abs(deltaYaw / distance);
            double distributionPitch = Math.abs(deltaPitch / distance);
            double maxYaw = speed * distributionYaw;
            double maxPitch = speed * distributionPitch;
            float moveYaw = (float)Math.max(Math.min(deltaYaw, maxYaw), -maxYaw);
            float movePitch = (float)Math.max(Math.min(deltaPitch, maxPitch), -maxPitch);
            return new Vector2f(moveYaw, movePitch);
        }
        return new Vector2f(0.0f, 0.0f);
    }

    public static Vector2f applySensitivityPatch(Vector2f rotation) {
        Vector2f previousRotation = RotationUtils.mc.thePlayer.getPreviousRotation();
        float mouseSensitivity = (float)((double)RotationUtils.mc.gameSettings.mouseSensitivity * (1.0 + Math.random() / 1.0E7) * (double)0.6f + (double)0.2f);
        double multiplier = (double)(mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0f) * 0.15;
        float yaw = previousRotation.x + (float)((double)Math.round((double)(rotation.x - previousRotation.x) / multiplier) * multiplier);
        float pitch = previousRotation.y + (float)((double)Math.round((double)(rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f));
    }

    public static Vector2f applySensitivityPatch(Vector2f rotation, Vector2f previousRotation) {
        float mouseSensitivity = (float)((double)RotationUtils.mc.gameSettings.mouseSensitivity * (1.0 + Math.random() / 1.0E7) * (double)0.6f + (double)0.2f);
        double multiplier = (double)(mouseSensitivity * mouseSensitivity * mouseSensitivity * 8.0f) * 0.15;
        float yaw = previousRotation.x + (float)((double)Math.round((double)(rotation.x - previousRotation.x) / multiplier) * multiplier);
        float pitch = previousRotation.y + (float)((double)Math.round((double)(rotation.y - previousRotation.y) / multiplier) * multiplier);
        return new Vector2f(yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f));
    }

    public static float[] faceTrajectory(Entity target, boolean predict, float predictSize, float gravity, float velocity) {
        EntityPlayerSP player = RotationUtils.mc.thePlayer;
        double posX = target.posX + (predict ? (target.posX - target.prevPosX) * (double)predictSize : 0.0) - (player.posX + (predict ? player.posX - player.prevPosX : 0.0));
        double posY = target.getEntityBoundingBox().minY + (predict ? (target.getEntityBoundingBox().minY - target.prevPosY) * (double)predictSize : 0.0) + (double)target.getEyeHeight() - 0.15 - (player.getEntityBoundingBox().minY + (predict ? player.posY - player.prevPosY : 0.0)) - (double)player.getEyeHeight();
        double posZ = target.posZ + (predict ? (target.posZ - target.prevPosZ) * (double)predictSize : 0.0) - (player.posZ + (predict ? player.posZ - player.prevPosZ : 0.0));
        double posSqrt = Math.sqrt(posX * posX + posZ * posZ);
        velocity = Math.min((velocity * velocity + velocity * 2.0f) / 3.0f, 1.0f);
        float gravityModifier = 0.12f * gravity;
        return new float[]{(float)Math.toDegrees(Math.atan2(posZ, posX)) - 90.0f, (float)(-Math.toDegrees(Math.atan(((double)(velocity * velocity) - Math.sqrt((double)(velocity * velocity * velocity * velocity) - (double)gravityModifier * ((double)gravityModifier * posSqrt * posSqrt + 2.0 * posY * (double)velocity * (double)velocity))) / ((double)gravityModifier * posSqrt))))};
    }

    public static Vector2f resetRotation(Vector2f rotation) {
        if (rotation == null) {
            return null;
        }
        float yaw = rotation.x + MathHelper.wrapAngleTo180_float(RotationUtils.mc.thePlayer.rotationYaw - rotation.x);
        float pitch = RotationUtils.mc.thePlayer.rotationPitch;
        return new Vector2f(yaw, pitch);
    }

    public static Vector2f smooth(Vector2f targetRotation, double speed) {
        return RotationUtils.smooth(RotationProcess.lastRotations, targetRotation, speed);
    }

    public static Vector2f smooth(Vector2f lastRotation, Vector2f targetRotation, double speed) {
        float yaw = targetRotation.x;
        float pitch = targetRotation.y;
        float lastYaw = lastRotation.x;
        float lastPitch = lastRotation.y;
        if (speed != 0.0) {
            Vector2f move = RotationUtils.move(targetRotation, speed);
            yaw = lastYaw + move.x;
            pitch = lastPitch + move.y;
            for (int i = 1; i <= (int)((double)((float)Minecraft.getDebugFPS() / 20.0f) + Math.random() * 10.0); ++i) {
                if ((double)(Math.abs(move.x) + Math.abs(move.y)) > 1.0E-4) {
                    yaw = (float)((double)yaw + (Math.random() - 0.5) / 1000.0);
                    pitch = (float)((double)pitch - Math.random() / 200.0);
                }
                Vector2f rotations = new Vector2f(yaw, pitch);
                Vector2f fixedRotations = RotationUtils.applySensitivityPatch(rotations);
                yaw = fixedRotations.x;
                pitch = Math.max(-90.0f, Math.min(90.0f, fixedRotations.y));
            }
        }
        return new Vector2f(yaw, pitch);
    }

    public static float getMovementYaw() {
        float yaw = 180.0f;
        KeyBinding forward = RotationUtils.mc.gameSettings.keyBindForward;
        KeyBinding back = RotationUtils.mc.gameSettings.keyBindBack;
        KeyBinding right = RotationUtils.mc.gameSettings.keyBindRight;
        KeyBinding left = RotationUtils.mc.gameSettings.keyBindLeft;
        if (back.isKeyDown()) {
            yaw -= 180.0f;
            if (right.isKeyDown()) {
                yaw -= 45.0f;
            }
            if (left.isKeyDown()) {
                yaw += 45.0f;
            }
        } else if (forward.isKeyDown()) {
            if (right.isKeyDown()) {
                yaw += 45.0f;
            }
            if (left.isKeyDown()) {
                yaw -= 45.0f;
            }
        } else {
            if (right.isKeyDown()) {
                yaw += 90.0f;
            }
            if (left.isKeyDown()) {
                yaw -= 90.0f;
            }
        }
        return (MathHelper.wrapAngleTo180_float(RotationUtils.mc.thePlayer.rotationYaw) + yaw % 360.0f + 360.0f) % 360.0f;
    }
}

