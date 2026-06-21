/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.player.MoveEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.InventoryUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

@ModuleInfo(label="Auto Armor", category=ModuleCategory.PLAYER)
public final class AutoArmorModule
extends Module {
    public Timer startTimer = new Timer();
    public Timer timer = new Timer();
    public ModeProperty modeProperty = new ModeProperty<Mode>("Mode", Mode.Open);
    public Property<Boolean> stop = new Property<Boolean>("Stop", true, () -> this.modeProperty.getValue() == Mode.Spoof);
    public NumberProperty startDelay = new NumberProperty("Start Delay", 150.0, 0.0, 1000.0, 1.0);
    public NumberProperty speed = new NumberProperty("Speed", 150.0, 0.0, 1000.0, 1.0);
    @EventLink
    private final Listener<MoveEvent> moveEventListener = event -> {
        KillAuraModule ka = Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class);
        EntityLivingBase target = KillAuraModule.target;
        if (this.modeProperty.getValue() != Mode.Spoof || Util.mc.currentScreen == null && target == null) {
            if (this.modeProperty.getValue() == Mode.Open) {
                if (Util.mc.currentScreen == null) {
                    this.startTimer.reset();
                }
                if (!this.startTimer.hasTimeElapsed((Double)this.startDelay.getValue(), false)) {
                    return;
                }
            }
            if (InventoryUtils.timer.hasTimeElapsed((Double)this.speed.getValue(), false)) {
                if (this.modeProperty.getValue() == Mode.Open && !(Util.mc.currentScreen instanceof GuiInventory)) {
                    return;
                }
                for (int type = 1; type < 5; ++type) {
                    ItemStack is;
                    if (!Util.mc.thePlayer.inventoryContainer.getSlot(4 + type).getHasStack() || InventoryUtils.isBestArmor(is = Util.mc.thePlayer.inventoryContainer.getSlot(4 + type).getStack(), type)) continue;
                    InventoryUtils.openInv(true);
                    InventoryUtils.drop(4 + type);
                    if (!this.stop.getValue().booleanValue()) {
                        InventoryUtils.closeInv(true);
                    }
                    InventoryUtils.timer.reset();
                    if ((Double)this.speed.getValue() != 0.0) break;
                }
                block1: for (int typex = 1; typex < 5; ++typex) {
                    if (!((double)InventoryUtils.timer.getTime() > (Double)this.speed.getValue())) continue;
                    for (int i = 9; i < 45; ++i) {
                        ItemStack is;
                        if (!Util.mc.thePlayer.inventoryContainer.getSlot(i).getHasStack() || !(InventoryUtils.getProtection(is = Util.mc.thePlayer.inventoryContainer.getSlot(i).getStack()) > 0.0f) || !InventoryUtils.isBestArmor(is, typex) || InventoryUtils.isBadStack(is, true, true)) continue;
                        InventoryUtils.openInv(true);
                        InventoryUtils.shiftClick(i);
                        if (!this.stop.getValue().booleanValue()) {
                            InventoryUtils.closeInv(true);
                        }
                        InventoryUtils.timer.reset();
                        if ((Double)this.speed.getValue() != 0.0) continue block1;
                    }
                }
            }
            if (InventoryUtils.timer.hasTimeElapsed(55.0, false)) {
                InventoryUtils.closeInv(true);
            }
            if (InventoryUtils.isInventoryOpen && this.stop.getValue().booleanValue()) {
                event.setForward(0.0f);
                event.setStrafe(0.0f);
                event.setJump(false);
                event.setSneak(false);
            }
        } else {
            InventoryUtils.closeInv(true);
        }
    };

    private static enum Mode {
        Open,
        Spoof;

    }
}

