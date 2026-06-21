/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.BlinkProcess;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(label="Hit Flick", category=ModuleCategory.COMBAT)
public final class HitFlickModule
extends Module {
    private final ModeProperty<Direction> direction = new ModeProperty<Direction>("Direction", Direction.Left);
    private final NumberProperty customAngle = new NumberProperty("Custom Angle", 90.0, () -> this.direction.getValue() == Direction.Custom, 1.0, 180.0, 1.0);
    private final NumberProperty cooldown = new NumberProperty("Cooldown", 1.0, 1.0, 40.0, 1.0);
    private final Property<Boolean> blink = new Property<Boolean>("Blink", false);
    private int sinceLastFlick;
    private float originalYaw;
    private float flickYaw;
    private FlickState state = FlickState.IDLE;
    @EventLink
    public final Listener<AttackEvent> attackEventListener = event -> {
        if (event.target == null || event.target == Util.mc.thePlayer) {
            return;
        }
        if (!(event.target instanceof EntityLivingBase)) {
            return;
        }
        if (this.state != FlickState.IDLE || this.sinceLastFlick < ((Double)this.cooldown.getValue()).intValue()) {
            return;
        }
        this.originalYaw = Util.mc.thePlayer.rotationYaw;
        this.flickYaw = this.originalYaw + this.getFlickAngle();
        this.state = FlickState.FLICKING_AWAY;
        if (this.blink.getValue().booleanValue()) {
            BlinkProcess.enable();
        }
    };
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre() || Util.mc.thePlayer == null) {
            return;
        }
        if (this.state == FlickState.IDLE) {
            ++this.sinceLastFlick;
            return;
        }
        if (this.state == FlickState.FLICKING_AWAY) {
            event.setYaw(this.flickYaw);
            event.setPitch(Util.mc.thePlayer.rotationPitch);
            this.state = FlickState.RESTORING;
        } else if (this.state == FlickState.RESTORING) {
            event.setYaw(this.originalYaw);
            event.setPitch(Util.mc.thePlayer.rotationPitch);
            this.state = FlickState.IDLE;
            this.sinceLastFlick = 0;
            if (this.blink.getValue().booleanValue()) {
                BlinkProcess.disable();
            }
        }
    };

    @Override
    public void onEnable() {
        this.sinceLastFlick = 0;
        this.state = FlickState.IDLE;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.state = FlickState.IDLE;
        this.sinceLastFlick = 0;
        if (this.blink.getValue().booleanValue()) {
            BlinkProcess.disable();
        }
        super.onDisable();
    }

    private float getFlickAngle() {
        return switch (((Direction)((Object)this.direction.getValue())).ordinal()) {
            default -> throw new IncompatibleClassChangeError();
            case 0 -> -90.0f;
            case 1 -> 90.0f;
            case 2 -> 180.0f;
            case 3 -> ((Double)this.customAngle.getValue()).floatValue();
        };
    }

    private static enum Direction {
        Left,
        Right,
        Back,
        Custom;

    }

    private static enum FlickState {
        IDLE,
        FLICKING_AWAY,
        RESTORING;

    }
}

