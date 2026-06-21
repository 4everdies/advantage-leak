/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.MouseOverEvent;
import cc.advantage.api.events.impl.game.RightClickEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.RayCastUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

@ModuleInfo(label="Hitbox Expand", category=ModuleCategory.COMBAT)
public final class HitboxExpandModule
extends Module {
    public final NumberProperty expand = new NumberProperty("Expand Amount", 0.0, 0.0, 6.0, 0.01);
    private final Property<Boolean> effectRange = new Property<Boolean>("Effect range", true);
    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event -> {
        event.setExpand(((Double)this.expand.getValue()).floatValue());
        if (!this.effectRange.getValue().booleanValue()) {
            event.setRange(event.getRange() - (Double)this.expand.getValue());
        }
    };
    @EventLink
    public final Listener<RightClickEvent> onRightClick = event -> {
        Util.mc.objectMouseOver = RayCastUtils.rayCast(RotationProcess.rotations, 4.5);
    };
}

