/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.InventoryUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockTNT;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAnvilBlock;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.opengl.Display;

@ModuleInfo(label="Stealer", category=ModuleCategory.PLAYER)
public final class StealerModule
extends Module {
    private final Timer timer = new Timer();
    private final Timer startTimer = new Timer();
    private final NumberProperty startDelayProperty = new NumberProperty("Start Delay", 50.0, 0.0, 1000.0, 25.0);
    private final NumberProperty minDelayProperty = new NumberProperty("Min Delay", 5.0, 0.0, 1000.0, 25.0);
    private final NumberProperty maxDelayProperty = new NumberProperty("Max Delay", 5.0, 0.0, 1000.0, 25.0);
    private final Property<Boolean> stealTrashItemsProperty = new Property<Boolean>("Steal Trash Items", false);
    private final Property<Boolean> autoCloseProperty = new Property<Boolean>("Auto Close", true);
    private final Property<Boolean> chestNameProperty = new Property<Boolean>("Check Chest Name", false);
    private final Property<Boolean> grabMouseProperty = new Property<Boolean>("Grab Mouse", false);
    private int decidedTimer = 0;
    private boolean gotItems;
    private int ticksInChest;
    private boolean lastInChest;
    @EventLink
    private final Listener<MotionEvent> motionEventListener = e -> {
        if (!e.isPre()) {
            return;
        }
        if (Util.mc.thePlayer.ticksExisted <= 60) {
            return;
        }
        if (this.grabMouseProperty.getValue().booleanValue() && Util.mc.currentScreen instanceof GuiChest && Display.isActive() && (!this.chestNameProperty.getValue().booleanValue() || ((GuiChest)Util.mc.currentScreen).lowerChestInventory.getDisplayName().getUnformattedText().contains("chest"))) {
            Util.mc.mouseHelper.mouseXYChange();
            Util.mc.mouseHelper.ungrabMouseCursor();
            Util.mc.mouseHelper.grabMouseCursor();
        }
        if (Util.mc.currentScreen instanceof GuiChest) {
            ++this.ticksInChest;
            if (this.ticksInChest * 50 > 255) {
                this.ticksInChest = 10;
            }
        } else {
            --this.ticksInChest;
            this.gotItems = false;
            if (this.ticksInChest < 0) {
                this.ticksInChest = 0;
            }
        }
    };
    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (Util.mc.thePlayer.ticksExisted <= 60) {
            return;
        }
        if (!this.lastInChest) {
            this.startTimer.reset();
        }
        this.lastInChest = Util.mc.currentScreen instanceof GuiChest;
        if (Util.mc.currentScreen instanceof GuiChest) {
            String name;
            if (this.chestNameProperty.getValue().booleanValue() && !(name = ((GuiChest)Util.mc.currentScreen).lowerChestInventory.getDisplayName().getUnformattedText()).toLowerCase().contains("chest")) {
                return;
            }
            if (!this.startTimer.hasTimeElapsed((Double)this.startDelayProperty.getValue(), false)) {
                return;
            }
            if (this.decidedTimer == 0) {
                int delayFirst = (int)Math.floor(Math.min((Double)this.minDelayProperty.getValue(), (Double)this.maxDelayProperty.getValue()));
                int delaySecond = (int)Math.ceil(Math.max((Double)this.minDelayProperty.getValue(), (Double)this.maxDelayProperty.getValue()));
                this.decidedTimer = RandomUtils.nextInt(delayFirst, delaySecond);
            }
            if (this.timer.hasTimeElapsed(this.decidedTimer, false)) {
                ContainerChest chest = (ContainerChest)Util.mc.thePlayer.openContainer;
                for (int i = 0; i < chest.inventorySlots.size(); ++i) {
                    ItemStack stack = chest.getLowerChestInventory().getStackInSlot(i);
                    if (stack == null || !this.itemWhitelisted(stack) || this.stealTrashItemsProperty.getValue().booleanValue()) continue;
                    Util.mc.playerController.windowClick(chest.windowId, i, 0, 1, Util.mc.thePlayer);
                    this.timer.reset();
                    int delayFirst = (int)Math.floor(Math.min((Double)this.minDelayProperty.getValue(), (Double)this.maxDelayProperty.getValue()));
                    int delaySecond = (int)Math.ceil(Math.max((Double)this.minDelayProperty.getValue(), (Double)this.maxDelayProperty.getValue()));
                    this.decidedTimer = RandomUtils.nextInt(delayFirst, delaySecond);
                    this.gotItems = true;
                    return;
                }
                if (this.gotItems && this.autoCloseProperty.getValue().booleanValue() && this.ticksInChest > 3) {
                    Util.mc.thePlayer.closeScreen();
                }
            }
        }
    };

    private boolean itemWhitelisted(ItemStack itemStack) {
        if (InventoryUtils.isBadStackStealer(itemStack, true, true)) {
            return false;
        }
        ArrayList<Item> whitelistedItems = new ArrayList<Item>(){
            {
                this.add(Items.ender_pearl);
                this.add(Items.iron_ingot);
                this.add(Items.snowball);
                this.add(Items.gold_ingot);
                this.add(Items.redstone);
                this.add(Items.diamond);
                this.add(Items.emerald);
                this.add(Items.quartz);
                this.add(Items.bow);
                this.add(Items.arrow);
                this.add(Items.fishing_rod);
                this.add(Items.egg);
                this.add(Items.water_bucket);
                this.add(Items.lava_bucket);
            }
        };
        Item item = itemStack.getItem();
        String itemName = itemStack.getDisplayName();
        if (itemName.contains("Right Click") || itemName.contains("Click to Use") || itemName.contains("Players Finder")) {
            return true;
        }
        ArrayList<Integer> whitelistedPotions = new ArrayList<Integer>(){
            {
                this.add(6);
                this.add(1);
                this.add(5);
                this.add(8);
                this.add(14);
                this.add(12);
                this.add(10);
                this.add(16);
            }
        };
        if (item instanceof ItemPotion) {
            int potionID = this.getPotionId(itemStack);
            return whitelistedPotions.contains(potionID);
        }
        return item instanceof ItemBlock && !(((ItemBlock)item).getBlock() instanceof BlockTNT) && !(((ItemBlock)item).getBlock() instanceof BlockSlime) && !(((ItemBlock)item).getBlock() instanceof BlockFalling) || item instanceof ItemAnvilBlock || item instanceof ItemSword || item instanceof ItemArmor || item instanceof ItemTool || item instanceof ItemFood || item instanceof ItemSkull || itemName.contains("\u00a7") || whitelistedItems.contains(item) && !item.equals(Items.spider_eye);
    }

    private int getPotionId(ItemStack potion) {
        Item item = potion.getItem();
        try {
            if (item instanceof ItemPotion) {
                ItemPotion p = (ItemPotion)item;
                return p.getEffects(potion.getMetadata()).get(0).getPotionID();
            }
        }
        catch (NullPointerException nullPointerException) {
            // empty catch block
        }
        return 0;
    }
}

