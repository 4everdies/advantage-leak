/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(label="Regen", category=ModuleCategory.PLAYER)
public final class RegenModule
extends Module {
    private final NumberProperty health = new NumberProperty("Minimum Health", 15.0, 1.0, 20.0, 1.0);
    private final NumberProperty packets = new NumberProperty("Speed", 20.0, 1.0, 100.0, 1.0);
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        if (Util.mc.thePlayer.getHealth() < ((Double)this.health.getValue()).floatValue()) {
            for (int i = 0; i < ((Double)this.packets.getValue()).intValue(); ++i) {
                PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ, Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch, Util.mc.thePlayer.onGround));
            }
        }
    };
}

