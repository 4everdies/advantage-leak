/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.mc.PathFinderUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.Vec3;

@ModuleInfo(label="TP Aura", category=ModuleCategory.COMBAT)
public final class TPAuraModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Single);
    public static NumberProperty reach = new NumberProperty("Reach", 100.0, 3.0, 200.0, 1.0);
    public static NumberProperty cps = new NumberProperty("CPS", 2.0, 1.0, 20.0, 1.0);
    private final Property<Boolean> crits = new Property<Boolean>("Do Critical Hits", true);
    private final Property<Boolean> render = new Property<Boolean>("Render Trail", true);
    private final Timer clickTimer = new Timer();
    private List<Vec3> path;
    public Entity target;
    private long nextSwing;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        List<Entity> targets = TargetSelectionProcess.getTargetList();
        if (targets.isEmpty()) {
            this.target = null;
            return;
        }
        this.target = targets.get(0);
        if (this.target == null || Util.mc.thePlayer.isDead) {
            return;
        }
        this.doAttack(targets);
    };
    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (!this.render.getValue().booleanValue() || this.path == null || this.target == null) {
            return;
        }
        Vec3 lastVector = null;
        for (Vec3 vector : this.path) {
            if (lastVector != null) {
                RenderUtils.drawLine(lastVector.xCoord, lastVector.yCoord + 0.01, lastVector.zCoord, vector.xCoord, vector.yCoord + 0.01, vector.zCoord, Color.WHITE, 1.0f);
            }
            lastVector = vector;
        }
    };

    @Override
    public void onDisable() {
        this.target = null;
        super.onDisable();
    }

    private void doAttack(List<Entity> targets) {
        if (this.clickTimer.hasTimeElapsed(this.nextSwing) && this.target != null && !Util.mc.gameSettings.keyBindAttack.isKeyDown() && !Util.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            long clicks = ((Double)cps.getValue()).intValue();
            this.nextSwing = 1000L / clicks;
            double range = (Double)reach.getValue();
            switch (((Mode)((Object)mode.getValue())).ordinal()) {
                case 0: {
                    if (!((double)Util.mc.thePlayer.getDistanceToEntity(this.target) <= range)) break;
                    this.attack(this.target);
                    break;
                }
                case 1: {
                    targets.removeIf(target -> (double)Util.mc.thePlayer.getDistanceToEntity((Entity)target) > range);
                    if (targets.isEmpty()) break;
                    targets.forEach(this::attack);
                }
            }
            this.clickTimer.reset();
        }
    }

    private void attack(Entity target) {
        Util.mc.playerController.syncCurrentPlayItem();
        AttackEvent event = new AttackEvent((EntityLivingBase)target);
        Advantage.INSTANCE.getEventBus().post(event);
        if (event.isCancelled()) {
            return;
        }
        target = event.target;
        this.path = PathFinderUtils.computePath(new Vec3(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ), new Vec3(target.posX, target.posY, target.posZ), true);
        if (this.path == null) {
            return;
        }
        for (Vec3 vector : this.path) {
            PacketUtils.sendSilentPacket(new C03PacketPlayer.C04PacketPlayerPosition(vector.xCoord, vector.yCoord, vector.zCoord, true));
        }
        Util.mc.thePlayer.swingItem();
        PacketUtils.sendSilentPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
        Collections.reverse(this.path);
        for (Vec3 vector : this.path) {
            PacketUtils.sendSilentPacket(new C03PacketPlayer.C04PacketPlayerPosition(vector.xCoord, vector.yCoord, vector.zCoord, true));
        }
        if (this.crits.getValue().booleanValue() || Util.mc.thePlayer.fallDistance > 0.0f && !Util.mc.thePlayer.onGround && !Util.mc.thePlayer.isOnLadder() && !Util.mc.thePlayer.isInWater() && !Util.mc.thePlayer.isPotionActive(Potion.blindness) && Util.mc.thePlayer.ridingEntity == null) {
            Util.mc.thePlayer.onCriticalHit(target);
        }
    }

    public static enum Mode {
        Single,
        Multi;

    }
}

