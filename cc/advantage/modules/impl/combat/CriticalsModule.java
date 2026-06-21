/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.player.StrafeEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;

@ModuleInfo(label="Criticals", category=ModuleCategory.COMBAT)
public final class CriticalsModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Edit);
    private boolean activateJump;
    @EventLink
    public final Listener<PacketSendEvent> packetSendEventListener = e -> {
        Packet<?> patt0$temp;
        this.setSuffix(((Mode)((Object)((Object)mode.getValue()))).toString());
        if (mode.getValue() == Mode.Edit && (patt0$temp = e.getPacket()) instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer)patt0$temp;
            packet.onGround = false;
        }
    };
    @EventLink
    public final Listener<StrafeEvent> strafeEventListener = e -> {
        if (mode.getValue() == Mode.Legit && this.activateJump) {
            Util.mc.thePlayer.jump();
            this.activateJump = false;
        }
    };
    @EventLink
    public final Listener<AttackEvent> attackEventListener = e -> {
        if (mode.getValue() == Mode.NCP) {
            boolean willCritLegit;
            boolean bl = willCritLegit = Util.mc.thePlayer.fallDistance > 0.0f && !Util.mc.thePlayer.onGround && !Util.mc.thePlayer.isOnLadder() && !Util.mc.thePlayer.isInWater() && !Util.mc.thePlayer.isPotionActive(Potion.blindness) && !Util.mc.thePlayer.isRiding();
            if (willCritLegit) {
                return;
            }
            PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY + 2.71875E-7, Util.mc.thePlayer.posZ, false));
            PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY + 0.0, Util.mc.thePlayer.posZ, false));
        }
        if (mode.getValue() == Mode.Vanilla) {
            Util.mc.thePlayer.onCriticalHit(e.target);
        }
        if (!(mode.getValue() != Mode.Legit || !Util.mc.thePlayer.onGround || Util.mc.thePlayer.isOnLadder() || Util.mc.thePlayer.isInWater() || Util.mc.thePlayer.isRiding() || Util.mc.gameSettings.keyBindJump.isKeyDown())) {
            this.activateJump = true;
        }
        if (mode.getValue() == Mode.Visual) {
            Util.mc.effectRenderer.emitParticleAtEntity(e.target, EnumParticleTypes.CRIT);
        }
    };

    @Override
    public void onDisable() {
        this.activateJump = false;
        super.onDisable();
    }

    private static enum Mode {
        Vanilla,
        Legit,
        Visual,
        Edit,
        NCP;

    }
}

