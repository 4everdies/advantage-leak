/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Block Outline", category=ModuleCategory.VISUALS)
public class BlockOutlineModule
extends Module {
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        if (Util.mc.objectMouseOver == null) {
            return;
        }
        if (Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = Util.mc.objectMouseOver.getBlockPos();
            RenderUtils.start3D();
            RenderUtils.color(ColorProcess.getColor().getRGB());
            double x = (double)pos.getX() - Util.mc.getRenderManager().renderPosX;
            double y = (double)pos.getY() - Util.mc.getRenderManager().renderPosY;
            double z = (double)pos.getZ() - Util.mc.getRenderManager().renderPosZ;
            double height = Util.mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMaxY() - Util.mc.theWorld.getBlockState(pos).getBlock().getBlockBoundsMinY();
            GL11.glLineWidth(1.0f);
            GL11.glBegin(3);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x, y + height, z);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x + 1.0, y, z);
            GL11.glVertex3d(x + 1.0, y + height, z);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x + 1.0, y, z + 1.0);
            GL11.glVertex3d(x + 1.0, y + height, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y, z + 1.0);
            GL11.glVertex3d(x, y + height, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x + 1.0, y, z);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y + height, z);
            GL11.glVertex3d(x + 1.0, y + height, z);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x, y, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y + height, z);
            GL11.glVertex3d(x, y + height, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x + 1.0, y, z + 1.0);
            GL11.glVertex3d(x + 1.0, y, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x + 1.0, y + height, z + 1.0);
            GL11.glVertex3d(x + 1.0, y + height, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x + 1.0, y, z + 1.0);
            GL11.glVertex3d(x + 1.0, y, z);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x + 1.0, y + height, z + 1.0);
            GL11.glVertex3d(x + 1.0, y + height, z);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y, z + 1.0);
            GL11.glVertex3d(x + 1.0, y, z + 1.0);
            GL11.glEnd();
            GL11.glBegin(3);
            GL11.glVertex3d(x, y + height, z + 1.0);
            GL11.glVertex3d(x + 1.0, y + height, z + 1.0);
            GL11.glEnd();
            RenderUtils.stop3D();
        }
    };
}

