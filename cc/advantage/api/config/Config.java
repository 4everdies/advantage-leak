/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.config;

import cc.advantage.Advantage;
import cc.advantage.api.config.ConfigManager;
import cc.advantage.api.config.Serializable;
import cc.advantage.modules.Module;
import cc.advantage.utils.render.DragUtils;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;

public final class Config
implements Serializable {
    private final String name;
    private final File file;

    public Config(String name) {
        this.name = name;
        this.file = new File(ConfigManager.CONFIGS_DIR, name + ".json");
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public File getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();
        JsonObject modulesObject = new JsonObject();
        JsonObject draggingObject = new JsonObject();
        for (Module module : Advantage.INSTANCE.getModuleManager().getModules()) {
            modulesObject.add(module.getLabel(), module.save(false));
        }
        for (String key : DragUtils.components.keySet()) {
            draggingObject.add(key, DragUtils.components.get(key).save());
        }
        jsonObject.add("Modules", modulesObject);
        jsonObject.add("Dragging", draggingObject);
        return jsonObject;
    }

    @Override
    public void load(JsonObject object) {
        if (object.has("Modules")) {
            JsonObject modulesObject = object.getAsJsonObject("Modules");
            for (Module module : Advantage.INSTANCE.getModuleManager().getModules()) {
                if (!modulesObject.has(module.getLabel())) continue;
                module.load(modulesObject.getAsJsonObject(module.getLabel()));
            }
        }
        if (object.has("Dragging")) {
            JsonObject draggingObject = object.getAsJsonObject("Dragging");
            for (String key : draggingObject.keySet()) {
                JsonObject componentData = draggingObject.getAsJsonObject(key);
                if (!DragUtils.components.containsKey(key)) {
                    DragUtils.components.put(key, new DragUtils.DraggableComponent(0.0, 0.0));
                }
                DragUtils.components.get(key).load(componentData);
            }
        }
    }
}
