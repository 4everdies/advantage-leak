/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

@ModuleInfo(label="Disabler", category=ModuleCategory.CLIENT)
public final class DisablerModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.VerusCombat);
    private boolean transaction;
    private MushPhase mushPhase = MushPhase.CANCEL;
    private final Timer mushTimer = new Timer();
    private long mushPhaseDuration = 0L;
    private int spamTickCounter = 0;
    @EventLink
    private final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        if (this.mode.getValue() == Mode.VerusCombat && event.getPacket() instanceof S32PacketConfirmTransaction) {
            event.setCancelled(true);
            PacketUtils.sendSilentPacket(new C0FPacketConfirmTransaction(this.transaction ? 1 : -1, (short)(this.transaction ? 1 : -1), this.transaction));
            this.transaction = !this.transaction;
        }
    };
    @EventLink
    private final Listener<PacketSendEvent> packetSendEventListener = e -> {
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 1: {
                Packet<?> patt0$temp = e.getPacket();
                if (!(patt0$temp instanceof C02PacketUseEntity)) break;
                C02PacketUseEntity useEntity = (C02PacketUseEntity)patt0$temp;
                Entity victim = Util.mc.theWorld.getEntityByID(useEntity.entityId);
                if (victim == null) {
                    return;
                }
                if (!useEntity.getAction().equals((Object)C02PacketUseEntity.Action.ATTACK)) break;
                double playerX = Util.mc.thePlayer.posX;
                double playerY = Util.mc.thePlayer.posY;
                double playerZ = Util.mc.thePlayer.posZ;
                double targetX = victim.posX;
                double targetY = victim.posY;
                double targetZ = victim.posZ;
                double directionX = targetX - playerX;
                double directionY = targetY - playerY;
                double directionZ = targetZ - playerZ;
                double distance = Math.sqrt((targetX - playerX) * (targetX - playerX) + (targetY - playerY) * (targetY - playerY) + (targetZ - playerZ) * (targetZ - playerZ));
                if (distance < 3.0) {
                    return;
                }
                double length = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
                double normalizedX = directionX / length;
                double normalizedZ = directionZ / length;
                double moveDistance = Math.min(0.21, length);
                double newX = playerX + normalizedX * moveDistance;
                double newZ = playerZ + normalizedZ * moveDistance;
                Util.mc.thePlayer.motionX = 0.08f;
                Util.mc.thePlayer.setPosition(newX, Util.mc.thePlayer.posY, newZ);
                break;
            }
            case 2: {
                if (this.mushPhase != MushPhase.CANCEL || !(e.getPacket() instanceof C0BPacketEntityAction)) break;
                e.setCancelled(true);
            }
        }
    };
    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        if (this.mode.getValue() != Mode.MushMC) {
            return;
        }
        if (this.mushPhaseDuration == 0L) {
            this.mushPhase = MushPhase.CANCEL;
            this.mushTimer.reset();
            this.mushPhaseDuration = 1000L;
            this.spamTickCounter = 0;
        }
        if (this.mushTimer.hasTimeElapsed(this.mushPhaseDuration, false)) {
            this.mushTimer.reset();
            if (this.mushPhase == MushPhase.CANCEL) {
                this.mushPhase = MushPhase.SPAM;
                this.mushPhaseDuration = 3000L;
                this.spamTickCounter = 0;
            } else {
                this.mushPhase = MushPhase.CANCEL;
                this.mushPhaseDuration = 1000L;
                this.spamTickCounter = 0;
            }
        }
        if (this.mushPhase == MushPhase.SPAM && Util.mc.thePlayer != null) {
            ++this.spamTickCounter;
            if (this.spamTickCounter % 2 == 0) {
                PacketUtils.sendSilentPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                PacketUtils.sendSilentPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            float yaw = Util.mc.thePlayer.rotationYaw + (float)(Math.random() * 2.0 - 1.0) * 0.5f;
            float pitch = Util.mc.thePlayer.rotationPitch + (float)(Math.random() * 2.0 - 1.0) * 0.3f;
            boolean onGround = Util.mc.thePlayer.onGround;
            PacketUtils.sendSilentPacket(new C03PacketPlayer.C05PacketPlayerLook(yaw, pitch, onGround));
            if (this.spamTickCounter % 3 == 0) {
                PacketUtils.sendSilentPacket(new C03PacketPlayer.C06PacketPlayerPosLook(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ, yaw, pitch, onGround));
            }
        }
    };

    @Override
    public void onEnable() {
        this.mushPhase = MushPhase.CANCEL;
        this.mushTimer.reset();
        this.mushPhaseDuration = 0L;
        this.spamTickCounter = 0;
        this.transaction = false;
    }

    private static enum Mode {
        VerusCombat("Verus Combat"),
        VulcanReach("Vulcan Reach"),
        MushMC("MushMC");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    private static enum MushPhase {
        CANCEL,
        SPAM;

    }
}

