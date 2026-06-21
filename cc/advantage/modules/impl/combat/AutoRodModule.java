/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.processes.RotationProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.RotationUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Auto Rod", category=ModuleCategory.COMBAT)
public final class AutoRodModule
extends Module {
    public static ModeProperty<TargetSelectionProcess.Entities> entities = new ModeProperty<TargetSelectionProcess.Entities>("Entities", TargetSelectionProcess.Entities.Optimal);
    private final NumberProperty minRange = new NumberProperty("Min Range", 3.0, 1.0, 8.0, 0.1);
    private final NumberProperty maxRange = new NumberProperty("Max Range", 4.5, 1.0, 8.0, 0.1);
    private final NumberProperty maxDelay = new NumberProperty("Max Delay", 100.0, 0.0, 1000.0, 5.0);
    private final NumberProperty maxRecastDelay = new NumberProperty("Max Recast Delay", 100.0, 0.0, 1000.0, 5.0);
    private final NumberProperty fov = new NumberProperty("FOV", 90.0, 0.0, 360.0, 1.0);
    private final Property<Boolean> ka = new Property<Boolean>("Only On Kill Aura", false);
    private final Property<Boolean> rotate = new Property<Boolean>("Rotate", true);
    private final NumberProperty predictSize = new NumberProperty("Predict Size", 2.0, this.rotate::getValue, (double)0.1f, 10.0, 0.1f);
    private EntityLivingBase currentTarget;
    private boolean usingRod;
    private int oldSlot;
    private long lastUseTime;
    private long lastRecastTime;
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (!Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled() && this.ka.getValue().booleanValue()) {
            return;
        }
        this.currentTarget = TargetSelectionProcess.getTarget();
        TargetSelectionProcess.setEntities((TargetSelectionProcess.Entities)((Object)((Object)entities.getValue())));
        if (this.currentTarget == null || !Util.mc.thePlayer.canEntityBeSeen(this.currentTarget) || Util.mc.thePlayer.isUsingItem()) {
            this.reset();
            return;
        }
        float range = Util.mc.thePlayer.getDistanceToEntity(this.currentTarget);
        if ((double)range >= (Double)this.minRange.getValue() && (double)range <= (Double)this.maxRange.getValue()) {
            if ((double)this.getRotationDifference(this.currentTarget) <= (Double)this.fov.getValue()) {
                if (!this.usingRod) {
                    int rod;
                    if (((double)(System.currentTimeMillis() - this.lastUseTime) >= (Double)this.maxDelay.getValue() || this.currentTarget.hurtTime <= 3) && (rod = this.findRod()) != -1) {
                        this.useRod(rod);
                        this.usingRod = true;
                        this.lastRecastTime = System.currentTimeMillis();
                    }
                } else if ((double)(System.currentTimeMillis() - this.lastRecastTime) >= (Double)this.maxRecastDelay.getValue() || this.currentTarget.hurtTime >= 9) {
                    this.reset();
                }
            }
        } else {
            this.reset();
        }
        if (this.rotate.getValue().booleanValue() && TargetSelectionProcess.getTarget() == null && (double)range > (Double)this.minRange.getValue() && (double)range <= (Double)this.maxRange.getValue()) {
            float[] finalRotation = RotationUtils.faceTrajectory(this.currentTarget, true, ((Double)this.predictSize.getValue()).floatValue(), 0.03f, 2.0f);
            RotationProcess.setRotations(new Vector2f(finalRotation[0], finalRotation[1]), 10.0, MovementFix.NORMAL);
        }
    };

    private float getRotationDifference(EntityLivingBase entity) {
        float[] rotations = this.getRotationsToEntity(entity);
        float yawDiff = Math.abs(rotations[0] - Util.mc.thePlayer.rotationYaw);
        float pitchDiff = Math.abs(rotations[1] - Util.mc.thePlayer.rotationPitch);
        return Math.max(yawDiff, pitchDiff);
    }

    private float[] getRotationsToEntity(EntityLivingBase entity) {
        double x = entity.posX - Util.mc.thePlayer.posX;
        double y = entity.posY + (double)entity.getEyeHeight() - (Util.mc.thePlayer.posY + (double)Util.mc.thePlayer.getEyeHeight());
        double z = entity.posZ - Util.mc.thePlayer.posZ;
        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float)(-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    private int findRod() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Util.mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemFishingRod)) continue;
            return i;
        }
        return -1;
    }

    private void useRod(int rodSlot) {
        this.oldSlot = Util.mc.thePlayer.inventory.currentItem;
        Util.mc.thePlayer.inventory.currentItem = rodSlot;
        this.lastUseTime = System.currentTimeMillis();
        Util.mc.playerController.sendUseItem(Util.mc.thePlayer, Util.mc.theWorld, Util.mc.thePlayer.getHeldItem());
    }

    private void reset() {
        if (this.oldSlot != -1) {
            Util.mc.thePlayer.inventory.currentItem = this.oldSlot;
            this.oldSlot = -1;
        }
        this.usingRod = false;
    }

    @Override
    public void onEnable() {
        this.currentTarget = null;
        this.usingRod = false;
        this.oldSlot = -1;
        this.lastUseTime = 0L;
        this.lastRecastTime = 0L;
    }

    @Override
    public void onDisable() {
        this.reset();
    }
}

