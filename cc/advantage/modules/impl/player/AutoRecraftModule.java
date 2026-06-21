/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.RandomUtils;

@ModuleInfo(label="Auto Recraft", category=ModuleCategory.PLAYER)
public final class AutoRecraftModule
extends Module {
    private final NumberProperty startMinDelay = new NumberProperty("Start Min Delay", 30.0, 1.0, 400.0, 1.0);
    private final NumberProperty startMaxDelay = new NumberProperty("Start Max Delay", 42.0, 1.0, 400.0, 1.0);
    private final NumberProperty recraftMinDelay = new NumberProperty("Recraft Min Delay", 30.0, 1.0, 400.0, 1.0);
    private final NumberProperty recraftMaxDelay = new NumberProperty("Recraft Max Delay", 42.0, 1.0, 400.0, 1.0);
    private final NumberProperty startWith = new NumberProperty("Start With", 3.0, 0.0, 41.0, 1.0);
    private final Property<Boolean> autoClose = new Property<Boolean>("Auto Close", false);
    private final Property<Boolean> autoOpen = new Property<Boolean>("Auto Open", true);
    private final Property<Boolean> cactusMode = new Property<Boolean>("Cactus", false);
    private final Property<Boolean> cocoaMode = new Property<Boolean>("Cocoa", true);
    private final Property<Boolean> mushroomMode = new Property<Boolean>("Mushroom", true);
    private final ModeProperty<SortMode> sortMode = new ModeProperty<SortMode>("Sort By", SortMode.Size);
    private final Timer startTimer = new Timer();
    private final Timer recraftTimer = new Timer();
    private final HashMap<String, Integer> recraftMap = new HashMap();
    private boolean started;
    private boolean openedByAutoOpen;
    private long recraftDelay = AutoRecraftModule.randomDelay(30L, 42L);
    private int step = 1;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        long delay;
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (!(Util.mc.currentScreen instanceof GuiInventory)) {
            if (this.autoOpen.getValue().booleanValue() && this.shouldAutoOpen() && Util.mc.currentScreen == null) {
                Util.mc.displayGuiScreen(new GuiInventory(Util.mc.thePlayer));
                this.openedByAutoOpen = true;
            } else {
                this.reset();
                return;
            }
        }
        if (this.started) {
            this.runRecraft();
        } else if (this.getTotalSoupsInInventory() <= ((Double)this.startWith.getValue()).intValue() && this.startTimer.hasTimeElapsed(delay = AutoRecraftModule.randomDelay(((Double)this.startMinDelay.getValue()).longValue(), ((Double)this.startMaxDelay.getValue()).longValue()))) {
            this.startTimer.reset();
            if (this.cactusMode.getValue().booleanValue() && this.hasCactusRecraft()) {
                this.getRecraft(1);
            } else if (this.cocoaMode.getValue().booleanValue() && this.hasCocoaRecraft()) {
                this.getRecraft(2);
            } else if (this.mushroomMode.getValue().booleanValue() && this.hasMushroomRecraft()) {
                this.getRecraft(3);
            }
            this.recraftDelay = AutoRecraftModule.randomDelay(((Double)this.recraftMinDelay.getValue()).longValue(), ((Double)this.recraftMaxDelay.getValue()).longValue());
            this.started = true;
        }
    };

    private void runRecraft() {
        if (this.recraftMap.isEmpty()) {
            this.reset();
            return;
        }
        if (!this.recraftTimer.hasTimeElapsed(this.recraftDelay)) {
            return;
        }
        this.recraftTimer.reset();
        this.recraftDelay = AutoRecraftModule.randomDelay(((Double)this.recraftMinDelay.getValue()).longValue(), ((Double)this.recraftMaxDelay.getValue()).longValue());
        if (this.recraftMap.size() == 2) {
            this.runTwoIngredientRecraft();
        } else if (this.recraftMap.size() == 3) {
            this.runMushroomRecraft();
        }
    }

    private void runTwoIngredientRecraft() {
        switch (this.step) {
            case 1: {
                this.click(this.recraftMap.get("bowl"), 1, 0);
                break;
            }
            case 2: {
                this.click(1, 0, 0);
                break;
            }
            case 3: {
                if (this.cactusMode.getValue().booleanValue() && this.recraftMap.containsKey("cactus")) {
                    this.click(this.recraftMap.get("cactus"), 1, 0);
                    break;
                }
                if (!this.cocoaMode.getValue().booleanValue() || !this.recraftMap.containsKey("cocoa")) break;
                this.click(this.recraftMap.get("cocoa"), 1, 0);
                break;
            }
            case 4: {
                this.click(2, 0, 0);
                break;
            }
            case 5: {
                this.click(0, 0, 1);
                break;
            }
            case 6: {
                if (Util.mc.thePlayer.inventoryContainer.getSlot(2).getStack() == null) break;
                this.click(2, 0, 1);
                return;
            }
            case 7: {
                if (Util.mc.thePlayer.inventoryContainer.getSlot(1).getStack() == null) break;
                this.click(1, 0, 1);
                return;
            }
            default: {
                this.reset();
                return;
            }
        }
        ++this.step;
    }

    private void runMushroomRecraft() {
        switch (this.step) {
            case 1: {
                this.click(this.recraftMap.get("bowl"), 1, 0);
                break;
            }
            case 2: {
                this.click(1, 0, 0);
                break;
            }
            case 3: {
                this.click(this.recraftMap.get("red"), 1, 0);
                break;
            }
            case 4: {
                this.click(2, 0, 0);
                break;
            }
            case 5: {
                this.click(this.recraftMap.get("brown"), 1, 0);
                break;
            }
            case 6: {
                this.click(3, 0, 0);
                break;
            }
            case 7: {
                this.click(0, 0, 1);
                break;
            }
            case 8: {
                if (Util.mc.thePlayer.inventoryContainer.getSlot(3).getStack() == null) break;
                this.click(3, 0, 1);
                return;
            }
            case 9: {
                if (Util.mc.thePlayer.inventoryContainer.getSlot(2).getStack() == null) break;
                this.click(2, 0, 1);
                return;
            }
            case 10: {
                if (Util.mc.thePlayer.inventoryContainer.getSlot(1).getStack() == null) break;
                this.click(1, 0, 1);
                return;
            }
            default: {
                this.reset();
                return;
            }
        }
        ++this.step;
    }

    private void click(int slot, int mouseButton, int mode) {
        Util.mc.playerController.windowClick(Util.mc.thePlayer.inventoryContainer.windowId, slot, mouseButton, mode, Util.mc.thePlayer);
    }

    private int getTotalSoupsInInventory() {
        int counter = 0;
        for (int i = 9; i < 45; ++i) {
            ItemStack itemStack = Util.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack == null || !(itemStack.getItem() instanceof ItemSoup)) continue;
            ++counter;
        }
        return counter;
    }

    private boolean hasCocoaRecraft() {
        AtomicBoolean bowl = new AtomicBoolean(false);
        AtomicBoolean cocoa = new AtomicBoolean(false);
        for (ItemStack itemStack : Util.mc.thePlayer.inventory.mainInventory) {
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemDye && EnumDyeColor.byDyeDamage(itemStack.getMetadata()) == EnumDyeColor.BROWN) {
                cocoa.set(true);
                continue;
            }
            if (itemStack.getItem() != Items.bowl) continue;
            bowl.set(true);
        }
        return bowl.get() && cocoa.get();
    }

    private boolean hasMushroomRecraft() {
        AtomicBoolean bowl = new AtomicBoolean(false);
        AtomicBoolean redMushroom = new AtomicBoolean(false);
        AtomicBoolean brownMushroom = new AtomicBoolean(false);
        for (ItemStack itemStack : Util.mc.thePlayer.inventory.mainInventory) {
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock)itemStack.getItem()).getBlock();
                if (block == Blocks.red_mushroom) {
                    redMushroom.set(true);
                    continue;
                }
                if (block != Blocks.brown_mushroom) continue;
                brownMushroom.set(true);
                continue;
            }
            if (itemStack.getItem() != Items.bowl) continue;
            bowl.set(true);
        }
        return bowl.get() && redMushroom.get() && brownMushroom.get();
    }

    private boolean hasCactusRecraft() {
        AtomicBoolean bowl = new AtomicBoolean(false);
        AtomicBoolean cactus = new AtomicBoolean(false);
        for (ItemStack itemStack : Util.mc.thePlayer.inventory.mainInventory) {
            if (itemStack == null) continue;
            if (itemStack.getItem() instanceof ItemBlock && ((ItemBlock)itemStack.getItem()).getBlock() == Blocks.cactus) {
                cactus.set(true);
                continue;
            }
            if (itemStack.getItem() != Items.bowl) continue;
            bowl.set(true);
        }
        return bowl.get() && cactus.get();
    }

    private boolean shouldAutoOpen() {
        if (!this.started) {
            if (this.getTotalSoupsInInventory() > ((Double)this.startWith.getValue()).intValue()) {
                return false;
            }
            return this.cactusMode.getValue() != false && this.hasCactusRecraft() || this.cocoaMode.getValue() != false && this.hasCocoaRecraft() || this.mushroomMode.getValue() != false && this.hasMushroomRecraft();
        }
        return !this.recraftMap.isEmpty();
    }

    private void getRecraft(int mode) {
        HashMap<Integer, String> itemSlotMap = new HashMap<Integer, String>();
        block5: for (int i = 9; i < 45; ++i) {
            ItemStack itemStack = Util.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (itemStack == null) continue;
            switch (mode) {
                case 1: {
                    if (itemStack.getItem() == Items.bowl) {
                        itemSlotMap.put(i, "bowl");
                        continue block5;
                    }
                    if (!(itemStack.getItem() instanceof ItemBlock) || ((ItemBlock)itemStack.getItem()).getBlock() != Blocks.cactus) continue block5;
                    itemSlotMap.put(i, "cactus");
                    continue block5;
                }
                case 2: {
                    if (itemStack.getItem() == Items.bowl) {
                        itemSlotMap.put(i, "bowl");
                        continue block5;
                    }
                    if (!(itemStack.getItem() instanceof ItemDye) || EnumDyeColor.byDyeDamage(itemStack.getMetadata()) != EnumDyeColor.BROWN) continue block5;
                    itemSlotMap.put(i, "cocoa");
                    continue block5;
                }
                case 3: {
                    if (itemStack.getItem() == Items.bowl) {
                        itemSlotMap.put(i, "bowl");
                        continue block5;
                    }
                    if (!(itemStack.getItem() instanceof ItemBlock)) continue block5;
                    Block block = ((ItemBlock)itemStack.getItem()).getBlock();
                    if (block == Blocks.red_mushroom) {
                        itemSlotMap.put(i, "red");
                        continue block5;
                    }
                    if (block != Blocks.brown_mushroom) continue block5;
                    itemSlotMap.put(i, "brown");
                    continue block5;
                }
            }
        }
        Stream<Object> itemSlotStream = itemSlotMap.entrySet().stream();
        itemSlotStream = this.sortMode.getValue() == SortMode.Size ? itemSlotStream.sorted(Comparator.comparingInt(entry -> Util.mc.thePlayer.inventoryContainer.getSlot((int)((Integer)entry.getKey()).intValue()).getStack().stackSize)) : itemSlotStream.sorted(Map.Entry.comparingByKey((e1, e2) -> e2 - e1));
        itemSlotStream.forEach(entry -> this.recraftMap.put((String)entry.getValue(), (Integer)entry.getKey()));
    }

    private void reset() {
        this.recraftTimer.reset();
        this.startTimer.reset();
        this.recraftMap.clear();
        this.started = false;
        this.step = 1;
        this.recraftDelay = 0L;
        if (this.autoClose.getValue().booleanValue() && this.openedByAutoOpen && Util.mc.currentScreen instanceof GuiInventory) {
            Util.mc.displayGuiScreen(null);
        }
        this.openedByAutoOpen = false;
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private static long randomDelay(long min, long max) {
        long high;
        long low = Math.min(min, max);
        if (low == (high = Math.max(min, max))) {
            return low;
        }
        return RandomUtils.nextLong(low, high + 1L);
    }

    private static enum SortMode {
        Size,
        Index;

    }
}

