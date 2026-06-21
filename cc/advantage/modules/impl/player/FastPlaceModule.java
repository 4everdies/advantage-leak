/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.ItemBlock;

@ModuleInfo(label="Fast Place", category=ModuleCategory.PLAYER)
public final class FastPlaceModule
extends Module {
    private final NumberProperty delay = new NumberProperty("Delay", 1.0, 0.0, 3.0, 1.0);
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (Util.mc.thePlayer == null || Util.mc.thePlayer.inventory == null || Util.mc.thePlayer.inventory.getCurrentItem() == null || !event.isPre()) {
            return;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            Util.mc.rightClickDelayTimer = Math.min(Util.mc.rightClickDelayTimer, ((Double)this.delay.getValue()).intValue());
        }
    };
}

