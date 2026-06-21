/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.BlinkUtils;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Blink", category=ModuleCategory.CLIENT)
public final class BlinkModule
extends Module {
    public final Property<Boolean> sentMode = new Property<Boolean>("Sent", true);
    public final Property<Boolean> receivedMode = new Property<Boolean>("Received", false);
    public final Property<Boolean> pulse = new Property<Boolean>("Pulse", true);
    public final NumberProperty pulseDelay = new NumberProperty("Pulse Delay", 1000.0, this.pulse::getValue, 500.0, 5000.0, 100.0);
    public final Property<Boolean> fakePlayer = new Property<Boolean>("FakePlayer", true);
    private final Timer pulseTimer = new Timer();
    @EventLink
    public final Listener<WorldLoadEvent> onWorldLoad = event -> {
        BlinkUtils.clear();
        BlinkUtils.removeFakePlayer();
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (!this.isEnabled()) {
            return;
        }
        boolean sent = this.sentMode.getValue();
        boolean receive = this.receivedMode.getValue();
        BlinkUtils.handleOutgoingPacket(event.getPacket(), event, sent, receive);
    };
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (!this.isEnabled()) {
            return;
        }
        boolean sent = this.sentMode.getValue();
        boolean receive = this.receivedMode.getValue();
        BlinkUtils.handleIncomingPacket(event.getPacket(), event, sent, receive);
    };
    @EventLink
    public final Listener<MotionEvent> onMotion = event -> {
        if (event.isPre()) {
            return;
        }
        if (Util.mc.thePlayer == null || Util.mc.thePlayer.isDead || Util.mc.thePlayer.ticksExisted <= 10) {
            BlinkUtils.unblink();
            return;
        }
        if (this.sentMode.getValue().booleanValue() && !this.receivedMode.getValue().booleanValue()) {
            BlinkUtils.flushReceivedPackets();
        } else if (this.receivedMode.getValue().booleanValue() && !this.sentMode.getValue().booleanValue()) {
            BlinkUtils.flushSentPackets();
        }
        if (this.pulse.getValue().booleanValue() && this.pulseTimer.hasTimeElapsed(((Double)this.pulseDelay.getValue()).longValue(), false)) {
            BlinkUtils.unblink();
            if (this.fakePlayer.getValue().booleanValue()) {
                BlinkUtils.addFakePlayer();
            }
            this.pulseTimer.reset();
        }
    };
    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (BlinkUtils.positions.isEmpty()) {
            return;
        }
        Color color = Color.WHITE;
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glEnable(3042);
        GL11.glDisable(2929);
        Util.mc.entityRenderer.disableLightmap();
        GL11.glBegin(3);
        GL11.glColor3f((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
        double renderPosX = Util.mc.getRenderManager().viewerPosX;
        double renderPosY = Util.mc.getRenderManager().viewerPosY;
        double renderPosZ = Util.mc.getRenderManager().viewerPosZ;
        for (Vec3 pos : BlinkUtils.positions) {
            GL11.glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ);
        }
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        GL11.glEnd();
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
    };

    @Override
    public void onEnable() {
        this.pulseTimer.reset();
        if (this.fakePlayer.getValue().booleanValue()) {
            BlinkUtils.addFakePlayer();
        }
    }

    @Override
    public void onDisable() {
        BlinkUtils.unblink();
    }

    @Override
    public String getSuffix() {
        int count = BlinkUtils.packets.size() + BlinkUtils.packetsReceived.size();
        return count > 0 ? count + " packets" : null;
    }
}

