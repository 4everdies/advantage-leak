/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.HitSlowDownEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.TickEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.BlinkProcess;
import cc.advantage.processes.RotationProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.InventoryUtils;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.mc.RayCastUtils;
import cc.advantage.utils.mc.RotationUtils;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Kill Aura", category=ModuleCategory.COMBAT)
public final class KillAuraModule
extends Module {
    public static EntityLivingBase target;
    public static boolean autoBlocking;
    public static boolean canAttack;
    List<Entity> targetList = new CopyOnWriteArrayList<Entity>();
    private static final Timer attackTimer;
    int blockTicks = 0;
    static long delay;
    static int elapsedTicks;
    private boolean shouldMiss = false;
    public int hitTicks;
    public static ModeProperty<TargetSelectionProcess.Mode> mode;
    public static ModeProperty<TargetSelectionProcess.Entities> entities;
    private final NumberProperty switchSpeed = new NumberProperty("Switch Speed", 2.0, () -> mode.getValue() == TargetSelectionProcess.Mode.Switch, 0.0, 10.0, 1.0);
    public static NumberProperty seekRange;
    public static NumberProperty killRange;
    public static NumberProperty blockingRange;
    public static NumberProperty swingRange;
    public static final Property<Boolean> newCombat;
    private static final NumberProperty min;
    private static final NumberProperty max;
    public static ModeProperty<AutoBlock> ab;
    public static Property<Boolean> alwaysShowBlocking;
    private final Property<Boolean> advanced = new Property<Boolean>("Advanced", false);
    private final Property<Boolean> missChance = new Property<Boolean>("Miss Chance", true, this.advanced::getValue);
    private final NumberProperty missRate = new NumberProperty("Miss Rate", 5.0, () -> this.advanced.getValue() != false && this.missChance.getValue() != false, 0.0, 20.0, 1.0);
    public static ModeProperty<Rotations> rotations;
    private final Property<Boolean> predictiveRotations = new Property<Boolean>("Predictive Rotations", true, () -> rotations.getValue() == Rotations.Regular);
    private final NumberProperty minRotSpeed = new NumberProperty("Min Rotation Speed", 3.0, 0.0, 10.0, 0.5);
    private final NumberProperty maxRotSpeed = new NumberProperty("Max Rotation Speed", 7.0, 0.0, 10.0, 0.5);
    public static final Property<Boolean> jitter;
    private final NumberProperty jitterFactor = new NumberProperty("Jitter Factor", 5.0, jitter::getValue, 1.0, 10.0, 1.0);
    public static final Property<Boolean> fix;
    public static final Property<Boolean> sprint;
    public static final Property<Boolean> legit;
    public static final Property<Boolean> raycast;
    private final Property<Boolean> teams = new Property<Boolean>("Teams", false);
    private final Queue<DelayedPacket> mushQueue = new ConcurrentLinkedQueue<DelayedPacket>();
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = ignored -> {
        while (!this.mushQueue.isEmpty()) {
            DelayedPacket dp = this.mushQueue.peek();
            if (System.currentTimeMillis() < dp.scheduledTime) break;
            this.mushQueue.poll();
            if (dp.cancel) continue;
            if (dp.spam) {
                PacketUtils.sendPacket(dp.packet);
                for (int i = 0; i < dp.spamCount; ++i) {
                    PacketUtils.sendPacket(dp.packet);
                }
                continue;
            }
            PacketUtils.sendPacket(dp.packet);
        }
        this.setSuffix(((TargetSelectionProcess.Mode)((Object)((Object)mode.getValue()))).toString());
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            this.resetCombatState(false);
            return;
        }
        TargetSelectionProcess.setMode((TargetSelectionProcess.Mode)((Object)((Object)mode.getValue())));
        TargetSelectionProcess.setEntities((TargetSelectionProcess.Entities)((Object)((Object)entities.getValue())));
        TargetSelectionProcess.setSeekRange(((Double)seekRange.getValue()).floatValue());
        TargetSelectionProcess.setDontTargetTeams(this.teams.getValue());
        TargetSelectionProcess.setSwitchTime(((Double)this.switchSpeed.getValue()).intValue());
        this.targetList = TargetSelectionProcess.getTargetList();
        target = TargetSelectionProcess.getTarget();
        if (this.targetList.isEmpty()) {
            target = null;
            this.unblock();
            canAttack = true;
            return;
        }
        if (target == null) {
            this.unblock();
            canAttack = true;
            return;
        }
        this.calculateRotations();
        if (ab.getValue() != AutoBlock.None && (double)Util.mc.thePlayer.getDistanceToEntity(target) <= (Double)blockingRange.getValue() && InventoryUtils.isHoldingSword()) {
            this.autoblock();
        }
        this.attack();
    };
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (event.isPre()) {
            ++this.hitTicks;
        }
    };
    @EventLink
    public final Listener<HitSlowDownEvent> hitSlowDownEventListener = e -> {
        if (sprint.getValue().booleanValue()) {
            e.setSprint(true);
            e.setSlowDown(1.0);
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = ignored -> this.resetCombatState(true);
    @EventLink
    public final Listener<TickEvent> tickEventListener = ignored -> ++elapsedTicks;
    @EventLink
    public final Listener<PacketSendEvent> packetSendEventListener = event -> {
        Packet<?> p;
        if (ab.getValue() == AutoBlock.NCP && autoBlocking) {
            Packet<?> patt1$temp;
            Packet<INetHandlerPlayServer> packet;
            Packet<?> patt0$temp = event.getPacket();
            if (patt0$temp instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging)(packet = (C07PacketPlayerDigging)patt0$temp)).getStatus().equals((Object)C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
                event.setCancelled(true);
            }
            if ((patt1$temp = event.getPacket()) instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement)(packet = (C08PacketPlayerBlockPlacement)patt1$temp)).getPlacedBlockDirection() == 255) {
                event.setCancelled(true);
            }
        }
        if (ab.getValue() == AutoBlock.Mush && ((p = event.getPacket()) instanceof C0APacketAnimation || p instanceof C07PacketPlayerDigging)) {
            if (event.isCancelled()) {
                return;
            }
            event.setCancelled(true);
            long delayMs = (long)(500.0 + Math.random() * 1000.0);
            double r = Math.random();
            boolean spam = false;
            boolean cancel = false;
            int spamExtra = 0;
            if (r < 0.43) {
                if (Math.random() < 0.5) {
                    spam = true;
                    spamExtra = 2 + (int)(Math.random() * 3.0);
                } else if (p instanceof C0APacketAnimation) {
                    cancel = true;
                }
            }
            this.mushQueue.add(new DelayedPacket(p, System.currentTimeMillis() + delayMs, spam, cancel, spamExtra));
        }
    };

    private void calculateRotations() {
        if (Util.mc.thePlayer == null || target == null || rotations.getValue() == Rotations.None) {
            return;
        }
        Vector2f rotation = RotationUtils.calculate(target, this.predictiveRotations.getValue(), (Double)seekRange.getValue());
        if (jitter.getValue().booleanValue()) {
            float jitterAmount = ((Double)this.jitterFactor.getValue()).floatValue();
            rotation.x += (float)((Math.random() - 0.5) * (double)jitterAmount);
            rotation.y += (float)((Math.random() - 0.5) * (double)jitterAmount);
        }
        float targetYaw = rotation.x;
        float targetPitch = rotation.y;
        float rotSpeed = (float)MathUtils.getRandom((Double)this.minRotSpeed.getValue(), (Double)this.maxRotSpeed.getValue());
        switch (((Rotations)((Object)rotations.getValue())).ordinal()) {
            case 0: {
                RotationProcess.setRotations(new Vector2f(targetYaw, targetPitch), rotSpeed, fix.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF);
                break;
            }
            case 1: {
                Vector2f polar = this.generatePolarRotation(target, targetYaw, targetPitch, rotSpeed);
                RotationProcess.setRotations(polar, rotSpeed, fix.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF);
                break;
            }
            case 2: {
                RotationProcess.setRotations(new Vector2f(targetYaw, targetPitch), 180.0, fix.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF);
            }
        }
    }

    private void autoblock() {
        if (Util.mc.thePlayer == null || Util.mc.playerController == null) {
            return;
        }
        if (target == null || !InventoryUtils.isHoldingSword()) {
            if (autoBlocking) {
                this.unblock();
            }
            return;
        }
        switch (((AutoBlock)((Object)ab.getValue())).ordinal()) {
            case 1: {
                autoBlocking = true;
                break;
            }
            case 4: {
                Util.mc.gameSettings.keyBindUseItem.setPressed(Util.mc.thePlayer.getDistanceToEntity(target) < 3.0f && this.hitTicks <= 5 && Util.mc.thePlayer.ticksSinceVelocity >= 5);
                autoBlocking = alwaysShowBlocking.getValue() != false || Util.mc.thePlayer.getDistanceToEntity(target) < 3.0f && this.hitTicks <= 5 && Util.mc.thePlayer.ticksSinceVelocity >= 5;
                ++this.blockTicks;
                if (Util.mc.gameSettings.keyBindUseItem.isPressed() || Util.mc.thePlayer.isUsingItem()) {
                    this.blockTicks = 0;
                }
                canAttack = this.blockTicks >= 2;
                break;
            }
            case 5: 
            case 6: {
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                autoBlocking = true;
                break;
            }
            case 2: {
                if (this.isObjectMouseOverBlock() && Util.mc.playerController.curBlockDamageMP != 0.0f) {
                    this.blockTicks = 0;
                }
                ++this.blockTicks;
                if (this.blockTicks >= 3) {
                    this.blockTicks = 1;
                }
                switch (this.blockTicks) {
                    case 1: {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                        autoBlocking = true;
                        BlinkProcess.disable();
                        break;
                    }
                    case 2: {
                        if (Util.mc.thePlayer.ticksExisted % 2 != 0) break;
                        BlinkProcess.enable();
                    }
                }
                break;
            }
            case 3: {
                if (this.isObjectMouseOverBlock() && Util.mc.playerController.curBlockDamageMP != 0.0f) {
                    this.blockTicks = 0;
                }
                ++this.blockTicks;
                if (this.blockTicks >= 3) {
                    this.blockTicks = 1;
                }
                switch (this.blockTicks) {
                    case 1: {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                        autoBlocking = true;
                        PacketUtils.sendPacket(new C09PacketHeldItemChange((Util.mc.thePlayer.inventory.currentItem + 1) % 9));
                        break;
                    }
                    case 2: {
                        if (Util.mc.thePlayer.ticksExisted % 2 != 0) break;
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(Util.mc.thePlayer.inventory.currentItem));
                    }
                }
                break;
            }
            case 7: {
                if (this.isObjectMouseOverBlock() && Util.mc.playerController.curBlockDamageMP != 0.0f) {
                    this.blockTicks = 0;
                }
                ++this.blockTicks;
                if (this.blockTicks >= 3) {
                    this.blockTicks = 1;
                }
                switch (this.blockTicks) {
                    case 1: {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                        autoBlocking = true;
                        int swordSlot = this.findSwordSlot(Util.mc.thePlayer.inventory.currentItem);
                        if (swordSlot == -1) break;
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(swordSlot));
                        Util.mc.thePlayer.inventory.currentItem = swordSlot;
                        break;
                    }
                    case 2: {
                        this.unblock();
                        canAttack = true;
                    }
                }
                break;
            }
            case 8: {
                if (autoBlocking) break;
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(Util.mc.thePlayer.getHeldItem()));
                autoBlocking = true;
            }
        }
    }

    private void unblock() {
        if (!autoBlocking) {
            canAttack = true;
            return;
        }
        this.blockTicks = -1;
        if (ab.getValue() == AutoBlock.Blink) {
            BlinkProcess.disable();
        }
        if (ab.getValue() == AutoBlock.Fake) {
            autoBlocking = false;
            canAttack = true;
            return;
        }
        if (ab.getValue() == AutoBlock.Legit && autoBlocking && Util.mc.gameSettings.keyBindUseItem.isKeyDown()) {
            Util.mc.gameSettings.keyBindUseItem.setPressed(false);
            autoBlocking = false;
            canAttack = true;
            return;
        }
        if (InventoryUtils.isHoldingSword() && ab.getValue() != AutoBlock.Legit) {
            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
        autoBlocking = false;
        canAttack = true;
    }

    private void attack() {
        MovingObjectPosition mop;
        if (Util.mc.thePlayer == null || Util.mc.playerController == null || target == null || !canAttack) {
            return;
        }
        if (!KillAuraModule.hitTimerDone()) {
            return;
        }
        double dist = Util.mc.thePlayer.getDistanceToEntity(target);
        if (dist > (Double)swingRange.getValue()) {
            return;
        }
        if (this.advanced.getValue().booleanValue() && this.missChance.getValue().booleanValue()) {
            if (this.shouldMiss) {
                this.shouldMiss = false;
                this.hitTicks = 0;
                attackTimer.reset();
                return;
            }
            if (Math.random() * 100.0 < (Double)this.missRate.getValue()) {
                this.shouldMiss = true;
                this.hitTicks = 0;
                attackTimer.reset();
                return;
            }
        }
        if (raycast.getValue().booleanValue() && ((mop = RayCastUtils.rayCast(RotationProcess.rotations, (Double)killRange.getValue())) == null || mop.entityHit != target)) {
            return;
        }
        if (!legit.getValue().booleanValue()) {
            Util.mc.thePlayer.swingItem();
            Util.mc.playerController.attackEntity(Util.mc.thePlayer, target);
        } else {
            Util.mc.clickMouse();
        }
        this.hitTicks = 0;
    }

    private static boolean hitTimerDone() {
        boolean returnVal = false;
        if (!newCombat.getValue().booleanValue()) {
            if (attackTimer.hasTimeElapsed(delay, false)) {
                double minVal = (Double)min.getValue();
                double maxVal = (Double)max.getValue();
                if (maxVal <= 0.0) {
                    maxVal = 1.0;
                }
                if (minVal < 0.0) {
                    minVal = 0.0;
                }
                if (minVal > maxVal) {
                    double t = minVal;
                    minVal = maxVal;
                    maxVal = t;
                }
                double cps = MathUtils.getRandom(minVal, maxVal);
                cps = Math.max(1.0, cps);
                returnVal = true;
                attackTimer.reset();
                delay = (long)(1000.0 / cps);
            }
        } else if (elapsedTicks >= KillAuraModule.getNewCombatDelay()) {
            elapsedTicks = 0;
            returnVal = true;
        }
        return returnVal;
    }

    private static int getNewCombatDelay() {
        if (Util.mc.thePlayer == null || Util.mc.thePlayer.inventory == null) {
            return 3;
        }
        int toolDelay = 3;
        if (Util.mc.thePlayer.inventory.getCurrentItem() == null) {
            return toolDelay;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
            toolDelay = 12;
        } else if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemAxe) {
            toolDelay = 25;
        } else if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemPickaxe) {
            toolDelay = 16;
        } else if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemTool) {
            toolDelay = 20;
        }
        return toolDelay;
    }

    private boolean shouldBlockPredictive() {
        if (Util.mc.thePlayer == null || target == null) {
            return false;
        }
        double distance = Util.mc.thePlayer.getDistanceToEntity(target);
        if (distance > 6.0) {
            return false;
        }
        double targetX = KillAuraModule.target.posX;
        double targetY = KillAuraModule.target.posY + (double)target.getEyeHeight();
        double targetZ = KillAuraModule.target.posZ;
        double deltaX = Util.mc.thePlayer.posX - targetX;
        double deltaY = Util.mc.thePlayer.posY + (double)Util.mc.thePlayer.getEyeHeight() - targetY;
        double deltaZ = Util.mc.thePlayer.posZ - targetZ;
        float yaw = (float)Math.toRadians(KillAuraModule.target.rotationYaw);
        float pitch = (float)Math.toRadians(KillAuraModule.target.rotationPitch);
        double targetLookX = -Math.sin(yaw) * Math.cos(pitch);
        double targetLookY = -Math.sin(pitch);
        double targetLookZ = Math.cos(yaw) * Math.cos(pitch);
        double deltaMag = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        double lookMag = Math.sqrt(targetLookX * targetLookX + targetLookY * targetLookY + targetLookZ * targetLookZ);
        if (deltaMag < 1.0E-6 || lookMag < 1.0E-6) {
            return false;
        }
        double dotProduct = (deltaX * targetLookX + deltaY * targetLookY + deltaZ * targetLookZ) / (deltaMag * lookMag);
        return dotProduct > 0.5 && KillAuraModule.target.swingProgress > 0.0f;
    }

    private Vector2f generatePolarRotation(EntityLivingBase targetEntity, float baseYaw, float basePitch, float rotSpeed) {
        long time = System.nanoTime();
        int id = targetEntity != null ? targetEntity.getEntityId() : 0;
        long ticks = Util.mc.thePlayer != null ? (long)Util.mc.thePlayer.ticksExisted : 0L;
        long seed = time ^ (long)id << 32 ^ ticks * -7046029254386353131L ^ Double.doubleToLongBits(Math.random());
        Random rng = new Random(seed);
        double t = (double)System.currentTimeMillis() / 1000.0 + rng.nextDouble() * 10.0;
        double[] freqs = new double[]{0.07 + rng.nextDouble() * 0.05, 0.13 + rng.nextDouble() * 0.08, 0.29 + rng.nextDouble() * 0.12};
        double maxBaseAmp = Math.max(1.0, Math.min(12.0, (double)rotSpeed * 2.5));
        double[] ampsYaw = new double[]{rng.nextDouble() * maxBaseAmp, rng.nextDouble() * (maxBaseAmp / 1.5), rng.nextDouble() * (maxBaseAmp / 2.0)};
        double[] ampsPitch = new double[]{rng.nextDouble() * (maxBaseAmp / 2.0), rng.nextDouble() * (maxBaseAmp / 3.0), rng.nextDouble() * (maxBaseAmp / 4.0)};
        double yawOffset = 0.0;
        double pitchOffset = 0.0;
        for (int i = 0; i < freqs.length; ++i) {
            double phase = rng.nextDouble() * Math.PI * 2.0;
            yawOffset += ampsYaw[i] * Math.sin(Math.PI * 2 * freqs[i] * t + phase);
            pitchOffset += ampsPitch[i] * Math.cos(Math.PI * 2 * freqs[i] * t + phase * 0.7);
        }
        if (rng.nextDouble() < 0.08) {
            yawOffset += (rng.nextDouble() - 0.5) * 30.0 * rng.nextDouble();
            pitchOffset += (rng.nextDouble() - 0.5) * 12.0 * rng.nextDouble();
        }
        yawOffset += rng.nextGaussian() * (0.5 + rng.nextDouble() * 2.0);
        pitchOffset += rng.nextGaussian() * (0.3 + rng.nextDouble() * 1.0);
        if (targetEntity != null) {
            double vx = targetEntity.motionX;
            double vz = targetEntity.motionZ;
            double velFactor = Math.sqrt(vx * vx + vz * vz);
            yawOffset += velFactor * (rng.nextDouble() * 8.0 - 4.0);
            pitchOffset += velFactor * (rng.nextDouble() * 3.0 - 1.5);
        }
        double yawClamp = 35.0;
        double pitchClamp = 30.0;
        double finalYaw = (double)baseYaw + this.clamp(yawOffset, -yawClamp, yawClamp);
        double finalPitch = (double)basePitch + this.clamp(pitchOffset, -pitchClamp, pitchClamp);
        finalPitch = Math.max(-90.0, Math.min(90.0, finalPitch));
        return new Vector2f((float)finalYaw, (float)finalPitch);
    }

    private double clamp(double v, double a, double b) {
        if (v < a) {
            return a;
        }
        if (v > b) {
            return b;
        }
        return v;
    }

    @Override
    public void onEnable() {
        double minVal = (Double)min.getValue();
        double maxVal = (Double)max.getValue();
        if (maxVal <= 0.0) {
            maxVal = 1.0;
        }
        if (minVal < 0.0) {
            minVal = 0.0;
        }
        if (minVal > maxVal) {
            double t = minVal;
            minVal = maxVal;
            maxVal = t;
        }
        double cps = MathUtils.getRandom(minVal, maxVal);
        cps = Math.max(1.0, cps);
        delay = (long)(1000.0 / cps);
        elapsedTicks = 0;
        this.shouldMiss = false;
        canAttack = true;
        autoBlocking = false;
        this.blockTicks = -1;
        attackTimer.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.resetCombatState(true);
        super.onDisable();
    }

    private void resetCombatState(boolean clearTargetList) {
        if (autoBlocking) {
            this.unblock();
        } else {
            canAttack = true;
        }
        target = null;
        this.shouldMiss = false;
        elapsedTicks = 0;
        delay = 0L;
        this.blockTicks = -1;
        attackTimer.reset();
        this.mushQueue.clear();
        if (clearTargetList) {
            this.targetList.clear();
        }
    }

    private boolean isObjectMouseOverBlock() {
        return Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private int findSwordSlot(int currentSlot) {
        for (int i = 0; i < 9; ++i) {
            ItemStack item;
            if (i == currentSlot || (item = Util.mc.thePlayer.inventory.getStackInSlot(i)) == null || !(item.getItem() instanceof ItemSword)) continue;
            return i;
        }
        return -1;
    }

    static {
        autoBlocking = false;
        canAttack = true;
        attackTimer = new Timer();
        delay = 0L;
        elapsedTicks = 0;
        mode = new ModeProperty<TargetSelectionProcess.Mode>("Mode", TargetSelectionProcess.Mode.Adaptive);
        entities = new ModeProperty<TargetSelectionProcess.Entities>("Entities", TargetSelectionProcess.Entities.Optimal);
        seekRange = new NumberProperty("Seek Range", 4.2, 3.0, 6.0, 0.1);
        killRange = new NumberProperty("Kill Range", 3.0, 3.0, 6.0, 0.1);
        blockingRange = new NumberProperty("Blocking Range", 4.2, 3.0, 6.0, 0.1);
        swingRange = new NumberProperty("Swing Range", 3.5, 3.0, 6.0, 0.1);
        newCombat = new Property<Boolean>("1.9+ Combat Delays", false);
        min = new NumberProperty("Min CPS", 9.0, () -> newCombat.getValue() == false, 0.0, 20.0, 0.5);
        max = new NumberProperty("Max CPS", 13.0, () -> newCombat.getValue() == false, 0.0, 20.0, 0.5);
        ab = new ModeProperty<AutoBlock>("Auto Block", AutoBlock.Fake);
        alwaysShowBlocking = new Property<Boolean>("Always Show Blocking", true, () -> ab.getValue() == AutoBlock.Legit);
        rotations = new ModeProperty<Rotations>("Rotations", Rotations.Regular);
        jitter = new Property<Boolean>("Jitter Rotations", false);
        fix = new Property<Boolean>("Move Fix", true);
        sprint = new Property<Boolean>("Keep Sprint", false);
        legit = new Property<Boolean>("Simulate Mouse Clicks", true);
        raycast = new Property<Boolean>("Ray Cast", true);
    }

    public static enum Rotations {
        Regular,
        Polar,
        Snap,
        None;

    }

    public static enum AutoBlock {
        None,
        Fake,
        Blink,
        Switch,
        Legit,
        NCP,
        Vanilla,
        Swap,
        Mush;

    }

    private static class DelayedPacket {
        final Packet<?> packet;
        long scheduledTime;
        boolean spam;
        boolean cancel;
        int spamCount;

        DelayedPacket(Packet<?> packet, long scheduledTime, boolean spam, boolean cancel, int spamCount) {
            this.packet = packet;
            this.scheduledTime = scheduledTime;
            this.spam = spam;
            this.cancel = cancel;
            this.spamCount = spamCount;
        }
    }
}

