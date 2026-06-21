/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(label="More KB", category=ModuleCategory.COMBAT)
public final class MoreKBModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Legit);
    private final Property<Boolean> intelligent = new Property<Boolean>("Intelligent", false);
    private final Property<Boolean> onlyGround = new Property<Boolean>("Only Ground", true);
    private boolean shouldSprintReset;
    private EntityLivingBase target;
    @EventLink
    public final Listener<AttackEvent> attackEventListener = event -> {
        if (event.target instanceof EntityLivingBase) {
            this.target = event.target;
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        this.setSuffix(((Mode)((Object)((Object)this.mode.getValue()))).toString());
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        if (this.mode.getValue() == Mode.LegitFast) {
            if (this.target != null && this.isMoving()) {
                if (!this.onlyGround.getValue().booleanValue() || Util.mc.thePlayer.onGround) {
                    Util.mc.thePlayer.sprintingTicksLeft = 0;
                }
                this.target = null;
            }
            return;
        }
        EntityLivingBase entity = null;
        if (Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && Util.mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
            entity = (EntityLivingBase)Util.mc.objectMouseOver.entityHit;
        }
        if (entity == null) {
            return;
        }
        double x = Util.mc.thePlayer.posX - entity.posX;
        double z = Util.mc.thePlayer.posZ - entity.posZ;
        float calcYaw = (float)(Math.atan2(z, x) * 180.0 / Math.PI - 90.0);
        float diffY = Math.abs(MathHelper.wrapAngleTo180_float(calcYaw - entity.rotationYawHead));
        if (this.intelligent.getValue().booleanValue() && diffY > 120.0f) {
            return;
        }
        if (entity.hurtTime == 10) {
            switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
                case 0: {
                    this.shouldSprintReset = true;
                    if (Util.mc.thePlayer.isSprinting()) {
                        Util.mc.thePlayer.setSprinting(false);
                        Util.mc.thePlayer.setSprinting(true);
                    }
                    this.shouldSprintReset = false;
                    break;
                }
                case 2: {
                    if (Util.mc.thePlayer.isSprinting()) {
                        Util.mc.thePlayer.setSprinting(false);
                    }
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    Util.mc.thePlayer.setSprinting(true);
                    break;
                }
                case 3: {
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    Util.mc.thePlayer.setSprinting(true);
                    break;
                }
                case 4: {
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    PacketUtils.sendPacket(new C0BPacketEntityAction(Util.mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    Util.mc.thePlayer.setSprinting(true);
                }
            }
        }
    };

    @Override
    public void onEnable() {
        this.shouldSprintReset = false;
        this.target = null;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.shouldSprintReset = false;
        this.target = null;
        super.onDisable();
    }

    private boolean isMoving() {
        return Util.mc.thePlayer.moveForward != 0.0f || Util.mc.thePlayer.moveStrafing != 0.0f;
    }

    private static enum Mode {
        Legit("Legit"),
        LegitFast("Legit Fast"),
        LessPacket("Less Packet"),
        Packet("Packet"),
        DoublePacket("Double Packet");

        private final String label;

        private Mode(String label) {
            this.label = label;
        }

        public String toString() {
            return this.label;
        }
    }
}

