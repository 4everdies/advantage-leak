/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Logger;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.Entity;
import net.minecraft.potion.Potion;

@ModuleInfo(label="Tick Base", category=ModuleCategory.COMBAT)
public final class TickBaseModule
extends Module {
    private final NumberProperty lagRange = new NumberProperty("Range", 8.0, 1.0, 15.0, 0.1);
    public Property<Boolean> swingCheckProperty = new Property<Boolean>("Swing Check", true);
    private Mode MODE = Mode.NONE;
    private long time;
    private long balance;
    private double range;
    private double distance;
    Entity target;
    @EventLink
    public Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (this.MODE.equals((Object)Mode.REDUCING)) {
            return;
        }
        this.target = TargetSelectionProcess.getTarget();
        if (this.target == null) {
            return;
        }
        if (this.swingCheckProperty.getValue().booleanValue() && !Util.mc.thePlayer.isSwingInProgress) {
            return;
        }
        this.distance = Util.mc.thePlayer.getDistanceToEntity(this.target);
        double range = this.distance;
        if (range >= 1.0 && this.balance >= 50L && this.MODE.equals((Object)Mode.BASING)) {
            this.balance -= 50L;
            ++Util.mc.timer.elapsedTicks;
        } else {
            if (this.balance != 0L) {
                Logger.chatPrint("Balance " + this.balance + " " + range);
            }
            this.balance = 0L;
            this.MODE = Mode.NONE;
        }
        if (range < (Double)this.lagRange.getValue() && this.range >= (Double)this.lagRange.getValue() && this.MODE.equals((Object)Mode.NONE)) {
            this.MODE = Mode.REDUCING;
            this.time = System.currentTimeMillis();
            this.balance = 0L;
        }
        this.range = range;
    };
    @EventLink
    public Listener<Render3DEvent> onRender3D = event -> {
        block5: {
            block4: {
                if (!this.MODE.equals((Object)Mode.REDUCING) || this.target == null) {
                    return;
                }
                if (this.distance <= 1.0) break block4;
                double d = System.currentTimeMillis() - this.time;
                double d2 = Util.mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.36 : 0.25;
                if (!(d >= this.range / d2 * 25.0 + 25.0)) break block5;
            }
            Util.mc.timer.timerSpeed = 1.0f;
            this.MODE = Mode.BASING;
            this.balance = System.currentTimeMillis() - this.time;
            return;
        }
        Util.mc.timer.timerSpeed = 0.0f;
    };

    static enum Mode {
        REDUCING,
        BASING,
        NONE;

    }
}

