/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.font;

import cc.advantage.api.font.CustomFont;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class CustomFontRenderer
extends CustomFont {
    protected CustomFont.CharData[] boldChars = new CustomFont.CharData[256];
    protected CustomFont.CharData[] italicChars = new CustomFont.CharData[256];
    protected CustomFont.CharData[] boldItalicChars = new CustomFont.CharData[256];
    public int FONT_HEIGHT = 9;
    private final int[] colorCode = new int[32];
    private boolean useMCustomFont = false;
    String nameFontTTF;
    protected DynamicTexture texBold;
    protected DynamicTexture texItalic;
    protected DynamicTexture texItalicBold;

    public CustomFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        this.setupMinecraftColorcodes();
    }

    public CustomFontRenderer(String NameFontTTF, float size, int fonttype, boolean antiAlias, boolean fractionalMetrics) {
        super(CustomFontRenderer.getFontFromTTF(new ResourceLocation("advantage/fonts/" + NameFontTTF + ".ttf"), size, fonttype), antiAlias, fractionalMetrics);
        this.nameFontTTF = NameFontTTF;
        this.useMCustomFont = NameFontTTF.equalsIgnoreCase("mc");
        this.setupMinecraftColorcodes();
    }

    public String getNameFontTTF() {
        return this.nameFontTTF;
    }

    public float drawString(String text, float x, float y, int color) {
        if (this.useMCustomFont) {
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, false);
        }
        return this.drawString(text, x, y, color, false);
    }

    public float drawString(String text, double x, double y, int color) {
        if (this.useMCustomFont) {
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, (float)x, (float)y, color, false);
        }
        return this.drawString(text, x, y, color, false);
    }

    public float drawStringWithShadow(String text, float x, float y, int color) {
        if (this.useMCustomFont) {
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, true);
        }
        float shadowWidth = this.drawString(text, x + 1.0f, y + 1.0f, color, true);
        return Math.max(shadowWidth, this.drawString(text, x, y, color, false));
    }

    public float drawStringWithShadow(String text, double x, double y, int color) {
        if (this.useMCustomFont) {
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, (float)x, (float)y, color, true);
        }
        float shadowWidth = this.drawString(text, x + 1.0, y + 1.0, color, true);
        return Math.max(shadowWidth, this.drawString(text, x, y, color, false));
    }

    public float drawCenteredString(String text, float x, float y, int color) {
        if (this.useMCustomFont) {
            int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, x - (float)width / 2.0f, y, color, false);
        }
        return this.drawString(text, x - (float)(this.getStringWidth(text) / 2), y, color);
    }

    public float drawCenteredString(String text, double x, double y, int color) {
        if (this.useMCustomFont) {
            int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, (float)(x - (double)((float)width / 2.0f)), (float)y, color, false);
        }
        return this.drawString(text, x - (double)(this.getStringWidth(text) / 2), y, color);
    }

    public float drawCenteredStringWithShadow(String text, float x, float y, int color) {
        if (this.useMCustomFont) {
            int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
            return Minecraft.getMinecraft().fontRendererObj.drawString(text, x - (float)width / 2.0f, y, color, true);
        }
        float shadowWidth = this.drawString(text, (double)(x - (float)(this.getStringWidth(text) / 2)) + 0.45, (double)y + 0.5, color, true);
        return this.drawString(text, x - (float)(this.getStringWidth(text) / 2), y, color);
    }

    public void drawStringWithOutline(String text, double x, double y, int color) {
        this.drawString(text, x - 0.5, y, 0);
        this.drawString(text, x + 0.5, y, 0);
        this.drawString(text, x, y - 0.5, 0);
        this.drawString(text, x, y + 0.5, 0);
        this.drawString(text, x, y, color);
    }

    public void drawCenteredStringWithOutline(String text, double x, double y, int color) {
        this.drawCenteredString(text, x - 0.5, y, 0);
        this.drawCenteredString(text, x + 0.5, y, 0);
        this.drawCenteredString(text, x, y - 0.5, 0);
        this.drawCenteredString(text, x, y + 0.5, 0);
        this.drawCenteredString(text, x, y, color);
    }

    public float drawCenteredStringWithShadow(String text, double x, double y, int color) {
        float shadowWidth = this.drawString(text, x - (double)(this.getStringWidth(text) / 2) + 0.45, y + 0.5, color, true);
        return this.drawString(text, x - (double)(this.getStringWidth(text) / 2), y, color);
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {
        Minecraft mc = Minecraft.getMinecraft();
        x -= 1.0;
        if (text == null) {
            return 0.0f;
        }
        if (color == 0x20FFFFFF) {
            color = 0xFFFFFF;
        }
        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }
        if (shadow) {
            color = (color & 0xFCFCFC) >> 2 | color & new Color(20, 20, 20, 200).getRGB();
        }
        CustomFont.CharData[] currentData = this.charData;
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        boolean randomCase = false;
        boolean bold = false;
        boolean italic = false;
        boolean strikethrough = false;
        boolean underline = false;
        boolean render = true;
        x *= 2.0;
        y = (y - 2.0) * 2.0;
        if (render) {
            GL11.glPushMatrix();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.color((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, alpha);
            int size = text.length();
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(this.tex.getGlTextureId());
            GL11.glBindTexture(3553, this.tex.getGlTextureId());
            for (int i = 0; i < size; ++i) {
                char character = text.charAt(i);
                if (String.valueOf(character).equals("\u00a7") && i < size) {
                    int colorIndex = 21;
                    try {
                        colorIndex = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (colorIndex < 16) {
                        bold = false;
                        italic = false;
                        randomCase = false;
                        underline = false;
                        strikethrough = false;
                        GlStateManager.bindTexture(this.tex.getGlTextureId());
                        currentData = this.charData;
                        if (colorIndex < 0 || colorIndex > 15) {
                            colorIndex = 15;
                        }
                        if (shadow) {
                            colorIndex += 16;
                        }
                        int colorcode = this.colorCode[colorIndex];
                        GlStateManager.color((float)(colorcode >> 16 & 0xFF) / 255.0f, (float)(colorcode >> 8 & 0xFF) / 255.0f, (float)(colorcode & 0xFF) / 255.0f, alpha);
                    } else if (colorIndex == 16) {
                        randomCase = true;
                    } else if (colorIndex == 17) {
                        bold = true;
                        currentData = italic ? this.charData : this.charData;
                    } else if (colorIndex == 18) {
                        strikethrough = true;
                    } else if (colorIndex == 19) {
                        underline = true;
                    } else if (colorIndex == 20) {
                        italic = true;
                        currentData = bold ? this.charData : this.charData;
                    } else if (colorIndex == 21) {
                        bold = false;
                        italic = false;
                        randomCase = false;
                        underline = false;
                        strikethrough = false;
                        GlStateManager.color((float)(color >> 16 & 0xFF) / 255.0f, (float)(color >> 8 & 0xFF) / 255.0f, (float)(color & 0xFF) / 255.0f, alpha);
                        GlStateManager.bindTexture(this.tex.getGlTextureId());
                        currentData = this.charData;
                    }
                    ++i;
                    continue;
                }
                if (character >= currentData.length || character < '\u0000') continue;
                GL11.glBegin(4);
                this.drawChar(currentData, character, (float)x, (float)y);
                GL11.glEnd();
                if (strikethrough) {
                    this.drawLine(x, y + (double)(currentData[character].height / 2), x + (double)currentData[character].width - 8.0, y + (double)(currentData[character].height / 2), 1.0f);
                }
                if (underline) {
                    this.drawLine(x, y + (double)currentData[character].height - 2.0, x + (double)currentData[character].width - 8.0, y + (double)currentData[character].height - 2.0, 1.0f);
                }
                x += (double)(currentData[character].width - 8 + this.charOffset);
            }
            GL11.glHint(3155, 4352);
            GL11.glPopMatrix();
        }
        return (float)x / 2.0f;
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        }
        if (this.useMCustomFont) {
            return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
        }
        int width = 0;
        CustomFont.CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();
        for (int i = 0; i < size; ++i) {
            char character = text.charAt(i);
            if (String.valueOf(character).equals("\u00a7") && i < size) {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) {
                    bold = true;
                    currentData = italic ? this.boldItalicChars : this.boldChars;
                } else if (colorIndex == 20) {
                    italic = true;
                    currentData = bold ? this.boldItalicChars : this.italicChars;
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }
                ++i;
                continue;
            }
            if (character >= currentData.length || character < '\u0000') continue;
            width += currentData[character].width - 8 + this.charOffset;
        }
        return width / 2;
    }

    public int getStringWidthCust(String text) {
        if (text == null) {
            return 0;
        }
        int width = 0;
        CustomFont.CharData[] currentData = this.charData;
        boolean bold = false;
        boolean italic = false;
        int size = text.length();
        for (int i = 0; i < size; ++i) {
            char character = text.charAt(i);
            if (String.valueOf(character).equals("\ufffd") && i < size) {
                int colorIndex = "0123456789abcdefklmnor".indexOf(character);
                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                } else if (colorIndex == 17) {
                    bold = true;
                    currentData = italic ? this.boldItalicChars : this.boldChars;
                } else if (colorIndex == 20) {
                    italic = true;
                    currentData = bold ? this.boldItalicChars : this.italicChars;
                } else if (colorIndex == 21) {
                    bold = false;
                    italic = false;
                    currentData = this.charData;
                }
                ++i;
                continue;
            }
            if (character >= currentData.length || character < '\u0000') continue;
            width += currentData[character].width - 8 + this.charOffset;
        }
        return (width - this.charOffset) / 2;
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
    }

    @Override
    public void setAntiAlias(boolean antiAlias) {
        super.setAntiAlias(antiAlias);
    }

    @Override
    public void setFractionalMetrics(boolean fractionalMetrics) {
        super.setFractionalMetrics(fractionalMetrics);
    }

    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    public List<String> wrapWords(String text, double width) {
        ArrayList<String> finalWords = new ArrayList<String>();
        if ((double)this.getStringWidth(text) > width) {
            String[] words = text.split(" ");
            Object currentWord = "";
            char lastColorCode = '\uffff';
            for (String word : words) {
                for (int i = 0; i < word.toCharArray().length; ++i) {
                    char c = word.toCharArray()[i];
                    if (!String.valueOf(c).equals("\ufffd") || i >= word.toCharArray().length - 1) continue;
                    lastColorCode = word.toCharArray()[i + 1];
                }
                if ((double)this.getStringWidth((String)currentWord + word + " ") < width) {
                    currentWord = (String)currentWord + word + " ";
                    continue;
                }
                finalWords.add((String)currentWord);
                currentWord = lastColorCode + word + " ";
            }
            if (((String)currentWord).length() > 0) {
                if ((double)this.getStringWidth((String)currentWord) < width) {
                    finalWords.add(lastColorCode + (String)currentWord + " ");
                    currentWord = "";
                } else {
                    for (String s : this.formatString((String)currentWord, width)) {
                        finalWords.add(s);
                    }
                }
            }
        } else {
            finalWords.add(text);
        }
        return finalWords;
    }

    public List<String> formatString(String string, double width) {
        ArrayList<String> finalWords = new ArrayList<String>();
        Object currentWord = "";
        char lastColorCode = '\uffff';
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (String.valueOf(c).equals("\ufffd") && i < chars.length - 1) {
                lastColorCode = chars[i + 1];
            }
            if ((double)this.getStringWidth((String)currentWord + c) < width) {
                currentWord = (String)currentWord + c;
                continue;
            }
            finalWords.add((String)currentWord);
            currentWord = lastColorCode + String.valueOf(c);
        }
        if (((String)currentWord).length() > 0) {
            finalWords.add((String)currentWord);
        }
        return finalWords;
    }

    private void setupMinecraftColorcodes() {
        for (int index = 0; index < 32; ++index) {
            int noClue = (index >> 3 & 1) * 85;
            int red = (index >> 2 & 1) * 170 + noClue;
            int green = (index >> 1 & 1) * 170 + noClue;
            int blue = (index >> 0 & 1) * 170 + noClue;
            if (index == 6) {
                red += 85;
            }
            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCode[index] = (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
        }
    }

    public static Font getFontFromTTF(ResourceLocation fontLocation, float fontSize, int fontType) {
        Font output = null;
        try {
            output = Font.createFont(fontType, Minecraft.getMinecraft().getResourceManager().getResource(fontLocation).getInputStream());
            output = output.deriveFont(fontSize);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new Font("Default", fontType, (int)fontSize);
        }
        return output;
    }

    public float getMiddleOfBox(float height) {
        return height / 2.0f - (float)this.getHeight() / 2.0f;
    }
}

