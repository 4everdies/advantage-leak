/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.world.TickEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(label="Trigger Bot", category=ModuleCategory.COMBAT)
public class TriggerBotModule
extends Module {
    static long delay = 0L;
    private static final Timer attackTimer = new Timer();
    static int elapsedTicks = 0;
    public static final Property<Boolean> newCombat = new Property<Boolean>("New Combat Delays", false);
    private static final NumberProperty min = new NumberProperty("Min CPS", 9.0, () -> newCombat.getValue() == false, 0.0, 20.0, 0.5);
    private static final NumberProperty max = new NumberProperty("Max CPS", 13.0, () -> newCombat.getValue() == false, 0.0, 20.0, 0.5);
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            this.attack();
        }
    };
    @EventLink
    public final Listener<TickEvent> tickEventListener = e -> ++elapsedTicks;

    private void attack() {
        if (!TriggerBotModule.hitTimerDone()) {
            return;
        }
        Util.mc.clickMouse();
    }

    private static boolean hitTimerDone() {
        boolean returnVal = false;
        if (!newCombat.getValue().booleanValue()) {
            if (attackTimer.hasTimeElapsed(delay, false)) {
                returnVal = true;
                attackTimer.reset();
                delay = (long)(1000.0 / MathUtils.getRandom(((Double)max.getValue()).floatValue(), Math.min(((Double)min.getValue()).floatValue(), ((Double)max.getValue()).floatValue() - 1.0f)));
            }
        } else if (elapsedTicks >= TriggerBotModule.getNewCombatDelay()) {
            elapsedTicks = 0;
            returnVal = true;
        }
        return returnVal;
    }

    private static int getNewCombatDelay() {
        int toolDelay = 3;
        if (Util.mc.thePlayer.inventory.getCurrentItem() == null) {
            return toolDelay;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
            toolDelay = 12;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemTool) {
            toolDelay = 20;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemPickaxe) {
            toolDelay = 16;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemAxe) {
            toolDelay = 25;
        }
        return toolDelay;
    }

    @Override
    public void onEnable() {
        delay = (long)(1000.0 / MathUtils.getRandom(((Double)max.getValue()).floatValue(), Math.max(((Double)min.getValue()).floatValue(), ((Double)max.getValue()).floatValue() - 1.0f)));
        super.onEnable();
    }
}

