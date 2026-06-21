/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.client.AntiBotModule;
import cc.advantage.modules.impl.client.MCFModule;
import cc.advantage.processes.LagManager;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="LagBreaker", category=ModuleCategory.COMBAT)
public final class LagBreakerModule
extends Module {
    private final NumberProperty startRange = new NumberProperty("Start Range", 2.5, 2.5, 3.0, 0.1);
    private final NumberProperty deactivateRange = new NumberProperty("Deactivate Range", 10.0, 4.0, 10.0, 0.1);
    private final Property<Boolean> fakeLagEnabled = new Property<Boolean>("FakeLag", true);
    private final NumberProperty fakeLagDelay = new NumberProperty("FL Delay", 200.0, 50.0, 5000.0, 10.0);
    private final Property<Boolean> lagRangeEnabled = new Property<Boolean>("LagRange", true);
    private final NumberProperty lagRangeDelay = new NumberProperty("LR Delay", 150.0, 0.0, 1000.0, 10.0);
    private final NumberProperty lagRangeRange = new NumberProperty("LR Range", 10.0, 3.0, 100.0, 0.5);
    private final Property<Boolean> lagRangeWeapons = new Property<Boolean>("LR Weapons Only", true);
    private final Property<Boolean> lagRangeTools = new Property<Boolean>("LR Allow Tools", false);
    private final Property<Boolean> lagRangeBotCheck = new Property<Boolean>("LR Bot Check", true);
    private final Property<Boolean> lagRangeTeams = new Property<Boolean>("LR Teams", true);
    private final ModeProperty<ShowPosition> lagRangeShowPos = new ModeProperty<ShowPosition>("LR Show Pos", ShowPosition.NONE);
    private final Property<Boolean> backtrackEnabled = new Property<Boolean>("BackTrack", true);
    private final NumberProperty backtrackMinDelay = new NumberProperty("BT Min Delay", 50.0, 10.0, 500.0, 10.0);
    private final NumberProperty backtrackMaxDelay = new NumberProperty("BT Max Delay", 100.0, 50.0, 1000.0, 10.0);
    private final NumberProperty backtrackDistanceMin = new NumberProperty("BT Dist Min", 0.0, 0.0, 6.0, 0.1);
    private final NumberProperty backtrackDistanceMax = new NumberProperty("BT Dist Max", 4.0, 0.5, 6.0, 0.1);
    private final ModeProperty<BackTrackESP> backtrackESP = new ModeProperty<BackTrackESP>("BT ESP", BackTrackESP.NONE);
    private final Property<Boolean> backtrackSmart = new Property<Boolean>("BT Smart", true);
    private final Queue<PacketData> fakeLagQueue = new ConcurrentLinkedQueue<PacketData>();
    private boolean fakeLagDispatching = false;
    private int tickIndex = -1;
    private long delayCounter = 0L;
    private boolean hasLagRangeTarget = false;
    private Vec3 lastPosition = null;
    private Vec3 currentPosition = null;
    private final Queue<BackTrackPacket> backtrackQueue = new ConcurrentLinkedQueue<BackTrackPacket>();
    private final List<Packet<?>> skipPackets = new ArrayList();
    private final Timer backtrackTimer = new Timer();
    private Vec3 backtrackVec;
    private EntityPlayer backtrackTarget;
    private int currentLatency;
    private boolean rangeActive;
    private final Random random = new Random();
    @EventLink
    public final Listener<WorldLoadEvent> onWorldLoad = event -> {
        this.fakeLagQueue.clear();
        this.fakeLagDispatching = false;
        this.tickIndex = -1;
        this.delayCounter = 0L;
        this.hasLagRangeTarget = false;
        this.lastPosition = null;
        this.currentPosition = null;
        this.backtrackQueue.clear();
        this.skipPackets.clear();
        this.backtrackVec = null;
        this.backtrackTarget = null;
        this.currentLatency = 0;
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        this.updateRangeState();
        if (!this.rangeActive) {
            this.flushFakeLag();
            LagManager.setDelay(0);
            this.releaseBacktrackPackets();
            return;
        }
        if (this.fakeLagEnabled.getValue().booleanValue()) {
            long now = System.currentTimeMillis();
            long delayMs = ((Double)this.fakeLagDelay.getValue()).longValue();
            while (!this.fakeLagQueue.isEmpty()) {
                PacketData pd = this.fakeLagQueue.peek();
                if (now - pd.timestamp < delayMs) break;
                this.fakeLagQueue.poll();
                this.fakeLagDispatching = true;
                PacketUtils.sendPacket(pd.packet);
                this.fakeLagDispatching = false;
            }
        }
        if (this.lagRangeEnabled.getValue().booleanValue()) {
            List players;
            LagManager.setDelay(0);
            this.hasLagRangeTarget = false;
            if ((!Util.mc.thePlayer.isUsingItem() || Util.mc.thePlayer.isBlocking()) && (!this.lagRangeWeapons.getValue().booleanValue() || this.hasRawUnbreakingEnchant() || this.lagRangeTools.getValue().booleanValue() && this.isHoldingTool()) && !(players = Util.mc.theWorld.loadedEntityList.stream().filter(e -> e instanceof EntityPlayer).map(e -> (EntityPlayer)e).filter(this::isValidLagRangeTarget).collect(Collectors.toList())).isEmpty()) {
                double height = Util.mc.thePlayer.getEyeHeight();
                Vec3 eyePosition = LagManager.getLastPosition().addVector(0.0, height, 0.0);
                Vec3 targetEyePosition = new Vec3(Util.mc.thePlayer.lastTickPosX, Util.mc.thePlayer.lastTickPosY + height, Util.mc.thePlayer.lastTickPosZ);
                Vec3 playerEyePosition = new Vec3(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY + height, Util.mc.thePlayer.posZ);
                for (EntityPlayer player : players) {
                    double distance;
                    if (player == null || !((distance = this.distanceToBox(player, playerEyePosition)) <= (Double)this.lagRangeRange.getValue())) continue;
                    double targetDist = this.distanceToBox(player, targetEyePosition);
                    double eyeDist = this.distanceToBox(player, eyePosition);
                    if (!(distance < targetDist) && !(distance < eyeDist)) continue;
                    if (this.tickIndex < 0) {
                        this.tickIndex = 0;
                        this.delayCounter += ((Double)this.lagRangeDelay.getValue()).longValue();
                        while (this.delayCounter > 0L) {
                            ++this.tickIndex;
                            this.delayCounter -= 50L;
                        }
                    }
                    LagManager.setDelay(this.tickIndex);
                    this.hasLagRangeTarget = true;
                    break;
                }
            }
            Vec3 savedPos = LagManager.getLastPosition();
            this.lastPosition = this.currentPosition == null ? savedPos : this.currentPosition;
            this.currentPosition = savedPos;
        }
        if (this.backtrackEnabled.getValue().booleanValue()) {
            EntityPlayer btTarget = this.backtrackTarget;
            Vec3 btVec = this.backtrackVec;
            if (btTarget != null && btVec != null) {
                double distance;
                double btDist;
                double realDist;
                if (this.backtrackSmart.getValue().booleanValue() && btTarget.hurtTime <= 2 && (realDist = (double)Util.mc.thePlayer.getDistanceToEntity(btTarget)) + 0.5 < (btDist = Util.mc.thePlayer.getDistance(btVec.xCoord, btVec.yCoord, btVec.zCoord))) {
                    this.currentLatency = 0;
                    this.releaseBacktrackPackets();
                    this.backtrackTarget = null;
                    this.backtrackVec = null;
                }
                if (this.backtrackTarget != null && ((distance = (double)Util.mc.thePlayer.getDistanceToEntity(this.backtrackTarget)) < (Double)this.backtrackDistanceMin.getValue() || distance > (Double)this.backtrackDistanceMax.getValue())) {
                    this.currentLatency = 0;
                    this.releaseBacktrackPackets();
                    this.backtrackTarget = null;
                    this.backtrackVec = null;
                }
            }
            long now = System.currentTimeMillis();
            while (!this.backtrackQueue.isEmpty()) {
                BackTrackPacket btp = this.backtrackQueue.peek();
                if (!btp.timer.hasTimeElapsed(btp.latency)) break;
                this.backtrackQueue.poll();
                this.skipPackets.add(btp.packet);
                this.processIncomingSilent(btp.packet);
            }
            if (this.backtrackQueue.isEmpty() && this.backtrackTarget != null) {
                this.backtrackVec = this.backtrackTarget.getPositionVector();
            }
        }
    };
    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (!this.rangeActive) {
            return;
        }
        if (this.fakeLagEnabled.getValue().booleanValue()) {
            if (this.fakeLagDispatching) {
                return;
            }
            event.setCancelled(true);
            this.fakeLagQueue.add(new PacketData(event.getPacket(), System.currentTimeMillis()));
        }
        if (this.lagRangeEnabled.getValue().booleanValue() && this.shouldResetLagRange(event.getPacket())) {
            LagManager.setDelay(0);
            this.tickIndex = -1;
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (!this.rangeActive || !this.backtrackEnabled.getValue().booleanValue()) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (this.skipPackets.contains(packet)) {
            this.skipPackets.remove(packet);
            return;
        }
        if (this.backtrackTarget == null) {
            this.releaseBacktrackPackets();
            return;
        }
        if (packet instanceof S08PacketPlayerPosLook || packet instanceof S40PacketDisconnect) {
            this.releaseBacktrackPackets();
            this.backtrackTarget = null;
            this.backtrackVec = null;
            return;
        }
        if (packet instanceof S13PacketDestroyEntities) {
            S13PacketDestroyEntities destroy = (S13PacketDestroyEntities)packet;
            for (int id : destroy.getEntityIDs()) {
                if (id != this.backtrackTarget.getEntityId()) continue;
                this.backtrackTarget = null;
                this.backtrackVec = null;
                this.releaseBacktrackPackets();
                return;
            }
        }
        if (packet instanceof S14PacketEntity) {
            S14PacketEntity s14 = (S14PacketEntity)packet;
            Entity e = s14.getEntity(Util.mc.theWorld);
            if (e == null || e.getEntityId() != this.backtrackTarget.getEntityId()) {
                return;
            }
            if (this.backtrackQueue.size() >= 50) {
                return;
            }
            this.backtrackVec = this.backtrackVec.addVector((double)s14.func_149062_c() / 32.0, (double)s14.func_149061_d() / 32.0, (double)s14.func_149064_e() / 32.0);
            this.backtrackQueue.add(new BackTrackPacket(packet, this.currentLatency));
            event.setCancelled(true);
        } else if (packet instanceof S18PacketEntityTeleport) {
            S18PacketEntityTeleport s18 = (S18PacketEntityTeleport)packet;
            if (s18.getEntityId() != this.backtrackTarget.getEntityId()) {
                return;
            }
            if (this.backtrackQueue.size() >= 50) {
                return;
            }
            this.backtrackVec = new Vec3((double)s18.getX() / 32.0, (double)s18.getY() / 32.0, (double)s18.getZ() / 32.0);
            this.backtrackQueue.add(new BackTrackPacket(packet, this.currentLatency));
            event.setCancelled(true);
        }
    };
    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        if (!this.rangeActive || !this.backtrackEnabled.getValue().booleanValue()) {
            return;
        }
        EntityLivingBase ent = event.target;
        if (ent instanceof EntityPlayer) {
            if (this.backtrackTarget == null || ent != this.backtrackTarget) {
                this.backtrackVec = ent.getPositionVector();
            }
            this.backtrackTarget = (EntityPlayer)ent;
            double distance = Util.mc.thePlayer.getDistanceToEntity(this.backtrackTarget);
            if (distance < (Double)this.backtrackDistanceMin.getValue() || distance > (Double)this.backtrackDistanceMax.getValue()) {
                return;
            }
            this.currentLatency = ((Double)this.backtrackMinDelay.getValue()).intValue() + this.random.nextInt(Math.max(1, ((Double)this.backtrackMaxDelay.getValue()).intValue() - ((Double)this.backtrackMinDelay.getValue()).intValue()));
            this.backtrackTimer.reset();
        }
    };
    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (Util.mc.getRenderManager() == null || Util.mc.getRenderViewEntity() == null) {
            return;
        }
        if (this.lagRangeEnabled.getValue().booleanValue() && this.lagRangeShowPos.getValue() != ShowPosition.NONE && Util.mc.gameSettings.thirdPersonView != 0 && this.hasLagRangeTarget && this.lastPosition != null && this.currentPosition != null) {
            Color color = Color.WHITE;
            double partialTicks = Util.mc.timer.renderPartialTicks;
            double x = this.lastPosition.xCoord + (this.currentPosition.xCoord - this.lastPosition.xCoord) * partialTicks;
            double y = this.lastPosition.yCoord + (this.currentPosition.yCoord - this.lastPosition.yCoord) * partialTicks;
            double z = this.lastPosition.zCoord + (this.currentPosition.zCoord - this.lastPosition.zCoord) * partialTicks;
            float size = Util.mc.thePlayer.getCollisionBorderSize();
            AxisAlignedBB aabb = new AxisAlignedBB((x -= Util.mc.getRenderManager().viewerPosX) - (double)Util.mc.thePlayer.width / 2.0, y -= Util.mc.getRenderManager().viewerPosY, (z -= Util.mc.getRenderManager().viewerPosZ) - (double)Util.mc.thePlayer.width / 2.0, x + (double)Util.mc.thePlayer.width / 2.0, y + (double)Util.mc.thePlayer.height, z + (double)Util.mc.thePlayer.width / 2.0).expand(size, size, size);
            RenderUtils.start3D();
            GlStateManager.color((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 1.0f);
            RenderUtils.drawBoundingBox(aabb);
            RenderUtils.stop3D();
        }
        if (this.backtrackEnabled.getValue().booleanValue() && this.backtrackTarget != null && this.backtrackVec != null && !this.backtrackTarget.isDead && this.currentLatency != 0) {
            BackTrackESP esp = (BackTrackESP)((Object)((Object)this.backtrackESP.getValue()));
            if (esp == BackTrackESP.NONE) {
                return;
            }
            Color color = Color.WHITE;
            double x = this.backtrackVec.xCoord - Util.mc.getRenderManager().viewerPosX;
            double y = this.backtrackVec.yCoord - Util.mc.getRenderManager().viewerPosY;
            double z = this.backtrackVec.zCoord - Util.mc.getRenderManager().viewerPosZ;
            if (esp == BackTrackESP.MODEL) {
                double dx = this.backtrackVec.xCoord - this.backtrackTarget.posX;
                double dy = this.backtrackVec.yCoord - this.backtrackTarget.posY;
                double dz = this.backtrackVec.zCoord - this.backtrackTarget.posZ;
                GlStateManager.pushMatrix();
                GlStateManager.translate(dx, dy, dz);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                Util.mc.getRenderManager().renderEntityStatic(this.backtrackTarget, Util.mc.timer.renderPartialTicks, false);
                GlStateManager.enableDepth();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
                return;
            }
            AxisAlignedBB playerBB = this.backtrackTarget.getEntityBoundingBox();
            double w = playerBB.maxX - playerBB.minX;
            double h = playerBB.maxY - playerBB.minY;
            AxisAlignedBB bb = new AxisAlignedBB(x - w / 2.0, y, z - w / 2.0, x + w / 2.0, y + h, z + w / 2.0);
            GlStateManager.pushMatrix();
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glEnable(3042);
            GL11.glDepthMask(false);
            switch (esp.ordinal()) {
                case 1: {
                    GL11.glLineWidth(2.0f);
                    RenderGlobal.drawOutlinedBoundingBox(bb, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                    break;
                }
                case 2: {
                    RenderGlobal.drawOutlinedBoundingBox(bb, color.getRed(), color.getGreen(), color.getBlue(), 63);
                    break;
                }
                case 4: {
                    GL11.glLineWidth(2.0f);
                    RenderGlobal.drawOutlinedBoundingBox(bb, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
                }
            }
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(3042);
            GL11.glDepthMask(true);
            GL11.glLineWidth(1.0f);
            GlStateManager.popMatrix();
        }
    };

    @Override
    public void onEnable() {
        this.fakeLagQueue.clear();
        this.fakeLagDispatching = false;
        this.tickIndex = -1;
        this.delayCounter = 0L;
        this.hasLagRangeTarget = false;
        this.lastPosition = null;
        this.currentPosition = null;
        this.backtrackQueue.clear();
        this.skipPackets.clear();
        this.backtrackVec = null;
        this.backtrackTarget = null;
        this.currentLatency = 0;
    }

    @Override
    public void onDisable() {
        this.fakeLagDispatching = true;
        while (!this.fakeLagQueue.isEmpty()) {
            PacketUtils.sendPacket(this.fakeLagQueue.poll().packet);
        }
        this.fakeLagDispatching = false;
        LagManager.setDelay(0);
        this.tickIndex = -1;
        this.delayCounter = 0L;
        this.hasLagRangeTarget = false;
        this.lastPosition = null;
        this.currentPosition = null;
        this.releaseBacktrackPackets();
    }

    private void updateRangeState() {
        EntityLivingBase target = TargetSelectionProcess.getTarget();
        if (target == null || Util.mc.thePlayer == null) {
            this.rangeActive = false;
            return;
        }
        double dist = Util.mc.thePlayer.getDistanceToEntity(target);
        this.rangeActive = dist >= (Double)this.startRange.getValue() && dist <= (Double)this.deactivateRange.getValue();
    }

    private boolean isValidLagRangeTarget(EntityPlayer player) {
        if (player == null) {
            return false;
        }
        if (player == Util.mc.thePlayer || player == Util.mc.thePlayer.ridingEntity) {
            return false;
        }
        if (player == Util.mc.getRenderViewEntity() || player == Util.mc.getRenderViewEntity().ridingEntity) {
            return false;
        }
        if (player.deathTime > 0) {
            return false;
        }
        if (this.isFriend(player)) {
            return false;
        }
        if (this.lagRangeTeams.getValue().booleanValue() && this.isSameTeam(player)) {
            return false;
        }
        return this.lagRangeBotCheck.getValue() == false || !this.isBot(player);
    }

    private boolean shouldResetLagRange(Packet<?> packet) {
        if (packet instanceof C02PacketUseEntity) {
            return true;
        }
        if (packet instanceof C07PacketPlayerDigging) {
            return ((C07PacketPlayerDigging)packet).getStatus() != C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;
        }
        if (packet instanceof C08PacketPlayerBlockPlacement) {
            ItemStack item = ((C08PacketPlayerBlockPlacement)packet).getStack();
            return item == null || !(item.getItem() instanceof ItemSword);
        }
        return false;
    }

    private void flushFakeLag() {
        this.fakeLagDispatching = true;
        while (!this.fakeLagQueue.isEmpty()) {
            PacketUtils.sendPacket(this.fakeLagQueue.poll().packet);
        }
        this.fakeLagDispatching = false;
    }

    private void releaseBacktrackPackets() {
        while (!this.backtrackQueue.isEmpty()) {
            BackTrackPacket btp = this.backtrackQueue.poll();
            this.skipPackets.add(btp.packet);
            this.processIncomingSilent(btp.packet);
        }
    }

    private void processIncomingSilent(Packet<?> packet) {
        if (packet == null) {
            return;
        }
        try {
            if (Util.mc.getNetHandler() != null) {
                packet.processPacket(Util.mc.getNetHandler());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private double distanceToBox(Entity entity, Vec3 from) {
        if (entity == null) {
            return Double.MAX_VALUE;
        }
        AxisAlignedBB box = entity.getEntityBoundingBox();
        double x = MathHelper.clamp_double(from.xCoord, box.minX, box.maxX);
        double y = MathHelper.clamp_double(from.yCoord, box.minY, box.maxY);
        double z = MathHelper.clamp_double(from.zCoord, box.minZ, box.maxZ);
        return from.distanceTo(new Vec3(x, y, z));
    }

    private boolean hasRawUnbreakingEnchant() {
        ItemStack stack = Util.mc.thePlayer.getHeldItem();
        return stack != null && EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) > 0;
    }

    private boolean isHoldingTool() {
        ItemStack stack = Util.mc.thePlayer.getHeldItem();
        return stack != null && (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemAxe || stack.getItem() instanceof ItemPickaxe || stack.getItem() instanceof ItemSpade);
    }

    private boolean isFriend(EntityPlayer player) {
        return MCFModule.excludedPlayers.contains(player);
    }

    private boolean isSameTeam(EntityPlayer player) {
        String myName = Util.mc.thePlayer.getDisplayName().getFormattedText();
        String theirName = player.getDisplayName().getFormattedText();
        for (String c : new String[]{"\u00a70", "\u00a71", "\u00a72", "\u00a73", "\u00a74", "\u00a75", "\u00a76", "\u00a77", "\u00a78", "\u00a79", "\u00a7a", "\u00a7b", "\u00a7c", "\u00a7d", "\u00a7e", "\u00a7f"}) {
            if (!myName.contains(c) || !theirName.contains(c)) continue;
            return true;
        }
        return false;
    }

    private boolean isBot(EntityPlayer player) {
        return AntiBotModule.botList.contains(player);
    }

    public static enum ShowPosition {
        NONE,
        DEFAULT,
        HUD;

    }

    public static enum BackTrackESP {
        NONE,
        BOX,
        FILLED,
        MODEL,
        WIREFRAME;

    }

    private static class PacketData {
        final Packet<?> packet;
        final long timestamp;

        PacketData(Packet<?> p, long t) {
            this.packet = p;
            this.timestamp = t;
        }
    }

    private static class BackTrackPacket {
        final Packet<?> packet;
        final Timer timer;
        final int latency;

        BackTrackPacket(Packet<?> packet, int latency) {
            this.packet = packet;
            this.timer = new Timer();
            this.latency = Math.max(latency, 1);
        }
    }
}

