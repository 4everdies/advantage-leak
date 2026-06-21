/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.InventoryUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(label="Knockback Delay", category=ModuleCategory.COMBAT)
public final class KnockbackDelayModule
extends Module {
    private final NumberProperty airDelay = new NumberProperty("Air Delay", 215.0, 0.0, 1000.0, 10.0);
    private final NumberProperty groundDelay = new NumberProperty("Ground Delay", 215.0, 0.0, 1000.0, 10.0);
    private final NumberProperty chance = new NumberProperty("Chance", 100.0, 0.0, 100.0, 1.0);
    private final NumberProperty activateRange = new NumberProperty("Activate Range", 2.0, 2.0, 3.0, 0.1);
    private final NumberProperty deactivateRange = new NumberProperty("Deactivate Range", 10.0, 3.0, 10.0, 0.1);
    private final Property<Boolean> realtimeDamage = new Property<Boolean>("Realtime Damage", true);
    private final Property<Boolean> requireTarget = new Property<Boolean>("Require Target", false);
    private final Property<Boolean> onlySwords = new Property<Boolean>("Only Swords", false);
    private final Queue<TimedPacket> packets = new ConcurrentLinkedQueue<TimedPacket>();
    private boolean blink;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        int delay;
        if (!event.isPre()) {
            return;
        }
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (Util.mc.isSingleplayer() || Util.mc.thePlayer.ticksExisted < 20) {
            return;
        }
        this.setSuffix(((Double)(Util.mc.thePlayer.onGround ? this.groundDelay : this.airDelay).getValue()).intValue() + "ms");
        if (Util.mc.currentScreen != null) {
            this.reset();
            return;
        }
        if (!this.shouldActivate()) {
            this.reset();
            return;
        }
        int n = delay = Util.mc.thePlayer.onGround ? ((Double)this.groundDelay.getValue()).intValue() : ((Double)this.airDelay.getValue()).intValue();
        if (!this.packets.isEmpty()) {
            this.handle(delay);
        }
        if (Util.mc.thePlayer.hurtTime > 0) {
            this.blink = true;
        } else if (this.packets.isEmpty()) {
            this.blink = false;
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> this.reset();
    @EventLink(value=4)
    public final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        S19PacketEntityStatus statusPacket;
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (Util.mc.isSingleplayer() || Util.mc.thePlayer.ticksExisted < 20 || event.isCancelled()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (this.shouldPass(packet)) {
            return;
        }
        if (this.realtimeDamage.getValue().booleanValue() && packet instanceof S19PacketEntityStatus && (statusPacket = (S19PacketEntityStatus)packet).getOpCode() == 2 && statusPacket.getEntity(Util.mc.theWorld) == Util.mc.thePlayer) {
            return;
        }
        if (this.blink) {
            event.setCancelled();
            this.packets.add(new TimedPacket(packet, System.currentTimeMillis()));
        }
    };

    @Override
    public void onEnable() {
        this.packets.clear();
        this.blink = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.reset();
        super.onDisable();
    }

    private boolean shouldActivate() {
        double distance;
        if (Math.random() * 100.0 > (Double)this.chance.getValue()) {
            return false;
        }
        Entity target = this.findTarget();
        if (this.requireTarget.getValue().booleanValue() && target == null) {
            return false;
        }
        if (target != null && ((distance = (double)Util.mc.thePlayer.getDistanceToEntity(target)) < (Double)this.activateRange.getValue() || distance > (Double)this.deactivateRange.getValue())) {
            return false;
        }
        return this.onlySwords.getValue() == false || InventoryUtils.isHoldingSword();
    }

    private boolean shouldPass(Packet<?> packet) {
        S2CPacketSpawnGlobalEntity globalEntityPacket;
        S2BPacketChangeGameState gameStatePacket;
        int state;
        if (packet instanceof S07PacketRespawn) {
            return true;
        }
        if (packet instanceof S03PacketTimeUpdate) {
            return true;
        }
        if (packet instanceof S06PacketUpdateHealth) {
            return true;
        }
        if (packet instanceof S13PacketDestroyEntities) {
            return true;
        }
        if (packet instanceof S02PacketChat) {
            return true;
        }
        if (packet instanceof S25PacketBlockBreakAnim) {
            return true;
        }
        if (packet instanceof S2FPacketSetSlot) {
            return true;
        }
        if (packet instanceof S2BPacketChangeGameState && ((state = (gameStatePacket = (S2BPacketChangeGameState)packet).getGameState()) == 1 || state == 2 || state == 7 || state == 8)) {
            return true;
        }
        if (packet instanceof S2CPacketSpawnGlobalEntity && (globalEntityPacket = (S2CPacketSpawnGlobalEntity)packet).func_149053_g() == 1) {
            return true;
        }
        if (packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect soundPacket = (S29PacketSoundEffect)packet;
            return "ambient.weather.thunder".equalsIgnoreCase(soundPacket.getSoundName());
        }
        return false;
    }

    private void reset() {
        if (!this.blink && this.packets.isEmpty()) {
            return;
        }
        this.blink = false;
        this.flush();
    }

    private void handle(int delay) {
        TimedPacket wrapper;
        while (!this.packets.isEmpty() && (wrapper = this.packets.peek()) != null && wrapper.elapsed(delay)) {
            this.packets.poll();
            this.processPacketSilent(wrapper.packet);
        }
    }

    private void flush() {
        TimedPacket wrapper;
        while ((wrapper = this.packets.poll()) != null) {
            this.processPacketSilent(wrapper.packet);
        }
    }

    private void processPacketSilent(Packet<?> packet) {
        try {
            if (Util.mc.getNetHandler() != null) {
                packet.processPacket(Util.mc.getNetHandler());
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Entity findTarget() {
        if (TargetSelectionProcess.getTarget() != null) {
            return TargetSelectionProcess.getTarget();
        }
        if (Util.mc.pointedEntity != null) {
            return Util.mc.pointedEntity;
        }
        if (Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            return Util.mc.objectMouseOver.entityHit;
        }
        return null;
    }

    private static final class TimedPacket {
        private final Packet<?> packet;
        private final long time;

        private TimedPacket(Packet<?> packet, long time) {
            this.packet = packet;
            this.time = time;
        }

        private boolean elapsed(int delayMs) {
            return System.currentTimeMillis() - this.time >= (long)delayMs;
        }
    }
}

