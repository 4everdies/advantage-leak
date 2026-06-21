/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

@ModuleInfo(label="Scoreboard", category=ModuleCategory.VISUALS)
public class ScoreboardModule
extends Module {
    public static ModeProperty<Mode> scoreboardStyle = new ModeProperty<Mode>("Scoreboard Style", Mode.Left);
    public static Property<Boolean> mcFont = new Property<Boolean>("Minecraft Font", false);
    private boolean renderedThisFrame = false;
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> {
        this.renderedThisFrame = false;
    };
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> {
        ScoreObjective scoreobjective1;
        int i1;
        if (!this.isEnabled()) {
            return;
        }
        if (this.renderedThisFrame) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }
        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective scoreobjective = null;
        ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(mc.thePlayer.getName());
        if (scoreplayerteam != null && (i1 = scoreplayerteam.getChatFormat().getColorIndex()) >= 0) {
            scoreobjective = scoreboard.getObjectiveInDisplaySlot(3 + i1);
        }
        ScoreObjective scoreObjective = scoreobjective1 = scoreobjective != null ? scoreobjective : scoreboard.getObjectiveInDisplaySlot(1);
        if (scoreobjective1 != null) {
            mc.ingameGUI.renderAdvantageScoreboard(scoreobjective1, new ScaledResolution(mc));
            this.renderedThisFrame = true;
        }
    };

    public static enum Mode {
        Vanilla("Vanilla"),
        VanillaOffset("Vanilla Offset"),
        Left("Left"),
        LeftOffset("Left Offset");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

