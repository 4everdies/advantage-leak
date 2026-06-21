/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.processes;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.modules.impl.client.ClientSettingsModule;
import cc.advantage.modules.impl.client.ThemeModule;
import cc.advantage.utils.misc.Pair;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import lombok.Generated;

public class ColorProcess {
    private static final Pair<Color, Color> ADVANTAGE = Pair.of(new Color(54, 59, 181), new Color(98, 102, 217));
    private static final Pair<Color, Color> WHITE = Pair.of(new Color(255, 255, 255), new Color(155, 155, 155));
    private static final Pair<Color, Color> RED = Pair.of(new Color(255, 57, 57), new Color(168, 14, 14));
    private static final Pair<Color, Color> RUBY = Pair.of(new Color(255, 0, 0), new Color(100, 0, 0));
    private static final Pair<Color, Color> DARK_PURPLE = Pair.of(new Color(100, 0, 180), new Color(50, 0, 130));
    private static final Pair<Color, Color> PURPLE = Pair.of(new Color(199, 139, 255), new Color(132, 26, 236));
    private static final Pair<Color, Color> LAVENDER = Pair.of(new Color(194, 156, 255), new Color(131, 101, 182));
    private static final Pair<Color, Color> PINK = Pair.of(new Color(255, 90, 255), new Color(255, 205, 255));
    private static final Pair<Color, Color> HOT_PINK = Pair.of(new Color(255, 0, 255), new Color(154, 51, 154));
    private static final Pair<Color, Color> VAPORWAVE = Pair.of(new Color(180, 0, 180), new Color(0, 200, 255));
    private static final Pair<Color, Color> SUNSET = Pair.of(new Color(161, 82, 230), new Color(255, 104, 69));
    private static final Pair<Color, Color> TENACITY = Pair.of(new Color(236, 133, 209), new Color(28, 167, 222));
    private static final Pair<Color, Color> FDP = Pair.of(new Color(29, 116, 148), new Color(38, 180, 113));
    private static final Pair<Color, Color> RISE = Pair.of(new Color(71, 148, 253), new Color(71, 253, 160));
    private static Color color = new Color(255, 255, 255);
    public static Pair<Color, Color> colors = Pair.of(new Color(255, 255, 255), new Color(255, 255, 255));
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> {
        if (ThemeModule.isThemeEnabled()) {
            ThemeModule.Theme theme = ThemeModule.getTheme();
            colors = theme.getGradient();
            color = theme.getAnimatedColor(75);
            return;
        }
        switch ((ClientSettingsModule.Color)((Object)((Object)ClientSettingsModule.color.getValue()))) {
            case Rainbow: {
                Color rainbow = RenderUtils.rainbowColors(15, 75);
                colors = Pair.of(rainbow, rainbow);
                color = rainbow;
                break;
            }
            case Astolfo: {
                Color astolfo = RenderUtils.astolfoColors(15, 75);
                colors = Pair.of(astolfo, astolfo);
                color = astolfo;
                break;
            }
            case Exhibition: {
                float hue = (float)(System.currentTimeMillis() % 3000L) / 3000.0f;
                color = Color.getHSBColor(hue, 0.55f, 0.9f);
                colors = Pair.of(color, color);
                break;
            }
            case Advantage: {
                ColorProcess.setGradient(ADVANTAGE, 75);
                break;
            }
            case White: {
                ColorProcess.setGradient(WHITE, 75);
                break;
            }
            case Red: {
                ColorProcess.setGradient(RED, 75);
                break;
            }
            case Ruby: {
                ColorProcess.setGradient(RUBY, 75);
                break;
            }
            case DarkPurple: {
                ColorProcess.setGradient(DARK_PURPLE, 75);
                break;
            }
            case Purple: {
                ColorProcess.setGradient(PURPLE, 75);
                break;
            }
            case Lavender: {
                ColorProcess.setGradient(LAVENDER, 75);
                break;
            }
            case Pink: {
                ColorProcess.setGradient(PINK, 75);
                break;
            }
            case HotPink: {
                ColorProcess.setGradient(HOT_PINK, 75);
                break;
            }
            case Vaporwave: {
                ColorProcess.setGradient(VAPORWAVE, 75);
                break;
            }
            case Sunset: {
                ColorProcess.setGradient(SUNSET, 20);
                break;
            }
            case Tenacity: {
                ColorProcess.setGradient(TENACITY, 75);
                break;
            }
            case FDP: {
                ColorProcess.setGradient(FDP, 75);
                break;
            }
            case Rise: {
                ColorProcess.setGradient(RISE, 75);
            }
        }
    };

    private static void setGradient(Pair<Color, Color> gradient, int index) {
        colors = gradient;
        color = RenderUtils.interpolateColorsBackAndForth(15, index, gradient.getFirst(), gradient.getSecond(), false);
    }

    @Generated
    public static Color getColor() {
        return color;
    }

    @Generated
    public static Pair<Color, Color> getColors() {
        return colors;
    }
}

