/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.interfaces.menu.alt;

import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.interfaces.menu.alt.CustomTextBox;
import cc.advantage.interfaces.menu.alt.SessionChanger;
import cc.advantage.interfaces.menu.alt.microsoft.MicrosoftOAuthTranslation;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.client.BgUtils;
import cc.advantage.utils.render.AnimatedMenuBackground;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.GlUtils;
import cc.advantage.utils.render.RenderUtils;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class AltManagerGui
extends GuiScreen {
    private int INFO_HEIGHT;
    private int LOGIN_WIDTH;
    private int LOGIN_HEIGHT;
    private int LOGIN_Y;
    private int BOX_X;
    private int BOX_Y;
    private int BOX_WIDTH;
    private static final int BOX_HEIGHT = 300;
    private static final int ENTRY_PADDING = 2;
    private int ENTRY_HEIGHT;
    private static final int TOP_BOX_HEIGHT = 40;
    private static final int SCROLLBAR_WIDTH = 4;
    private static final int PADDING = 6;
    private static int BOX_ACCENT_SIZE = 1;
    private static final String NUMBERS = "0123456789";
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private final ArrayList<Integer> selectedAlts = new ArrayList();
    private CustomTextBox username;
    private CustomTextBox password;
    private ArrayList<String> alts = new ArrayList();
    private int scrollOffset = 0;
    private boolean draggingScrollbar = false;
    private int dragStartY;
    private int scrollStart;
    private String statusString;
    private boolean isLoggingIn = false;
    private final CustomFontRenderer titleFont;
    private final CustomFontRenderer buttonFont;
    private final CustomFontRenderer infoFont;
    private final CustomFontRenderer altFont;
    private final long startTime;
    private int buttonWidth;
    private int buttonHeight;
    private final int buttonSpacing = 8;
    private final Map<String, ResourceLocation> headCache = new HashMap<String, ResourceLocation>();
    private final Map<String, Boolean> headLoading = new HashMap<String, Boolean>();
    private final Map<String, Integer> headTries = new HashMap<String, Integer>();
    private final ResourceLocation placeholderHead = new ResourceLocation("advantage/images/Steve.png");

    public AltManagerGui() {
        this.startTime = System.currentTimeMillis();
        this.titleFont = FontUtils.getFont("semi-big");
        this.buttonFont = FontUtils.getFont("advantage");
        this.altFont = FontUtils.getFont("advantage");
        this.infoFont = FontUtils.getFont("small");
    }

    @Override
    public void initGui() {
        this.alts.clear();
        this.loadAltsFromFile();
        this.selectedAlts.clear();
        this.buttonList.clear();
        this.username = new CustomTextBox(0, this.altFont, 0, 0, 0, 20);
        this.password = new CustomTextBox(0, this.altFont, 0, 0, 0, 20);
        super.initGui();
    }

    private void loadAltsFromFile() {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "Advantage");
        File file = new File(dir, "alts.txt");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file));){
            String line;
            while ((line = br.readLine()) != null) {
                if ((line = line.trim()).isEmpty() || !line.startsWith("cracked|") && !line.startsWith("microsoft|") && !line.startsWith("microsoftOAuth|")) continue;
                this.alts.add(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAltsToFile() {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "Advantage");
        File file = new File(dir, "alts.txt");
        try (PrintWriter out = new PrintWriter(file);){
            for (String alt : this.alts) {
                out.println(alt);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.BOX_WIDTH = (int)((double)this.width * 0.6);
        this.BOX_X = this.width - this.BOX_WIDTH - 6 - BOX_ACCENT_SIZE;
        this.BOX_Y = 6 + BOX_ACCENT_SIZE;
        this.LOGIN_HEIGHT = (int)((double)this.height * 0.4) - 12;
        this.LOGIN_Y = this.height / 2 - this.LOGIN_HEIGHT / 2;
        this.LOGIN_WIDTH = this.width - this.BOX_WIDTH - 18;
        this.INFO_HEIGHT = (this.height - this.LOGIN_HEIGHT) / 2 - 12;
        this.buttonWidth = (this.LOGIN_WIDTH - 12 - 16) / 3;
        this.buttonHeight = (int)((double)this.LOGIN_HEIGHT * 0.15);
        int titleBottom = this.LOGIN_Y + 12 + this.titleFont.getHeight();
        int buttonsTop = this.LOGIN_Y + this.LOGIN_HEIGHT - this.buttonHeight - 12;
        int textFeildHeight = (buttonsTop - titleBottom - 6) / 3;
        this.ENTRY_HEIGHT = 36;
        this.username.setPlaceholder("Username");
        this.username.xPosition = 12;
        this.username.yPosition = this.LOGIN_Y + 12 + this.titleFont.getHeight();
        this.username.setWidth(this.LOGIN_WIDTH - 12);
        this.username.setHeight(textFeildHeight);
        this.password.setPlaceholder("Password");
        this.password.xPosition = 12;
        this.password.yPosition = this.LOGIN_Y + 18 + this.titleFont.getHeight() + textFeildHeight;
        this.password.setWidth(this.LOGIN_WIDTH - 12);
        this.password.setHeight(textFeildHeight);
        GlStateManager.disableAlpha();
        AnimatedMenuBackground.draw(this.width, this.height, this.startTime, BgUtils.getInstance().getCurrentIndex());
        GlStateManager.enableAlpha();
        Gui.drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, 130).getRGB());
        RenderUtils.drawRect(this.BOX_X + this.BOX_WIDTH, this.BOX_Y - BOX_ACCENT_SIZE, BOX_ACCENT_SIZE, 13.0f, this.getHueColorAt(this.BOX_X + this.BOX_WIDTH - this.BOX_WIDTH / 3, this.BOX_Y - 2));
        RenderUtils.drawRect((float)(this.BOX_X + this.BOX_WIDTH) - (float)this.BOX_WIDTH / 3.0f, this.BOX_Y - BOX_ACCENT_SIZE, (float)this.BOX_WIDTH / 3.0f, BOX_ACCENT_SIZE, this.getHueColorAt(this.BOX_X + this.BOX_WIDTH - this.BOX_WIDTH / 3, this.BOX_Y - 2));
        RenderUtils.drawRect(this.BOX_X, this.BOX_Y, this.BOX_WIDTH, 40.0f, new Color(22, 22, 22, 140));
        this.altFont.drawStringWithShadow("Current User:", this.BOX_X + 8, this.BOX_Y + 6, 0xFFFFFF);
        String currentUser = Minecraft.getMinecraft().getSession().getUsername();
        this.altFont.drawStringWithShadow(currentUser, this.BOX_X + 8, this.BOX_Y + 20, ColorProcess.getColor().getRGB());
        this.drawAltSwitcher(mouseX, mouseY);
        this.drawLoginBox(mouseX, mouseY);
        this.drawInfoBoxes(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLoginBox(int mouseX, int mouseY) {
        RenderUtils.drawRect(6.0f, (float)this.LOGIN_Y + (float)this.LOGIN_HEIGHT / 4.0f, BOX_ACCENT_SIZE, (float)this.LOGIN_HEIGHT / 2.0f, this.getHueColorAt(6 - BOX_ACCENT_SIZE, this.LOGIN_Y + this.LOGIN_HEIGHT / 2));
        RenderUtils.drawRect(6 + BOX_ACCENT_SIZE, this.LOGIN_Y, this.LOGIN_WIDTH, this.LOGIN_HEIGHT, new Color(22, 22, 22, 140));
        this.titleFont.drawCenteredString("Login", 6.0f + (float)this.LOGIN_WIDTH / 2.0f, this.LOGIN_Y + 6, new Color(255, 255, 255).getRGB());
        this.username.drawTextBox();
        this.password.drawTextBox();
        this.drawActionButtons(mouseX, mouseY);
        this.altFont.drawCenteredString(this.statusString, 6 + this.LOGIN_WIDTH / 2, this.LOGIN_Y + this.LOGIN_HEIGHT - 6 - this.altFont.getHeight(), new Color(255, 255, 255).getRGB());
    }

    private void drawInfoBoxes(int mouseX, int mouseY) {
        RenderUtils.drawRect(6.0f, 6.0f, BOX_ACCENT_SIZE, (float)this.INFO_HEIGHT / 3.0f, this.getHueColorAt(6 - BOX_ACCENT_SIZE, 6 + this.INFO_HEIGHT / 3));
        RenderUtils.drawRect(6.0f, 6.0f, (float)this.LOGIN_WIDTH / 3.0f, BOX_ACCENT_SIZE, this.getHueColorAt(6 - BOX_ACCENT_SIZE, 6 + this.INFO_HEIGHT / 3));
        RenderUtils.drawRect(6 + BOX_ACCENT_SIZE, 6 + BOX_ACCENT_SIZE, this.LOGIN_WIDTH, this.INFO_HEIGHT, new Color(22, 22, 22, 140));
        this.titleFont.drawCenteredString("Keybinds", 6.0f + (float)this.LOGIN_WIDTH / 2.0f, 12.0f, new Color(255, 255, 255).getRGB());
        this.altFont.drawString("CLICK - Login to alt", 12.0f, 18 + this.titleFont.getHeight(), new Color(255, 255, 255).getRGB());
        this.altFont.drawString("ALT+CLICK - Select alt", 12.0f, 24 + this.titleFont.getHeight() + this.altFont.getHeight(), new Color(255, 255, 255).getRGB());
        this.altFont.drawString("ALT+BACKSPACE - Delete selected alts", 12.0f, 30 + this.titleFont.getHeight() + this.altFont.getHeight() * 2, new Color(255, 255, 255).getRGB());
        this.altFont.drawString("ALT+A - Select all alts", 12.0f, 36 + this.titleFont.getHeight() + this.altFont.getHeight() * 3, new Color(255, 255, 255).getRGB());
        RenderUtils.drawRect(6.0f, (float)(this.height - 6) - (float)this.LOGIN_HEIGHT / 3.0f, BOX_ACCENT_SIZE, (float)this.LOGIN_HEIGHT / 3.0f, this.getHueColorAt(6, this.LOGIN_Y + this.LOGIN_HEIGHT * 2 + 6 - this.LOGIN_HEIGHT / 3));
        RenderUtils.drawRect(6.0f, this.height - 6, (float)this.LOGIN_WIDTH / 3.0f, BOX_ACCENT_SIZE, this.getHueColorAt(6, this.LOGIN_Y + this.LOGIN_HEIGHT * 2 + 6 - this.LOGIN_HEIGHT / 3));
        RenderUtils.drawRect(6 + BOX_ACCENT_SIZE, this.LOGIN_Y + this.LOGIN_HEIGHT + 6 - BOX_ACCENT_SIZE, this.LOGIN_WIDTH, this.INFO_HEIGHT, new Color(22, 22, 22, 140));
        this.titleFont.drawCenteredString("Info", 6.0f + (float)this.LOGIN_WIDTH / 2.0f, this.LOGIN_Y + this.LOGIN_HEIGHT + 12, new Color(255, 255, 255).getRGB());
        this.altFont.drawString("user:pass currently broken use oauth.", 12.0f, this.LOGIN_Y + this.LOGIN_HEIGHT + this.titleFont.getHeight() + 18, new Color(255, 255, 255).getRGB());
        this.altFont.drawString("logging in with username only", 12.0f, this.LOGIN_Y + this.LOGIN_HEIGHT + this.titleFont.getHeight() + 30, new Color(255, 255, 255).getRGB());
        this.altFont.drawString("creates cracked. use oauth for", 12.0f, this.LOGIN_Y + this.LOGIN_HEIGHT + this.titleFont.getHeight() + 42, new Color(255, 255, 255).getRGB());
        this.altFont.drawString("token/cookie alts", 12.0f, this.LOGIN_Y + this.LOGIN_HEIGHT + this.titleFont.getHeight() + 54, new Color(255, 255, 255).getRGB());
    }

    private void drawAltSwitcher(int mouseX, int mouseY) {
        int listX = this.BOX_X + 5;
        int listY = this.BOX_Y + 40 + 5;
        int listWidth = this.BOX_WIDTH - 4 - 10;
        int listHeight = this.height - listY - 5;
        int COLUMNS = 3;
        int cellWidth = listWidth / 3;
        int cellHeight = this.ENTRY_HEIGHT;
        int rowStride = cellHeight + 2;
        int visibleRows = listHeight / rowStride;
        int startIndex = this.scrollOffset * 3;
        this.enableScissor(listX, listY, listWidth, listHeight);
        for (int row = 0; row < visibleRows; ++row) {
            int altIndex;
            for (int col = 0; col < 3 && (altIndex = startIndex + row * 3 + col) < this.alts.size(); ++col) {
                int x = listX + col * cellWidth;
                int y = listY + row * rowStride;
                String[] parts = this.alts.get(altIndex).split("\\|", 2);
                String altType = parts[0];
                String altName = parts[1];
                String uuid = altType.equals("microsoftOAuth") || altType.equals("microsoft") ? altName : "";
                this.drawCustomCell(x, y, cellWidth - 4, cellHeight, altName, uuid, altIndex, mouseX, mouseY);
            }
        }
        this.disableScissor();
        int scrollbarX = this.BOX_X + this.BOX_WIDTH - 4 - 2;
        int scrollbarY = listY;
        int scrollbarHeight = listHeight;
        RenderUtils.drawRoundedRect(scrollbarX, scrollbarY, 4.0f, scrollbarHeight, 2.0f, true, new Color(50, 50, 50, 180));
        int totalRows = (int)Math.ceil((float)this.alts.size() / 3.0f);
        int maxScroll = Math.max(0, totalRows - visibleRows);
        int thumbHeight = Math.max(scrollbarHeight * visibleRows / Math.max(1, totalRows), 20);
        int thumbY = scrollbarY + (scrollbarHeight - thumbHeight) * this.scrollOffset / Math.max(1, maxScroll);
        RenderUtils.drawRoundedRect(scrollbarX, thumbY, 4.0f, thumbHeight, 2.0f, true, new Color(100, 100, 100, 220));
    }

    private Color getHueColorAt(int x, int y) {
        long time = System.currentTimeMillis() - this.startTime;
        long weightX = 100L;
        long weightY = 40L;
        long speed = 3000L;
        float normal = 3000.0f;
        float hue = (float)((time + (long)x * weightX + (long)y * weightY) % speed) / normal;
        return Color.getHSBColor(hue, 0.5f, 0.95f);
    }

    private void drawActionButtons(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int startY = this.LOGIN_Y + this.LOGIN_HEIGHT - this.buttonHeight * 2 - 6;
        int totalWidth = this.buttonWidth * 3 + 16;
        int startX = 12;
        this.drawCustomButton(startX, startY, this.buttonWidth, this.buttonHeight, "login", mouseX, mouseY);
        this.drawCustomButton(startX + (this.buttonWidth + 8), startY, this.buttonWidth, this.buttonHeight, "oauth", mouseX, mouseY);
        this.drawCustomButton(startX + (this.buttonWidth + 8) * 2, startY, this.buttonWidth, this.buttonHeight, "gen cracked", mouseX, mouseY);
    }

    private void drawCustomButton(int x, int y, int width, int height, String text, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        Color bgColor = hovered ? new Color(60, 60, 60) : new Color(40, 40, 40);
        RenderUtils.drawRect(x, y - 1, width, 1.0f, this.getHueColorAt(x, y));
        RenderUtils.drawRect(x, y, width, height, bgColor);
        int textX = x + (width - this.buttonFont.getStringWidth(text)) / 2;
        int textY = y + (height - this.buttonFont.getHeight()) / 2;
        this.buttonFont.drawString(text, textX, textY, hovered ? ColorProcess.getColor().getRGB() : 0xFFFFFF);
    }

    private void drawCustomCell(int x, int y, int width, int height, String text, String uuid, int index, int mouseX, int mouseY) {
        boolean selected = this.selectedAlts.contains(index);
        Color bgColor = selected ? new Color(55, 55, 70) : new Color(22, 22, 22);
        RenderUtils.drawRect(x, y, width, height, bgColor);
        this.altFont.drawString(text, x + height, y + 2, 0xFFFFFF);
        this.loadHead(uuid);
        this.drawHead(x, y, uuid, height);
    }

    public void loadHead(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return;
        }
        if (this.headCache.containsKey(uuid)) {
            return;
        }
        if (this.headLoading.getOrDefault(uuid, false).booleanValue()) {
            return;
        }
        if (this.headTries.getOrDefault(uuid, 0) > 5) {
            return;
        }
        this.headLoading.put(uuid, true);
        this.headTries.put(uuid, this.headTries.getOrDefault(uuid, 0) + 1);
        this.headCache.put(uuid, this.placeholderHead);
        new Thread(() -> {
            try {
                URI uri = URI.create("https://mc-heads.net/avatar/" + uuid);
                URLConnection connection = uri.toURL().openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                connection.setRequestProperty("Accept", "image/png");
                BufferedImage image = ImageIO.read(connection.getInputStream());
                if (image == null) {
                    throw new IOException("Failed to read image");
                }
                this.mc.addScheduledTask(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation head = this.mc.getTextureManager().getDynamicTextureLocation("HEAD-" + uuid, texture);
                    this.headCache.put(uuid, head);
                    this.headLoading.put(uuid, false);
                });
            }
            catch (IOException e) {
                e.printStackTrace();
                this.headLoading.put(uuid, false);
            }
        }).start();
    }

    public void drawHead(int x, int y, String uuid, int cellHeight) {
        ResourceLocation head = uuid == null || uuid.isEmpty() ? this.placeholderHead : this.headCache.getOrDefault(uuid, this.placeholderHead);
        int size = cellHeight - 4;
        GlUtils.setup2DRendering();
        GlUtils.startBlend();
        GlStateManager.disableAlpha();
        this.mc.getTextureManager().bindTexture(head);
        Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0.0f, 0.0f, size, size, (float)size, (float)size);
        GlUtils.endBlend();
    }

    private void enableScissor(int x, int y, int width, int height) {
        ScaledResolution sr = new ScaledResolution(this.mc);
        int scale = sr.getScaleFactor();
        int scissorX = x * scale;
        int scissorY = (sr.getScaledHeight() - y - height) * scale;
        int scissorWidth = width * scale;
        int scissorHeight = height * scale;
        GL11.glEnable(3089);
        GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);
    }

    private void disableScissor() {
        GL11.glDisable(3089);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int index;
        int row;
        int col;
        int rowStride;
        int cellWidth;
        int COLUMNS;
        int listWidth;
        int listY;
        int listX;
        this.username.mouseClicked(mouseX, mouseY, mouseButton);
        this.password.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int centerX = this.width / 2;
        int totalWidth = this.buttonWidth * 3 + 16;
        int startY = this.LOGIN_Y + this.LOGIN_HEIGHT - this.buttonHeight * 2 - 6;
        int startX = 12;
        if (!this.isLoggingIn && this.isMouseOverButton(mouseX, mouseY, startX, startY, this.buttonWidth, this.buttonHeight)) {
            if (this.password.getText().isEmpty()) {
                this.handleCrackedLogin(this.username.getText());
            }
            return;
        }
        if (!this.isLoggingIn && this.isMouseOverButton(mouseX, mouseY, startX + (this.buttonWidth + 8), startY, this.buttonWidth, this.buttonHeight)) {
            this.handleOAuthLogin();
            return;
        }
        if (this.isMouseOverButton(mouseX, mouseY, startX + (this.buttonWidth + 8) * 2, startY, this.buttonWidth, this.buttonHeight)) {
            String username = AltManagerGui.generateRandomString();
            this.handleCrackedLogin(username);
            return;
        }
        if (GuiScreen.isAltKeyDown()) {
            listX = this.BOX_X + 5;
            listY = this.BOX_Y + 40 + 5;
            listWidth = this.BOX_WIDTH - 4 - 10;
            COLUMNS = 3;
            cellWidth = listWidth / 3;
            rowStride = this.ENTRY_HEIGHT + 2;
            col = (mouseX - listX) / cellWidth;
            row = (mouseY - listY) / rowStride;
            if (col >= 0 && col < 3 && row >= 0 && (index = (this.scrollOffset + row) * 3 + col) >= 0 && index < this.alts.size()) {
                if (this.selectedAlts.contains(index)) {
                    this.selectedAlts.remove((Object)index);
                } else {
                    this.selectedAlts.add(index);
                }
                return;
            }
        }
        if (!GuiScreen.isAltKeyDown()) {
            listX = this.BOX_X + 5;
            listY = this.BOX_Y + 40 + 5;
            listWidth = this.BOX_WIDTH - 4 - 10;
            COLUMNS = 3;
            cellWidth = listWidth / 3;
            rowStride = this.ENTRY_HEIGHT + 2;
            col = (mouseX - listX) / cellWidth;
            row = (mouseY - listY) / rowStride;
            if (col >= 0 && col < 3 && row >= 0 && (index = (this.scrollOffset + row) * 3 + col) >= 0 && index < this.alts.size()) {
                this.loginWithAlt(this.alts.get(index));
                return;
            }
        }
        int scrollbarX = this.BOX_X + this.BOX_WIDTH - 4 - 2;
        int scrollbarY = this.BOX_Y + 40 + 5;
        int scrollbarHeight = 250;
        if (mouseX >= scrollbarX && mouseX <= scrollbarX + 4 && mouseY >= scrollbarY && mouseY <= scrollbarY + scrollbarHeight) {
            this.draggingScrollbar = true;
            this.dragStartY = mouseY;
            this.scrollStart = this.scrollOffset;
        }
    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }

    private void loginWithAlt(String alt) {
        String username;
        String refreshToken;
        if (alt.startsWith("cracked|")) {
            String username2 = alt.split("\\|")[1];
            SessionChanger.getInstance().setUserOffline(username2);
        } else if (alt.startsWith("microsoft|")) {
            String[] parts = alt.split("\\|");
            if (parts.length >= 3) {
                String email = parts[2];
                String pass = parts[3];
                SessionChanger.getInstance().setUserMicrosoft(email, pass);
            }
        } else if (alt.startsWith("microsoftOAuth|") && (refreshToken = this.loadRefreshToken(username = alt.split("\\|")[1])) != null) {
            MicrosoftOAuthTranslation.LoginData login = MicrosoftOAuthTranslation.login(refreshToken);
            this.mc.setSession(new Session(login.username, login.uuid, login.mcToken, "microsoft"));
        }
    }

    private void saveAlts() {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "advantage");
        File file = new File(dir, "alts.txt");
        try (PrintWriter out = new PrintWriter(file);){
            for (String alt : this.alts) {
                out.println(alt);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAltToFile(String email, String password, String sessionUsername) {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "advantage");
        File file = new File(dir, "alts.txt");
        try (FileWriter fw = new FileWriter(file, true);
             PrintWriter out = new PrintWriter(fw);){
            out.println("microsoft|" + sessionUsername + "|" + email + "|" + password);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveOAuthAltToFile(String username, String refreshToken) {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "Advantage");
        File altsFile = new File(dir, "alts.txt");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (FileWriter fw = new FileWriter(altsFile, true);
             PrintWriter out = new PrintWriter(fw);){
            out.println("microsoftOAuth|" + username);
            this.alts.add("microsoftOAuth|" + username);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        File tokensFile = new File(dir, "tokens.txt");
        try (FileWriter fw = new FileWriter(tokensFile, true);
             PrintWriter out = new PrintWriter(fw);){
            out.println(username + "|" + refreshToken);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadRefreshToken(String username) {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "Advantage");
        File file = new File(dir, "tokens.txt");
        if (!file.exists()) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file));){
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 2 || !parts[0].equals(username)) continue;
                String string = parts[1];
                return string;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.draggingScrollbar = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (this.draggingScrollbar) {
            int scrollbarHeight = 250;
            int deltaY = mouseY - this.dragStartY;
            int visibleEntries = scrollbarHeight / this.ENTRY_HEIGHT;
            int maxScrollLocal = Math.max(0, this.alts.size() - visibleEntries);
            if (maxScrollLocal > 0) {
                int scrollRange = scrollbarHeight - Math.max(scrollbarHeight * visibleEntries / (this.alts.size() == 0 ? 1 : this.alts.size()), 20);
                int scrollDelta = deltaY * maxScrollLocal / scrollRange;
                this.scrollOffset = Math.min(maxScrollLocal, Math.max(0, this.scrollStart + scrollDelta));
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int visibleEntries = 250 / this.ENTRY_HEIGHT;
            int maxScrollLocal = Math.max(0, this.alts.size() - visibleEntries);
            if (wheel > 0) {
                this.scrollOffset = Math.max(0, this.scrollOffset - 1);
            } else if (wheel < 0) {
                this.scrollOffset = Math.min(maxScrollLocal, this.scrollOffset + 1);
            }
        }
    }

    private void handleCrackedLogin(String username) {
        if (this.isLoggingIn) {
            return;
        }
        if (username.isEmpty()) {
            return;
        }
        this.isLoggingIn = true;
        this.mc.setSession(new Session(username, username, "0", "legacy"));
        this.saveCrackedToFile(username);
        this.statusString = "Logged in with " + username;
        this.clearTextBoxes();
        this.isLoggingIn = false;
    }

    private void handleOAuthLogin() {
        if (this.isLoggingIn) {
            return;
        }
        this.isLoggingIn = true;
        this.statusString = "Awaiting response for Microsoft login...";
        MicrosoftOAuthTranslation.getRefreshToken(refreshToken -> {
            try {
                if (refreshToken != null) {
                    MicrosoftOAuthTranslation.LoginData login = MicrosoftOAuthTranslation.login(refreshToken);
                    if (login.isGood()) {
                        this.mc.setSession(new Session(login.username, login.uuid, login.mcToken, "microsoft"));
                        this.saveOAuthAltToFile(login.username, login.newRefreshToken);
                        this.statusString = "Logged in with " + login.username;
                    } else {
                        this.statusString = "Failed to login with Microsoft OAuth";
                    }
                } else {
                    this.statusString = "Failed to get refresh token";
                }
            }
            finally {
                this.isLoggingIn = false;
            }
        });
    }

    private void saveCrackedToFile(String sessionUsername) {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "Advantage");
        File file = new File(dir, "alts.txt");
        try (FileWriter fw = new FileWriter(file, true);
             PrintWriter out = new PrintWriter(fw);){
            out.println("cracked|" + sessionUsername);
            this.alts.add("cracked|" + sessionUsername);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
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

    private void clearTextBoxes() {
        this.username.setText("");
        this.password.setText("");
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.username.keyTyped(typedChar, keyCode);
        this.password.keyTyped(typedChar, keyCode);
        if (GuiScreen.isAltKeyDown() && keyCode == Keyboard.KEY_A) {
            this.selectedAlts.clear();
            for (int i = 0; i < this.alts.size(); ++i) {
                this.selectedAlts.add(i);
            }
            return;
        }
        if (GuiScreen.isAltKeyDown() && keyCode == Keyboard.KEY_BACK) {
            if (!this.selectedAlts.isEmpty()) {
                this.selectedAlts.sort((a, b) -> b - a);
                for (int index : this.selectedAlts) {
                    if (index < 0 || index >= this.alts.size()) continue;
                    this.alts.remove(index);
                }
                this.selectedAlts.clear();
                this.saveAltsToFile();
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}

