/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.api.properties.impl.Representation;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.client.ClientSettingsModule;
import cc.advantage.modules.impl.client.ThemeModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import cc.advantage.utils.render.Translate;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

@ModuleInfo(label="Array List", category=ModuleCategory.VISUALS)
public final class ArrayListModule
extends Module {
    private final Property<Boolean> bg = new Property<Boolean>("Background", true);
    private final ModeProperty<BGColorMode> bgColor = new ModeProperty<BGColorMode>("Background Color", BGColorMode.Normal, this.bg::getValue);
    private final NumberProperty bgAlpha = new NumberProperty("Background Opacity", 100.0, this.bg::getValue, 1.0, 255.0, 1.0);
    private final Property<Boolean> outline = new Property<Boolean>("Outline", true);
    private final ModeProperty<LineMode> line = new ModeProperty<LineMode>("Line", LineMode.Off, () -> this.outline.getValue() == false);
    private final Property<Boolean> hideVisuals = new Property<Boolean>("Hide Visuals", false);
    private static final Property<Boolean> useMcFont = new Property<Boolean>("Use MC Font", false);
    private final Property<Boolean> noSpaces = new Property<Boolean>("No Spaces", false);
    private final NumberProperty characterSpacing = new NumberProperty("Char Spacing", 0.0, -5.0, 10.0, 1.0, Representation.INT);
    private final Property<Boolean> showSuffix = new Property<Boolean>("Show Suffix", true);
    private final ModeProperty<SuffixMode> suffixMode = new ModeProperty<SuffixMode>("Suffix Mode", SuffixMode.Space, this.showSuffix::getValue);
    private final Property<Boolean> lowercase = new Property<Boolean>("Lowercase", true);
    private final ModeProperty<ColorMode> colorMode = new ModeProperty<ColorMode>("Color Mode", ColorMode.Fade);
    private final NumberProperty offsetX = new NumberProperty("Offset X", 0.0, -50.0, 50.0, 1.0);
    private final NumberProperty offsetY = new NumberProperty("Offset Y", 0.0, -50.0, 50.0, 1.0);
    private final Property<Boolean> roundedBg = new Property<Boolean>("Rounded Background", false);
    private final NumberProperty roundRadius = new NumberProperty("Round Radius", 3.0, this.roundedBg::getValue, 0.0, 10.0, 0.1);
    private final NumberProperty roundedSpacing = new NumberProperty("Rounded Spacing", 1.0, this.roundedBg::getValue, 0.0, 5.0, 0.1);
    private static final Map<Module, String> displayLabelCache = new HashMap<Module, String>();
    private static List<Module> moduleCache;
    @EventLink
    public Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (moduleCache != null) {
            CustomFontRenderer fr = this.getActiveFont();
            if (fr == null) {
                return;
            }
            boolean mcFont = this.isMcFontActive();
            int previousCharOffset = fr.getCharOffset();
            if (!mcFont) {
                fr.setCharOffset(this.getCharacterSpacing());
            }
            for (Module module : moduleCache) {
                displayLabelCache.put(module, this.getDisplayLabel(module));
            }
            try {
                moduleCache.sort(new LengthComparator());
            }
            finally {
                if (!mcFont) {
                    fr.setCharOffset(previousCharOffset);
                }
            }
        }
    };
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> this.renderArrayList();
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> this.renderArrayList();

    private CustomFontRenderer getActiveFont() {
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        if (useMcFont.getValue().booleanValue()) {
            fr = FontUtils.getFont("mc");
        }
        return fr;
    }

    private boolean isMcFontActive() {
        return useMcFont.getValue() != false || this.getActiveFont() == FontUtils.getFont("mc");
    }

    private int getCharacterSpacing() {
        return ((Double)this.characterSpacing.getValue()).intValue();
    }

    private int getVisibleCharacterCount(String text) {
        if (text == null) {
            return 0;
        }
        int visibleCharacters = 0;
        boolean formatting = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (formatting) {
                formatting = false;
                continue;
            }
            if (c == '\u00a7') {
                formatting = true;
                continue;
            }
            ++visibleCharacters;
        }
        return visibleCharacters;
    }

    private int getTextWidth(CustomFontRenderer fr, String text) {
        if (text == null) {
            return 0;
        }
        if (this.isMcFontActive()) {
            return Util.mc.fontRendererObj.getStringWidth(text) + this.getVisibleCharacterCount(text) * this.getCharacterSpacing();
        }
        return fr == null ? 0 : fr.getStringWidth(text);
    }

    private float drawText(CustomFontRenderer fr, String text, float x, float y, int color) {
        if (text == null) {
            return x;
        }
        if (this.isMcFontActive()) {
            return this.drawMcText(text, x, y, color);
        }
        return fr.drawStringWithShadow(text, x, y, color);
    }

    private float drawMcText(String text, float x, float y, int color) {
        int spacing = this.getCharacterSpacing();
        if (spacing == 0) {
            return Util.mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
        }
        float cursorX = x;
        int currentColor = color;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '\u00a7' && i + 1 < text.length()) {
                char code;
                if ((code = Character.toLowerCase(text.charAt(++i))) == 'r') {
                    currentColor = color;
                    continue;
                }
                if ("0123456789abcdef".indexOf(code) < 0) continue;
                currentColor = Util.mc.fontRendererObj.getColorCode(code);
                continue;
            }
            Util.mc.fontRendererObj.drawStringWithShadow(String.valueOf(c), cursorX, y, currentColor);
            cursorX += (float)(Util.mc.fontRendererObj.getCharWidth(c) + spacing);
        }
        return cursorX;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderArrayList() {
        CustomFontRenderer fr = this.getActiveFont();
        ScaledResolution sr = new ScaledResolution(Util.mc);
        if (fr == null) {
            return;
        }
        int previousCharOffset = fr.getCharOffset();
        boolean mcFont = this.isMcFontActive();
        if (!mcFont) {
            fr.setCharOffset(this.getCharacterSpacing());
        }
        try {
            int lastVisibleModuleIndex;
            float screenX = (float)sr.getScaledWidth() - ((Double)this.offsetX.getValue()).floatValue();
            float startY = 2.0f + ((Double)this.offsetY.getValue()).floatValue();
            if (moduleCache == null) {
                this.updateModulePositions(sr);
            }
            float y = startY;
            float previousModuleWidth = -1.0f;
            ArrayList<Module> filteredModules = new ArrayList<Module>();
            for (Module module : moduleCache) {
                if (this.hideVisuals.getValue().booleanValue() && module.getCategory() == ModuleCategory.VISUALS) continue;
                filteredModules.add(module);
            }
            int moduleCacheSize = filteredModules.size();
            for (lastVisibleModuleIndex = moduleCacheSize - 1; lastVisibleModuleIndex > 0 && !((Module)filteredModules.get(lastVisibleModuleIndex)).isVisible(); --lastVisibleModuleIndex) {
            }
            int firstVisibleModuleIndex = -1;
            float spacing = this.roundedBg.getValue() != false ? ((Double)this.roundedSpacing.getValue()).floatValue() : 0.0f;
            int visibleModuleCount = 0;
            for (int i = 0; i < moduleCacheSize; ++i) {
                Module module = (Module)filteredModules.get(i);
                Translate translate = module.getTranslate();
                String name = displayLabelCache.get(module);
                float moduleWidth = this.getTextWidth(fr, name);
                boolean visible = module.isVisible();
                if (visible) {
                    if (firstVisibleModuleIndex == -1) {
                        firstVisibleModuleIndex = i;
                    }
                    translate.animate(screenX - moduleWidth - (float)(this.line.getValue() != LineMode.Off ? 2 : 1), y);
                    y += 12.0f + spacing;
                } else {
                    translate.animate(screenX, y);
                }
                double translateX = translate.getX();
                double translateY = translate.getY();
                if (!visible && !(translateX < (double)screenX)) continue;
                int visibleModuleIndex = visibleModuleCount;
                int aColor = this.getColorForModule(visibleModuleIndex);
                double top = translateY - 2.0;
                if (this.bg.getValue().booleanValue()) {
                    if (this.roundedBg.getValue().booleanValue()) {
                        RenderUtils.drawRoundedRect(translateX - 1.0, top, (double)(moduleWidth + 2.0f), 12.0, (Double)this.roundRadius.getValue(), this.getColorForBG());
                    } else {
                        Gui.drawRect(translateX - 1.0, translateY - 2.0, screenX, translateY + 10.0, this.getColorForBG().getRGB());
                    }
                }
                this.drawText(fr, name, (float)translateX - (!mcFont ? 0.5f : 0.0f), (float)translateY, aColor);
                if (this.outline.getValue().booleanValue() && !this.roundedBg.getValue().booleanValue()) {
                    Gui.drawRect(translateX - 2.0, translateY - 2.0, translateX - 1.0, translateY + 10.0, aColor);
                    double outlineTop = top - 1.0;
                    double outlineBottom = translateY + 10.0;
                    if (i != firstVisibleModuleIndex && moduleWidth - previousModuleWidth > 0.0f) {
                        Gui.drawRect(translateX - 2.0, outlineTop, screenX - previousModuleWidth - 3.0f, outlineTop + 1.0, aColor);
                    }
                    if (i != lastVisibleModuleIndex) {
                        String nextModuleName;
                        float nextModuleWidth;
                        Module nextModule = null;
                        int indexOffset = 1;
                        while (i + indexOffset <= lastVisibleModuleIndex && !(nextModule = (Module)filteredModules.get(i + indexOffset)).isVisible()) {
                            nextModule = null;
                            ++indexOffset;
                        }
                        if (nextModule != null && (double)(moduleWidth - (nextModuleWidth = (float)this.getTextWidth(fr, nextModuleName = displayLabelCache.get(nextModule)))) > 0.5) {
                            Gui.drawRect(translateX - 2.0, outlineBottom, screenX - nextModuleWidth - 3.0f, outlineBottom + 1.0, aColor);
                        }
                    } else {
                        Gui.drawRect(translateX - 2.0, outlineBottom, screenX, outlineBottom + 1.0, aColor);
                    }
                    if (i == firstVisibleModuleIndex) {
                        Gui.drawRect(screenX - 1.0f, startY - 2.0f, screenX, translateY + 10.0 + (double)spacing, aColor);
                    } else {
                        Module prevModule = null;
                        for (int j = i - 1; j >= 0; --j) {
                            if (!((Module)filteredModules.get(j)).isVisible()) continue;
                            prevModule = (Module)filteredModules.get(j);
                            break;
                        }
                        if (prevModule != null) {
                            double prevY = prevModule.getTranslate().getY();
                            Gui.drawRect(screenX - 1.0f, prevY + 10.0, screenX, translateY + 10.0 + (double)spacing, aColor);
                        }
                    }
                    if (i == firstVisibleModuleIndex) {
                        Gui.drawRect(translateX - 2.0, startY - 2.0f, screenX, startY - 1.0f, aColor);
                    }
                }
                if (this.line.getValue() != LineMode.Off) {
                    if (this.line.getValue() == LineMode.Rise) {
                        Gui.drawRect(screenX - 1.0f, translateY - 2.0, screenX, translateY + 8.0, aColor);
                    } else if (this.line.getValue() == LineMode.Right) {
                        if (i == firstVisibleModuleIndex) {
                            Gui.drawRect(screenX - 1.0f, startY - 2.0f, screenX, translateY + 10.0 + (double)spacing, aColor);
                        } else {
                            Module prevModule = null;
                            for (j = i - 1; j >= 0; --j) {
                                if (!((Module)filteredModules.get(j)).isVisible()) continue;
                                prevModule = (Module)filteredModules.get(j);
                                break;
                            }
                            if (prevModule != null) {
                                double prevY = prevModule.getTranslate().getY();
                                Gui.drawRect(screenX - 1.0f, prevY + 10.0, screenX, translateY + 10.0 + (double)spacing, aColor);
                            }
                        }
                    } else if (this.line.getValue() == LineMode.RightTop) {
                        if (i == firstVisibleModuleIndex) {
                            Gui.drawRect(screenX - 1.0f, startY - 2.0f, screenX, translateY + 10.0 + (double)spacing, aColor);
                        } else {
                            Module prevModule = null;
                            for (j = i - 1; j >= 0; --j) {
                                if (!((Module)filteredModules.get(j)).isVisible()) continue;
                                prevModule = (Module)filteredModules.get(j);
                                break;
                            }
                            if (prevModule != null) {
                                double prevY = prevModule.getTranslate().getY();
                                Gui.drawRect(screenX - 1.0f, prevY + 10.0, screenX, translateY + 10.0 + (double)spacing, aColor);
                            }
                        }
                        if (i == firstVisibleModuleIndex) {
                            Gui.drawRect(translateX - 2.0, startY - 2.0f, screenX, startY - 1.0f, aColor);
                        }
                    } else if (this.line.getValue() == LineMode.Left) {
                        Gui.drawRect(translateX - 2.0, translateY - 2.0, translateX - 1.0, translateY + 10.0, aColor);
                    } else if (this.line.getValue() == LineMode.Bottom) {
                        if (i == lastVisibleModuleIndex) {
                            Gui.drawRect(translateX - 1.0, translateY + 10.0, screenX, translateY + 11.0, aColor);
                        }
                    } else if (this.line.getValue() == LineMode.Top && i == firstVisibleModuleIndex) {
                        Gui.drawRect(translateX - 2.0, startY - 2.0f, screenX, startY - 1.0f, aColor);
                    }
                }
                previousModuleWidth = moduleWidth;
                ++visibleModuleCount;
            }
        }
        finally {
            if (!mcFont) {
                fr.setCharOffset(previousCharOffset);
            }
        }
    }

    private Color getColorForBG() {
        int alpha = ((Double)this.bgAlpha.getValue()).intValue();
        return switch (((BGColorMode)((Object)this.bgColor.getValue())).ordinal()) {
            default -> throw new IncompatibleClassChangeError();
            case 0 -> new Color(0, 0, 0, alpha);
            case 1 -> new Color(ColorProcess.getColor().getRed(), ColorProcess.getColor().getGreen(), ColorProcess.getColor().getBlue(), alpha);
            case 2 -> new Color(255, 255, 255, alpha);
        };
    }

    private int getColorForModule(int visibleModuleIndex) {
        int offset;
        int n = offset = this.colorMode.getValue() == ColorMode.Fade ? visibleModuleIndex * 75 : 75;
        if (ThemeModule.isThemeEnabled()) {
            return ThemeModule.getTheme().getAnimatedColor(offset).getRGB();
        }
        if (ClientSettingsModule.color.getValue() == ClientSettingsModule.Color.Astolfo) {
            return RenderUtils.astolfoColors(offset / 2, offset).getRGB();
        }
        if (ClientSettingsModule.color.getValue() == ClientSettingsModule.Color.Rainbow) {
            return RenderUtils.rainbowColors(offset / 2, offset).getRGB();
        }
        if (ClientSettingsModule.color.getValue() == ClientSettingsModule.Color.Exhibition) {
            float hue = (float)(System.currentTimeMillis() % 3000L) / 3000.0f;
            if (this.colorMode.getValue() == ColorMode.Fade) {
                hue += (float)visibleModuleIndex * 0.035f;
            }
            if (hue > 1.0f) {
                hue %= 1.0f;
            }
            return Color.getHSBColor(hue, 0.55f, 0.9f).getRGB();
        }
        return RenderUtils.interpolateColorsBackAndForth(15, offset, ColorProcess.colors.getFirst(), ColorProcess.colors.getSecond(), false).getRGB();
    }

    private String getDisplayLabel(Module m) {
        String label = m.getLabel();
        String suffix = m.getSuffix();
        if (this.noSpaces.getValue().booleanValue()) {
            label = label.replace(" ", "");
            if (suffix != null) {
                suffix = suffix.replace(" ", "");
            }
        }
        if (this.lowercase.getValue().booleanValue()) {
            label = label.toLowerCase();
            if (suffix != null) {
                suffix = suffix.toLowerCase();
            }
        }
        if (suffix != null && this.showSuffix.getValue().booleanValue()) {
            return switch (((SuffixMode)((Object)this.suffixMode.getValue())).ordinal()) {
                default -> throw new IncompatibleClassChangeError();
                case 0 -> label + " \u00a77" + suffix;
                case 1 -> label + " \u00a77- " + suffix;
                case 2 -> label + " \u00a77[" + suffix + "]";
                case 3 -> label + " \u00a77| " + suffix;
            };
        }
        return label;
    }

    private void updateModulePositions(ScaledResolution scaledResolution) {
        CustomFontRenderer fr = this.getActiveFont();
        if (fr == null) {
            return;
        }
        if (moduleCache == null) {
            moduleCache = new ArrayList<Module>(Advantage.INSTANCE.getModuleManager().getModules());
        }
        float y = 2.0f + ((Double)this.offsetY.getValue()).floatValue();
        float screenX = (float)scaledResolution.getScaledWidth() - ((Double)this.offsetX.getValue()).floatValue();
        float spacing = this.roundedBg.getValue() != false ? ((Double)this.roundedSpacing.getValue()).floatValue() : 0.0f;
        for (Module module : moduleCache) {
            if (this.hideVisuals.getValue().booleanValue() && module.getCategory() == ModuleCategory.VISUALS) continue;
            if (module.isEnabled()) {
                module.getTranslate().setX(screenX - (float)this.getTextWidth(fr, this.getDisplayLabel(module)) + 2.0f);
            } else {
                module.getTranslate().setX(screenX);
            }
            module.getTranslate().setY(y);
            if (!module.isEnabled()) continue;
            y += 12.0f + spacing;
        }
    }

    public static enum BGColorMode {
        Normal,
        Theme,
        White;

    }

    private static enum LineMode {
        Off("Off"),
        Rise("Rise"),
        Top("Top"),
        Right("Right"),
        RightTop("Right Top"),
        Left("Left"),
        Bottom("Bottom");

        public String name;

        private LineMode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static enum SuffixMode {
        Space("Space"),
        Dash("Dash"),
        Brackets("Brackets"),
        Pipe("Pipe");

        public String name;

        private SuffixMode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }

    public static enum ColorMode {
        Static,
        Fade;

    }

    private class LengthComparator
    extends ModuleComparator {
        private LengthComparator() {
        }

        @Override
        public int compare(Module o1, Module o2) {
            CustomFontRenderer fr = ArrayListModule.this.getActiveFont();
            return Float.compare(ArrayListModule.this.getTextWidth(fr, displayLabelCache.get(o2)), ArrayListModule.this.getTextWidth(fr, displayLabelCache.get(o1)));
        }
    }

    private static abstract class ModuleComparator
    implements Comparator<Module> {
        protected CustomFontRenderer fontRenderer;

        private ModuleComparator() {
        }

        @Override
        public abstract int compare(Module var1, Module var2);

        public CustomFontRenderer getFontRenderer() {
            return this.fontRenderer;
        }

        public void setFontRenderer(CustomFontRenderer fontRenderer) {
            this.fontRenderer = fontRenderer;
        }
    }
}

