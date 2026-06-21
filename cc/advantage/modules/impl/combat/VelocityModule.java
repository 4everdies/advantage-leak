/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.MoveEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.LagProcess;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Velocity", category=ModuleCategory.COMBAT)
public final class VelocityModule
extends Module {
    public static final ModeProperty<Mode> modeProperty = new ModeProperty<Mode>("Mode", Mode.Edit);
    public final NumberProperty chance = new NumberProperty("Chance", 100.0, () -> modeProperty.getValue() == Mode.Legit, 0.0, 100.0, 1.0);
    public final Property<Boolean> legitTiming = new Property<Boolean>("Legit Timing", false, () -> modeProperty.getValue() == Mode.Legit);
    public NumberProperty horizontal = new NumberProperty("Horizontal", 100.0, () -> modeProperty.getValue() == Mode.Edit, 0.0, 100.0, 1.0);
    public NumberProperty vertical = new NumberProperty("Vertical", 100.0, () -> modeProperty.getValue() == Mode.Edit, 0.0, 100.0, 1.0);
    public NumberProperty reduceX = new NumberProperty("Reduce X", 100.0, () -> modeProperty.getValue() == Mode.Reduce, 0.0, 100.0, 1.0);
    public NumberProperty reduceZ = new NumberProperty("Reduce Z", 100.0, () -> modeProperty.getValue() == Mode.Reduce, 0.0, 100.0, 1.0);
    private final NumberProperty delay = new NumberProperty("Delay", 10.0, () -> modeProperty.getValue() == Mode.Delay, 1.0, 50.0, 1.0);
    private final Property<Boolean> legit = new Property<Boolean>("Legit Lag", true, () -> modeProperty.getValue() == Mode.Delay);
    boolean delayed = false;
    private boolean velocity;
    private boolean jump;
    @EventLink
    public final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        Packet<INetHandlerPlayClient> p;
        if (modeProperty.getValue() == Mode.Edit && event.getPacket() instanceof S12PacketEntityVelocity && (p = (S12PacketEntityVelocity)event.getPacket()).getEntityID() == Util.mc.thePlayer.getEntityId()) {
            p.setMotionX((int)((double)p.getMotionX() * (Double)this.horizontal.getValue() / 100.0));
            p.setMotionZ((int)((double)p.getMotionZ() * (Double)this.horizontal.getValue() / 100.0));
            p.setMotionY((int)((double)p.getMotionY() * (Double)this.vertical.getValue() / 100.0));
        }
        if (modeProperty.getValue() == Mode.Reverse && event.getPacket() instanceof S12PacketEntityVelocity && (p = (S12PacketEntityVelocity)event.getPacket()).getEntityID() == Util.mc.thePlayer.getEntityId()) {
            p.setMotionX(p.getMotionX() * -1);
            p.setMotionZ(p.getMotionZ() * -1);
        }
        if (modeProperty.getValue() == Mode.Cancel && event.getPacket() instanceof S12PacketEntityVelocity && (p = (S12PacketEntityVelocity)event.getPacket()).getEntityID() == Util.mc.thePlayer.getEntityId()) {
            event.setCancelled();
        }
        if (modeProperty.getValue() == Mode.Legit) {
            S12PacketEntityVelocity wrapper;
            if (Util.mc.thePlayer == null) {
                return;
            }
            if (!Util.mc.thePlayer.onGround) {
                return;
            }
            p = event.getPacket();
            if (p instanceof S12PacketEntityVelocity && (wrapper = (S12PacketEntityVelocity)p).getEntityID() == Util.mc.thePlayer.getEntityId() && wrapper.getMotionY() > 0 && (!this.legitTiming.getValue().booleanValue() || Util.mc.thePlayer.ticksSinceVelocity <= 14 || Util.mc.thePlayer.onGroundTicks <= 1)) {
                this.jump = true;
            }
        }
    };
    @EventLink
    public final Listener<MoveEvent> onMove = event -> {
        if (this.jump && MovementUtils.isMoving() && Math.random() * 100.0 < (Double)this.chance.getValue()) {
            event.setJump(true);
        }
    };
    @EventLink
    private final Listener<MotionEvent> motionEventListener = event -> {
        this.setSuffix(((Mode)((Object)((Object)modeProperty.getValue()))).toString());
        if (event.isPre() && modeProperty.getValue() == Mode.Legit) {
            this.jump = false;
        }
        if (!event.isPre() && modeProperty.getValue() == Mode.Delay && Util.mc.thePlayer.hurtTime > 0 && !Util.mc.thePlayer.isBurning()) {
            LagProcess.spoof(((Double)this.delay.getValue()).intValue() * 10, this.legit.getValue(), true, this.legit.getValue(), false);
            this.delayed = true;
        }
        if (modeProperty.getValue() == Mode.Delay && this.delayed && !event.isPre() && (Util.mc.thePlayer.hurtTime == 0 || Util.mc.thePlayer.isBurning())) {
            LagProcess.dispatch();
            LagProcess.disable();
            this.delayed = false;
        }
    };
    @EventLink(value=4)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (modeProperty.getValue() == Mode.Grim && this.velocity) {
            RotationProcess.setRotations(new Vector2f(Util.mc.thePlayer.rotationYaw, 90.0f), 10.0, MovementFix.NORMAL);
            if (Util.mc.objectMouseOver != null) {
                Util.mc.clickMouse();
            }
            this.velocity = false;
        }
    };
    @EventLink(value=0)
    public final Listener<PacketReceiveEvent> onReceiveLow = event -> {
        S12PacketEntityVelocity wrapper;
        if (modeProperty.getValue() != Mode.Grim) {
            return;
        }
        Packet<?> patt0$temp = event.getPacket();
        if (patt0$temp instanceof S12PacketEntityVelocity && (wrapper = (S12PacketEntityVelocity)patt0$temp).getEntityID() == Util.mc.thePlayer.getEntityId()) {
            event.setCancelled(true);
            this.velocity = true;
        }
    };
    @EventLink
    private final Listener<AttackEvent> attackEventListener = event -> {
        if (modeProperty.getValue() == Mode.Reduce && event.target instanceof EntityLivingBase && Util.mc.thePlayer.hurtTime > 0) {
            Util.mc.thePlayer.motionX *= (Double)this.reduceX.getValue() / 100.0;
            Util.mc.thePlayer.motionZ *= (Double)this.reduceZ.getValue() / 100.0;
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

    public static enum Mode {
        Legit,
        Edit,
        Grim,
        Cancel,
        Reverse,
        Reduce,
        Delay;

    }
}

