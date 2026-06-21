/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Hit Select", category=ModuleCategory.COMBAT)
public class HitSelectionModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Stop);
    public static ModeProperty<Type> type = new ModeProperty<Type>("Type", Type.Reduce);
    private static final NumberProperty chance = new NumberProperty("Chance", 80.0, 10.0, 100.0, 1.0);
    private static final NumberProperty threshold = new NumberProperty("Threshold", 400.0, 300.0, 500.0, 1.0);
    private long lastAttackTime = -1L;
    private boolean currentShouldAttack = false;
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = e -> this.resetState();
    @EventLink
    public final Listener<AttackEvent> attackEventListener = e -> {
        if (mode.getValue() == Mode.Cancel && !this.currentShouldAttack) {
            e.setCancelled(true);
            return;
        }
        if ((mode.getValue() == Mode.Cancel || mode.getValue() == Mode.Stop) && this.currentShouldAttack) {
            this.lastAttackTime = System.currentTimeMillis();
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (Util.mc.thePlayer == null) {
            return;
        }
        this.currentShouldAttack = false;
        if (Math.random() * 100.0 > (Double)chance.getValue()) {
            this.currentShouldAttack = true;
        } else {
            switch (((Type)((Object)((Object)type.getValue()))).ordinal()) {
                case 0: {
                    double dx = Util.mc.thePlayer.posX - Util.mc.thePlayer.prevPosX;
                    double dz = Util.mc.thePlayer.posZ - Util.mc.thePlayer.prevPosZ;
                    double speed = Math.sqrt(dx * dx + dz * dz);
                    this.currentShouldAttack = speed > 0.1;
                    break;
                }
                case 1: {
                    this.currentShouldAttack = Util.mc.thePlayer.hurtTime > 0 && !Util.mc.thePlayer.onGround;
                    break;
                }
                case 2: {
                    boolean bl = this.currentShouldAttack = !Util.mc.thePlayer.onGround && Util.mc.thePlayer.motionY < 0.0;
                }
            }
            if (!this.currentShouldAttack) {
                boolean bl = this.currentShouldAttack = (double)(System.currentTimeMillis() - this.lastAttackTime) >= (Double)threshold.getValue();
            }
        }
        if (mode.getValue() == Mode.Stop) {
            KillAuraModule.canAttack = this.currentShouldAttack;
        }
    };

    private void resetState() {
        this.lastAttackTime = -1L;
        this.currentShouldAttack = false;
    }

    @Override
    public void onDisable() {
        this.resetState();
        super.onDisable();
    }

    public static enum Type {
        Movement,
        Reduce,
        Critical;

    }

    public static enum Mode {
        Cancel,
        Stop;

    }
}

