/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;

@ModuleInfo(label="W Tap", category=ModuleCategory.COMBAT)
public class WTapModule
extends Module {
    public ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Legit);
    public static final NumberProperty chance = new NumberProperty("Chance", 100.0, 0.0, 100.0, 1.0);
    private boolean unsprint;
    private boolean wTap;
    private EntityLivingBase target;
    @EventLink
    public Listener<AttackEvent> attackEventListener = event -> {
        if (event.target == null) {
            return;
        }
        if (this.mode.getValue() == Mode.Packet) {
            this.target = event.target;
        }
        if (this.mode.getValue() == Mode.Legit) {
            boolean bl = this.wTap = Math.random() * 100.0 < (Double)chance.getValue() && event.target.hurtTime >= 6;
            if (!this.wTap || this.unsprint) {
                return;
            }
            if (Util.mc.thePlayer.isSprinting() || Util.mc.gameSettings.keyBindSprint.isKeyDown()) {
                Util.mc.gameSettings.keyBindSprint.setPressed(true);
                this.unsprint = true;
            }
        }
    };
    @EventLink
    public Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        if (this.mode.getValue() == Mode.Legit) {
            if (!this.wTap) {
                return;
            }
            if (this.unsprint && Math.random() * 100.0 < (Double)chance.getValue()) {
                Util.mc.gameSettings.keyBindSprint.setPressed(false);
                this.unsprint = false;
            }
        }
        if (this.mode.getValue() == Mode.Packet && this.target != null && this.target.hurtTime == 9 && Math.random() * 100.0 < (Double)chance.getValue()) {
            PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private static enum Mode {
        Legit("Legit"),
        Packet("Packet");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

