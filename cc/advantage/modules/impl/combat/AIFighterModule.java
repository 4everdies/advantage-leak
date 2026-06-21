/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.combat;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.events.impl.world.TickEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.processes.TargetSelectionProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.MathUtils;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.PathFinderUtils;
import cc.advantage.utils.mc.RotationUtils;
import cc.advantage.utils.misc.MovementFix;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="AI Fighter", category=ModuleCategory.COMBAT)
public class AIFighterModule
extends Module {
    public static ModeProperty<TargetSelectionProcess.Mode> mode = new ModeProperty<TargetSelectionProcess.Mode>("Mode", TargetSelectionProcess.Mode.Adaptive);
    public static ModeProperty<TargetSelectionProcess.Entities> entities = new ModeProperty<TargetSelectionProcess.Entities>("Entities", TargetSelectionProcess.Entities.Optimal);
    public static ModeProperty<Rotations> rotations = new ModeProperty<Rotations>("Rotations", Rotations.Player);
    private final NumberProperty speed = new NumberProperty("Rotation Speed", 5.0, 0.0, 10.0, 1.0);
    public static final Property<Boolean> fix = new Property<Boolean>("Move Fix", true, () -> rotations.getValue() == Rotations.Server);
    public static final Property<Boolean> newCombat = new Property<Boolean>("New Combat Delays", false);
    private static final NumberProperty min = new NumberProperty("Min CPS", 9.0, () -> newCombat.getValue() == false, 0.0, 20.0, 0.5);
    private static final NumberProperty max = new NumberProperty("Max CPS", 13.0, () -> newCombat.getValue() == false, 0.0, 20.0, 0.5);
    private final NumberProperty switchSpeed = new NumberProperty("Switch Speed", 2.0, () -> mode.getValue() == TargetSelectionProcess.Mode.Switch, 0.0, 10.0, 1.0);
    public static NumberProperty seekRange = new NumberProperty("Seek Range", 20.0, 3.0, 100.0, 0.1);
    public static NumberProperty killRange = new NumberProperty("Kill Range", 3.0, 3.0, 6.0, 0.1);
    private final Property<Boolean> teams = new Property<Boolean>("Teams", false);
    private final Property<Boolean> renderPath = new Property<Boolean>("Render Path", true);
    public static EntityLivingBase target;
    List<Entity> targetList = new CopyOnWriteArrayList<Entity>();
    private List<Vec3> path;
    static long delay;
    private static final Timer attackTimer;
    static int elapsedTicks;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        this.setSuffix(((TargetSelectionProcess.Mode)((Object)((Object)mode.getValue()))).toString());
        TargetSelectionProcess.setMode((TargetSelectionProcess.Mode)((Object)((Object)mode.getValue())));
        TargetSelectionProcess.setEntities((TargetSelectionProcess.Entities)((Object)((Object)entities.getValue())));
        TargetSelectionProcess.setSeekRange(((Double)seekRange.getValue()).floatValue());
        TargetSelectionProcess.setDontTargetTeams(this.teams.getValue());
        TargetSelectionProcess.setSwitchTime(((Double)this.switchSpeed.getValue()).intValue());
        this.targetList = TargetSelectionProcess.getTargetList();
        target = TargetSelectionProcess.getTarget();
        Util.mc.gameSettings.keyBindForward.setPressed(target != null && !((double)Util.mc.thePlayer.getDistanceToEntity(target) <= (Double)killRange.getValue()));
        Util.mc.gameSettings.keyBindJump.setPressed(Util.mc.thePlayer.isCollidedHorizontally || Util.mc.thePlayer.isInWater());
        if (this.targetList.isEmpty()) {
            target = null;
            return;
        }
        if (target == null) {
            return;
        }
        Vector2f rotation = RotationUtils.calculate(target, mode.getValue() == TargetSelectionProcess.Mode.Adaptive, (Double)seekRange.getValue());
        if (rotations.getValue() == Rotations.Player) {
            float yawDiff = MathHelper.wrapAngleTo180_float(rotation.x - Util.mc.thePlayer.rotationYaw);
            float pitchDiff = rotation.y - Util.mc.thePlayer.rotationPitch;
            float maxRotationStep = (float)((Double)this.speed.getValue()).intValue() * 18.0f;
            yawDiff = MathHelper.clamp_float(yawDiff, -maxRotationStep, maxRotationStep);
            pitchDiff = MathHelper.clamp_float(pitchDiff, -maxRotationStep, maxRotationStep);
            Util.mc.thePlayer.rotationYaw += yawDiff;
            Util.mc.thePlayer.rotationPitch = MathHelper.clamp_float(Util.mc.thePlayer.rotationPitch + pitchDiff, -90.0f, 90.0f);
        } else {
            RotationProcess.setRotations(rotation, ((Double)this.speed.getValue()).intValue(), fix.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF);
        }
        if (this.renderPath.getValue().booleanValue() && (double)Util.mc.thePlayer.getDistanceToEntity(target) <= (Double)seekRange.getValue()) {
            this.path = PathFinderUtils.computePath(new Vec3(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ), new Vec3(AIFighterModule.target.posX, AIFighterModule.target.posY, AIFighterModule.target.posZ), true);
        }
        this.attack();
    };
    @EventLink
    public final Listener<TickEvent> tickEventListener = e -> ++elapsedTicks;
    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (!this.renderPath.getValue().booleanValue() || this.path == null || target == null) {
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

    private void attack() {
        if (target == null || !AIFighterModule.hitTimerDone() || (double)Util.mc.thePlayer.getDistanceToEntity(target) > (Double)killRange.getValue()) {
            return;
        }
        Util.mc.clickMouse();
    }

    private static boolean hitTimerDone() {
        boolean returnVal = false;
        if (!newCombat.getValue().booleanValue()) {
            if (attackTimer.hasTimeElapsed(delay, false)) {
                returnVal = true;
                attackTimer.reset();
                delay = (long)(1000.0 / MathUtils.getRandom(((Double)max.getValue()).floatValue(), Math.min(((Double)min.getValue()).floatValue(), ((Double)max.getValue()).floatValue() - 1.0f)));
            }
        } else if (elapsedTicks >= AIFighterModule.getNewCombatDelay()) {
            elapsedTicks = 0;
            returnVal = true;
        }
        return returnVal;
    }

    private static int getNewCombatDelay() {
        int toolDelay = 3;
        if (Util.mc.thePlayer.inventory.getCurrentItem() == null) {
            return toolDelay;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
            toolDelay = 12;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemTool) {
            toolDelay = 20;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemPickaxe) {
            toolDelay = 16;
        }
        if (Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemAxe) {
            toolDelay = 25;
        }
        return toolDelay;
    }

    @Override
    public void onEnable() {
        delay = (long)(1000.0 / MathUtils.getRandom(((Double)max.getValue()).floatValue(), Math.max(((Double)min.getValue()).floatValue(), ((Double)max.getValue()).floatValue() - 1.0f)));
        super.onEnable();
    }

    @Override
    public void onDisable() {
        target = null;
        this.targetList.clear();
        super.onDisable();
    }

    static {
        delay = 0L;
        attackTimer = new Timer();
        elapsedTicks = 0;
    }

    public static enum Rotations {
        Player,
        Server;

    }
}

