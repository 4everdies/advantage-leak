/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.Advantage;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.misc.Pair;
import cc.advantage.utils.render.RenderUtils;
import java.awt.Color;

@ModuleInfo(label="Theme", category=ModuleCategory.CLIENT, description="Changes the client color theme")
public final class ThemeModule
extends Module {
    public static final ModeProperty<Theme> theme = new ModeProperty<Theme>("Theme", Theme.Advantage);

    public ThemeModule() {
        this.toggle();
    }

    public static boolean isThemeEnabled() {
        if (Advantage.INSTANCE.getModuleManager() == null) {
            return false;
        }
        ThemeModule module = Advantage.INSTANCE.getModuleManager().getModule(ThemeModule.class);
        return module != null && module.isEnabled();
    }

    public static Theme getTheme() {
        return (Theme)((Object)theme.getValue());
    }

    public static enum Theme {
        Advantage("Advantage", new Color(136, 72, 235), new Color(10, 25, 76), new Color(168, 38, 116)),
        Red("Red", new Color(255, 112, 112), new Color(190, 42, 42), new Color(105, 12, 20)),
        Blue("Blue", new Color(84, 180, 255), new Color(35, 93, 188), new Color(8, 27, 90)),
        Purple("Purple", new Color(205, 148, 255), new Color(122, 45, 190), new Color(50, 13, 102));

        private final String name;
        private final Color light;
        private final Color semiDark;
        private final Color dark;
        private final Pair<Color, Color> gradient;

        private Theme(String name, Color light, Color semiDark, Color dark) {
            this.name = name;
            this.light = light;
            this.semiDark = semiDark;
            this.dark = dark;
            this.gradient = Pair.of(light, dark);
        }

        public Pair<Color, Color> getGradient() {
            return this.gradient;
        }

        public Color getAnimatedColor(int index) {
            int angle = (int)((System.currentTimeMillis() / 15L + (long)index) % 360L);
            angle = (angle >= 180 ? 360 - angle : angle) * 2;
            float phase = (float)angle / 360.0f;
            if (phase < 0.5f) {
                return RenderUtils.interpolateColorC(this.light, this.semiDark, phase * 2.0f);
            }
            return RenderUtils.interpolateColorC(this.semiDark, this.dark, (phase - 0.5f) * 2.0f);
        }

        public String toString() {
            return this.name;
        }
    }
}

