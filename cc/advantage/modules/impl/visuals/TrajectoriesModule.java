/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.Random;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Trajectories", category=ModuleCategory.VISUALS)
public class TrajectoriesModule
extends Module {
    private final NumberProperty width = new NumberProperty("Width", 6.0, 0.0, 12.0, 1.0);
    private final ArrayList<Vec3> positions = new ArrayList();
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        this.positions.clear();
        ItemStack itemStack = Util.mc.thePlayer.getCurrentEquippedItem();
        Object m = null;
        if (itemStack != null && (itemStack.getItem() instanceof ItemSnowball || itemStack.getItem() instanceof ItemEgg || itemStack.getItem() instanceof ItemBow || itemStack.getItem() instanceof ItemEnderPearl)) {
            float prevRotationPitch;
            float prevRotationYaw;
            EntityPlayerSP thrower = Util.mc.thePlayer;
            float rotationYaw = thrower.prevRotationYaw + (thrower.rotationYaw - thrower.prevRotationYaw) * Util.mc.timer.renderPartialTicks;
            float rotationPitch = thrower.prevRotationPitch + (thrower.rotationPitch - thrower.prevRotationPitch) * Util.mc.timer.renderPartialTicks;
            double posX = thrower.lastTickPosX + (thrower.posX - thrower.lastTickPosX) * (double)Util.mc.timer.renderPartialTicks;
            double posY = thrower.lastTickPosY + (double)((Entity)thrower).getEyeHeight() + (thrower.posY - thrower.lastTickPosY) * (double)Util.mc.timer.renderPartialTicks;
            double posZ = thrower.lastTickPosZ + (thrower.posZ - thrower.lastTickPosZ) * (double)Util.mc.timer.renderPartialTicks;
            posX -= (double)(MathHelper.cos(rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
            posY -= (double)0.1f;
            posZ -= (double)(MathHelper.sin(rotationYaw / 180.0f * (float)Math.PI) * 0.16f);
            float multipicator = 0.4f;
            if (itemStack.getItem() instanceof ItemBow) {
                multipicator = 1.0f;
            }
            double motionX = -MathHelper.sin(rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float)Math.PI) * multipicator;
            double motionZ = MathHelper.cos(rotationYaw / 180.0f * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0f * (float)Math.PI) * multipicator;
            double motionY = -MathHelper.sin(rotationPitch / 180.0f * (float)Math.PI) * multipicator;
            double x = motionX;
            double y = motionY;
            double z = motionZ;
            float inaccuracy = 0.0f;
            float velocity = 1.5f;
            if (itemStack.getItem() instanceof ItemBow) {
                int i = Util.mc.thePlayer.getItemInUseDuration() - Util.mc.thePlayer.getItemInUseCount();
                float f = (float)i / 20.0f;
                if ((double)(f = (f * f + f * 2.0f) / 3.0f) < 0.1) {
                    return;
                }
                if (f > 1.0f) {
                    f = 1.0f;
                }
                velocity = f * 2.0f * 1.5f;
            }
            Random rand = new Random();
            float ff = MathHelper.sqrt_double(x * x + y * y + z * z);
            x /= (double)ff;
            y /= (double)ff;
            z /= (double)ff;
            x += rand.nextGaussian() * (double)0.0075f * 0.0;
            y += rand.nextGaussian() * (double)0.0075f * 0.0;
            z += rand.nextGaussian() * (double)0.0075f * 0.0;
            motionX = x *= (double)velocity;
            motionY = y *= (double)velocity;
            motionZ = z *= (double)velocity;
            rotationYaw = prevRotationYaw = (float)(MathHelper.atan2(x, z) * 180.0 / Math.PI);
            rotationPitch = prevRotationPitch = (float)(MathHelper.atan2(y, MathHelper.sqrt_double(x * x + z * z)) * 180.0 / Math.PI);
            boolean b = true;
            int ticksInAir = 0;
            while (b) {
                if (ticksInAir > 300) {
                    b = false;
                }
                ++ticksInAir;
                Vec3 vec3 = new Vec3(posX, posY, posZ);
                Vec3 vec4 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                Object movingobjectposition = Util.mc.theWorld.rayTraceBlocks(vec3, vec4);
                vec3 = new Vec3(posX, posY, posZ);
                vec4 = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                if (movingobjectposition != null) {
                    vec4 = new Vec3(((MovingObjectPosition)movingobjectposition).hitVec.xCoord, ((MovingObjectPosition)movingobjectposition).hitVec.yCoord, ((MovingObjectPosition)movingobjectposition).hitVec.zCoord);
                }
                for (Entity entity : Util.mc.theWorld.loadedEntityList) {
                    if (entity == Util.mc.thePlayer || !(entity instanceof EntityLivingBase)) continue;
                    float f2 = 0.3f;
                    AxisAlignedBB localAxisAlignedBB = entity.getEntityBoundingBox().expand(0.3f, 0.3f, 0.3f);
                    MovingObjectPosition localMovingObjectPosition = localAxisAlignedBB.calculateIntercept(vec3, vec4);
                    if (localMovingObjectPosition == null) continue;
                    movingobjectposition = localMovingObjectPosition;
                    break;
                }
                if (movingobjectposition != null) {
                    b = false;
                }
                m = movingobjectposition;
                posX += motionX;
                posY += motionY;
                posZ += motionZ;
                float f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
                rotationYaw = (float)(MathHelper.atan2(motionX, motionZ) * 180.0 / Math.PI);
                rotationPitch = (float)(MathHelper.atan2(motionY, f3) * 180.0 / Math.PI);
                while (rotationPitch - prevRotationPitch < -180.0f) {
                    prevRotationPitch -= 360.0f;
                }
                while (rotationPitch - prevRotationPitch >= 180.0f) {
                    prevRotationPitch += 360.0f;
                }
                while (rotationYaw - prevRotationYaw < -180.0f) {
                    prevRotationYaw -= 360.0f;
                }
                while (rotationYaw - prevRotationYaw >= 180.0f) {
                    prevRotationYaw += 360.0f;
                }
                float f4 = 0.99f;
                float f5 = 0.03f;
                if (itemStack.getItem() instanceof ItemBow) {
                    f5 = 0.05f;
                }
                motionX *= (double)0.99f;
                motionY *= (double)0.99f;
                motionZ *= (double)0.99f;
                motionY -= (double)f5;
                this.positions.add(new Vec3(posX, posY, posZ));
            }
            if (this.positions.size() > 1) {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glDisable(3553);
                GlStateManager.disableCull();
                GL11.glDepthMask(false);
                GL11.glColor4f((float)ColorProcess.getColor().getRed() / 255.0f, (float)ColorProcess.getColor().getGreen() / 255.0f, (float)ColorProcess.getColor().getBlue() / 255.0f, 0.7f);
                GL11.glLineWidth(((Double)this.width.getValue()).floatValue() / 2.0f);
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                worldrenderer.begin(3, DefaultVertexFormats.POSITION);
                for (Vec3 vec5 : this.positions) {
                    worldrenderer.pos((double)((float)vec5.xCoord) - Util.mc.getRenderManager().getRenderPosX(), (double)((float)vec5.yCoord) - Util.mc.getRenderManager().getRenderPosY(), (double)((float)vec5.zCoord) - Util.mc.getRenderManager().getRenderPosZ()).endVertex();
                }
                tessellator.draw();
                if (m != null) {
                    GL11.glColor4f((float)ColorProcess.getColor().getRed() / 255.0f, (float)ColorProcess.getColor().getGreen() / 255.0f, (float)ColorProcess.getColor().getBlue() / 255.0f, 0.3f);
                    Vec3 hitVec = ((MovingObjectPosition)m).hitVec;
                    EnumFacing enumFacing1 = ((MovingObjectPosition)m).sideHit;
                    float minX = (float)(hitVec.xCoord - Util.mc.getRenderManager().getRenderPosX());
                    float maxX = (float)(hitVec.xCoord - Util.mc.getRenderManager().getRenderPosX());
                    float minY = (float)(hitVec.yCoord - Util.mc.getRenderManager().getRenderPosY());
                    float maxY = (float)(hitVec.yCoord - Util.mc.getRenderManager().getRenderPosY());
                    float minZ = (float)(hitVec.zCoord - Util.mc.getRenderManager().getRenderPosZ());
                    float maxZ = (float)(hitVec.zCoord - Util.mc.getRenderManager().getRenderPosZ());
                    if (enumFacing1 == EnumFacing.SOUTH) {
                        minX -= 0.4f;
                        maxX += 0.4f;
                        minY -= 0.4f;
                        maxY += 0.4f;
                        maxZ += 0.02f;
                        minZ += 0.05f;
                    } else if (enumFacing1 == EnumFacing.NORTH) {
                        minX -= 0.4f;
                        maxX += 0.4f;
                        minY -= 0.4f;
                        maxY += 0.4f;
                        maxZ -= 0.02f;
                        minZ -= 0.05f;
                    } else if (enumFacing1 == EnumFacing.EAST) {
                        maxX += 0.02f;
                        minX += 0.05f;
                        minY -= 0.4f;
                        maxY += 0.4f;
                        minZ -= 0.4f;
                        maxZ += 0.4f;
                    } else if (enumFacing1 == EnumFacing.WEST) {
                        maxX -= 0.02f;
                        minX -= 0.05f;
                        minY -= 0.4f;
                        maxY += 0.4f;
                        minZ -= 0.4f;
                        maxZ += 0.4f;
                    } else if (enumFacing1 == EnumFacing.UP) {
                        minX -= 0.4f;
                        maxX += 0.4f;
                        maxY += 0.02f;
                        minY += 0.05f;
                        minZ -= 0.4f;
                        maxZ += 0.4f;
                    } else if (enumFacing1 == EnumFacing.DOWN) {
                        minX -= 0.4f;
                        maxX += 0.4f;
                        maxY -= 0.02f;
                        minY -= 0.05f;
                        minZ -= 0.4f;
                        maxZ += 0.4f;
                    }
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION);
                    worldrenderer.pos(minX, minY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, maxZ).endVertex();
                    worldrenderer.pos(minX, maxY, maxZ).endVertex();
                    worldrenderer.pos(minX, maxY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, maxZ).endVertex();
                    worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                    worldrenderer.pos(minX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, minZ).endVertex();
                    worldrenderer.pos(maxX, maxY, minZ).endVertex();
                    worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, minZ).endVertex();
                    worldrenderer.pos(minX, maxY, minZ).endVertex();
                    worldrenderer.pos(maxX, maxY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, minZ).endVertex();
                    worldrenderer.pos(minX, maxY, minZ).endVertex();
                    worldrenderer.pos(minX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, maxY, minZ).endVertex();
                    worldrenderer.endVertex();
                    tessellator.draw();
                    GL11.glLineWidth(2.0f);
                    worldrenderer.begin(3, DefaultVertexFormats.POSITION);
                    worldrenderer.pos(minX, minY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, maxZ).endVertex();
                    worldrenderer.pos(minX, maxY, maxZ).endVertex();
                    worldrenderer.pos(minX, maxY, minZ).endVertex();
                    worldrenderer.pos(minX, minY, minZ).endVertex();
                    worldrenderer.pos(maxX, minY, minZ).endVertex();
                    worldrenderer.pos(maxX, maxY, minZ).endVertex();
                    worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, maxZ).endVertex();
                    worldrenderer.pos(maxX, minY, minZ).endVertex();
                    worldrenderer.pos(maxX, minY, maxZ).endVertex();
                    worldrenderer.pos(minX, minY, maxZ).endVertex();
                    worldrenderer.pos(minX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, maxY, maxZ).endVertex();
                    worldrenderer.pos(maxX, maxY, minZ).endVertex();
                    worldrenderer.pos(minX, maxY, minZ).endVertex();
                    worldrenderer.endVertex();
                    tessellator.draw();
                }
                GL11.glLineWidth(1.0f);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glDepthMask(true);
                GlStateManager.enableCull();
                GL11.glEnable(3553);
                GL11.glEnable(2929);
                GL11.glDisable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2848);
            }
        }
    };
}

