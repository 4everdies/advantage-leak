/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.mc;

import cc.advantage.utils.Util;
import com.google.common.base.Predicates;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

public class RayCastUtils
extends Util {
    public static MovingObjectPosition rayCast(Vector2f rotation, double range) {
        return RayCastUtils.rayCast(rotation, range, 0.0f);
    }

    public static MovingObjectPosition rayCast(Vector2f rotation, double range, float expand) {
        return RayCastUtils.rayCast(rotation, range, expand, RayCastUtils.mc.thePlayer);
    }

    public static MovingObjectPosition rayCast(Vector2f rotation, double range, float expand, Entity entity) {
        float partialTicks = RayCastUtils.mc.timer.renderPartialTicks;
        if (entity != null && RayCastUtils.mc.theWorld != null) {
            MovingObjectPosition objectMouseOver = entity.rayTraceCustom(range, rotation.x, rotation.y);
            double d1 = range;
            Vec3 vec3 = entity.getPositionEyes(partialTicks);
            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }
            Vec3 vec31 = RayCastUtils.mc.thePlayer.getVectorForRotation(rotation.y, rotation.x);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0f;
            List<Entity> list = RayCastUtils.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range).expand(1.0, 1.0, 1.0), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;
            for (Entity entity1 : list) {
                double d3;
                float f1 = entity1.getCollisionBorderSize() + expand;
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (!(d2 >= 0.0)) continue;
                    pointedEntity = entity1;
                    vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                    d2 = 0.0;
                    continue;
                }
                if (movingobjectposition == null || !((d3 = vec3.distanceTo(movingobjectposition.hitVec)) < d2) && d2 != 0.0) continue;
                pointedEntity = entity1;
                vec33 = movingobjectposition.hitVec;
                d2 = d3;
            }
            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                objectMouseOver = new MovingObjectPosition(pointedEntity, vec33);
            }
            return objectMouseOver;
        }
        return null;
    }

    public static boolean overBlock(Vector2f rotation, EnumFacing enumFacing, BlockPos pos, boolean strict) {
        MovingObjectPosition movingObjectPosition = RayCastUtils.mc.thePlayer.rayTraceCustom(4.5, rotation.x, rotation.y);
        if (movingObjectPosition == null) {
            return false;
        }
        Vec3 hitVec = movingObjectPosition.hitVec;
        if (hitVec == null) {
            return false;
        }
        return movingObjectPosition.getBlockPos().equals(pos) && (!strict || movingObjectPosition.sideHit == enumFacing);
    }

    public static boolean overBlock(EnumFacing enumFacing, BlockPos pos, boolean strict) {
        MovingObjectPosition movingObjectPosition = RayCastUtils.mc.objectMouseOver;
        if (movingObjectPosition == null) {
            return false;
        }
        Vec3 hitVec = movingObjectPosition.hitVec;
        if (hitVec == null || movingObjectPosition.getBlockPos() == null) {
            return false;
        }
        return movingObjectPosition.getBlockPos().equals(pos) && (!strict || movingObjectPosition.sideHit == enumFacing);
    }

    public static Boolean overBlock(Vector2f rotation, BlockPos pos) {
        return RayCastUtils.overBlock(rotation, EnumFacing.UP, pos, false);
    }

    public static Boolean overBlock(Vector2f rotation, BlockPos pos, EnumFacing enumFacing) {
        return RayCastUtils.overBlock(rotation, enumFacing, pos, true);
    }
}

