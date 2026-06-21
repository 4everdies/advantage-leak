/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(label="Auto Soup", category=ModuleCategory.COMBAT)
public final class AutoSoupModule
extends Module {
    private final NumberProperty health = new NumberProperty("Health", 10.0, 0.5, 20.0, 0.5);
    private final Property<Boolean> dropSoup = new Property<Boolean>("Drop Soup", true);
    private final NumberProperty healDelay = new NumberProperty("Heal Delay", 50.0, 1.0, 400.0, 1.0);
    private final NumberProperty dropDelay = new NumberProperty("Drop Delay", 50.0, 1.0, 400.0, 1.0);
    private final NumberProperty switchDelay = new NumberProperty("Switch Delay", 50.0, 1.0, 400.0, 1.0);
    private final Timer healTimer = new Timer();
    private final Timer dropTimer = new Timer();
    private final Timer switchTimer = new Timer();
    private int soupIndex = Integer.MIN_VALUE;
    private int originalIndex = Integer.MIN_VALUE;
    private int step = 1;
    private boolean started;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null || Util.mc.currentScreen != null) {
            return;
        }
        if (this.started) {
            this.runSoupSequence();
            return;
        }
        this.soupIndex = this.getSoupInHotbar();
        if (this.soupIndex != Integer.MIN_VALUE && Util.mc.thePlayer.getHealth() <= ((Double)this.health.getValue()).floatValue()) {
            this.originalIndex = Util.mc.thePlayer.inventory.currentItem;
            this.started = true;
        }
    };

    private void runSoupSequence() {
        if (this.step >= 2 && this.step <= 3 && Util.mc.thePlayer.inventory.currentItem != this.soupIndex) {
            Util.mc.thePlayer.inventory.currentItem = this.soupIndex;
        }
        switch (this.step) {
            case 1: {
                if (!this.switchTimer.hasTimeElapsed((Double)this.switchDelay.getValue())) break;
                this.switchTimer.reset();
                Util.mc.thePlayer.inventory.currentItem = this.soupIndex;
                ++this.step;
                break;
            }
            case 2: {
                if (!this.healTimer.hasTimeElapsed((Double)this.healDelay.getValue())) break;
                this.healTimer.reset();
                if (Util.mc.gameSettings.keyBindUseItem.isKeyDown()) {
                    KeyBinding.setKeyBindState(Util.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                    ++this.step;
                    break;
                }
                KeyBinding.setKeyBindState(Util.mc.gameSettings.keyBindUseItem.getKeyCode(), true);
                break;
            }
            case 3: {
                if (this.dropSoup.getValue().booleanValue()) {
                    if (!this.dropTimer.hasTimeElapsed((Double)this.dropDelay.getValue())) break;
                    this.dropTimer.reset();
                    C07PacketPlayerDigging.Action action = GuiScreen.isCtrlKeyDown() ? C07PacketPlayerDigging.Action.DROP_ALL_ITEMS : C07PacketPlayerDigging.Action.DROP_ITEM;
                    Util.mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(action, BlockPos.ORIGIN, EnumFacing.DOWN));
                    ++this.step;
                    break;
                }
                ++this.step;
                break;
            }
            case 4: {
                if (!this.switchTimer.hasTimeElapsed((Double)this.switchDelay.getValue())) break;
                this.switchTimer.reset();
                Util.mc.thePlayer.inventory.currentItem = this.originalIndex;
                ++this.step;
                break;
            }
            default: {
                this.reset();
            }
        }
    }

    private int getSoupInHotbar() {
        for (int i = 36; i < 45; ++i) {
            ItemStack itemStack = Util.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack == null || !(itemStack.getItem() instanceof ItemSoup)) continue;
            return i - 36;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private void reset() {
        this.healTimer.reset();
        this.dropTimer.reset();
        this.switchTimer.reset();
        this.originalIndex = Integer.MIN_VALUE;
        this.soupIndex = Integer.MIN_VALUE;
        this.started = false;
        this.step = 1;
        if (Util.mc.gameSettings != null) {
            KeyBinding.setKeyBindState(Util.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
        }
    }
}

