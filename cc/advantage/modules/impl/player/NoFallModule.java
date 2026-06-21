/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.InventoryUtils;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="No Fall", category=ModuleCategory.PLAYER)
public class NoFallModule
extends Module {
    public ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Clutch);
    public Timer timer = new Timer();
    private boolean timered = false;
    public boolean canWork = false;
    public boolean pickup = false;
    @EventLink
    private final Listener<MotionEvent> motionEventListener = e -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        if (this.mode.getValue() == Mode.Vanilla && Util.mc.thePlayer.fallDistance >= 3.0f) {
            PacketUtils.sendPacket(new C03PacketPlayer(true));
        }
        if (this.mode.getValue() == Mode.Edit) {
            e.setOnGround(false);
            e.setPosY(e.getPosY() + Math.random() / (double)1.0E20f);
        }
        if (this.mode.getValue() == Mode.Round && Util.mc.thePlayer.fallDistance >= 3.0f) {
            e.setPosY(e.getPosY() * (double)Math.round(e.getPosY()));
        }
    };
    @EventLink
    private final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (this.mode.getValue() == Mode.Clutch) {
            if (Util.mc.thePlayer.fallDistance > 2.9f) {
                int item = InventoryUtils.getBucketSlot();
                if (item == -1) {
                    item = InventoryUtils.getCobwebSlot();
                }
                if (item == -1) {
                    if (this.canWork && !this.pickup) {
                        RotationProcess.setRotations(new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch), 10.0, MovementFix.NORMAL);
                        this.canWork = false;
                        this.pickup = false;
                        return;
                    }
                } else {
                    Util.mc.thePlayer.inventory.currentItem = item;
                    RotationProcess.setRotations(new Vector2f(Util.mc.thePlayer.rotationYaw, 90.0f), 10.0, MovementFix.NORMAL);
                    this.canWork = true;
                    if (!(Util.mc.thePlayer.isInWater() || Util.mc.thePlayer.isInWeb || this.pickup || Util.mc.theWorld.getBlockState(new BlockPos(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY - 2.0, Util.mc.thePlayer.posZ)).getBlock() == Blocks.water || Util.mc.theWorld.getBlockState(new BlockPos(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY - 2.0, Util.mc.thePlayer.posZ)).getBlock() == Blocks.air)) {
                        Util.mc.rightClickMouse();
                        this.pickup = true;
                        this.timer.reset();
                    }
                }
            } else {
                if (!this.canWork) {
                    return;
                }
                if (Util.mc.thePlayer.isInWater() && this.pickup) {
                    Util.mc.rightClickMouse();
                    this.pickup = false;
                } else {
                    RotationProcess.setRotations(new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch), 10.0, MovementFix.NORMAL);
                    this.canWork = false;
                    this.pickup = false;
                }
                if (this.timer.hasTimeElapsed(150.0, false)) {
                    RotationProcess.setRotations(new Vector2f(Util.mc.thePlayer.rotationYaw, Util.mc.thePlayer.rotationPitch), 10.0, MovementFix.NORMAL);
                    this.canWork = false;
                    this.pickup = false;
                    return;
                }
            }
        }
    };
    @EventLink
    private final Listener<PacketSendEvent> packetSendEventListener = e -> {
        if (this.mode.getValue() == Mode.Vulcan && e.getPacket() instanceof C03PacketPlayer && Util.mc.thePlayer.fallDistance > 3.0f) {
            C03PacketPlayer packet = (C03PacketPlayer)e.getPacket();
            packet.onGround = true;
            Util.mc.thePlayer.fallDistance = 0.0f;
            Util.mc.thePlayer.setVelocity(0.0, 0.0, 0.0);
        }
    };
    @EventLink
    private final Listener<WorldLoadEvent> worldLoadEventListener = e -> {
        if (this.timered) {
            this.timered = false;
            Util.mc.timer.timerSpeed = 1.0f;
        }
        if (this.pickup || this.canWork) {
            this.pickup = false;
            this.canWork = false;
        }
    };

    @Override
    public void onDisable() {
        if (this.timered) {
            this.timered = false;
            Util.mc.timer.timerSpeed = 1.0f;
        }
        super.onDisable();
    }

    private static enum Mode {
        Vanilla,
        Vulcan,
        Clutch,
        Edit,
        Round;

    }
}

