/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.config;

import cc.advantage.api.config.Config;
import cc.advantage.utils.misc.Manager;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;

public final class ConfigManager
extends Manager<Config> {
    public static final File CONFIGS_DIR = new File("Advantage", "configs");
    public static final String EXTENSION = ".json";

    public ConfigManager() {
        super(ConfigManager.loadConfigs());
        if (!CONFIGS_DIR.exists()) {
            boolean bl = CONFIGS_DIR.mkdirs();
        }
    }

    public boolean loadConfig(String configName) {
        if (configName == null) {
            return false;
        }
        Config config = this.findConfig(configName);
        if (config == null) {
            return false;
        }
        try {
            FileReader reader = new FileReader(config.getFile());
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject)parser.parse(reader);
            config.load(object);
            return true;
        }
        catch (FileNotFoundException e) {
            return false;
        }
    }

    public boolean saveConfig(String configName) {
        if (configName == null) {
            return false;
        }
        Config config = this.findConfig(configName);
        if (config == null) {
            Config newConfig = config = new Config(configName);
            this.getElements().add(newConfig);
        }
        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.save());
        try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) {
            return null;
        }
        for (Config config : this.getElements()) {
            if (!config.getName().equalsIgnoreCase(configName)) continue;
            return config;
        }
        if (new File(CONFIGS_DIR, configName + EXTENSION).exists()) {
            return new Config(configName);
        }
        return null;
    }

    public boolean deleteConfig(String configName) {
        if (configName == null) {
            return false;
        }
        Config config = this.findConfig(configName);
        if (config != null) {
            File f = config.getFile();
            this.getElements().remove(config);
            return f.exists() && f.delete();
        }
        return false;
    }

    private static ArrayList<Config> loadConfigs() {
        ArrayList<Config> loadedConfigs = new ArrayList<Config>();
        File[] files = CONFIGS_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!FilenameUtils.getExtension(file.getName()).equals("json")) continue;
                loadedConfigs.add(new Config(FilenameUtils.removeExtension(file.getName())));
            }
        }
        return loadedConfigs;
    }
}
