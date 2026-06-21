/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.interfaces.menu.alt;

import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.interfaces.menu.alt.AltManagerGui;
import cc.advantage.interfaces.menu.alt.SessionChanger;
import cc.advantage.utils.client.BgUtils;
import cc.advantage.utils.render.AnimatedMenuBackground;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

public class GuiLogin
extends GuiScreen {
    private GuiTextField username;
    private final CustomFontRenderer titleFont;
    private final CustomFontRenderer buttonFont;
    private final int buttonWidth = 140;
    private final int buttonHeight = 25;
    private final int buttonSpacing = 8;
    private final long startTime = System.currentTimeMillis();
    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    public GuiLogin() {
        this.titleFont = FontUtils.getFont("advantage");
        this.buttonFont = FontUtils.getFont("advantage");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(this.mc);
        AnimatedMenuBackground.draw(this.width, this.height, this.startTime, BgUtils.getInstance().getCurrentIndex());
        int centerX = this.width / 2;
        int centerY = sr.getScaledHeight() / 2;
        int panelWidth = 220;
        int panelHeight = 174;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        int inputX = centerX - 70;
        int inputY = centerY - 34;
        Color accent = AnimatedMenuBackground.getAccentColor(this.startTime, 255);
        RenderUtils.drawRoundedRect(panelX - 3, panelY - 3, panelWidth + 6, panelHeight + 6, 9.0f, new Color(0, 0, 0, 75));
        RenderUtils.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 8.0f, new Color(7, 9, 28, 216));
        RenderUtils.drawRoundedRect(panelX, panelY, panelWidth, 2.0f, 1.0f, accent);
        this.titleFont.drawStringWithShadow("Cracked Login", centerX - this.titleFont.getStringWidth("Cracked Login") / 2, panelY + 22, accent.getRGB());
        this.fontRendererObj.drawStringWithShadow("usuario", inputX, inputY - 12, 13290216);
        RenderUtils.drawRoundedRect(inputX - 1, inputY - 1, 142.0f, 28.0f, 6.0f, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
        RenderUtils.drawRoundedRect(inputX, inputY, 140.0f, 26.0f, 6.0f, new Color(8, 12, 34, 220));
        if (this.username.getText().isEmpty()) {
            this.fontRendererObj.drawStringWithShadow("usuario", inputX + 10, inputY + 9, 0x777790);
        }
        this.username.drawTextBox();
        this.drawCustomButtons(mouseX, mouseY, centerX, centerY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCustomButtons(int mouseX, int mouseY, int centerX, int centerY) {
        int startX = centerX - 70;
        int startY = centerY + 10;
        this.drawButton(startX, startY, 140, 25, "login", mouseX, mouseY);
        this.drawButton(startX, startY + 25 + 8, 140, 25, "random", mouseX, mouseY);
        this.drawButton(startX, startY + 66, 140, 25, "cancel", mouseX, mouseY);
    }

    private void drawButton(int x, int y, int width, int height, String text, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        Color accent = AnimatedMenuBackground.getAccentColor(this.startTime, 255);
        Color bgColor = hovered ? new Color(Math.min(255, accent.getRed() + 24), Math.min(255, accent.getGreen() + 12), Math.min(255, accent.getBlue() + 28), 235) : new Color(12, 16, 42, 215);
        RenderUtils.drawRoundedRect(x, y, width, height, 6.0f, bgColor);
        RenderUtils.drawRoundedRect(x, y, width, 1.0f, 0.5f, hovered ? new Color(255, 255, 255, 95) : accent);
        int textX = x + (width - this.buttonFont.getStringWidth(text)) / 2;
        int textY = y + (height - this.buttonFont.getHeight()) / 2;
        this.buttonFont.drawString(text, textX, textY, 0xFFFFFF);
    }

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = this.width / 2;
        int centerY = sr.getScaledHeight() / 2;
        this.username = new GuiTextField(100, this.fontRendererObj, centerX - 70 + 10, centerY - 27, 120, 18);
        this.username.setFocused(true);
        this.username.setEnableBackgroundDrawing(false);
        this.username.setTextColor(0xFFFFFF);
        this.username.setDisabledTextColour(0xB8B8C8);
        Keyboard.enableRepeatEvents(true);
    }

    public static String generateRandomString() {
        int i;
        StringBuilder result = new StringBuilder();
        for (i = 0; i < 4; ++i) {
            result.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }
        for (i = 0; i < 4; ++i) {
            result.append(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));
        }
        return result.toString();
    }

    @Override
    protected void keyTyped(char character, int key) {
        try {
            super.keyTyped(character, key);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (character == '\t' && !this.username.isFocused()) {
            this.username.setFocused(true);
        }
        if (character == '\r') {
            this.handleLogin();
        }
        this.username.textboxKeyTyped(character, key);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        try {
            super.mouseClicked(mouseX, mouseY, button);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.username.mouseClicked(mouseX, mouseY, button);
        ScaledResolution sr = new ScaledResolution(this.mc);
        int centerX = this.width / 2;
        int centerY = sr.getScaledHeight() / 2;
        int startX = centerX - 70;
        int startY = centerY + 10;
        if (this.isMouseOverButton(mouseX, mouseY, startX, startY, 140, 25)) {
            this.handleLogin();
        } else if (this.isMouseOverButton(mouseX, mouseY, startX, startY + 25 + 8, 140, 25)) {
            String text = GuiLogin.generateRandomString();
            SessionChanger.getInstance().setUserOffline(text);
            this.saveAltToFile(text);
            this.mc.displayGuiScreen(new AltManagerGui());
        } else if (this.isMouseOverButton(mouseX, mouseY, startX, startY + 66, 140, 25)) {
            this.mc.displayGuiScreen(new AltManagerGui());
        }
    }

    private void handleLogin() {
        if (this.username.getText().equals("")) {
            this.mc.displayGuiScreen(new GuiLogin());
        } else {
            SessionChanger.getInstance().setUserOffline(this.username.getText());
            this.saveAltToFile(this.username.getText());
            this.mc.displayGuiScreen(new AltManagerGui());
        }
    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }

    @Override
    public void onGuiClosed() {
        this.mc.entityRenderer.loadEntityShader(null);
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        this.username.updateCursorCounter();
    }

    private void saveAltToFile(String sessionUsername) {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "Advantage");
        File file = new File(dir, "alts.txt");
        try (FileWriter fw = new FileWriter(file, true);
             PrintWriter out = new PrintWriter(fw);){
            out.println("cracked|" + sessionUsername);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

