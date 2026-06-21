/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.interfaces.menu.alt;

import cc.advantage.api.font.CustomFontRenderer;
import java.awt.Color;
import lombok.Generated;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

public class CustomTextBox
extends Gui {
    private final int id;
    private final CustomFontRenderer fontRendererInstance;
    public int xPosition;
    public int yPosition;
    private int width;
    private int height;
    private String text = "";
    private final int maxStringLength = 32;
    private boolean focused;
    private String placeholder = "";
    private boolean selectedAll;

    public CustomTextBox(int componentId, CustomFontRenderer fontRendererObj, int x, int y, int w, int h) {
        this.id = componentId;
        this.fontRendererInstance = fontRendererObj;
        this.xPosition = x;
        this.yPosition = y;
        this.width = w;
        this.height = h;
    }

    public void drawTextBox() {
        CustomTextBox.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, new Color(43, 43, 43).getRGB());
        CustomTextBox.drawRect(this.xPosition + 1, this.yPosition + 1, this.xPosition + this.width - 1, this.yPosition + this.height - 1, new Color(30, 30, 30).getRGB());
        boolean empty = this.text.isEmpty();
        String renderText = empty ? this.placeholder : this.text;
        int color = empty ? 0x777777 : 0xFFFFFF;
        this.fontRendererInstance.drawString(renderText + (this.focused && !empty && System.currentTimeMillis() / 500L % 2L == 0L ? "|" : ""), this.xPosition + 4, (float)this.yPosition + (float)this.height / 2.0f - (float)this.fontRendererInstance.getHeight() / 2.0f, color);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean bl = this.focused = mouseButton == 0 && mouseX >= this.xPosition && mouseX <= this.xPosition + this.width && mouseY >= this.yPosition && mouseY <= this.yPosition + this.height;
        if (!this.focused) {
            this.selectedAll = false;
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (!this.focused) {
            return;
        }
        if (keyCode == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            this.selectedAll = true;
            return;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            if (this.selectedAll) {
                this.text = "";
                this.selectedAll = false;
                return;
            }
            if (!this.text.isEmpty()) {
                this.text = this.text.substring(0, this.text.length() - 1);
            }
            return;
        }
        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
            this.focused = false;
            this.selectedAll = false;
            return;
        }
        if (typedChar >= ' ' && typedChar != '\u007f') {
            if (this.selectedAll) {
                this.text = "";
                this.selectedAll = false;
            }
            if (this.text.length() < 32) {
                this.text = this.text + typedChar;
            }
        }
    }

    public void setText(String text) {
        this.text = text.length() > 32 ? text.substring(0, 32) : text;
    }

    @Generated
    public int getWidth() {
        return this.width;
    }

    @Generated
    public int getHeight() {
        return this.height;
    }

    @Generated
    public void setWidth(int width) {
        this.width = width;
    }

    @Generated
    public void setHeight(int height) {
        this.height = height;
    }

    @Generated
    public String getText() {
        return this.text;
    }

    @Generated
    public boolean isFocused() {
        return this.focused;
    }

    @Generated
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Generated
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}

