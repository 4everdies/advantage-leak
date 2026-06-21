/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.animations.Direction;
import cc.advantage.utils.render.animations.impl.SmoothStepAnimation;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Fire Flies", category=ModuleCategory.VISUALS)
public final class FireFliesModule
extends Module {
    public static Property<Boolean> darkImprint = new Property<Boolean>("Dark Imprint", false);
    private final Property<Boolean> lighting = new Property<Boolean>("Lighting", false);
    private final NumberProperty spawnDelay = new NumberProperty("Spawn Delay", 3.0, 1.0, 10.0, 1.0);
    private final NumberProperty maxAliveTime = new NumberProperty("Max Alive Time", 1000.0, 500.0, 6000.0, 500.0);
    private final ArrayList<FirePart> FIRE_PARTS_LIST = new ArrayList();
    private final ResourceLocation FIRE_PART_TEX = new ResourceLocation("advantage/images/firepart.png");
    private final Tessellator tessellator = Tessellator.getInstance();
    private final WorldRenderer buffer = this.tessellator.getWorldRenderer();
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        if (Util.mc.thePlayer != null && Util.mc.thePlayer.ticksExisted == 1) {
            this.FIRE_PARTS_LIST.forEach(FirePart::setToRemove);
        }
        this.FIRE_PARTS_LIST.forEach(FirePart::updatePart);
        this.FIRE_PARTS_LIST.removeIf(FirePart::isToRemove);
        if (Util.mc.thePlayer.ticksExisted % (int)((Double)this.spawnDelay.getValue() + 1.0) == 0) {
            this.FIRE_PARTS_LIST.add(new FirePart(this.generateVecForPart(10.0, 4.0), ((Double)this.maxAliveTime.getValue()).floatValue()));
            this.FIRE_PARTS_LIST.add(new FirePart(this.generateVecForPart(6.0, 5.0), ((Double)this.maxAliveTime.getValue()).floatValue()));
        }
    };
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = event -> {
        if (!this.FIRE_PARTS_LIST.isEmpty()) {
            this.setupGLDrawsFireParts(() -> {
                this.bindResource(this.FIRE_PART_TEX);
                this.FIRE_PARTS_LIST.forEach(part -> this.drawPart((FirePart)part, Util.mc.timer.renderPartialTicks));
            });
        }
    };

    private int getPartColor(FirePart part) {
        return new Color(ColorProcess.getColor().getRed(), ColorProcess.getColor().getGreen(), ColorProcess.getColor().getBlue(), (int)(part.animation.getOutput() * 255.0)).getRGB();
    }

    private float getRandom(double min, double max) {
        return (float)MathUtils.getRandom(min, max);
    }

    private Vec3 generateVecForPart(double rangeXZ, double rangeY) {
        Vec3 pos = Util.mc.thePlayer.getPositionVector().addVector(this.getRandom(-rangeXZ, rangeXZ), this.getRandom(-rangeY / 2.0, rangeY), this.getRandom(-rangeXZ, rangeXZ));
        for (int i = 0; i < 30; ++i) {
            pos = Util.mc.thePlayer.getPositionVector().addVector(this.getRandom(-rangeXZ, rangeXZ), this.getRandom(-rangeY / 2.0, rangeY), this.getRandom(-rangeXZ, rangeXZ));
        }
        return pos;
    }

    private void setupGLDrawsFireParts(Runnable partsRender) {
        double glX = Util.mc.getRenderManager().viewerPosX;
        double glY = Util.mc.getRenderManager().viewerPosY;
        double glZ = Util.mc.getRenderManager().viewerPosZ;
        GL11.glPushMatrix();
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        Util.mc.entityRenderer.disableLightmap();
        GL11.glEnable(3042);
        GL11.glLineWidth(1.0f);
        GL11.glEnable(3553);
        GL11.glDisable(2896);
        GL11.glShadeModel(7425);
        GL11.glDisable(3008);
        GL11.glDisable(2884);
        GL11.glDepthMask(false);
        GL11.glTranslated(-glX, -glY, -glZ);
        partsRender.run();
        GL11.glTranslated(glX, glY, glZ);
        GL11.glDepthMask(true);
        GL11.glEnable(2884);
        GL11.glEnable(3008);
        GL11.glLineWidth(1.0f);
        GL11.glShadeModel(7424);
        GL11.glEnable(3553);
        GlStateManager.resetColor();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glPopMatrix();
    }

    private void bindResource(ResourceLocation toBind) {
        Util.mc.getTextureManager().bindTexture(toBind);
    }

    private void drawBindedTexture(float x, float y, float x2, float y2, int c) {
        this.buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        this.buffer.pos(x, y).tex(0.0, 0.0).color(c).endVertex();
        this.buffer.pos(x, y2).tex(0.0, 1.0).color(c).endVertex();
        this.buffer.pos(x2, y2).tex(1.0, 1.0).color(c).endVertex();
        this.buffer.pos(x2, y).tex(1.0, 0.0).color(c).endVertex();
        this.tessellator.draw();
    }

    private void drawPart(FirePart part, float pTicks) {
        if (darkImprint.getValue().booleanValue()) {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            this.drawSparkPartsList(this.getPartColor(part), part, pTicks);
            this.drawTrailPartsList(this.getPartColor(part), part);
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
        } else {
            this.drawSparkPartsList(this.getPartColor(part), part, pTicks);
            this.drawTrailPartsList(this.getPartColor(part), part);
        }
        Vec3 pos = part.getRenderPosVec(pTicks);
        GL11.glPushMatrix();
        GL11.glTranslated(pos.xCoord, pos.yCoord, pos.zCoord);
        GL11.glNormal3d(1.0, 1.0, 1.0);
        GL11.glRotated(-Util.mc.getRenderManager().playerViewY, 0.0, 1.0, 0.0);
        GL11.glRotated(Util.mc.getRenderManager().playerViewX, Util.mc.gameSettings.thirdPersonView == 2 ? -1.0 : 1.0, 0.0, 0.0);
        GL11.glScaled(-0.1, -0.1, 0.1);
        float scale = 7.0f;
        this.drawBindedTexture(-scale / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, this.getPartColor(part));
        if (this.lighting.getValue().booleanValue()) {
            this.drawBindedTexture(-(scale *= 3.0f) / 2.0f, -scale / 2.0f, scale / 2.0f, scale / 2.0f, RenderUtils.applyOpacity(RenderUtils.darker(this.getPartColor(part), 0.2f), (float)(part.animation.getOutput() / 7.0)));
        }
        GL11.glPopMatrix();
    }

    private void drawSparkPartsList(int color, FirePart firePart, float partialTicks) {
        if (firePart.SPARK_PARTS.size() < 2) {
            return;
        }
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glEnable(2832);
        GL11.glPointSize(1.5f + 6.0f * MathHelper.clamp_float((float)(1.0 - (Util.mc.thePlayer.getDistance((float)firePart.getPosVec().xCoord, (float)firePart.getPosVec().yCoord + 1.6f, (float)firePart.getPosVec().zCoord) - 3.0) / 10.0), 0.0f, 1.0f));
        GL11.glBegin(0);
        for (SparkPart spark : firePart.SPARK_PARTS) {
            RenderUtils.color(color);
            GL11.glVertex3d(spark.getRenderPosX(partialTicks), spark.getRenderPosY(partialTicks), spark.getRenderPosZ(partialTicks));
        }
        GL11.glEnd();
        GlStateManager.resetColor();
        GL11.glEnable(3008);
        GL11.glEnable(3553);
    }

    private void drawTrailPartsList(int color, FirePart firePart) {
        if (firePart.TRAIL_PARTS.size() < 2) {
            return;
        }
        GL11.glDisable(3553);
        GL11.glLineWidth(1.0E-5f + 8.0f * MathHelper.clamp_float((float)(1.0 - (Util.mc.thePlayer.getDistance((float)firePart.getPosVec().xCoord, (float)firePart.getPosVec().yCoord + 1.6f, (float)firePart.getPosVec().zCoord) - 3.0) / 20.0), 0.0f, 1.0f));
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glBegin(3);
        for (TrailPart trail : firePart.TRAIL_PARTS) {
            RenderUtils.color(color);
            GL11.glVertex3d(trail.x, trail.y, trail.z);
        }
        GL11.glEnd();
        GlStateManager.resetColor();
        GL11.glEnable(3008);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glLineWidth(1.0f);
        GL11.glEnable(3553);
    }

    private class FirePart {
        List<TrailPart> TRAIL_PARTS;
        List<SparkPart> SPARK_PARTS = new ArrayList<SparkPart>();
        Vec3 prevPos;
        Vec3 pos;
        public SmoothStepAnimation animation = new SmoothStepAnimation(400, 1.0);
        int msChangeSideRate = this.getMsChangeSideRate();
        float moveYawSet = FireFliesModule.this.getRandom(0.0, 360.0);
        float speed = FireFliesModule.this.getRandom(0.1, 0.25);
        float yMotion = FireFliesModule.this.getRandom(-0.075, 0.1);
        float moveYaw = this.moveYawSet;
        float maxAlive;
        long startTime;
        long rateTimer = this.startTime = System.currentTimeMillis();
        boolean toRemove;

        public FirePart(Vec3 pos, float maxAlive) {
            this.pos = pos;
            this.prevPos = pos;
            this.maxAlive = maxAlive;
            this.TRAIL_PARTS = new ArrayList<TrailPart>();
        }

        public float getTimePC() {
            return MathHelper.clamp_float((float)(System.currentTimeMillis() - this.startTime) / this.maxAlive, 0.0f, 1.0f);
        }

        public Vec3 getPosVec() {
            return this.pos;
        }

        public Vec3 getRenderPosVec(float pTicks) {
            Vec3 pos = this.getPosVec();
            return pos.addVector(-(this.prevPos.xCoord - pos.xCoord) * (double)pTicks, -(this.prevPos.yCoord - pos.yCoord) * (double)pTicks, -(this.prevPos.zCoord - pos.zCoord) * (double)pTicks);
        }

        public void updatePart() {
            if (System.currentTimeMillis() - this.rateTimer >= (long)this.msChangeSideRate) {
                this.msChangeSideRate = this.getMsChangeSideRate();
                this.rateTimer = System.currentTimeMillis();
                this.moveYawSet = FireFliesModule.this.getRandom(0.0, 360.0);
            }
            this.moveYaw = MathUtils.lerp(this.moveYaw, this.moveYawSet, 0.065f);
            float motionX = -((float)Math.sin(Math.toRadians(this.moveYaw))) * (this.speed /= 1.005f);
            float motionZ = (float)Math.cos(Math.toRadians(this.moveYaw)) * this.speed;
            this.prevPos = this.pos;
            float scaleBox = 0.1f;
            float delente = !Util.mc.theWorld.getCollisionBoxes(new AxisAlignedBB(this.pos.xCoord - (double)(scaleBox / 2.0f), this.pos.yCoord, this.pos.zCoord - (double)(scaleBox / 2.0f), this.pos.xCoord + (double)(scaleBox / 2.0f), this.pos.yCoord + (double)scaleBox, this.pos.zCoord + (double)(scaleBox / 2.0f))).isEmpty() ? 0.3f : 1.0f;
            this.pos = this.pos.addVector(motionX / delente, (this.yMotion /= 1.02f) / delente, motionZ / delente);
            if (this.getTimePC() >= 1.0f || this.animation.timerUtil.hasTimeElapsed((Double)FireFliesModule.this.maxAliveTime.getValue())) {
                this.animation.setDirection(Direction.BACKWARDS);
            }
            if (this.animation.finished(Direction.BACKWARDS)) {
                this.setToRemove();
            }
            if (!this.TRAIL_PARTS.isEmpty()) {
                this.TRAIL_PARTS.removeIf(trailPart -> this.animation.finished(Direction.BACKWARDS));
            }
            if (!this.SPARK_PARTS.isEmpty()) {
                this.SPARK_PARTS.removeIf(sparkPart -> this.animation.finished(Direction.BACKWARDS));
            }
            this.TRAIL_PARTS.add(new TrailPart(this));
            for (int i = 0; i < 2; ++i) {
                this.SPARK_PARTS.add(new SparkPart(this));
            }
            this.SPARK_PARTS.forEach(SparkPart::motionSparkProcess);
        }

        public void setToRemove() {
            this.toRemove = true;
        }

        int getMsChangeSideRate() {
            return (int)FireFliesModule.this.getRandom(300.5, 900.5);
        }

        @Generated
        public boolean isToRemove() {
            return this.toRemove;
        }
    }

    private class SparkPart {
        double posX;
        double posY;
        double posZ;
        double prevPosX;
        double prevPosY;
        double prevPosZ;
        double speed = Math.random() / 30.0;
        double radianYaw = Math.random() * 360.0;
        double radianPitch = -90.0 + Math.random() * 180.0;

        SparkPart(FirePart part) {
            this.posX = part.getPosVec().xCoord;
            this.posY = part.getPosVec().yCoord;
            this.posZ = part.getPosVec().zCoord;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
        }

        void motionSparkProcess() {
            double radYaw = Math.toRadians(this.radianYaw);
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.posX += Math.sin(radYaw) * this.speed;
            this.posY += Math.cos(Math.toRadians(this.radianPitch - 90.0)) * this.speed;
            this.posZ += Math.cos(radYaw) * this.speed;
        }

        double getRenderPosX(float partialTicks) {
            return this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks;
        }

        double getRenderPosY(float partialTicks) {
            return this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks;
        }

        double getRenderPosZ(float partialTicks) {
            return this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks;
        }
    }

    private class TrailPart {
        double x;
        double y;
        double z;

        public TrailPart(FirePart part) {
            this.x = part.getPosVec().xCoord;
            this.y = part.getPosVec().yCoord;
            this.z = part.getPosVec().zCoord;
        }
    }
}

