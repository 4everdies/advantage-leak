/*
 * Decompiled with CFR 0.152.
 */
package de.florianmichael.viamcp.fixes;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

public class AttackOrder {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void sendConditionalSwing(MovingObjectPosition mop) {
        if (mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
            AttackOrder.mc.thePlayer.swingItem();
        }
    }

    public static void sendFixedAttack(EntityPlayer entityIn, Entity target) {
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            AttackOrder.mc.thePlayer.swingItem();
            AttackOrder.mc.playerController.attackEntity(entityIn, target);
        } else {
            AttackOrder.mc.playerController.attackEntity(entityIn, target);
            AttackOrder.mc.thePlayer.swingItem();
        }
    }
}

