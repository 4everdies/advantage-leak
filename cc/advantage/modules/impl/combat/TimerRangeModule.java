/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(label="Timer Range", category=ModuleCategory.COMBAT)
public final class TimerRangeModule
extends Module {
    public static final NumberProperty delay = new NumberProperty("Delay", 80.0, 0.0, 200.0, 1.0);
    private final Property<Boolean> ka = new Property<Boolean>("Only On Kill Aura", false);
    private int tickableTick = 0;
    private float currentTimerSpeed = 1.0f;
    private boolean burstNextTick = false;
    private boolean slowNextTick = false;
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        this.setSuffix(String.valueOf(delay.getValue()));
        if (this.ka.getValue().booleanValue() && !Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled()) {
            if (this.currentTimerSpeed != 1.0f) {
                this.slowlyReturnToNormal();
            }
            return;
        }
        EntityLivingBase target = TargetSelectionProcess.getTarget();
        if (target != null && this.tickableTick == 0) {
            this.adjustTimerRange(target);
        } else if (this.currentTimerSpeed != 1.0f) {
            this.slowlyReturnToNormal();
            if (this.tickableTick > 0) {
                --this.tickableTick;
            }
        } else if (this.tickableTick > 0) {
            --this.tickableTick;
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        if (this.currentTimerSpeed != 1.0f) {
            this.slowlyReturnToNormal();
        }
        this.tickableTick = 0;
        this.currentTimerSpeed = 1.0f;
        this.burstNextTick = false;
        this.slowNextTick = false;
    };

    private void adjustTimerRange(EntityLivingBase target) {
        double dist = Util.mc.thePlayer.getDistanceToEntity(target);
        if (!this.burstNextTick && !this.slowNextTick && dist <= (double)TargetSelectionProcess.getSeekRange() + 0.15 && dist > (double)TargetSelectionProcess.getSeekRange() - 0.55) {
            this.burstNextTick = true;
        }
        if (this.burstNextTick) {
            Util.mc.timer.timerSpeed = this.currentTimerSpeed = 8.0f;
            this.burstNextTick = false;
            this.slowNextTick = true;
            return;
        }
        if (this.slowNextTick) {
            Util.mc.timer.timerSpeed = this.currentTimerSpeed = 0.1f;
            this.tickableTick = ((Double)delay.getValue()).intValue();
            this.slowNextTick = false;
            return;
        }
        this.slowlyReturnToNormal();
    }

    private void slowlyReturnToNormal() {
        if (Math.abs(this.currentTimerSpeed - 1.0f) > 0.01f) {
            this.currentTimerSpeed += (1.0f - this.currentTimerSpeed) * 0.5f;
            Util.mc.timer.timerSpeed = this.currentTimerSpeed;
        } else {
            Util.mc.timer.timerSpeed = 1.0f;
        }
    }

    @Override
    public void onDisable() {
        this.tickableTick = 0;
        this.currentTimerSpeed = 1.0f;
        this.burstNextTick = false;
        this.slowNextTick = false;
        super.onDisable();
    }
}

