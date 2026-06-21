/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.MouseOverEvent;
import cc.advantage.api.events.impl.game.RightClickEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.mc.RayCastUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Reach", category=ModuleCategory.COMBAT)
public final class ReachModule
extends Module {
    public final NumberProperty minRange = new NumberProperty("Min Range", 3.0, 4.0, 6.0, 0.01);
    public final NumberProperty maxRange = new NumberProperty("Max Range", 3.0, 4.0, 6.0, 0.01);
    private final NumberProperty bufferDecrease = new NumberProperty("Buffer Decrease", 1.0, () -> this.bufferAbuse.getValue() == false, 0.1, 10.0, 0.1);
    private final NumberProperty maxBuffer = new NumberProperty("Max Buffer", 5.0, () -> this.bufferAbuse.getValue() == false, 1.0, 200.0, 1.0);
    private final Property<Boolean> bufferAbuse = new Property<Boolean>("Buffer Abuse", false);
    private int lastId;
    private int attackTicks;
    private double combo;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        ++this.attackTicks;
    };
    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event -> event.setRange(MathUtils.getRandom((Double)this.minRange.getValue(), (Double)this.maxRange.getValue()));
    @EventLink
    public final Listener<RightClickEvent> onRightClick = event -> {
        Util.mc.objectMouseOver = RayCastUtils.rayCast(RotationProcess.rotations, 4.5);
    };
    @EventLink
    public final Listener<AttackEvent> onAttackEvent = event -> {
        EntityLivingBase entity = event.target;
        if (this.bufferAbuse.getValue().booleanValue()) {
            if (RayCastUtils.rayCast((Vector2f)RotationProcess.rotations, (double)3.0).typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) {
                if ((this.attackTicks > 9 || entity.getEntityId() != this.lastId) && this.combo < (double)((Double)this.maxBuffer.getValue()).intValue()) {
                    this.combo += 1.0;
                } else {
                    event.setCancelled();
                }
            } else {
                this.combo = Math.max(0.0, this.combo - (Double)this.bufferDecrease.getValue());
            }
        } else {
            this.combo = 0.0;
        }
        this.lastId = entity.getEntityId();
        this.attackTicks = 0;
    };
}

