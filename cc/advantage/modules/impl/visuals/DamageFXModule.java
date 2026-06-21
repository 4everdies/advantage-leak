/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.game.LivingUpdateEvent;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.FontUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import lombok.Generated;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Damage FX", category=ModuleCategory.VISUALS)
public final class DamageFXModule
extends Module {
    public Property<Boolean> customFont = new Property<Boolean>("Custom Font", false);
    private final HashMap<EntityLivingBase, Float> healthMap = new HashMap();
    private final ArrayDeque<Particles> particles = new ArrayDeque();
    @EventLink
    public final Listener<LivingUpdateEvent> livingUpdateEventListener = e -> {
        float health;
        float floatValue;
        if (e.getEntity() == null || !(e.getEntity() instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase entity = (EntityLivingBase)e.getEntity();
        if (entity == Util.mc.thePlayer) {
            return;
        }
        if (!this.healthMap.containsKey(entity)) {
            this.healthMap.put(entity, Float.valueOf(entity.getHealth()));
        }
        if ((floatValue = this.healthMap.get(entity).floatValue()) != (health = entity.getHealth())) {
            boolean crit;
            boolean heal = health > floatValue;
            boolean bl = crit = entity.hurtResistantTime < 18 || Util.mc.thePlayer.motionY < 0.0 && !Util.mc.thePlayer.onGround;
            String color = heal ? "\u00a7a" : (crit ? "\u00a7c" : "\u00a7e");
            String text = floatValue - health < 0.0f ? color + DamageFXModule.roundToPlace((floatValue - health) * -1.0f, 1) : color + DamageFXModule.roundToPlace(floatValue - health, 1);
            Location location = new Location(entity);
            location.setY(entity.getEntityBoundingBox().minY + (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) / 2.0);
            location.setX(location.getX() - 0.5 + (double)new Random(System.currentTimeMillis()).nextInt(5) * 0.1);
            location.setZ(location.getZ() - 0.5 + (double)new Random(System.currentTimeMillis() + 1L).nextInt(5) * 0.1);
            this.particles.add(new Particles(location, text));
            this.healthMap.remove(entity);
            this.healthMap.put(entity, Float.valueOf(entity.getHealth()));
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        Iterator<Particles> iterator2 = this.particles.iterator();
        while (iterator2.hasNext()) {
            Particles update = iterator2.next();
            ++update.ticks;
            if (update.ticks <= 10) {
                update.location.setY(update.location.getY() + (double)update.ticks * 0.005);
            }
            if (update.ticks <= 20) continue;
            iterator2.remove();
        }
    };
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = event -> {
        for (Particles p : this.particles) {
            double x = p.location.getX();
            double n = x - Util.mc.getRenderManager().renderPosX;
            double y = p.location.getY();
            double n2 = y - Util.mc.getRenderManager().renderPosY;
            double z = p.location.getZ();
            double n3 = z - Util.mc.getRenderManager().renderPosZ;
            GlStateManager.pushMatrix();
            GlStateManager.enablePolygonOffset();
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
            GlStateManager.translate((float)n, (float)n2, (float)n3);
            GlStateManager.rotate(-Util.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            float textY = Util.mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f;
            GlStateManager.rotate(Util.mc.getRenderManager().playerViewX, textY, 0.0f, 0.0f);
            double size = 0.03;
            GlStateManager.scale(-0.03, -0.03, 0.03);
            GL11.glDepthMask(false);
            if (this.customFont.getValue().booleanValue()) {
                FontUtils.getCurrentFont().drawStringWithShadow(p.text, -(FontUtils.getCurrentFont().getStringWidth(p.text) / 2), -(FontUtils.getCurrentFont().FONT_HEIGHT - 1), 0);
                FontUtils.getCurrentFont().drawStringWithShadow(p.text, -(FontUtils.getCurrentFont().getStringWidth(p.text) / 2), -(FontUtils.getCurrentFont().FONT_HEIGHT - 1), 0);
            } else {
                Util.mc.fontRendererObj.drawStringWithShadow(p.text, -(Util.mc.fontRendererObj.getStringWidth(p.text) / 2), -(Util.mc.fontRendererObj.FONT_HEIGHT - 1), 0);
                Util.mc.fontRendererObj.drawStringWithShadow(p.text, -(Util.mc.fontRendererObj.getStringWidth(p.text) / 2), -(Util.mc.fontRendererObj.FONT_HEIGHT - 1), 0);
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDepthMask(true);
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
            GlStateManager.disablePolygonOffset();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    };

    public static double roundToPlace(double p_roundToPlace_0_, int p_roundToPlace_2_) {
        if (p_roundToPlace_2_ < 0) {
            throw new IllegalArgumentException();
        }
        return new BigDecimal(p_roundToPlace_0_).setScale(p_roundToPlace_2_, RoundingMode.HALF_UP).doubleValue();
    }

    public static class Particles {
        public int ticks;
        public Location location;
        public String text;

        public Particles(Location location, String text) {
            this.location = location;
            this.text = text;
            this.ticks = 0;
        }
    }

    public static class Location {
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;

        public Location(double x, double y, double z, float yaw, float pitch) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public Location(EntityLivingBase entity) {
            this.x = entity.posX;
            this.y = entity.posY;
            this.z = entity.posZ;
            this.yaw = 0.0f;
            this.pitch = 0.0f;
        }

        @Generated
        public double getX() {
            return this.x;
        }

        @Generated
        public double getY() {
            return this.y;
        }

        @Generated
        public double getZ() {
            return this.z;
        }

        @Generated
        public float getYaw() {
            return this.yaw;
        }

        @Generated
        public float getPitch() {
            return this.pitch;
        }

        @Generated
        public void setX(double x) {
            this.x = x;
        }

        @Generated
        public void setY(double y) {
            this.y = y;
        }

        @Generated
        public void setZ(double z) {
            this.z = z;
        }

        @Generated
        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        @Generated
        public void setPitch(float pitch) {
            this.pitch = pitch;
        }
    }
}

