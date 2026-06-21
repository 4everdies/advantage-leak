/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.api.properties.impl.Representation;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.DragUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import org.json.JSONObject;

@ModuleInfo(label="Overlay", category=ModuleCategory.VISUALS)
public final class OverlayModule
extends Module {
    private static final String COMPONENT_KEY = "Overlay";
    private static final String API_BASE = "https://mush.com.br/api/player/";
    private static final int HEADER_HEIGHT = 22;
    private static final int ROW_HEIGHT = 22;
    private static final int PLAYER_COLUMN_WIDTH = 118;
    private static final int[] COLUMN_WIDTHS = new int[]{118, 45, 38, 45, 48, 38, 48, 42, 45};
    private static final int MAX_NAME_WIDTH = 87;
    private final NumberProperty maxPlayers = new NumberProperty("Max Players", 8.0, 1.0, 16.0, 1.0, Representation.INT);
    private final NumberProperty refreshDelay = new NumberProperty("Refresh", 60.0, 10.0, 300.0, 5.0, Representation.INT);
    private final Property<Boolean> onlyMush = new Property<Boolean>("Only Mush", true);
    private final Property<Boolean> hideSelf = new Property<Boolean>("Hide Self", false);
    private final ModeProperty<GameMode> gameMode = new ModeProperty<GameMode>("Game", GameMode.BedWars);
    private final NumberProperty scale = new NumberProperty("Scale", 100.0, 50.0, 100.0, 5.0, Representation.INT);
    private final Map<String, MushStats> statsCache = new ConcurrentHashMap<String, MushStats>();
    private final Set<String> pendingRequests = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread2 = new Thread(runnable, "Advantage-Mush-Overlay");
        thread2.setDaemon(true);
        return thread2;
    });
    private static boolean positionInitialized = false;
    @EventLink
    public final Listener<Render2DEvent> render2DEventListener = event -> this.renderOverlay();
    @EventLink
    public final Listener<ShaderEvent> shaderEventListener = event -> this.renderOverlay();

    private void initializePosition(ScaledResolution sr, int width, int height) {
        if (!positionInitialized && !DragUtils.components.containsKey(COMPONENT_KEY)) {
            DragUtils.components.put(COMPONENT_KEY, new DragUtils.DraggableComponent((double)(sr.getScaledWidth() - width) / 2.0, (double)sr.getScaledHeight() / 3.0 - (double)height / 2.0));
            positionInitialized = true;
        }
    }

    private void renderOverlay() {
        this.setSuffix(((GameMode)((Object)this.gameMode.getValue())).toString());
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null || Util.mc.getNetHandler() == null) {
            return;
        }
        if (this.onlyMush.getValue().booleanValue() && !this.isMushServer()) {
            return;
        }
        List<OverlayRow> rows = this.collectRows();
        if (rows.isEmpty()) {
            return;
        }
        int baseWidth = this.getOverlayWidth();
        int baseHeight = 22 + rows.size() * 22;
        float scaleFactor = ((Double)this.scale.getValue()).floatValue() / 100.0f;
        int width = Math.round((float)baseWidth * scaleFactor);
        int height = Math.round((float)baseHeight * scaleFactor);
        ScaledResolution sr = new ScaledResolution(Util.mc);
        this.initializePosition(sr, width, height);
        DragUtils.DraggableComponent component = DragUtils.components.get(COMPONENT_KEY);
        if (component == null) {
            return;
        }
        component.setWidth(width);
        component.setHeight(height);
        GlStateManager.pushMatrix();
        GlStateManager.translate(component.getX(), component.getY(), 0.0);
        GlStateManager.scale(scaleFactor, scaleFactor, 1.0f);
        this.drawBackground(baseWidth, baseHeight, rows.size());
        this.drawHeader(rows.size());
        this.drawRows(rows);
        GlStateManager.popMatrix();
    }

    private List<OverlayRow> collectRows() {
        List<NetworkPlayerInfo> playerInfos = GuiPlayerTabOverlay.field_175252_a.sortedCopy(Util.mc.thePlayer.sendQueue.getPlayerInfoMap());
        ArrayList<OverlayRow> rows = new ArrayList<OverlayRow>();
        for (NetworkPlayerInfo info : playerInfos) {
            String name = info.getGameProfile().getName();
            if (!this.isValidPlayerName(name) || this.hideSelf.getValue().booleanValue() && name.equalsIgnoreCase(Util.mc.thePlayer.getName())) continue;
            EntityPlayer player = Util.mc.theWorld.getPlayerEntityByUUID(info.getGameProfile().getId());
            rows.add(new OverlayRow(info, player, this.getStats(name)));
        }
        rows.sort(Comparator.comparingDouble(row -> row.distance < 0.0 ? Double.MAX_VALUE : row.distance));
        int limit = ((Double)this.maxPlayers.getValue()).intValue();
        if (rows.size() > limit) {
            return new ArrayList<OverlayRow>(rows.subList(0, limit));
        }
        return rows;
    }

    private void drawBackground(int width, int height, int rowCount) {
        Color header = new Color(0, 0, 0, 238);
        Color body = new Color(30, 30, 30, 205);
        RenderUtils.drawRoundedRect(0.0f, 0.0f, width, height, 8.0f, body);
        RenderUtils.drawRoundedRect(0.0f, 0.0f, width, 22.0f, 8.0f, header);
        Gui.drawRect(0, 15, width, 22, header.getRGB());
        for (int i = 0; i < rowCount; ++i) {
            if (i % 2 != 1) continue;
            int y = 22 + i * 22;
            Gui.drawRect(0, y, width, y + 22, new Color(255, 255, 255, 8).getRGB());
        }
    }

    private void drawHeader(int players) {
        int x = 9;
        int textColor = new Color(235, 238, 243).getRGB();
        int mutedColor = new Color(205, 212, 222).getRGB();
        int accentColor = ColorProcess.getColor().getRGB();
        FontUtils.getFont("bold").drawString("Players", x, 6.0f, textColor);
        FontUtils.getFont("bold").drawString(String.valueOf(players), x += FontUtils.getFont("bold").getStringWidth("Players") + 5, 6.0f, accentColor);
        int columnX = COLUMN_WIDTHS[0];
        this.drawColumnHeader("Dist", columnX, COLUMN_WIDTHS[1], mutedColor);
        this.drawColumnHeader("HP", columnX += COLUMN_WIDTHS[1], COLUMN_WIDTHS[2], mutedColor);
        this.drawColumnHeader("K/D", columnX += COLUMN_WIDTHS[2], COLUMN_WIDTHS[3], mutedColor);
        this.drawColumnHeader("Kills", columnX += COLUMN_WIDTHS[3], COLUMN_WIDTHS[4], mutedColor);
        this.drawColumnHeader("WS", columnX += COLUMN_WIDTHS[4], COLUMN_WIDTHS[5], mutedColor);
        this.drawColumnHeader("TK/D", columnX += COLUMN_WIDTHS[5], COLUMN_WIDTHS[6], mutedColor);
        this.drawColumnHeader("W/L", columnX += COLUMN_WIDTHS[6], COLUMN_WIDTHS[7], mutedColor);
        this.drawColumnHeader("Ping", columnX += COLUMN_WIDTHS[7], COLUMN_WIDTHS[8], mutedColor);
    }

    private void drawRows(List<OverlayRow> rows) {
        int muted = new Color(182, 190, 202).getRGB();
        int strong = new Color(238, 242, 248).getRGB();
        for (int i = 0; i < rows.size(); ++i) {
            OverlayRow row = rows.get(i);
            int y = 22 + i * 22;
            int centerY = y + 11;
            this.drawHead(row.info, 8, y + 3, 16);
            FontUtils.getFont("advantage").drawString(this.trimToWidth(row.info.getGameProfile().getName(), 87), 30.0f, centerY - 5, strong);
            int columnX = COLUMN_WIDTHS[0];
            this.drawColumnValue(this.formatDistance(row.distance), columnX, COLUMN_WIDTHS[1], centerY, muted);
            this.drawColumnValue(this.formatHealth(row.health), columnX += COLUMN_WIDTHS[1], COLUMN_WIDTHS[2], centerY, muted);
            this.drawColumnValue(row.stats.formatKd(), columnX += COLUMN_WIDTHS[2], COLUMN_WIDTHS[3], centerY, muted);
            this.drawColumnValue(row.stats.formatKills(), columnX += COLUMN_WIDTHS[3], COLUMN_WIDTHS[4], centerY, muted);
            this.drawColumnValue(row.stats.formatWinstreak(), columnX += COLUMN_WIDTHS[4], COLUMN_WIDTHS[5], centerY, muted);
            this.drawColumnValue(row.stats.formatFinalKd(), columnX += COLUMN_WIDTHS[5], COLUMN_WIDTHS[6], centerY, muted);
            this.drawColumnValue(row.stats.formatWl(), columnX += COLUMN_WIDTHS[6], COLUMN_WIDTHS[7], centerY, muted);
            this.drawColumnValue(Math.max(0, row.info.getResponseTime()) + "ms", columnX += COLUMN_WIDTHS[7], COLUMN_WIDTHS[8], centerY, muted);
        }
        RenderUtils.resetColor();
    }

    private void drawColumnHeader(String text, int x, int width, int color) {
        FontUtils.getFont("bold").drawCenteredString(text, (double)x + (double)width / 2.0, 6.0, color);
    }

    private void drawColumnValue(String text, int x, int width, int centerY, int color) {
        FontUtils.getFont("advantage").drawCenteredString(text, (double)x + (double)width / 2.0, (double)(centerY - 5), color);
    }

    private void drawHead(NetworkPlayerInfo info, int x, int y, int size) {
        try {
            Util.mc.getTextureManager().bindTexture(info.getLocationSkin());
            Gui.drawScaledCustomSizeModalRect(x, y, 8.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
            Gui.drawScaledCustomSizeModalRect(x, y, 40.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
        }
        catch (Exception ignored) {
            RenderUtils.drawRoundedRect(x, y, size, size, 3.0f, ColorProcess.getColor());
        }
    }

    private MushStats getStats(String name) {
        String cacheKey = ((GameMode)((Object)this.gameMode.getValue())).statsKey + ":" + name.toLowerCase(Locale.ROOT);
        MushStats cached = this.statsCache.get(cacheKey);
        long now = System.currentTimeMillis();
        long cacheTime = ((Double)this.refreshDelay.getValue()).longValue() * 1000L;
        if (cached == null || now - cached.fetchedAt > cacheTime) {
            this.requestStats(cacheKey, name);
        }
        return cached == null ? MushStats.loading(now) : cached;
    }

    private void requestStats(String cacheKey, String name) {
        if (!this.pendingRequests.add(cacheKey)) {
            return;
        }
        this.executor.execute(() -> {
            try {
                this.statsCache.put(cacheKey, this.fetchStats(name));
            }
            catch (Exception ignored) {
                this.statsCache.put(cacheKey, MushStats.empty(System.currentTimeMillis()));
            }
            finally {
                this.pendingRequests.remove(cacheKey);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private MushStats fetchStats(String name) throws Exception {
        JSONObject modeStats;
        InputStream stream;
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        HttpsURLConnection connection = (HttpsURLConnection)new URL(API_BASE + encodedName).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("User-Agent", "AdvantageOverlay/1.0");
        int responseCode = connection.getResponseCode();
        InputStream inputStream = stream = responseCode / 100 == 2 ? connection.getInputStream() : connection.getErrorStream();
        if (stream == null) {
            return MushStats.empty(System.currentTimeMillis());
        }
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));){
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        finally {
            connection.disconnect();
        }
        JSONObject root = new JSONObject(response.toString());
        if (!root.optBoolean("success", false)) {
            return MushStats.empty(System.currentTimeMillis());
        }
        JSONObject payload = root.optJSONObject("response");
        if (payload == null) {
            return MushStats.empty(System.currentTimeMillis());
        }
        JSONObject stats = payload.optJSONObject("stats");
        JSONObject jSONObject = modeStats = stats == null ? null : stats.optJSONObject(((GameMode)((Object)this.gameMode.getValue())).statsKey);
        if (modeStats == null) {
            return MushStats.empty(System.currentTimeMillis());
        }
        return MushStats.fromJson(modeStats, (GameMode)((Object)this.gameMode.getValue()), System.currentTimeMillis());
    }

    private boolean isMushServer() {
        return Util.mc.getCurrentServerData() != null && Util.mc.getCurrentServerData().serverIP != null && Util.mc.getCurrentServerData().serverIP.toLowerCase(Locale.ROOT).contains("mush");
    }

    private boolean isValidPlayerName(String name) {
        return name != null && name.length() >= 3 && name.length() <= 16 && name.matches("[A-Za-z0-9_]+");
    }

    private int getOverlayWidth() {
        int width = 0;
        for (int columnWidth : COLUMN_WIDTHS) {
            width += columnWidth;
        }
        return width;
    }

    private String trimToWidth(String text, int maxWidth) {
        if (FontUtils.getFont("advantage").getStringWidth(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int suffixWidth = FontUtils.getFont("advantage").getStringWidth(suffix);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); ++i) {
            String next = String.valueOf(builder) + String.valueOf(text.charAt(i));
            if (FontUtils.getFont("advantage").getStringWidth(next) + suffixWidth > maxWidth) break;
            builder.append(text.charAt(i));
        }
        return String.valueOf(builder) + suffix;
    }

    private String formatDistance(double distance) {
        return distance < 0.0 ? "-" : String.format(Locale.US, "%.1f", distance);
    }

    private String formatHealth(double health) {
        return health < 0.0 ? "-" : String.format(Locale.US, "%.1f", health);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.pendingRequests.clear();
    }

    private static enum GameMode {
        BedWars("BedWars", "bedwars"),
        SkyWars("SkyWars", "skywars_r1"),
        HungerGames("HG", "hungergames"),
        PvP("PvP", "pvp");

        private final String name;
        private final String statsKey;

        private GameMode(String name, String statsKey) {
            this.name = name;
            this.statsKey = statsKey;
        }

        public String toString() {
            return this.name;
        }
    }

    private static final class OverlayRow {
        private final NetworkPlayerInfo info;
        private final MushStats stats;
        private final double distance;
        private final double health;

        private OverlayRow(NetworkPlayerInfo info, EntityPlayer player, MushStats stats) {
            this.info = info;
            this.stats = stats;
            this.distance = player == null ? -1.0 : (double)Util.mc.thePlayer.getDistanceToEntity(player);
            this.health = player == null ? -1.0 : (double)(player.getHealth() + player.getAbsorptionAmount());
        }
    }

    private static final class MushStats {
        private final long fetchedAt;
        private final boolean loaded;
        private final int kills;
        private final int deaths;
        private final int wins;
        private final int losses;
        private final int winstreak;
        private final int finalKills;
        private final int finalDeaths;

        private MushStats(long fetchedAt, boolean loaded, int kills, int deaths, int wins, int losses, int winstreak, int finalKills, int finalDeaths) {
            this.fetchedAt = fetchedAt;
            this.loaded = loaded;
            this.kills = kills;
            this.deaths = deaths;
            this.wins = wins;
            this.losses = losses;
            this.winstreak = winstreak;
            this.finalKills = finalKills;
            this.finalDeaths = finalDeaths;
        }

        private static MushStats loading(long now) {
            return new MushStats(now, false, 0, 0, 0, 0, 0, 0, 0);
        }

        private static MushStats empty(long now) {
            return new MushStats(now, true, 0, 0, 0, 0, 0, 0, 0);
        }

        private static MushStats fromJson(JSONObject object, GameMode mode, long now) {
            int kills = MushStats.firstInt(object, "kills", "arena_kills", "mode_hg_kills", "soup_kills");
            int deaths = MushStats.firstInt(object, "deaths", "arena_deaths", "mode_hg_deaths", "soup_deaths");
            int wins = MushStats.firstInt(object, "wins", "arena_wins", "mode_hg_wins", "soup_wins");
            int losses = MushStats.firstInt(object, "losses", "arena_losses", "mode_hg_losses", "soup_losses");
            int winstreak = MushStats.firstInt(object, "winstreak", "arena_killstreak", "mode_hg_winstreak", "soup_winstreak");
            int finalKills = mode == GameMode.BedWars ? object.optInt("final_kills", 0) : kills;
            int finalDeaths = mode == GameMode.BedWars ? object.optInt("final_deaths", 0) : deaths;
            return new MushStats(now, true, kills, deaths, wins, losses, winstreak, finalKills, finalDeaths);
        }

        private String formatKd() {
            return this.formatRatio(this.kills, this.deaths);
        }

        private String formatKills() {
            return this.loaded ? String.valueOf(this.kills) : "...";
        }

        private String formatWinstreak() {
            return this.loaded ? String.valueOf(this.winstreak) : "...";
        }

        private String formatFinalKd() {
            return this.formatRatio(this.finalKills, this.finalDeaths);
        }

        private String formatWl() {
            return this.formatRatio(this.wins, this.losses);
        }

        private static int firstInt(JSONObject object, String ... keys2) {
            for (String key : keys2) {
                if (!object.has(key)) continue;
                return object.optInt(key, 0);
            }
            return 0;
        }

        private String formatRatio(int first, int second) {
            if (!this.loaded) {
                return "...";
            }
            if (first == 0 && second == 0) {
                return "-";
            }
            if (second == 0) {
                return String.valueOf(first);
            }
            return String.format(Locale.US, "%.2f", (double)first / (double)second);
        }
    }
}

