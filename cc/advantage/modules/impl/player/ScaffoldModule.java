/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.modules.impl.player;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.player.MoveEvent;
import cc.advantage.api.events.impl.player.StrafeEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.notifications.NotificationManager;
import cc.advantage.api.notifications.NotificationType;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.movement.SpeedModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.EnumFacingOffset;
import cc.advantage.utils.client.Logger;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.InventoryUtils;
import cc.advantage.utils.mc.MovementUtils;
import cc.advantage.utils.mc.PacketUtils;
import cc.advantage.utils.mc.PlayerUtils;
import cc.advantage.utils.mc.RayCastUtils;
import cc.advantage.utils.mc.RotationUtils;
import cc.advantage.utils.misc.MovementFix;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.animations.Animation;
import cc.advantage.utils.render.animations.Direction;
import cc.advantage.utils.render.animations.impl.DecelerateAnimation;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import lombok.Generated;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.util.Vector3d;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Scaffold", category=ModuleCategory.PLAYER)
public final class ScaffoldModule
extends Module {
    private static final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Normal);
    private static final ModeProperty<Rotations> rotations = new ModeProperty<Rotations>("Rotations", Rotations.Normal, () -> mode.getValue() != Mode.SlowTelly && mode.getValue() != Mode.FastTelly && mode.getValue() != Mode.Hypixel);
    private static final ModeProperty<SearchAlgorithm> searchAlgorithm = new ModeProperty<SearchAlgorithm>("Search Algorithm", SearchAlgorithm.Normal);
    private final NumberProperty minRotationSpeed = new NumberProperty("Min Rotation Speed", 5.0, 0.0, 10.0, 1.0);
    private final NumberProperty maxRotationSpeed = new NumberProperty("Max Rotation Speed", 8.0, 0.0, 10.0, 1.0);
    public static Property<Boolean> limitRotations = new Property<Boolean>("Limit Rotations", false);
    private final NumberProperty rotationLimiterYawMax = new NumberProperty("Yaw Max", 30.0, limitRotations::getValue, 30.0, 180.0, 1.0);
    private final NumberProperty rotationLimiterYawMin = new NumberProperty("Yaw Min", 30.0, limitRotations::getValue, 30.0, 180.0, 1.0);
    private final NumberProperty rotationLimiterPitchMax = new NumberProperty("Pitch Max", 30.0, limitRotations::getValue, 20.0, 90.0, 1.0);
    private final NumberProperty rotationLimiterPitchMin = new NumberProperty("Pitch Min", 30.0, limitRotations::getValue, 20.0, 90.0, 1.0);
    private final NumberProperty placeDelay = new NumberProperty("Place Delay", 0.0, 0.0, 10.0, 1.0);
    public Property<Boolean> packetPlace = new Property<Boolean>("Packet Place", false);
    public Property<Boolean> packetSwing = new Property<Boolean>("Packet Swing", false);
    private static final ModeProperty<SprintMode> sprintMode = new ModeProperty<SprintMode>("Sprint Mode", SprintMode.None);
    private static final ModeProperty<TowerMode> towerMode = new ModeProperty<TowerMode>("Tower Mode", TowerMode.None);
    private static final Property<Boolean> towerMove = new Property<Boolean>("Tower Move", true, () -> towerMode.getValue() != TowerMode.None);
    public static Property<Boolean> moveFix = new Property<Boolean>("Move Fix", true);
    private final ModeProperty<RayCast> rayCast = new ModeProperty<RayCast>("Ray Cast", RayCast.Normal);
    public static ModeProperty<JumpMode> autoJump = new ModeProperty<JumpMode>("Auto Jump", JumpMode.None);
    private final NumberProperty jumpDelayTicks = new NumberProperty("Auto Jump Delay", 0.0, () -> autoJump.getValue() != JumpMode.None, 0.0, 5.0, 1.0);
    public static Property<Boolean> edge = new Property<Boolean>("Jump Only On Edge", false, () -> autoJump.getValue() != JumpMode.None);
    public static Property<Boolean> keepY = new Property<Boolean>("Keep Y", false, () -> autoJump.getValue() != JumpMode.None);
    private static final Property<Boolean> sneak = new Property<Boolean>("Sneak", false);
    private final NumberProperty sneakEvery = new NumberProperty("Sneak Every", 1.0, sneak::getValue, 0.0, 10.0, 1.0);
    private static final Property<Boolean> safeWalk = new Property<Boolean>("Safe Walk", false);
    private static final Property<Boolean> safeWalkOnAir = new Property<Boolean>("Safe Walk On Air", false, safeWalk::getValue);
    private final NumberProperty expand = new NumberProperty("Expand", 0.0, 0.0, 4.0, 1.0);
    private static final ModeProperty<BlockCounter> blockCounter = new ModeProperty<BlockCounter>("Block Counter", BlockCounter.None);
    private final Property<Boolean> render = new Property<Boolean>("Render Block Selection", true);
    private final Timer delayTimer = new Timer();
    private Vec3 targetBlock;
    private EnumFacingOffset enumFacing;
    public Vec3i offset = new Vec3i(0, 0, 0);
    private BlockPos blockFace;
    private float targetYaw;
    private float targetPitch;
    private float yawDrift;
    private float pitchDrift;
    private int ticksOnAir;
    public double startY;
    private boolean canPlace;
    private int directionalChange;
    private int blockCount;
    private Animation anim = new DecelerateAnimation(250, 1.0);
    private float rotSpeed;
    private boolean overrided;
    public int recursions;
    public int recursion;
    private boolean stop;
    private int blocksPlaced;
    private int towerTick;
    private int towerDelay;
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        this.offset = new Vec3i(0, 0, 0);
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        this.setSuffix(((Mode)((Object)((Object)mode.getValue()))).toString());
        this.resetBinds(false, false, true, true, false, false);
        if (safeWalk.getValue().booleanValue()) {
            Util.mc.thePlayer.safeWalk = safeWalkOnAir.getValue() != false ? true : Util.mc.thePlayer.onGround;
        }
        this.sprint();
        this.sneak();
        this.tower();
        this.recursion = 0;
        while (this.recursion <= this.recursions) {
            boolean sameY;
            if (!this.overrided) {
                this.rotSpeed = (float)MathUtils.getRandom((Double)this.minRotationSpeed.getValue(), (Double)this.maxRotationSpeed.getValue());
            }
            if (((Double)this.expand.getValue()).intValue() != 0) {
                double direction = MovementUtils.direction(Util.mc.thePlayer.rotationYaw, Util.mc.gameSettings.keyBindForward.isKeyDown() ? 1.0 : (Util.mc.gameSettings.keyBindBack.isKeyDown() ? -1.0 : 0.0), Util.mc.gameSettings.keyBindRight.isKeyDown() ? -1.0 : (Util.mc.gameSettings.keyBindLeft.isKeyDown() ? 1.0 : 0.0));
                for (int range = 0; range <= ((Double)this.expand.getValue()).intValue(); ++range) {
                    if (!(PlayerUtils.blockAheadOfPlayer(range, (double)this.offset.getY() - 0.5) instanceof BlockAir)) continue;
                    this.offset = this.offset.add(new Vec3i((int)(-Math.sin(direction) * (double)(range + 1)), 0, (int)(Math.cos(direction) * (double)(range + 1))));
                    break;
                }
            }
            boolean keepYActive = keepY.getValue() != false || mode.getValue() == Mode.SlowTelly;
            boolean bl = sameY = (keepYActive || Advantage.INSTANCE.getModuleManager().getModule(SpeedModule.class).isEnabled()) && !Util.mc.gameSettings.keyBindJump.isKeyDown() && MovementUtils.isMoving();
            if (InventoryUtils.findBlock() == -1) {
                NotificationManager.post(NotificationType.DISABLE, "Scaffold", "No blocks found, disabling Scaffold.");
                this.toggle();
                return;
            }
            if (InventoryUtils.findBlock() != -1) {
                Util.mc.thePlayer.inventory.currentItem = InventoryUtils.findBlock();
            }
            this.ticksOnAir = this.doesNotContainBlock(1) && (!sameY || this.doesNotContainBlock(2) && this.doesNotContainBlock(3) && this.doesNotContainBlock(4)) ? ++this.ticksOnAir : 0;
            this.canPlace = Util.mc.thePlayer.inventory.currentItem == InventoryUtils.findBlock() && this.ticksOnAir > 0 && this.delayTimer.hasTimeElapsed(((Double)this.placeDelay.getValue()).longValue() * 20L);
            this.targetBlock = PlayerUtils.getPlacePossibility(this.offset.getX(), this.offset.getY(), this.offset.getZ(), sameY ? Integer.valueOf((int)Math.floor(this.startY)) : null);
            if (this.targetBlock == null) {
                return;
            }
            this.enumFacing = PlayerUtils.getEnumFacing(this.targetBlock, this.offset.getY() < 0);
            if (this.enumFacing == null) {
                return;
            }
            BlockPos position = new BlockPos(this.targetBlock.xCoord, this.targetBlock.yCoord, this.targetBlock.zCoord);
            this.blockFace = position.add(this.enumFacing.getOffset().xCoord, this.enumFacing.getOffset().yCoord, this.enumFacing.getOffset().zCoord);
            if (this.blockFace == null || this.enumFacing == null || this.enumFacing.getEnumFacing() == null) {
                return;
            }
            this.doRotations();
            if (this.targetBlock == null || this.enumFacing == null || this.blockFace == null) {
                return;
            }
            if (this.startY - 1.0 != Math.floor(this.targetBlock.yCoord) && sameY) {
                return;
            }
            if (Util.mc.thePlayer.inventory.getCurrentItem() == null || !(Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock)) {
                return;
            }
            if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock && this.canPlace && (RayCastUtils.overBlock(this.enumFacing.getEnumFacing(), this.blockFace, this.rayCast.getValue() == RayCast.Strict) || this.rayCast.getValue() == RayCast.None)) {
                this.place();
                this.ticksOnAir = 0;
            }
            if (Util.mc.gameSettings.keyBindJump.isKeyDown() && Util.mc.thePlayer.posY % 1.0 > 0.5) {
                this.startY = Math.floor(Util.mc.thePlayer.posY);
            }
            if ((Util.mc.thePlayer.posY < this.startY || Util.mc.thePlayer.onGround) && !MovementUtils.isMoving()) {
                this.startY = Math.floor(Util.mc.thePlayer.posY);
            }
            ++this.recursion;
        }
    };
    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (!moveFix.getValue().booleanValue()) {
            MovementUtils.useDiagonalSpeed();
        }
        if (!Util.mc.gameSettings.keyBindJump.isPressed()) {
            this.jump();
        }
    };
    @EventLink
    public final Listener<MoveEvent> moveEventListener = event -> {
        if (this.stop) {
            event.setForward(0.0f);
            event.setStrafe(0.0f);
            event.setJump(false);
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = PacketUtils::correctBlockCount;
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = event -> {
        if (this.render.getValue().booleanValue()) {
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDisable(3553);
            GlStateManager.disableCull();
            GL11.glDepthMask(false);
            float red = (float)ColorProcess.getColor().getRed() / 255.0f;
            float green = (float)ColorProcess.getColor().getGreen() / 255.0f;
            float blue = (float)ColorProcess.getColor().getBlue() / 255.0f;
            float lineWidth = 0.0f;
            if (this.blockFace != null) {
                if (Util.mc.thePlayer.getDistance(this.blockFace.getX(), this.blockFace.getY(), this.blockFace.getZ()) > 1.0) {
                    double d0 = 1.0 - Util.mc.thePlayer.getDistance(this.blockFace.getX(), this.blockFace.getY(), this.blockFace.getZ()) / 20.0;
                    if (d0 < 0.3) {
                        d0 = 0.3;
                    }
                    lineWidth *= (float)d0;
                }
                RenderUtils.drawBlockESP(this.blockFace, red, green, blue, 0.3137255f, 1.0f, lineWidth);
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glDepthMask(true);
            GlStateManager.enableCull();
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDisable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(2848);
        }
    };
    @EventLink
    public final Listener<Render2DEvent> render2DEventListener = event -> this.renderBlockCounter();
    @EventLink
    public final Listener<ShaderEvent> shaderEventListener = event -> this.renderBlockCounter();

    @Override
    public void onEnable() {
        this.anim = new DecelerateAnimation(250, 1.0);
        if (Util.mc.thePlayer != null) {
            this.targetYaw = Util.mc.thePlayer.rotationYaw - 180.0f;
            this.targetPitch = 90.0f;
            this.pitchDrift = (float)((Math.random() - 0.5) * (Math.random() - 0.5) * 10.0);
            this.yawDrift = (float)((Math.random() - 0.5) * (Math.random() - 0.5) * 10.0);
            this.startY = Math.floor(Util.mc.thePlayer.posY);
            this.targetBlock = null;
            this.overrided = false;
            this.recursions = 0;
            this.towerTick = 0;
            this.towerDelay = 0;
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.anim = new DecelerateAnimation(250, 1.0);
        if (Util.mc.thePlayer != null) {
            Util.mc.thePlayer.safeWalk = false;
            Util.mc.timer.timerSpeed = 1.0f;
            this.overrided = false;
            this.stop = false;
            this.blocksPlaced = 0;
            this.towerTick = 0;
            this.towerDelay = 0;
        }
        this.resetBinds();
        super.onDisable();
    }

    private void renderBlockCounter() {
        if (blockCounter.getValue() == BlockCounter.None) {
            return;
        }
        switch (((BlockCounter)((Object)blockCounter.getValue())).ordinal()) {
            case 0: {
                this.anim.setDirection(this.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
                if (!this.isEnabled() && this.anim.isDone()) {
                    return;
                }
                ScaledResolution sr = new ScaledResolution(Util.mc);
                float output = this.anim.getOutput().floatValue();
                if (Util.mc.thePlayer.inventory.getCurrentItem() != null && Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
                    this.blockCount = Util.mc.thePlayer.inventory.getCurrentItem().stackSize;
                }
                float blockWH = Util.mc.thePlayer.inventory.getCurrentItem() != null ? 15.0f : -2.0f;
                int spacing = 3;
                String text = "\u00a7l" + this.blockCount + "\u00a7r block" + (this.blockCount != 1 ? "s" : "");
                float textWidth = FontUtils.getFont("bold").getStringWidth(text);
                float totalWidth = (textWidth + blockWH + (float)spacing + 6.0f) * output;
                float x = (float)sr.getScaledWidth() / 2.0f - totalWidth / 2.0f;
                float y = (float)sr.getScaledHeight() - ((float)sr.getScaledHeight() / 2.0f - 20.0f);
                float height = 20.0f;
                RenderUtils.startScissor(x - 1.5f, y - 1.5f, totalWidth + 3.0f, height + 3.0f);
                RenderUtils.drawRoundedRect(x, y, totalWidth, height, 5.0f, new Color(ColorProcess.getColor().darker().getRed(), ColorProcess.getColor().darker().getGreen(), ColorProcess.getColor().darker().getBlue(), 130));
                FontUtils.getFont("bold").drawString(text, x + 3.0f + blockWH + (float)spacing, y + height / 2.0f - (float)FontUtils.getFont("bold").getHeight() / 2.0f + 0.5f, -1);
                RenderHelper.enableGUIStandardItemLighting();
                Util.mc.getRenderItem().renderItemAndEffectIntoGUI(Util.mc.thePlayer.inventory.getCurrentItem(), (int)x + 3, (int)(y + 10.0f - blockWH / 2.0f));
                RenderHelper.disableStandardItemLighting();
                RenderUtils.endScissor();
            }
        }
    }

    public void resetBinds() {
        this.resetBinds(true, true, true, true, true, true);
    }

    public void resetBinds(boolean sneak, boolean jump, boolean right, boolean left, boolean forward, boolean back) {
        if (sneak) {
            Util.mc.gameSettings.keyBindSneak.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindSneak.getKeyCode()));
        }
        if (jump) {
            Util.mc.gameSettings.keyBindJump.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindJump.getKeyCode()));
        }
        if (right) {
            Util.mc.gameSettings.keyBindRight.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindRight.getKeyCode()));
        }
        if (left) {
            Util.mc.gameSettings.keyBindLeft.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindLeft.getKeyCode()));
        }
        if (forward) {
            Util.mc.gameSettings.keyBindForward.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindForward.getKeyCode()));
        }
        if (back) {
            Util.mc.gameSettings.keyBindBack.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindBack.getKeyCode()));
        }
    }

    public void doRotations() {
        MovementFix movementFix = moveFix.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF;
        switch (((Mode)((Object)mode.getValue())).ordinal()) {
            case 0: {
                Util.mc.entityRenderer.getMouseOver(1.0f);
                if (!this.canPlace || Util.mc.gameSettings.keyBindPickBlock.isKeyDown() || Util.mc.objectMouseOver.sideHit == this.enumFacing.getEnumFacing() && Util.mc.objectMouseOver.getBlockPos().equals(this.blockFace)) break;
                this.getBaseRotations();
                if (rotations.getValue() != Rotations.StaticYaw) break;
                this.targetYaw = Util.mc.thePlayer.rotationYaw - 180.0f;
                break;
            }
            case 4: {
                if (this.canPlace) {
                    if (this.enumFacing.getEnumFacing() == EnumFacing.UP) {
                        this.targetPitch = 90.0f;
                    } else {
                        double staticYaw = (float)(Math.toDegrees(Math.atan2(this.enumFacing.getOffset().zCoord, this.enumFacing.getOffset().xCoord)) % 360.0) - 90.0f;
                        double staticPitch = 80.0;
                        this.targetYaw = (float)staticYaw + this.yawDrift;
                        this.targetPitch = (float)staticPitch + this.pitchDrift;
                    }
                } else if (Math.random() > 0.99 || this.targetPitch % 90.0f == 0.0f) {
                    this.yawDrift = (float)(Math.random() - 0.5);
                    this.pitchDrift = (float)(Math.random() - 0.5);
                }
                if (!Util.mc.gameSettings.keyBindForward.isKeyDown() || Util.mc.gameSettings.keyBindJump.isKeyDown()) break;
                double offset = 0.0;
                double speed = 0.0;
                switch (Util.mc.thePlayer.getHorizontalFacing()) {
                    case NORTH: {
                        offset = Util.mc.thePlayer.posX - Math.floor(Util.mc.thePlayer.posX);
                        speed = Util.mc.thePlayer.motionZ;
                        break;
                    }
                    case EAST: {
                        offset = Util.mc.thePlayer.posZ - Math.floor(Util.mc.thePlayer.posZ);
                        speed = Util.mc.thePlayer.motionX;
                        break;
                    }
                    case SOUTH: {
                        offset = 1.0 - (Util.mc.thePlayer.posX - Math.floor(Util.mc.thePlayer.posX));
                        speed = Util.mc.thePlayer.motionZ;
                        break;
                    }
                    case WEST: {
                        offset = 1.0 - (Util.mc.thePlayer.posZ - Math.floor(Util.mc.thePlayer.posZ));
                        speed = Util.mc.thePlayer.motionX;
                        break;
                    }
                    default: {
                        Logger.chatPrint("Unknown " + Math.random());
                    }
                }
                speed = Math.abs(speed);
                if (speed < 0.086 && Math.abs(offset - 0.5) < 0.4 && ((Double)this.placeDelay.getValue()).intValue() <= 1) break;
                if (offset < 0.5 + (Math.random() - 0.5) / 10.0) {
                    Util.mc.gameSettings.keyBindLeft.setPressed(false);
                    Util.mc.gameSettings.keyBindRight.setPressed(true);
                    break;
                }
                Util.mc.gameSettings.keyBindRight.setPressed(false);
                Util.mc.gameSettings.keyBindLeft.setPressed(true);
                break;
            }
            case 2: {
                if (this.recursion != 0) break;
                if (Util.mc.thePlayer.offGroundTicks <= 9 && Util.mc.thePlayer.offGroundTicks > 3) {
                    Util.mc.entityRenderer.getMouseOver(1.0f);
                    if (!this.canPlace || Util.mc.gameSettings.keyBindPickBlock.isKeyDown() || Util.mc.objectMouseOver.sideHit == this.enumFacing.getEnumFacing() && Util.mc.objectMouseOver.getBlockPos().equals(this.blockFace)) break;
                    this.getBaseRotations();
                    break;
                }
                this.targetPitch = Util.mc.thePlayer.rotationPitch;
                this.targetYaw = Util.mc.thePlayer.rotationYaw;
                this.canPlace = false;
                break;
            }
            case 3: {
                if (this.recursion != 0) break;
                Util.mc.entityRenderer.getMouseOver(1.0f);
                if (Util.mc.thePlayer.hurtTime == 0 && Util.mc.thePlayer.onGround) {
                    this.targetYaw = (float)Math.toDegrees(MovementUtils.direction());
                }
                if (Util.mc.thePlayer.onGround && MovementUtils.isMoving()) {
                    this.targetYaw = Util.mc.thePlayer.rotationYaw;
                    break;
                }
                if (!this.canPlace || Util.mc.gameSettings.keyBindPickBlock.isKeyDown() || Util.mc.objectMouseOver.sideHit == this.enumFacing.getEnumFacing() && Util.mc.objectMouseOver.getBlockPos().equals(this.blockFace)) break;
                this.getBaseRotations();
                break;
            }
            case 1: {
                boolean diagonal;
                this.overrided = true;
                boolean bl = diagonal = RotationUtils.getMovementYaw() % 90.0f > 10.0f && RotationUtils.getMovementYaw() % 90.0f < 80.0f;
                if (this.recursion != 0) break;
                Util.mc.entityRenderer.getMouseOver(1.0f);
                if (Util.mc.thePlayer.onGround && MovementUtils.isMoving() && !Util.mc.gameSettings.keyBindJump.isKeyDown()) {
                    this.rotSpeed = 10.0f;
                    this.targetYaw = Util.mc.thePlayer.rotationYaw;
                    this.canPlace = false;
                    break;
                }
                if (!this.canPlace || Util.mc.gameSettings.keyBindPickBlock.isKeyDown() || Util.mc.objectMouseOver.sideHit == this.enumFacing.getEnumFacing() && Util.mc.objectMouseOver.getBlockPos().equals(this.blockFace)) break;
                this.rotSpeed = 0.9f;
                if (diagonal) {
                    this.rotSpeed = 1.0f;
                }
                this.getBaseRotations();
                this.canPlace = true;
                break;
            }
            case 5: {
                this.targetYaw = Util.mc.thePlayer.rotationYaw - Util.mc.thePlayer.rotationYaw % 90.0f - 180.0f + (float)(45 * (Util.mc.thePlayer.rotationYaw > 0.0f ? 1 : -1));
                this.targetPitch = 76.4f;
                movementFix = MovementFix.TRADITIONAL;
                double spacing = 0.15;
                boolean edgeX = Math.abs(Util.mc.thePlayer.posX % 1.0) > 1.0 - spacing || Math.abs(Util.mc.thePlayer.posX % 1.0) < spacing;
                boolean edgeZ = Math.abs(Util.mc.thePlayer.posZ % 1.0) > 1.0 - spacing || Math.abs(Util.mc.thePlayer.posZ % 1.0) < spacing;
                Util.mc.gameSettings.keyBindRight.setPressed(edgeX && edgeZ || Keyboard.isKeyDown(Util.mc.gameSettings.keyBindLeft.getKeyCode()));
                Util.mc.gameSettings.keyBindBack.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindForward.getKeyCode()));
                Util.mc.gameSettings.keyBindForward.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindBack.getKeyCode()));
                Util.mc.gameSettings.keyBindLeft.setPressed(Keyboard.isKeyDown(Util.mc.gameSettings.keyBindRight.getKeyCode()));
                ++this.directionalChange;
                if (Math.abs(MathHelper.wrapAngleTo180_double(this.targetYaw - RotationProcess.lastServerRotations.getX())) > 10.0) {
                    this.directionalChange = (int)(Math.random() * 4.0);
                    this.yawDrift = (float)(Math.random() - 0.5) / 10.0f;
                    this.pitchDrift = (float)(Math.random() - 0.5) / 10.0f;
                }
                if (Math.random() > 0.99) {
                    this.yawDrift = (float)(Math.random() - 0.5) / 10.0f;
                    this.pitchDrift = (float)(Math.random() - 0.5) / 10.0f;
                }
                if (this.directionalChange <= 10) {
                    Util.mc.gameSettings.keyBindSneak.setPressed(true);
                } else if (this.directionalChange == 11) {
                    Util.mc.gameSettings.keyBindSneak.setPressed(false);
                }
                this.targetYaw += this.yawDrift;
                this.targetPitch += this.pitchDrift;
            }
        }
        Vector2f limitedRotations = this.applyRotationLimits(this.targetYaw, this.targetPitch);
        this.targetYaw = limitedRotations.x;
        this.targetPitch = limitedRotations.y;
        if (this.rotSpeed != 0.0f && this.blockFace != null && this.enumFacing != null) {
            RotationProcess.setRotations(new Vector2f(this.targetYaw, this.targetPitch), this.rotSpeed, movementFix);
        }
    }

    public void getBaseRotations() {
        switch (((SearchAlgorithm)((Object)searchAlgorithm.getValue())).ordinal()) {
            case 0: {
                EntityPlayerSP player = Util.mc.thePlayer;
                double difference = player.posY + (double)player.getEyeHeight() - this.targetBlock.yCoord - 0.5 - (Math.random() - 0.5) * 0.1;
                MovingObjectPosition movingObjectPosition = null;
                for (int offset = -180; offset <= 180; offset += 45) {
                    player.setPosition(player.posX, player.posY - difference, player.posZ);
                    movingObjectPosition = RayCastUtils.rayCast(new Vector2f(player.rotationYaw + (float)(offset * 3), 0.0f), 4.5);
                    player.setPosition(player.posX, player.posY + difference, player.posZ);
                    if (movingObjectPosition == null || movingObjectPosition.hitVec == null) {
                        return;
                    }
                    Vector2f rotations = RotationUtils.calculate(movingObjectPosition.hitVec);
                    if (!RayCastUtils.overBlock(rotations, this.blockFace, this.enumFacing.getEnumFacing()).booleanValue()) continue;
                    this.targetYaw = rotations.x;
                    this.targetPitch = rotations.y;
                    return;
                }
                Vector2f rotations = RotationUtils.calculate(new Vector3d(this.blockFace.getX(), this.blockFace.getY(), this.blockFace.getZ()), this.enumFacing.getEnumFacing());
                if (RayCastUtils.overBlock(new Vector2f(this.targetYaw, this.targetPitch), this.blockFace, this.enumFacing.getEnumFacing()).booleanValue()) break;
                this.targetYaw = rotations.x;
                this.targetPitch = rotations.y;
                break;
            }
            case 1: {
                EntityPlayerSP player = Util.mc.thePlayer;
                double deltaX = (double)this.blockFace.getX() - player.posX + 0.5;
                double deltaY = (double)this.blockFace.getY() - (player.posY + (double)player.getEyeHeight()) + 0.5;
                double deltaZ = (double)this.blockFace.getZ() - player.posZ + 0.5;
                double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                float baseYaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
                float basePitch = (float)(-Math.toDegrees(Math.atan2(deltaY, horizontalDistance)));
                float bestYaw = baseYaw;
                float bestPitch = basePitch;
                double bestDistance = Double.MAX_VALUE;
                for (float yawOffset = -15.0f; yawOffset <= 15.0f; yawOffset += 3.0f) {
                    for (float pitchOffset = -15.0f; pitchOffset <= 15.0f; pitchOffset += 3.0f) {
                        double pitchDiff;
                        double yawDiff;
                        double totalDistance;
                        float testYaw = baseYaw + yawOffset;
                        float testPitch = basePitch + pitchOffset;
                        Vector2f testRotations = new Vector2f(testYaw, testPitch = MathHelper.clamp_float(testPitch, -90.0f, 90.0f));
                        if (!RayCastUtils.overBlock(testRotations, this.enumFacing.getEnumFacing(), this.blockFace, this.rayCast.getValue() == RayCast.Strict) || !((totalDistance = Math.sqrt((yawDiff = (double)Math.abs(MathHelper.wrapAngleTo180_float(testYaw - RotationProcess.rotations.x))) * yawDiff + (pitchDiff = (double)Math.abs(testPitch - RotationProcess.rotations.y)) * pitchDiff)) < bestDistance)) continue;
                        bestDistance = totalDistance;
                        bestYaw = testYaw;
                        bestPitch = testPitch;
                    }
                }
                if (bestDistance != Double.MAX_VALUE) {
                    this.targetYaw = bestYaw;
                    this.targetPitch = bestPitch;
                    break;
                }
                Vector2f rotations = RotationUtils.calculate(new Vector3d(this.blockFace.getX(), this.blockFace.getY(), this.blockFace.getZ()), this.enumFacing.getEnumFacing());
                this.targetYaw = rotations.x;
                this.targetPitch = rotations.y;
                break;
            }
            case 2: {
                if (RayCastUtils.overBlock(RotationProcess.rotations, this.enumFacing.getEnumFacing(), this.blockFace, true)) {
                    return;
                }
                for (float possibleYaw = Util.mc.thePlayer.rotationYaw - 180.0f; possibleYaw <= Util.mc.thePlayer.rotationYaw + 360.0f - 180.0f; possibleYaw += 45.0f) {
                    for (float possiblePitch = 90.0f; possiblePitch > 30.0f; possiblePitch -= possiblePitch > (float)(Util.mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 60 : 80) ? 1.0f : 10.0f) {
                        if (!RayCastUtils.overBlock(new Vector2f(possibleYaw, possiblePitch), this.enumFacing.getEnumFacing(), this.blockFace, true)) continue;
                        this.targetYaw = possibleYaw;
                        this.targetPitch = possiblePitch;
                    }
                }
                break;
            }
        }
    }

    private float getHypixelYaw() {
        float upperOffset;
        float lowerOffset;
        float snappedBase = (float)Math.round(Util.mc.thePlayer.rotationYaw / 45.0f) * 45.0f;
        if (Math.abs(snappedBase % 90.0f) < 0.001f) {
            lowerOffset = 111.0f;
            upperOffset = 111.0f;
        } else {
            lowerOffset = 137.0f;
            upperOffset = 137.0f;
        }
        float lowerCandidate = snappedBase - lowerOffset;
        float upperCandidate = snappedBase + upperOffset;
        return Math.abs(Util.mc.thePlayer.rotationYaw - lowerCandidate) <= Math.abs(upperCandidate - Util.mc.thePlayer.rotationYaw) ? lowerCandidate : upperCandidate;
    }

    private Vector2f applyRotationLimits(float targetYaw, float targetPitch) {
        if (!limitRotations.getValue().booleanValue()) {
            return new Vector2f(targetYaw, targetPitch);
        }
        float currentYaw = Util.mc.thePlayer.rotationYaw;
        float currentPitch = Util.mc.thePlayer.rotationPitch;
        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - currentYaw);
        float maxYaw = ((Double)this.rotationLimiterYawMax.getValue()).floatValue();
        float minYaw = ((Double)this.rotationLimiterYawMin.getValue()).floatValue();
        if (Math.abs(yawDiff) > maxYaw) {
            yawDiff = yawDiff > 0.0f ? maxYaw : -maxYaw;
        } else if (Math.abs(yawDiff) < minYaw) {
            yawDiff = yawDiff > 0.0f ? minYaw : -minYaw;
        }
        float limitedYaw = currentYaw + yawDiff;
        float pitchDiff = targetPitch - currentPitch;
        float maxPitch = ((Double)this.rotationLimiterPitchMax.getValue()).floatValue();
        float minPitch = ((Double)this.rotationLimiterPitchMin.getValue()).floatValue();
        if (Math.abs(pitchDiff) > maxPitch) {
            pitchDiff = pitchDiff > 0.0f ? maxPitch : -maxPitch;
        } else if (Math.abs(pitchDiff) < minPitch) {
            pitchDiff = pitchDiff > 0.0f ? minPitch : -minPitch;
        }
        float limitedPitch = MathHelper.clamp_float(currentPitch + pitchDiff, -90.0f, 90.0f);
        return new Vector2f(limitedYaw, limitedPitch);
    }

    public Vec3 getHitVec() {
        Vec3 hitVec = new Vec3((double)this.blockFace.getX() + Math.random(), (double)this.blockFace.getY() + Math.random(), (double)this.blockFace.getZ() + Math.random());
        MovingObjectPosition movingObjectPosition = RayCastUtils.rayCast(RotationProcess.rotations, Util.mc.playerController.getBlockReachDistance());
        switch (this.enumFacing.getEnumFacing()) {
            case DOWN: {
                hitVec.yCoord = this.blockFace.getY();
                break;
            }
            case UP: {
                hitVec.yCoord = this.blockFace.getY() + 1;
                break;
            }
            case NORTH: {
                hitVec.zCoord = this.blockFace.getZ();
                break;
            }
            case EAST: {
                hitVec.xCoord = this.blockFace.getX() + 1;
                break;
            }
            case SOUTH: {
                hitVec.zCoord = this.blockFace.getZ() + 1;
                break;
            }
            case WEST: {
                hitVec.xCoord = this.blockFace.getX();
            }
        }
        if (movingObjectPosition != null && movingObjectPosition.getBlockPos() != null && movingObjectPosition.hitVec != null && movingObjectPosition.getBlockPos().equals(this.blockFace) && movingObjectPosition.sideHit == this.enumFacing.getEnumFacing()) {
            hitVec = movingObjectPosition.hitVec;
        }
        return hitVec;
    }

    private void place() {
        Vec3 hitVec = this.getHitVec();
        if (!this.packetPlace.getValue().booleanValue()) {
            Util.mc.rightClickMouse();
        } else if (Util.mc.playerController.onPlayerRightClick(Util.mc.thePlayer, Util.mc.theWorld, Util.mc.thePlayer.inventory.getCurrentItem(), this.blockFace, this.enumFacing.getEnumFacing(), hitVec)) {
            if (!this.packetSwing.getValue().booleanValue()) {
                Util.mc.thePlayer.swingItem();
            } else {
                PacketUtils.sendPacket(new C0APacketAnimation());
            }
        }
        ++this.blocksPlaced;
        this.delayTimer.reset();
    }

    public void jump() {
        boolean keepYActive;
        if (Util.mc.thePlayer.onGroundTicks < ((Double)this.jumpDelayTicks.getValue()).intValue()) {
            return;
        }
        if (Util.mc.gameSettings.keyBindJump.isKeyDown()) {
            return;
        }
        if ((mode.getValue() == Mode.FastTelly || mode.getValue() == Mode.SlowTelly || mode.getValue() == Mode.Hypixel) && autoJump.getValue() == JumpMode.None) {
            autoJump.setValue(JumpMode.Normal);
        }
        boolean bl = keepYActive = keepY.getValue() != false || mode.getValue() == Mode.SlowTelly;
        if (keepYActive && autoJump.getValue() != JumpMode.None && Util.mc.thePlayer.onGround && MovementUtils.isMoving() && Util.mc.thePlayer.posY == this.startY && (!edge.getValue().booleanValue() || ScaffoldModule.isNearEdge())) {
            this.handleJump();
        }
        if (autoJump.getValue() != JumpMode.None && !keepYActive && (!edge.getValue().booleanValue() || ScaffoldModule.isNearEdge()) && Util.mc.thePlayer.onGround && MovementUtils.isMoving()) {
            this.handleJump();
        }
    }

    private void handleJump() {
        if (mode.getValue() == Mode.Hypixel && !(MovementUtils.getSpeed() <= 0.02) && Util.mc.thePlayer.offGroundTicks >= 9) {
            return;
        }
        if (autoJump.getValue() == JumpMode.Dev) {
            Util.mc.thePlayer.motionY = 0.42f;
        }
        if (autoJump.getValue() == JumpMode.Normal) {
            Util.mc.thePlayer.jump();
        }
    }

    private void sprint() {
        switch (((SprintMode)((Object)sprintMode.getValue())).ordinal()) {
            case 0: {
                Util.mc.thePlayer.setSprinting(MovementUtils.isMoving());
                break;
            }
            case 1: {
                break;
            }
            case 2: {
                Util.mc.gameSettings.keyBindSprint.setPressed(false);
                Util.mc.thePlayer.setSprinting(false);
            }
        }
    }

    private void sneak() {
        if (sneak.getValue().booleanValue()) {
            if (this.blocksPlaced >= ((Double)this.sneakEvery.getValue()).intValue() && !Util.mc.gameSettings.keyBindSneak.isPressed()) {
                Util.mc.gameSettings.keyBindSneak.setPressed(true);
                this.blocksPlaced = 0;
            } else {
                Util.mc.gameSettings.keyBindSneak.setPressed(false);
            }
        }
    }

    public void tower() {
        if (towerMode.getValue() == TowerMode.None || !Util.mc.gameSettings.keyBindJump.isKeyDown()) {
            return;
        }
        if (!towerMove.getValue().booleanValue() && !MovementUtils.isMoving()) {
            return;
        }
        block0 : switch (((TowerMode)((Object)towerMode.getValue())).ordinal()) {
            case 2: {
                if (Util.mc.thePlayer.posY % 1.0 <= 0.00153598) {
                    Util.mc.thePlayer.setPosition(Util.mc.thePlayer.posX, Math.floor(Util.mc.thePlayer.posY), Util.mc.thePlayer.posZ);
                    Util.mc.thePlayer.motionY = 0.41998;
                    break;
                }
                if (!(Util.mc.thePlayer.posY % 1.0 < 0.1) || !Util.mc.thePlayer.onGround) break;
                Util.mc.thePlayer.setPosition(Util.mc.thePlayer.posX, Math.floor(Util.mc.thePlayer.posY), Util.mc.thePlayer.posZ);
                break;
            }
            case 0: {
                Util.mc.thePlayer.motionY = 0.42;
                break;
            }
            case 1: {
                switch (Util.mc.thePlayer.offGroundTicks) {
                    case 3: {
                        Util.mc.timer.timerSpeed = 1.25f;
                        break;
                    }
                    case 4: {
                        Util.mc.timer.timerSpeed = 1.12f;
                        break;
                    }
                    case 5: {
                        Util.mc.timer.timerSpeed = 1.06f;
                        break;
                    }
                    case 6: {
                        Util.mc.timer.timerSpeed = 1.0f;
                    }
                }
                break;
            }
            case 3: {
                if (Util.mc.thePlayer.isCollidedHorizontally || Util.mc.thePlayer.hurtTime > 5 || Util.mc.thePlayer.isPotionActive(Potion.jump) || Util.mc.thePlayer.inventory.getCurrentItem() == null || !(Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock)) {
                    this.resetTowerState();
                    break;
                }
                int yState = (int)(Util.mc.thePlayer.posY % 1.0 * 100.0);
                switch (this.towerTick) {
                    case 0: {
                        if (!Util.mc.thePlayer.onGround) break block0;
                        this.towerTick = 1;
                        Util.mc.thePlayer.motionY = -0.0784000015258789;
                        break block0;
                    }
                    case 1: {
                        if (yState == 0 && this.isAirBelowForTower()) {
                            this.startY = Math.floor(Util.mc.thePlayer.posY);
                            this.towerTick = 2;
                            ++this.towerDelay;
                            Util.mc.thePlayer.motionY = 0.42f;
                            if (MovementUtils.isMoving()) {
                                MovementUtils.setSpeed(MovementUtils.getSpeed());
                                break block0;
                            }
                            MovementUtils.stop();
                            break block0;
                        }
                        this.resetTowerState();
                        break block0;
                    }
                    case 2: {
                        this.towerTick = 3;
                        Util.mc.thePlayer.motionY -= MathUtils.getRandom(0.00101, 0.00109);
                        break block0;
                    }
                    case 3: {
                        if (this.towerDelay >= 4) {
                            this.towerTick = 4;
                            this.towerDelay = 0;
                            break block0;
                        }
                        this.towerTick = 1;
                        Util.mc.thePlayer.motionY = 1.0 - Util.mc.thePlayer.posY % 1.0;
                        break block0;
                    }
                    case 4: {
                        this.towerTick = 5;
                        break block0;
                    }
                    case 5: {
                        if (!this.isAirBelowForTower()) {
                            this.towerTick = 0;
                            break block0;
                        }
                        this.towerTick = 1;
                        Util.mc.thePlayer.motionY -= 0.08;
                        Util.mc.thePlayer.motionY *= (double)0.98f;
                        Util.mc.thePlayer.motionY -= 0.08;
                        Util.mc.thePlayer.motionY *= (double)0.98f;
                        break block0;
                    }
                }
                this.resetTowerState();
            }
        }
    }

    private void resetTowerState() {
        this.towerTick = 0;
        this.towerDelay = 0;
    }

    private boolean isAirBelowForTower() {
        BlockPos below = new BlockPos(Util.mc.thePlayer).down();
        return PlayerUtils.blockRelativeToPlayer(0.0, -1.0, 0.0).isReplaceable(Util.mc.theWorld, below);
    }

    public boolean doesNotContainBlock(int down) {
        return PlayerUtils.blockRelativeToPlayer(this.offset.getX(), -down + this.offset.getY(), this.offset.getZ()).isReplaceable(Util.mc.theWorld, new BlockPos(Util.mc.thePlayer).down(down));
    }

    public static boolean isNearEdge() {
        double x = Util.mc.thePlayer.posX;
        double z = Util.mc.thePlayer.posZ;
        int y = (int)Math.floor(Util.mc.thePlayer.posY) - 2;
        double expand = 0.15;
        for (double dx = -expand; dx <= expand; dx += expand) {
            for (double dz = -expand; dz <= expand; dz += expand) {
                BlockPos pos;
                if (dx == 0.0 && dz == 0.0 || Util.mc.theWorld.getBlockState(pos = new BlockPos(x + dx, (double)y, z + dz)).getBlock().isFullBlock()) continue;
                return true;
            }
        }
        return false;
    }

    @Generated
    public int getTicksOnAir() {
        return this.ticksOnAir;
    }

    @Generated
    public void setTicksOnAir(int ticksOnAir) {
        this.ticksOnAir = ticksOnAir;
    }

    private static enum RayCast {
        None,
        Normal,
        Strict;

    }

    private static enum BlockCounter {
        Tenacity,
        None;

    }

    private static enum Mode {
        Normal("Normal"),
        Hypixel("Hypixel"),
        SlowTelly("Slow Telly"),
        FastTelly("Fast Telly"),
        Breezily("Breezily"),
        GodBridge("God Bridge");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    private static enum Rotations {
        Normal("Normal"),
        StaticYaw("Static Yaw");

        public String name;

        private Rotations(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    private static enum SearchAlgorithm {
        Normal,
        Secondary,
        Simple;

    }

    private static enum JumpMode {
        None,
        Normal,
        Dev;

    }

    private static enum SprintMode {
        Vanilla,
        Legit,
        None;

    }

    private static enum TowerMode {
        Vanilla,
        Dev,
        NCP,
        Telly,
        None;

    }
}

