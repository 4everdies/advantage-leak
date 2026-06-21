/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.misc;

import cc.advantage.Advantage;
import cc.advantage.api.config.ConfigManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GitHubConfigFetcher {
    private static final String REPO_API_URL = "https://api.github.com/repos/semsic/configs/contents";
    private static final String RAW_URL = "https://raw.githubusercontent.com/semsic/configs/main/";

    public static List<String> fetchConfigList() {
        ArrayList<String> configs = new ArrayList<String>();
        try {
            String line;
            URL url = new URL(REPO_API_URL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            JsonArray files = new JsonParser().parse(response.toString()).getAsJsonArray();
            for (JsonElement element : files) {
                String name = element.getAsJsonObject().get("name").getAsString();
                if (!name.endsWith(".json")) continue;
                configs.add(name.replace(".json", ""));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return configs;
    }

    public static boolean downloadAndLoadConfig(String configName) {
        try {
            String line;
            String configUrl = RAW_URL + configName + ".json";
            URL url = new URL(configUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            File configFile = new File(ConfigManager.CONFIGS_DIR, configName + ".json");
            Files.write(Paths.get(configFile.getPath(), new String[0]), content.toString().getBytes(), new OpenOption[0]);
            return Advantage.INSTANCE.getConfigManager().loadConfig(configName);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

