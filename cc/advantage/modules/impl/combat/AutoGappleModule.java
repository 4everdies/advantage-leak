/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;

@ModuleInfo(label="Auto Gapple", category=ModuleCategory.COMBAT)
public final class AutoGappleModule
extends Module {
    private final NumberProperty health = new NumberProperty("Health", 15.0, 1.0, 20.0, 1.0);
    private final NumberProperty delay = new NumberProperty("Delay", 50.0, 0.0, 100.0, 5.0);
    private final Timer stopWatch = new Timer();
    private int attackTicks;
    private long nextEat;
    private boolean eating;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        ++this.attackTicks;
        if (Util.mc.currentScreen != null) {
            this.attackTicks = 0;
        }
        if (Util.mc.thePlayer.isPotionActive(Potion.regeneration) && this.eating) {
            Util.mc.gameSettings.keyBindUseItem.setPressed(false);
            this.eating = false;
            if (Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled() && KillAuraModule.target != null && !KillAuraModule.canAttack) {
                KillAuraModule.canAttack = true;
            }
        }
        if (Util.mc.thePlayer.onGroundTicks <= 1 || !this.stopWatch.hasTimeElapsed(this.nextEat) || this.attackTicks < 10 || Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class).isEnabled() || Util.mc.thePlayer.isPotionActive(Potion.regeneration)) {
            return;
        }
        for (int i = 0; i < 9; ++i) {
            Item item;
            ItemStack stack = Util.mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || !((item = stack.getItem()) instanceof ItemAppleGold) || !(Util.mc.thePlayer.getHealth() <= ((Double)this.health.getValue()).floatValue())) continue;
            Util.mc.thePlayer.inventory.currentItem = i;
            Util.mc.playerController.syncCurrentPlayItem();
            Util.mc.gameSettings.keyBindUseItem.setPressed(true);
            this.eating = true;
            if (Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled() && KillAuraModule.target != null) {
                KillAuraModule.canAttack = false;
            }
            this.nextEat = ((Double)this.delay.getValue()).longValue() * 10L;
            this.stopWatch.reset();
            break;
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        if (this.eating) {
            Util.mc.gameSettings.keyBindUseItem.setPressed(false);
            this.eating = false;
            if (Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled() && KillAuraModule.target != null && !KillAuraModule.canAttack) {
                KillAuraModule.canAttack = true;
            }
        }
    };
    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        this.attackTicks = 0;
    };

    @Override
    public void onDisable() {
        if (this.eating) {
            Util.mc.gameSettings.keyBindUseItem.setPressed(false);
            this.eating = false;
            if (Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled() && KillAuraModule.target != null && !KillAuraModule.canAttack) {
                KillAuraModule.canAttack = true;
            }
        }
        super.onDisable();
    }
}

