/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.interfaces.menu.main;

import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.interfaces.menu.alt.AltManagerGui;
import cc.advantage.utils.client.BgUtils;
import cc.advantage.utils.render.AnimatedMenuBackground;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class CustomMainMenu
extends GuiScreen {
    private final ResourceLocation logoImage = new ResourceLocation("advantage/images/Advantage.png");
    private final CustomFontRenderer buttonFont;
    private final CustomFontRenderer changelogFont;
    private final CustomFontRenderer timeFont;
    private final int buttonWidth = 120;
    private final int buttonHeight = 30;
    private final int buttonSpacing = 8;
    private final int buttonsYOffset = 60;
    private final long startTime = System.currentTimeMillis();
    ArrayList<String> changelogEntries;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static ArrayList<String> commitBuffer = new ArrayList();
    private static long commitBufferLife = 0L;
    private static final long commitBufferTTL = 300000L;

    public CustomMainMenu() {
        this.buttonFont = FontUtils.getFont("advantage");
        this.changelogFont = FontUtils.getFont("advantage");
        this.timeFont = FontUtils.getFont("big");
    }

    @Override
    public void initGui() {
        long now = System.currentTimeMillis();
        if (!commitBuffer.isEmpty() && now - commitBufferLife < 300000L) {
            this.changelogEntries = new ArrayList<String>(commitBuffer);
            super.initGui();
            return;
        }
        this.changelogEntries = new ArrayList();
        this.changelogEntries.add("loading...");
        new Thread(() -> {
            try {
                ArrayList<String> entries = CustomMainMenu.fetchLatestCommitMessages("x0lumie", "Advantage", 4);
                commitBuffer = new ArrayList<String>(entries);
                commitBufferLife = System.currentTimeMillis();
                this.mc.addScheduledTask(() -> {
                    this.changelogEntries = entries;
                    return this.changelogEntries;
                });
            }
            catch (IOException e) {
                ArrayList<String> fallback = new ArrayList<String>();
                if ("HTTP_403".equals(e.getMessage())) {
                    fallback.add("403: github ratelimited");
                } else {
                    fallback.add("failed to load");
                }
                this.mc.addScheduledTask(() -> {
                    this.changelogEntries = fallback;
                    return this.changelogEntries;
                });
            }
            catch (Exception e) {
                ArrayList<String> fallback = new ArrayList<String>();
                fallback.add("unexpected error");
                this.mc.addScheduledTask(() -> {
                    this.changelogEntries = fallback;
                    return this.changelogEntries;
                });
            }
        }).start();
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        AnimatedMenuBackground.draw(this.width, this.height, this.startTime, BgUtils.getInstance().getCurrentIndex());
        GlStateManager.enableAlpha();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String currentTime = timeFormat.format(new Date());
        float heightScale = (float)this.height / 480.0f;
        int timeYOffset = -210;
        int scaledTimeYOffset = (int)((float)timeYOffset * Math.min(heightScale, 1.2f));
        int timeX = (this.width - this.timeFont.getStringWidth(currentTime)) / 2;
        int timeY = this.height / 2 + scaledTimeYOffset;
        this.timeFont.drawStringWithShadow(currentTime, timeX, timeY, 0xFFFFFF);
        RenderUtils.drawImage(this.logoImage, (float)this.width / 2.0f - 78.5f, (float)this.height / 10.0f, 157.0f, 125.0f);
        this.drawCustomButtons(mouseX, mouseY);
        this.drawChangelog();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawCustomButtons(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int startY = this.height / 2 + 60;
        int totalWidth = 376;
        int startX = centerX - totalWidth / 2;
        this.drawButton(startX, startY, 120, 30, "singleplayer", mouseX, mouseY);
        this.drawButton(startX + 120 + 8, startY, 120, 30, "multiplayer", mouseX, mouseY);
        this.drawButton(startX + 256, startY, 120, 30, "alts", mouseX, mouseY);
        this.drawButton(startX + 256, startY + 38, 120, 30, "change bg", mouseX, mouseY);
        this.drawButton(startX, startY + 38, 120, 30, "options", mouseX, mouseY);
        this.drawButton(startX + 128, startY + 38, 120, 30, "quit", mouseX, mouseY);
    }

    private void drawButton(int x, int y, int width, int height, String text, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        long time = System.currentTimeMillis() - this.startTime;
        long weightX = 100L;
        long weightY = 600L;
        long speed = 3000L;
        float normal = 3000.0f;
        float hue = (float)((time + (long)x * weightX + (long)y * weightY) % speed) / normal;
        Color bgColor = hovered ? new Color(60, 60, 60, 200) : new Color(40, 40, 40, 180);
        RenderUtils.drawRect(x, y - 1, width, 1.0f, Color.getHSBColor(hue, 0.5f, 0.95f));
        RenderUtils.drawRect(x, y, width, height, bgColor);
        int textX = x + (width - this.buttonFont.getStringWidth(text)) / 2;
        int textY = y + (height - this.buttonFont.getHeight()) / 2;
        this.buttonFont.drawString(text, textX, textY, 0xFFFFFF);
    }

    private void drawChangelog() {
        int x = 20;
        int y = 20;
        int width = 170;
        long time = System.currentTimeMillis() - this.startTime;
        float baseHue = (float)(time % 3000L) / 3000.0f;
        int fontHeight = this.changelogFont.getHeight();
        int titleOffset = fontHeight + 14;
        int entrySpacing = fontHeight + 4;
        int totalHeight = titleOffset + this.changelogEntries.size() * entrySpacing + 2;
        RenderUtils.drawRoundedRect(x, y, width, totalHeight, 8.0f, true, new Color(20, 25, 30, 120));
        this.changelogFont.drawStringWithShadow("changelog", x + 8, y + 6, Color.getHSBColor(baseHue, 0.8f, 1.0f).getRGB());
        int entryY = y + titleOffset;
        for (int i = 0; i < this.changelogEntries.size(); ++i) {
            String text = this.changelogEntries.get(i).toLowerCase();
            if (text.length() > 20) {
                text = text.substring(0, 20);
            }
            float entryHue = (float)((time + (long)(i * 500)) % 3000L) / 3000.0f;
            this.changelogFont.drawStringWithShadow(text, x + 8, entryY, Color.getHSBColor(entryHue, 0.5f, 0.95f).getRGB());
            entryY += entrySpacing;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            int centerX = this.width / 2;
            int totalWidth = 376;
            int startX = centerX - totalWidth / 2;
            int startY = this.height / 2 + 60;
            if (this.isMouseOverButton(mouseX, mouseY, startX, startY, 120, 30)) {
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
            }
            if (this.isMouseOverButton(mouseX, mouseY, startX + 120 + 8, startY, 120, 30)) {
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
            }
            if (this.isMouseOverButton(mouseX, mouseY, startX + 128, startY + 38, 120, 30)) {
                this.mc.shutdown();
            }
            if (this.isMouseOverButton(mouseX, mouseY, startX + 256, startY, 120, 30)) {
                this.mc.displayGuiScreen(new AltManagerGui());
            }
            if (this.isMouseOverButton(mouseX, mouseY, startX + 256, startY + 38, 120, 30)) {
                BgUtils.getInstance().cycleBackground();
            }
            if (this.isMouseOverButton(mouseX, mouseY, startX, startY + 38, 120, 30)) {
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
            }
        }
    }

    public static ArrayList<String> fetchLatestCommitMessages(String owner, String repo, int limit) throws Exception {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/commits?per_page=" + limit;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(3L)).header("Accept", "application/vnd.github+json").header("User-Agent", "Java-GitHub-Client").GET().build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP_" + response.statusCode());
        }
        JsonArray commits = JsonParser.parseString(response.body()).getAsJsonArray();
        ArrayList<String> messages = new ArrayList<String>(commits.size());
        for (int i = 0; i < commits.size(); ++i) {
            JsonObject commitObj = commits.get(i).getAsJsonObject();
            JsonObject commit = commitObj.getAsJsonObject("commit");
            messages.add(commit.get("message").getAsString());
        }
        return messages;
    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int buttonX, int buttonY, int buttonWidth, int buttonHeight) {
        return mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

