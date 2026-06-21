/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.world.TickEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(label="FPS Enhancer", category=ModuleCategory.CLIENT)
public final class FpsEnhancerModule
extends Module {
    public static final Property<Boolean> reduceParticles = new Property<Boolean>("Reduce Particles", true);
    public static final Property<Boolean> disableConfigAutoSave = new Property<Boolean>("Disable Config Auto Save", false);
    private static final Property<Boolean> adaptiveRenderDistance = new Property<Boolean>("Adaptive Render Distance", false);
    private static final Property<Boolean> cullDistantEntities = new Property<Boolean>("Cull Distant Entities", true);
    private static final Property<Boolean> cullInvisibleEntities = new Property<Boolean>("Cull Invisible Entities", true);
    private static final Property<Boolean> optimizeMemory = new Property<Boolean>("Optimize Memory", false);
    private static final NumberProperty gcInterval = new NumberProperty("GC Interval (seconds)", 30.0, 10.0, 120.0, 5.0);
    private static final NumberProperty entityCullDistance = new NumberProperty("Entity Cull Distance", 64.0, 32.0, 256.0, 16.0);
    private static final Property<Boolean> skipDeadEntities = new Property<Boolean>("Skip Dead Entities", false);
    private static final Property<Boolean> batchRendering = new Property<Boolean>("Batch Rendering", false);
    private static final int ENTITY_CULL_INTERVAL_TICKS = 5;
    private static final int INVISIBLE_CULL_INTERVAL_TICKS = 20;
    private long lastGCTime = 0L;
    private int tickCounter = 0;
    private final Map<Entity, Double> originalRenderWeights = new WeakHashMap<Entity, Double>();
    @EventLink
    public final Listener<TickEvent> tickEventListener = event -> {
        ++this.tickCounter;
        if (optimizeMemory.getValue().booleanValue()) {
            boolean memoryPressure;
            long currentTime = System.currentTimeMillis();
            long intervalMs = ((Double)gcInterval.getValue()).longValue() * 1000L;
            Runtime runtime = Runtime.getRuntime();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            boolean bl = memoryPressure = (double)usedMemory > (double)runtime.maxMemory() * 0.85;
            if (memoryPressure && currentTime - this.lastGCTime >= intervalMs) {
                System.gc();
                this.lastGCTime = currentTime;
            }
        }
        if (skipDeadEntities.getValue().booleanValue() && this.tickCounter % 200 == 0 && Util.mc.theWorld != null) {
            Util.mc.theWorld.loadedEntityList.removeIf(entity -> {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase livingBase = (EntityLivingBase)entity;
                    return !livingBase.isEntityAlive();
                }
                return false;
            });
        }
        if (cullDistantEntities.getValue().booleanValue() && this.tickCounter % 5 == 0) {
            this.applyDistanceCulling();
        }
        if (cullInvisibleEntities.getValue().booleanValue() && Util.mc.theWorld != null && Util.mc.thePlayer != null && this.tickCounter % 20 == 0) {
            for (Entity entity2 : Util.mc.theWorld.loadedEntityList) {
                if (entity2 == Util.mc.thePlayer || entity2 == Util.mc.getRenderViewEntity() || !entity2.isInvisible() && this.isEntityInView(entity2)) continue;
                entity2.ignoreFrustumCheck = false;
            }
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        if (batchRendering.getValue().booleanValue()) {
            this.applyBatchRenderingOptimization();
        }
        if (adaptiveRenderDistance.getValue().booleanValue()) {
            EntityLivingBase entity = this.getFarthest(96.0);
            if (entity == null) {
                Util.mc.gameSettings.renderDistanceChunks = 4;
            } else {
                double distance = Util.mc.thePlayer.getDistanceToEntity(entity);
                Util.mc.gameSettings.renderDistanceChunks = distance > 96.0 ? 6 : (int)(distance / 16.0);
            }
        }
    };

    public FpsEnhancerModule() {
        this.toggle();
    }

    @Override
    public void onEnable() {
        this.lastGCTime = System.currentTimeMillis();
        this.tickCounter = 0;
    }

    @Override
    public void onDisable() {
        this.restoreRenderWeights();
    }

    private EntityLivingBase getFarthest(double range) {
        if (Util.mc.theWorld == null || Util.mc.thePlayer == null) {
            return null;
        }
        double distSq = range * range;
        EntityLivingBase target = null;
        for (Object object : Util.mc.theWorld.loadedEntityList) {
            EntityLivingBase player;
            double currentDistSq;
            Entity entity = (Entity)object;
            if (!(entity instanceof EntityLivingBase) || !((currentDistSq = Util.mc.thePlayer.getDistanceSqToEntity(player = (EntityLivingBase)entity)) >= distSq)) continue;
            distSq = currentDistSq;
            target = player;
        }
        return target;
    }

    private boolean isEntityInView(Entity entity) {
        if (Util.mc.thePlayer == null) {
            return true;
        }
        double deltaX = entity.posX - Util.mc.thePlayer.posX;
        double deltaZ = entity.posZ - Util.mc.thePlayer.posZ;
        float yaw = Util.mc.thePlayer.rotationYaw;
        double yawRadians = Math.toRadians(yaw);
        double lookX = -Math.sin(yawRadians);
        double lookZ = Math.cos(yawRadians);
        double entityDistSq = deltaX * deltaX + deltaZ * deltaZ;
        if (entityDistSq < 0.001) {
            return true;
        }
        double entityDist = Math.sqrt(entityDistSq);
        double dotProduct = lookX * (deltaX /= entityDist) + lookZ * (deltaZ /= entityDist);
        return dotProduct > -0.5;
    }

    private void applyBatchRenderingOptimization() {
        if (!batchRendering.getValue().booleanValue() || Util.mc.theWorld == null || this.tickCounter % 5 != 0) {
            return;
        }
        for (Entity entity : Util.mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase)) continue;
            entity.renderDistanceWeight = Math.max(entity.renderDistanceWeight, 1.5);
        }
    }

    private void applyDistanceCulling() {
        if (Util.mc.theWorld == null || Util.mc.thePlayer == null) {
            return;
        }
        Entity renderView = Util.mc.getRenderViewEntity();
        double cullDistance = (Double)entityCullDistance.getValue();
        double cullDistanceSq = cullDistance * cullDistance;
        for (Entity entity : Util.mc.theWorld.loadedEntityList) {
            double deltaZ;
            double deltaY;
            double deltaX;
            double distanceSq;
            if (entity == Util.mc.thePlayer || entity == renderView) continue;
            if (!this.originalRenderWeights.containsKey(entity)) {
                this.originalRenderWeights.put(entity, entity.renderDistanceWeight);
            }
            if ((distanceSq = (deltaX = entity.posX - Util.mc.thePlayer.posX) * deltaX + (deltaY = entity.posY - Util.mc.thePlayer.posY) * deltaY + (deltaZ = entity.posZ - Util.mc.thePlayer.posZ) * deltaZ) > cullDistanceSq) {
                entity.renderDistanceWeight = 0.0;
                continue;
            }
            Double originalWeight = this.originalRenderWeights.get(entity);
            entity.renderDistanceWeight = originalWeight == null ? 1.0 : originalWeight;
        }
    }

    private void restoreRenderWeights() {
        for (Map.Entry<Entity, Double> entry : this.originalRenderWeights.entrySet()) {
            Entity entity = entry.getKey();
            if (entity == null) continue;
            entity.renderDistanceWeight = entry.getValue();
        }
        this.originalRenderWeights.clear();
    }
}

