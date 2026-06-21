/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.interfaces.menu.login;

import cc.advantage.Advantage;
import cc.advantage.interfaces.menu.main.CustomMainMenu;
import cc.advantage.security.HWIDUtil;
import cc.advantage.security.WebLogin;
import cc.advantage.utils.render.AnimatedMenuBackground;
import cc.advantage.utils.render.RenderUtils;
import java.awt.Color;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class GuiLogin
extends GuiScreen {
    private static final ResourceLocation ADVANTAGE_LOGO = new ResourceLocation("advantage/images/Advantage.png");
    private static final int PANEL_WIDTH = 264;
    private static final int PANEL_HEIGHT = 190;
    private static final int INPUT_WIDTH = 208;
    private static final int INPUT_HEIGHT = 30;
    private static final int LOGO_WIDTH = 94;
    private static final int LOGO_HEIGHT = 75;
    private GuiTextField usernameField;
    private String statusMessage = "";
    private final long startTime = System.currentTimeMillis();
    private int loginButtonX;
    private int loginButtonY;
    private int loginButtonWidth;
    private int loginButtonHeight;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelY = centerY - 95;
        int inputY = panelY + 112;
        this.usernameField = new GuiTextField(0, this.fontRendererObj, centerX - 92, inputY + 9, 184, 18);
        this.usernameField.setMaxStringLength(32);
        this.usernameField.setFocused(true);
        this.usernameField.setEnableBackgroundDrawing(false);
        this.usernameField.setTextColor(0xFFFFFF);
        this.usernameField.setDisabledTextColour(0xB8B8C8);
        this.loginButtonWidth = 208;
        this.loginButtonHeight = 26;
        this.loginButtonX = centerX - this.loginButtonWidth / 2;
        this.loginButtonY = panelY + 154;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        AnimatedMenuBackground.draw(this.width, this.height, this.startTime);
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 132;
        int panelY = centerY - 95;
        int inputX = centerX - 104;
        int inputY = panelY + 112;
        Color accent = AnimatedMenuBackground.getAccentColor(this.startTime, 255);
        RenderUtils.drawRoundedRect(panelX - 3, panelY - 3, 270.0f, 196.0f, 9.0f, new Color(0, 0, 0, 75));
        RenderUtils.drawRoundedRect(panelX, panelY, 264.0f, 190.0f, 8.0f, new Color(7, 9, 28, 216));
        RenderUtils.drawRoundedRect(panelX, panelY, 264.0f, 2.0f, 1.0f, accent);
        RenderUtils.drawImage(ADVANTAGE_LOGO, (float)centerX - 47.0f, panelY + 10, 94.0f, 75.0f);
        GuiLogin.drawCenteredString(this.fontRendererObj, "Welcome", centerX, panelY + 88, 0xB8B8D8);
        this.fontRendererObj.drawStringWithShadow("Username", inputX, inputY - 12, 13290216);
        RenderUtils.drawRoundedRect(inputX - 1, inputY - 1, 210.0f, 32.0f, 6.0f, new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 120));
        RenderUtils.drawRoundedRect(inputX, inputY, 208.0f, 30.0f, 6.0f, new Color(8, 12, 34, 220));
        if (this.usernameField.getText().isEmpty()) {
            this.fontRendererObj.drawStringWithShadow("Username", inputX + 12, inputY + 11, 0x777790);
        }
        this.usernameField.drawTextBox();
        this.drawLoginButton(mouseX, mouseY, accent);
        if (!this.statusMessage.isEmpty()) {
            GuiLogin.drawCenteredString(this.fontRendererObj, this.statusMessage, centerX, this.loginButtonY + 36, 16739481);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        this.usernameField.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_RETURN) {
            this.login();
            return;
        }
        this.usernameField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && this.isMouseOverLoginButton(mouseX, mouseY)) {
            this.login();
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void login() {
        String username = this.usernameField.getText().trim();
        if (username.isEmpty()) {
            this.statusMessage = "Please enter a username.";
            return;
        }
        this.statusMessage = "Connecting...";
        this.usernameField.setEnabled(false);
        new Thread(() -> {
            try {
                String hwid = HWIDUtil.getHWID();
                WebLogin.LoginResult result = WebLogin.login(username, hwid);
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if (result.success) {
                        Advantage.onLoginSuccess(username);
                        Minecraft.getMinecraft().displayGuiScreen(new CustomMainMenu());
                    } else {
                        this.statusMessage = result.message != null ? result.message : "Login failed";
                        this.usernameField.setEnabled(true);
                    }
                });
            }
            catch (Exception e) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    this.statusMessage = "Error: " + e.getMessage();
                    this.usernameField.setEnabled(true);
                });
            }
        }).start();
    }

    private void drawLoginButton(int mouseX, int mouseY, Color accent) {
        boolean hovered = this.isMouseOverLoginButton(mouseX, mouseY);
        Color buttonColor = hovered ? new Color(Math.min(255, accent.getRed() + 24), Math.min(255, accent.getGreen() + 12), Math.min(255, accent.getBlue() + 28), 235) : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 210);
        RenderUtils.drawRoundedRect(this.loginButtonX, this.loginButtonY, this.loginButtonWidth, this.loginButtonHeight, 6.0f, buttonColor);
        RenderUtils.drawRoundedRect(this.loginButtonX, this.loginButtonY, this.loginButtonWidth, 1.0f, 0.5f, new Color(255, 255, 255, hovered ? 90 : 55));
        GuiLogin.drawCenteredString(this.fontRendererObj, "Login", this.loginButtonX + this.loginButtonWidth / 2, this.loginButtonY + 9, 0xFFFFFF);
    }

    private boolean isMouseOverLoginButton(int mouseX, int mouseY) {
        return mouseX >= this.loginButtonX && mouseX <= this.loginButtonX + this.loginButtonWidth && mouseY >= this.loginButtonY && mouseY <= this.loginButtonY + this.loginButtonHeight;
    }
}

