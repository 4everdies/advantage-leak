/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import cc.advantage.api.font.CustomFontRenderer;
import java.util.HashMap;
import java.util.Map;

public class FontUtils {
    private static final Map<String, CustomFontRenderer> fontRegistry = new HashMap<String, CustomFontRenderer>();
    private static final Map<String, CustomFontRenderer> scaledFontCache = new HashMap<String, CustomFontRenderer>();
    private static String currentFont = "advantage";
    public static final String BUG = "a";
    public static final String LIST = "b";
    public static final String BOMB = "c";
    public static final String EYE = "d";
    public static final String PERSON = "e";
    public static final String WHEELCHAIR = "f";
    public static final String SCRIPT = "g";
    public static final String SKIP_LEFT = "h";
    public static final String PAUSE = "i";
    public static final String PLAY = "j";
    public static final String SKIP_RIGHT = "k";
    public static final String SHUFFLE = "l";
    public static final String INFO = "m";
    public static final String SETTINGS = "n";
    public static final String CHECKMARK = "o";
    public static final String XMARK = "p";
    public static final String TRASH = "q";
    public static final String WARNING = "r";
    public static final String FOLDER = "s";
    public static final String LOAD = "t";
    public static final String SAVE = "u";
    public static final String UPVOTE_OUTLINE = "v";
    public static final String UPVOTE = "w";
    public static final String DOWNVOTE_OUTLINE = "x";
    public static final String DOWNVOTE = "y";
    public static final String DROPDOWN_ARROW = "z";
    public static final String PIN = "s";
    public static final String EDIT = "A";
    public static final String SEARCH = "B";
    public static final String UPLOAD = "C";
    public static final String REFRESH = "D";
    public static final String ADD_FILE = "E";
    public static final String STAR_OUTLINE = "F";
    public static final String STAR = "G";

    public static void registerFont(String name, CustomFontRenderer font) {
        fontRegistry.put(name, font);
    }

    public static CustomFontRenderer getFont(String name) {
        return fontRegistry.get(name);
    }

    public static void setCurrentFont(String name) {
        if (fontRegistry.containsKey(name)) {
            currentFont = name;
        }
    }

    public static CustomFontRenderer getCurrentFont() {
        return FontUtils.getFont(currentFont);
    }

    public static void swapFonts(String name1, String name2) {
        CustomFontRenderer font1 = FontUtils.getFont(name1);
        CustomFontRenderer font2 = FontUtils.getFont(name2);
        if (font1 != null && font2 != null) {
            fontRegistry.put(name1, font2);
            fontRegistry.put(name2, font1);
            if (currentFont.equals(name1)) {
                currentFont = name2;
            } else if (currentFont.equals(name2)) {
                currentFont = name1;
            }
        }
    }

    public static void aliasFont(String aliasName, String sourceName) {
        CustomFontRenderer source = FontUtils.getFont(sourceName);
        if (source != null) {
            FontUtils.registerFont(aliasName, source);
        }
    }

    public static void rebindFont(String name, CustomFontRenderer newFont) {
        if (fontRegistry.containsKey(name)) {
            fontRegistry.put(name, newFont);
        }
    }

    public static CustomFontRenderer getScaledFont(String name, float scale) {
        String cacheKey = name + "|" + scale;
        if (scaledFontCache.containsKey(cacheKey)) {
            return scaledFontCache.get(cacheKey);
        }
        CustomFontRenderer original = FontUtils.getFont(name);
        if (original == null) {
            return null;
        }
        String fontName = original.getNameFontTTF();
        float originalSize = original.getFont().getSize();
        float newSize = originalSize * scale;
        CustomFontRenderer scaledFont = new CustomFontRenderer(fontName, newSize, 0, true, false);
        scaledFontCache.put(cacheKey, scaledFont);
        return scaledFont;
    }

    public static void clearScaledFontCache() {
        scaledFontCache.clear();
    }

    private static CustomFontRenderer createFont(String name, int size) {
        return new CustomFontRenderer(name, size, 0, true, false);
    }

    static {
        FontUtils.registerFont("small", FontUtils.createFont("advantage", 10));
        FontUtils.registerFont("advantage", FontUtils.createFont("advantage", 18));
        FontUtils.registerFont("bold", FontUtils.createFont("advantage-bold", 18));
        FontUtils.registerFont("semi-big", FontUtils.createFont("advantage", 32));
        FontUtils.registerFont("big", FontUtils.createFont("advantage-bold", 48));
        FontUtils.registerFont("noto", FontUtils.createFont("noto", 18));
        FontUtils.registerFont("arial", FontUtils.createFont("arial", 18));
        FontUtils.registerFont("apple", FontUtils.createFont("apple", 18));
        FontUtils.registerFont("sans", FontUtils.createFont("sans", 18));
        FontUtils.registerFont("convection", FontUtils.createFont("convection", 18));
        FontUtils.registerFont("icon", FontUtils.createFont("icon", 40));
        FontUtils.registerFont("verdana", FontUtils.createFont("verdana", 18));
        FontUtils.registerFont("sfui", FontUtils.createFont("sfui", 18));
        FontUtils.registerFont("intellij", FontUtils.createFont("intellij", 18));
        FontUtils.registerFont("tahoma", FontUtils.createFont("tahoma", 18));
        FontUtils.registerFont("mc", FontUtils.createFont("mc", 18));
    }
}

