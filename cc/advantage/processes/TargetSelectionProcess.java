/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  lombok.NonNull
 */
package cc.advantage.processes;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.player.AttackEvent;
import cc.advantage.modules.impl.client.AntiBotModule;
import cc.advantage.modules.impl.client.MCFModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Generated;
import lombok.NonNull;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;

public class TargetSelectionProcess {
    private static EntityLivingBase target;
    private static List<Entity> targetList;
    private static final Timer switchTimer;
    private static Mode mode;
    private static Entities entities;
    private static boolean dontTargetTeams;
    private static float seekRange;
    private static int switchTime;
    private int targetIndex;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        targetList = this.getTargets();
        if (targetList.isEmpty()) {
            target = null;
            return;
        }
        this.selectTarget();
    };
    @EventLink
    public final Listener<AttackEvent> attackEventListener = event -> {
        event.target = target;
    };

    public TargetSelectionProcess() {
        mode = Mode.Adaptive;
        entities = Entities.Optimal;
        dontTargetTeams = false;
        seekRange = 4.2f;
        switchTime = 2;
    }

    private void selectTarget() {
        if (targetList.isEmpty()) {
            target = null;
            return;
        }
        switch (mode.ordinal()) {
            case 2: {
                target = (EntityLivingBase)targetList.get(0);
                break;
            }
            case 1: {
                if (this.targetIndex >= targetList.size()) {
                    this.targetIndex = 0;
                }
                if (switchTimer.hasTimeElapsed(switchTime * 100)) {
                    this.targetIndex = (this.targetIndex + 1) % targetList.size();
                    switchTimer.reset();
                }
                target = (EntityLivingBase)targetList.get(this.targetIndex);
                break;
            }
            case 0: {
                target = targetList.stream().min(Comparator.comparingDouble(e -> Util.mc.thePlayer.getDistanceToEntity((Entity)e))).orElse(null);
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected value: " + String.valueOf((Object)entities));
            }
        }
    }

    private List<Entity> getTargets() {
        return Util.mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityLivingBase).filter(entity -> entity != Util.mc.thePlayer).filter(entity -> !entity.isDead).filter(entity -> ((EntityLivingBase)entity).getHealth() > 0.0f).filter(entity -> Util.mc.thePlayer.getDistanceToEntity((Entity)entity) <= seekRange).filter(entity -> !AntiBotModule.botList.contains(entity)).filter(entity -> !MCFModule.excludedPlayers.contains(entity)).filter(this::isValidEntity).collect(Collectors.toList());
    }

    private boolean isValidEntity(Entity entity) {
        if (dontTargetTeams && TargetSelectionProcess.inTeam(Util.mc.thePlayer, entity)) {
            return false;
        }
        return switch (entities.ordinal()) {
            case 0 -> {
                if (entity instanceof EntityPlayer || entity instanceof EntityMob) {
                    yield true;
                }
                yield false;
            }
            case 1 -> entity instanceof EntityPlayer;
            case 2 -> true;
            default -> throw new IllegalStateException("Unexpected value: " + String.valueOf((Object)entities));
        };
    }

    private static boolean inTeam(@NonNull ICommandSender entity0, @NonNull ICommandSender entity1) {
        if (entity0 == null) {
            throw new NullPointerException("entity0 is marked non-null but is null");
        }
        if (entity1 == null) {
            throw new NullPointerException("entity1 is marked non-null but is null");
        }
        String s = "\u00a7" + TargetSelectionProcess.teamColor(entity0);
        return entity0.getDisplayName().getFormattedText().contains(s) && entity1.getDisplayName().getFormattedText().contains(s);
    }

    @NonNull
    private static String teamColor(@NonNull ICommandSender player) {
        if (player == null) {
            throw new NullPointerException("player is marked non-null but is null");
        }
        Matcher matcher = Pattern.compile("\u00a7(.).*\u00a7r").matcher(player.getDisplayName().getFormattedText());
        return matcher.find() ? matcher.group(1) : "f";
    }

    @Generated
    public static EntityLivingBase getTarget() {
        return target;
    }

    @Generated
    public static void setTarget(EntityLivingBase target) {
        TargetSelectionProcess.target = target;
    }

    @Generated
    public static List<Entity> getTargetList() {
        return targetList;
    }

    @Generated
    public static Mode getMode() {
        return mode;
    }

    @Generated
    public static void setMode(Mode mode) {
        TargetSelectionProcess.mode = mode;
    }

    @Generated
    public static Entities getEntities() {
        return entities;
    }

    @Generated
    public static void setEntities(Entities entities) {
        TargetSelectionProcess.entities = entities;
    }

    @Generated
    public static boolean isDontTargetTeams() {
        return dontTargetTeams;
    }

    @Generated
    public static void setDontTargetTeams(boolean dontTargetTeams) {
        TargetSelectionProcess.dontTargetTeams = dontTargetTeams;
    }

    @Generated
    public static float getSeekRange() {
        return seekRange;
    }

    @Generated
    public static void setSeekRange(float seekRange) {
        TargetSelectionProcess.seekRange = seekRange;
    }

    @Generated
    public static int getSwitchTime() {
        return switchTime;
    }

    @Generated
    public static void setSwitchTime(int switchTime) {
        TargetSelectionProcess.switchTime = switchTime;
    }

    static {
        targetList = new CopyOnWriteArrayList<Entity>();
        switchTimer = new Timer();
    }

    public static enum Mode {
        Adaptive,
        Switch,
        Single;

    }

    public static enum Entities {
        Optimal,
        Players,
        All;

    }
}

