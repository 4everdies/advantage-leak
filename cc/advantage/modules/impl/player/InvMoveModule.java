/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.InventoryUtils;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;

@ModuleInfo(label="Inventory Movement", category=ModuleCategory.PLAYER)
public class InvMoveModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Normal);
    private boolean sentFirstOpen = false;
    private boolean failedClientStatus = false;
    private boolean failedCloseWindow = false;
    KeyBinding[] moveKeys;
    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener;
    @EventLink
    private final Listener<PacketReceiveEvent> packetReceiveEventListener;

    public InvMoveModule() {
        this.moveKeys = new KeyBinding[]{Util.mc.gameSettings.keyBindForward, Util.mc.gameSettings.keyBindBack, Util.mc.gameSettings.keyBindLeft, Util.mc.gameSettings.keyBindRight, Util.mc.gameSettings.keyBindJump};
        this.preUpdateEventListener = event -> {
            this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
            if (!(Util.mc.currentScreen instanceof GuiChat)) {
                KeyBinding[] keyBindingArray = this.moveKeys;
                int n = this.moveKeys.length;
                for (int n2 = 0; n2 < n; ++n2) {
                    KeyBinding bind = keyBindingArray[n2];
                    KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
                }
                if (this.mode.getValue() == Mode.Hypixel && (Util.mc.currentScreen instanceof GuiInventory || InventoryUtils.isInventoryOpen)) {
                    int safePacketTick;
                    if (!this.sentFirstOpen) {
                        PacketUtils.sendSilentPacket(new C0DPacketCloseWindow());
                        this.sentFirstOpen = true;
                    }
                    int n2 = safePacketTick = Util.mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 3 : 4;
                    if (Util.mc.thePlayer.ticksExisted % safePacketTick == 0) {
                        PacketUtils.sendSilentPacket(new C0DPacketCloseWindow());
                    } else if (Util.mc.thePlayer.ticksExisted % safePacketTick == 1) {
                        PacketUtils.sendSilentPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                    }
                } else {
                    this.sentFirstOpen = false;
                }
            }
        };
        this.packetReceiveEventListener = event -> {
            if (this.mode.getValue() == Mode.Hypixel) {
                if (event.getPacket() instanceof C16PacketClientStatus) {
                    if (this.failedClientStatus) {
                        event.setCancelled();
                    }
                    this.failedClientStatus = true;
                }
                if (event.getPacket() instanceof C0DPacketCloseWindow) {
                    if (this.failedCloseWindow) {
                        event.setCancelled();
                    }
                    this.failedCloseWindow = true;
                }
            }
        };
    }

    @Override
    public void onDisable() {
        if (!GameSettings.isKeyDown(Util.mc.gameSettings.keyBindForward) || Util.mc.currentScreen != null) {
            Util.mc.gameSettings.keyBindForward.setPressed(false);
        }
        if (!GameSettings.isKeyDown(Util.mc.gameSettings.keyBindBack) || Util.mc.currentScreen != null) {
            Util.mc.gameSettings.keyBindBack.setPressed(false);
        }
        if (!GameSettings.isKeyDown(Util.mc.gameSettings.keyBindRight) || Util.mc.currentScreen != null) {
            Util.mc.gameSettings.keyBindRight.setPressed(false);
        }
        if (!GameSettings.isKeyDown(Util.mc.gameSettings.keyBindLeft) || Util.mc.currentScreen != null) {
            Util.mc.gameSettings.keyBindLeft.setPressed(false);
        }
        if (!GameSettings.isKeyDown(Util.mc.gameSettings.keyBindJump) || Util.mc.currentScreen != null) {
            Util.mc.gameSettings.keyBindJump.setPressed(false);
        }
        if (!GameSettings.isKeyDown(Util.mc.gameSettings.keyBindSprint) || Util.mc.currentScreen != null) {
            Util.mc.gameSettings.keyBindSprint.setPressed(false);
        }
        this.sentFirstOpen = false;
        this.failedClientStatus = false;
        this.failedCloseWindow = false;
        super.onDisable();
    }

    private static enum Mode {
        Normal,
        Hypixel;

    }
}

