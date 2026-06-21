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
import net.minecraft.init.Items;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

@ModuleInfo(label="Manager", category=ModuleCategory.PLAYER)
public final class ManagerModule
extends Module {
    public ModeProperty<Mode> modeProperty = new ModeProperty<Mode>("Mode", Mode.Open);
    public Property<Boolean> stopProperty = new Property<Boolean>("Stop", true, () -> this.modeProperty.getValue() == Mode.Spoof);
    public Property<Boolean> throwGarbageProperty = new Property<Boolean>("Throw Garbage", true);
    public NumberProperty startDelayProperty = new NumberProperty("Start Delay", 150.0, 0.0, 1000.0, 1.0);
    public NumberProperty speedProperty = new NumberProperty("Speed", 150.0, 0.0, 1000.0, 1.0);
    public Property<Boolean> swordProperty = new Property<Boolean>("Sword", true);
    public NumberProperty swordSlotProperty = new NumberProperty("Sword Slot", 1.0, this.swordProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> axeProperty = new Property<Boolean>("Axe", true);
    public NumberProperty axeSlotProperty = new NumberProperty("Axe Slot", 2.0, this.axeProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> pickaxeProperty = new Property<Boolean>("Pickaxe", true);
    public NumberProperty pickaxeSlotProperty = new NumberProperty("Pickaxe", 3.0, this.pickaxeProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> shovelProperty = new Property<Boolean>("Shovel", false);
    public NumberProperty shovelSlotProperty = new NumberProperty("Shovel Slot", 4.0, this.shovelProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> bowProperty = new Property<Boolean>("Bow", false);
    public NumberProperty bowSlotProperty = new NumberProperty("Bow Slot", 5.0, this.bowProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> blocksProperty = new Property<Boolean>("Blocks", true);
    public NumberProperty blockSlotProperty = new NumberProperty("Block Slot", 6.0, this.blocksProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> projectilesProperty = new Property<Boolean>("Projectiles", true);
    public NumberProperty projectileSlotProperty = new NumberProperty("Projectile Slot", 7.0, this.projectilesProperty::getValue, 1.0, 9.0, 1.0);
    public Property<Boolean> waterBucketProperty = new Property<Boolean>("Water Bucket", true);
    public NumberProperty waterBucketSloProperty = new NumberProperty("Water Bucket Slot", 8.0, this.waterBucketProperty::getValue, 1.0, 9.0, 1.0);
    public Timer startTimer = new Timer();
    public Timer timer = new Timer();
    @EventLink
    private final Listener<MoveEvent> moveEventListener = event -> {
        this.setSuffix(((Mode)((Object)((Object)this.modeProperty.getValue()))).toString());
        if (this.modeProperty.getValue() == Mode.Spoof) {
            if (Util.mc.currentScreen == null) {
                this.startTimer.reset();
            }
            if (!this.startTimer.hasTimeElapsed((Double)this.startDelayProperty.getValue(), false)) {
                return;
            }
        }
        KillAuraModule ka = Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class);
        EntityLivingBase target = KillAuraModule.target;
        if (this.modeProperty.getValue() == Mode.Spoof && (Util.mc.currentScreen != null || target != null)) {
            InventoryUtils.closeInv(true);
            return;
        }
        if (this.modeProperty.getValue() == Mode.Open && !(Util.mc.currentScreen instanceof GuiInventory)) {
            return;
        }
        for (int i = 9; i < 45; ++i) {
            if (!Util.mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;
            ItemStack is = Util.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (!InventoryUtils.timer.hasTimeElapsed((Double)this.speedProperty.getValue(), false)) continue;
            if ((Double)this.swordSlotProperty.getValue() != 0.0 && is.getItem() instanceof ItemSword && is == InventoryUtils.bestSword() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.bestSword()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.swordSlotProperty.getValue())).getStack() != is && this.swordProperty.getValue().booleanValue()) {
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.swordSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.bowSlotProperty.getValue() != 0.0 && is.getItem() instanceof ItemBow && is == InventoryUtils.bestBow() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.bestBow()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.bowSlotProperty.getValue())).getStack() != is && this.bowProperty.getValue().booleanValue()) {
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.bowSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.pickaxeSlotProperty.getValue() != 0.0 && is.getItem() instanceof ItemPickaxe && is == InventoryUtils.bestPick() && is != InventoryUtils.bestWeapon() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.bestPick()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.pickaxeSlotProperty.getValue())).getStack() != is && this.pickaxeProperty.getValue().booleanValue()) {
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.pickaxeSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.axeSlotProperty.getValue() != 0.0 && is.getItem() instanceof ItemAxe && is == InventoryUtils.bestAxe() && is != InventoryUtils.bestWeapon() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.bestAxe()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.axeSlotProperty.getValue())).getStack() != is && this.axeProperty.getValue().booleanValue()) {
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.axeSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.shovelSlotProperty.getValue() != 0.0 && is.getItem() instanceof ItemSpade && is == InventoryUtils.bestShovel() && is != InventoryUtils.bestWeapon() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.bestShovel()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.shovelSlotProperty.getValue())).getStack() != is && this.shovelProperty.getValue().booleanValue()) {
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.shovelSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.blockSlotProperty.getValue() != 0.0 && is.getItem() instanceof ItemBlock && is == InventoryUtils.getBlockSlotInventory() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.getBlockSlotInventory()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.blockSlotProperty.getValue())).getStack() != is && this.blocksProperty.getValue().booleanValue()) {
                if (Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.blockSlotProperty.getValue())).getStack() != null && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.blockSlotProperty.getValue())).getStack().getItem() instanceof ItemBlock && !InventoryUtils.invalidBlocks.contains(((ItemBlock)Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.blockSlotProperty.getValue())).getStack().getItem()).getBlock())) {
                    return;
                }
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.blockSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.projectileSlotProperty.getValue() != 0.0 && is == InventoryUtils.getProjectileSlotInventory() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.getProjectileSlotInventory()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.projectileSlotProperty.getValue())).getStack() != is && this.projectilesProperty.getValue().booleanValue()) {
                if (Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.projectileSlotProperty.getValue())).getStack() != null && (Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.projectileSlotProperty.getValue())).getStack().getItem() instanceof ItemSnowball || Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.projectileSlotProperty.getValue())).getStack().getItem() instanceof ItemEgg || Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.projectileSlotProperty.getValue())).getStack().getItem() instanceof ItemFishingRod)) {
                    return;
                }
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.projectileSlotProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if ((Double)this.waterBucketSloProperty.getValue() != 0.0 && is.getItem() == Items.water_bucket && is == InventoryUtils.getBucketSlotInventory() && Util.mc.thePlayer.inventoryContainer.getInventory().contains(InventoryUtils.getBucketSlotInventory()) && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.shovelSlotProperty.getValue())).getStack() != is && this.waterBucketProperty.getValue().booleanValue()) {
                if (Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.waterBucketSloProperty.getValue())).getStack() != null && Util.mc.thePlayer.inventoryContainer.getSlot((int)(35.0 + (Double)this.waterBucketSloProperty.getValue())).getStack().getItem() == Items.water_bucket) {
                    return;
                }
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, (int)((Double)this.waterBucketSloProperty.getValue() - 1.0), 2, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) {
                    break;
                }
            } else if (InventoryUtils.isBadStack(is, true, true) && this.throwGarbageProperty.getValue().booleanValue()) {
                InventoryUtils.openInv(true);
                Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, i, 1, 4, Util.mc.thePlayer);
                if (!this.stopProperty.getValue().booleanValue()) {
                    InventoryUtils.closeInv(true);
                }
                InventoryUtils.timer.reset();
                if ((Double)this.speedProperty.getValue() != 0.0) break;
            }
            if (!InventoryUtils.timer.hasTimeElapsed(55.0, false)) continue;
            InventoryUtils.closeInv(true);
        }
        if (InventoryUtils.isInventoryOpen && this.stopProperty.getValue().booleanValue()) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
            event.setJump(false);
            event.setSneak(false);
        }
    };

    @Override
    public void onDisable() {
        InventoryUtils.closeInv(true);
        super.onDisable();
    }

    public static enum Mode {
        Open,
        Spoof;

    }
}

