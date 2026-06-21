/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.combat.KillAuraModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.GlUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.util.AxisAlignedBB;

@ModuleInfo(label="Blink Range", category=ModuleCategory.COMBAT)
public final class BlinkRangeModule
extends Module {
    public static ModeProperty<Mode> modeProperty = new ModeProperty<Mode>("Mode", Mode.Range);
    public static NumberProperty rangeToUnblinkProperty = new NumberProperty("Range To Reset Blink", 2.0, () -> modeProperty.getValue() == Mode.Range, 0.1, 6.0, 0.05);
    public static final Property<Boolean> displayProperty = new Property<Boolean>("Display", true);
    public static final Property<Boolean> onlyWithKillaura = new Property<Boolean>("Only With Killaura", true);
    public static final Property<Boolean> renderBlinkPos = new Property<Boolean>("Render Blink Pos", true);
    public final Property<Boolean> outgoing = new Property<Boolean>("Outgoing", true);
    public final Property<Boolean> incoming = new Property<Boolean>("Incoming", false);
    public final Property<Boolean> dummy = new Property<Boolean>("Dummy", false);
    public final Property<Boolean> ambush = new Property<Boolean>("Ambush", false);
    public final Property<Boolean> autoDisable = new Property<Boolean>("AutoDisable", true);
    public final Property<Boolean> autoResetEnabled = new Property<Boolean>("AutoReset", false);
    public final NumberProperty resetAfter = new NumberProperty("ResetAfter", 100.0, 1.0, 1000.0, 1.0);
    public final ModeProperty<ResetAction> resetAction = new ModeProperty<ResetAction>("ResetAction", ResetAction.RESET);
    private final Timer timer = new Timer();
    private boolean blinked = false;
    private double blinkedX;
    private double blinkedY;
    private double blinkedZ;
    private EntityOtherPlayerMP dummyPlayer;
    private int tickCounter;
    private final Queue<PacketEntry> outgoingQueue = new ConcurrentLinkedQueue<PacketEntry>();
    private final Queue<PacketEntry> incomingQueue = new ConcurrentLinkedQueue<PacketEntry>();
    @EventLink
    public Listener<WorldLoadEvent> onWorldLoad = event -> {
        if (this.blinked) {
            this.releaseAllPackets();
            this.blinked = false;
        }
        this.removeDummy();
        this.tickCounter = 0;
    };
    @EventLink
    public Listener<PacketSendEvent> onPacketSend = event -> {
        if (!this.blinked) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (this.ambush.getValue().booleanValue()) {
            if (packet instanceof C02PacketUseEntity) {
                C02PacketUseEntity useEntity = (C02PacketUseEntity)packet;
                if (useEntity.getAction() == C02PacketUseEntity.Action.ATTACK || useEntity.getAction() == C02PacketUseEntity.Action.INTERACT_AT) {
                    this.releaseAllPackets();
                    this.blinked = false;
                    return;
                }
            } else if (packet instanceof C18PacketSpectate) {
                this.releaseAllPackets();
                this.blinked = false;
                return;
            }
        }
        if (this.outgoing.getValue().booleanValue()) {
            event.setCancelled(true);
            this.outgoingQueue.add(new PacketEntry(packet));
        }
    };
    @EventLink
    public Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (!this.blinked) {
            return;
        }
        if (this.incoming.getValue().booleanValue()) {
            event.setCancelled(true);
            this.incomingQueue.add(new PacketEntry(event.getPacket()));
        }
    };
    @EventLink
    public Listener<MotionEvent> motionEventListener = event -> {
        if (event.isPre()) {
            return;
        }
        if (this.blinked) {
            ++this.tickCounter;
            if (this.autoResetEnabled.getValue().booleanValue() && this.tickCounter > ((Double)this.resetAfter.getValue()).intValue()) {
                this.tickCounter = 0;
                if (this.resetAction.getValue() == ResetAction.RESET) {
                    this.outgoingQueue.clear();
                    this.incomingQueue.clear();
                } else {
                    this.releaseAllPackets();
                    if (this.dummyPlayer != null) {
                        this.dummyPlayer.copyLocationAndAnglesFrom(Util.mc.thePlayer);
                    }
                }
                Util.mc.thePlayer.sendChatMessage("\u00a78[\u00a7bBlink\u00a78] \u00a7fAuto reset");
                if (this.autoDisable.getValue().booleanValue()) {
                    this.releaseAllPackets();
                    this.blinked = false;
                }
            }
        }
        if (onlyWithKillaura.getValue().booleanValue() && !Advantage.INSTANCE.getModuleManager().getModule(KillAuraModule.class).isEnabled()) {
            if (this.blinked) {
                this.releaseAllPackets();
                this.blinked = false;
                this.timer.reset();
            }
            return;
        }
        this.setSuffix(modeProperty.getValue() == Mode.Range ? String.valueOf(((Double)rangeToUnblinkProperty.getValue()).intValue()) : String.valueOf(this.timer.getTime()));
        if (modeProperty.getValue() == Mode.Time) {
            if (this.timer.hasTimeElapsed(500.0) && !this.blinked) {
                this.enableBlink();
                this.blinked = true;
                this.timer.reset();
            }
            if (this.timer.hasTimeElapsed(500.0) && this.blinked) {
                this.releaseAllPackets();
                this.blinked = false;
                this.timer.reset();
            }
        }
        if (modeProperty.getValue() == Mode.Range) {
            if (TargetSelectionProcess.getTarget() == null) {
                if (this.blinked) {
                    this.releaseAllPackets();
                    this.blinked = false;
                }
                return;
            }
            if (!MovementUtils.isMoving()) {
                if (this.blinked) {
                    this.releaseAllPackets();
                    this.blinked = false;
                }
                return;
            }
            double distance = Util.mc.thePlayer.getDistanceToEntity(TargetSelectionProcess.getTarget());
            if (distance <= (Double)KillAuraModule.seekRange.getValue() && distance > (Double)rangeToUnblinkProperty.getValue()) {
                if (!this.blinked) {
                    this.blinkedX = Util.mc.thePlayer.posX;
                    this.blinkedY = Util.mc.thePlayer.posY;
                    this.blinkedZ = Util.mc.thePlayer.posZ;
                    this.enableBlink();
                    this.blinked = true;
                }
            } else if (distance <= (Double)rangeToUnblinkProperty.getValue() && this.blinked) {
                this.releaseAllPackets();
                this.blinked = false;
            }
        }
    };
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = event -> {
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        ScaledResolution sr = new ScaledResolution(Util.mc);
        if (this.blinked && displayProperty.getValue().booleanValue()) {
            fr.drawStringWithShadow("Blinking..", (float)sr.getScaledWidth() / 2.0f - (float)fr.getStringWidth("Blinking..") / 2.0f, (float)sr.getScaledHeight() / 10.0f - (float)fr.FONT_HEIGHT, Color.YELLOW.getRGB());
        }
    };
    @EventLink
    public Listener<Render3DEvent> render3DEventListener = event -> {
        if (!this.blinked || !renderBlinkPos.getValue().booleanValue() || Util.mc.gameSettings.thirdPersonView == 0) {
            return;
        }
        double x = this.blinkedX - Util.mc.getRenderManager().viewerPosX;
        double y = this.blinkedY - Util.mc.getRenderManager().viewerPosY;
        double z = this.blinkedZ - Util.mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = new AxisAlignedBB(x - 0.3, y, z - 0.3, x + 0.3, y + 1.8, z + 0.3);
        Color color = ColorProcess.getColor();
        RenderUtils.start3D();
        GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        RenderUtils.drawBoundingBox(bb);
        RenderUtils.stop3D();
        GlUtils.resetColor();
    };

    @Override
    public void onEnable() {
        this.timer.reset();
        this.tickCounter = 0;
        this.outgoingQueue.clear();
        this.incomingQueue.clear();
        if (this.dummy.getValue().booleanValue()) {
            this.spawnDummy();
        }
    }

    @Override
    public void onDisable() {
        if (this.blinked) {
            this.releaseAllPackets();
            this.blinked = false;
        }
        this.removeDummy();
        this.timer.reset();
    }

    private void enableBlink() {
        this.tickCounter = 0;
        if (this.dummy.getValue().booleanValue() && this.dummyPlayer == null) {
            this.spawnDummy();
        }
    }

    private void releaseAllPackets() {
        Packet<?> packet;
        while (!this.outgoingQueue.isEmpty()) {
            packet = this.outgoingQueue.poll().packet;
            if (Util.mc.getNetHandler() == null) continue;
            Util.mc.getNetHandler().addToSendQueue(packet);
        }
        while (!this.incomingQueue.isEmpty()) {
            packet = this.incomingQueue.poll().packet;
            this.processIncoming(packet);
        }
    }

    private void processIncoming(Packet<?> packet) {
        if (packet == null || Util.mc.getNetHandler() == null) {
            return;
        }
        try {
            packet.processPacket(Util.mc.getNetHandler());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private void spawnDummy() {
        if (this.dummyPlayer != null) {
            return;
        }
        EntityOtherPlayerMP clone = new EntityOtherPlayerMP(Util.mc.theWorld, Util.mc.thePlayer.getGameProfile());
        clone.rotationYawHead = Util.mc.thePlayer.rotationYawHead;
        clone.copyLocationAndAnglesFrom(Util.mc.thePlayer);
        Util.mc.theWorld.addEntityToWorld(-1, clone);
        this.dummyPlayer = clone;
    }

    private void removeDummy() {
        if (this.dummyPlayer != null) {
            Util.mc.theWorld.removeEntity(this.dummyPlayer);
            this.dummyPlayer = null;
        }
    }

    public static enum ResetAction {
        RESET,
        BLINK;

    }

    private static class PacketEntry {
        private final Packet<?> packet;

        private PacketEntry(Packet<?> packet) {
            this.packet = packet;
        }
    }

    public static enum Mode {
        Range,
        Time;

    }
}

