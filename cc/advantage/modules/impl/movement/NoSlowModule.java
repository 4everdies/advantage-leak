/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.movement;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.ItemSlowdownEvent;
import cc.advantage.api.events.impl.player.TeleportEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.BlinkProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.InventoryUtils;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@ModuleInfo(label="No Slow", category=ModuleCategory.MOVEMENT)
public final class NoSlowModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Vanilla);
    private final NumberProperty amount = new NumberProperty("Amount", 2.0, () -> this.mode.getValue() == Mode.Ticks, 2.0, 5.0, 1.0);
    public final Property<Boolean> food = new Property<Boolean>("Food", true, () -> this.mode.getValue() != Mode.Blink && this.mode.getValue() != Mode.Hypixel);
    public final Property<Boolean> potion = new Property<Boolean>("Potion", true, () -> this.mode.getValue() != Mode.Blink && this.mode.getValue() != Mode.Hypixel);
    public final Property<Boolean> sword = new Property<Boolean>("Sword", true, () -> this.mode.getValue() != Mode.Blink && this.mode.getValue() != Mode.Hypixel);
    public final Property<Boolean> bow = new Property<Boolean>("Bow", true, () -> this.mode.getValue() != Mode.Blink && this.mode.getValue() != Mode.Hypixel);
    public static boolean isUsing;
    private int disable;
    public int tick;
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        ++this.disable;
        if (this.mode.getValue() == Mode.Blink) {
            if (Util.mc.thePlayer.isUsingItem()) {
                if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemFood) {
                    BlinkProcess.enable();
                    isUsing = true;
                }
            } else if (isUsing) {
                BlinkProcess.disable();
                isUsing = false;
            }
        }
        if (Util.mc.thePlayer.isUsingItem()) {
            if (this.mode.getValue() == Mode.Hypixel && this.tick == 0 && InventoryUtils.isHoldingSword()) {
                PacketUtils.sendPacket(new C09PacketHeldItemChange((Util.mc.thePlayer.inventory.currentItem + 1) % 9));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                ++this.tick;
            }
        } else {
            this.tick = 0;
        }
    };
    @EventLink
    public final Listener<ItemSlowdownEvent> itemSlowdownEventListener = e -> {
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 0: {
                if (this.food.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                    e.setCancelled();
                }
                if (this.potion.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
                    e.setCancelled();
                }
                if (this.sword.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    e.setCancelled();
                }
                if (!this.bow.getValue().booleanValue() || !Util.mc.thePlayer.isUsingItem() || !(Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow)) break;
                e.setCancelled();
                break;
            }
            case 2: {
                if (!this.sword.getValue().booleanValue() || !Util.mc.thePlayer.isUsingItem() || !(Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) break;
                e.setCancelled();
                break;
            }
            case 3: {
                if (!Util.mc.thePlayer.isUsingItem() || !(Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood)) break;
                e.setCancelled();
                break;
            }
            case 5: {
                if ((double)Util.mc.thePlayer.onGroundTicks % (Double)this.amount.getValue() == 0.0 || !MovementUtils.isOnGround()) break;
                if (this.food.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                    e.setCancelled();
                }
                if (this.potion.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
                    e.setCancelled();
                }
                if (this.sword.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    e.setCancelled();
                }
                if (!this.bow.getValue().booleanValue() || !Util.mc.thePlayer.isUsingItem() || !(Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow)) break;
                e.setCancelled();
                break;
            }
            case 4: {
                if (this.food.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                    e.setCancelled();
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                }
                if (this.potion.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
                    e.setCancelled();
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                }
                if (this.sword.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    e.setCancelled();
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                }
                if (!this.bow.getValue().booleanValue() || !Util.mc.thePlayer.isUsingItem() || !(Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow)) break;
                e.setCancelled();
                PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                break;
            }
            case 1: {
                if (this.food.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
                    e.setCancelled();
                    if (this.disable > 10) {
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                    }
                }
                if (this.potion.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
                    e.setCancelled();
                    if (this.disable > 10) {
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                    }
                }
                if (this.sword.getValue().booleanValue() && Util.mc.thePlayer.isUsingItem() && Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    e.setCancelled();
                    if (this.disable > 10) {
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                    }
                }
                if (!this.bow.getValue().booleanValue() || !Util.mc.thePlayer.isUsingItem() || !(Util.mc.thePlayer.getHeldItem().getItem() instanceof ItemBow)) break;
                e.setCancelled();
                if (this.disable <= 10) break;
                PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem % 8 + 1));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
            }
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = e -> {
        isUsing = false;
        this.disable = 0;
    };
    @EventLink
    public final Listener<TeleportEvent> teleportEventListener = e -> {
        isUsing = false;
        this.disable = 0;
    };

    @Override
    public void onDisable() {
        isUsing = false;
        super.onDisable();
    }

    private static enum Mode {
        Vanilla("Vanilla"),
        UpdatedNCP("Mushmc"),
        Hypixel("Hypixel"),
        Blink("Blink"),
        Switch("Switch"),
        Ticks("Ticks");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

