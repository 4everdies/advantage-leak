/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.modules;

import cc.advantage.Advantage;
import cc.advantage.api.config.Serializable;
import cc.advantage.api.notifications.NotificationManager;
import cc.advantage.api.notifications.NotificationType;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.MultiModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.Toggleable;
import cc.advantage.modules.impl.client.ToggleSoundsModule;
import cc.advantage.modules.impl.visuals.NotificationsModule;
import cc.advantage.utils.client.SoundUtils;
import cc.advantage.utils.misc.Manager;
import cc.advantage.utils.render.Translate;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Field;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.Minecraft;

public class Module
extends Manager<Property<?>>
implements Toggleable,
Serializable {
    private final String label = this.getClass().getAnnotation(ModuleInfo.class).label();
    private final String description = this.getClass().getAnnotation(ModuleInfo.class).description();
    private final ModuleCategory category = this.getClass().getAnnotation(ModuleInfo.class).category();
    private int key = this.getClass().getAnnotation(ModuleInfo.class).key();
    private boolean enabled;
    private boolean hidden;
    private String suffix;
    private final Translate translate = new Translate(0.0, 0.0);

    public void resetPropertyValues() {
        for (Property property : this.getElements()) {
            property.callFirstTime();
        }
    }

    public Translate getTranslate() {
        return this.translate;
    }

    public ModuleCategory getCategory() {
        return this.category;
    }

    public void reflectProperties() {
        for (Field field : this.getClass().getDeclaredFields()) {
            Class<Property> type = field.getType();
            if (!type.isAssignableFrom(Property.class) && !type.isAssignableFrom(NumberProperty.class) && !type.isAssignableFrom(ModeProperty.class) && !type.isAssignableFrom(MultiModeProperty.class)) continue;
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            try {
                this.elements.add((Property)field.get(this));
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLabel() {
        return this.label;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                this.onEnable();
                Advantage.INSTANCE.getEventBus().subscribe(this);
            } else {
                Advantage.INSTANCE.getEventBus().unsubscribe(this);
                this.onDisable();
            }
        }
    }

    public boolean isVisible() {
        return this.enabled && !this.hidden;
    }

    @Override
    public void toggle() {
        this.setEnabled(!this.enabled);
        if (Minecraft.getMinecraft().thePlayer != null) {
            if (Advantage.INSTANCE.getModuleManager().getModule(NotificationsModule.class).isEnabled() && NotificationsModule.toggleNotifications.getValue().booleanValue()) {
                String titleToggle = "Module toggled";
                String descriptionToggleOn = this.getLabel() + " was \u00a7aenabled!";
                String descriptionToggleOff = this.getLabel() + " was \u00a7cdisabled!";
                if (this.enabled) {
                    NotificationManager.post(NotificationType.SUCCESS, titleToggle, descriptionToggleOn);
                } else {
                    NotificationManager.post(NotificationType.DISABLE, titleToggle, descriptionToggleOff);
                }
            }
            if (Advantage.INSTANCE.getModuleManager().getModule(ToggleSoundsModule.class).isEnabled()) {
                switch ((ToggleSoundsModule.Mode)((Object)ToggleSoundsModule.mode.getValue())) {
                    case Advantage: {
                        if (this.enabled) {
                            SoundUtils.playSound("advantage-enable.wav");
                            break;
                        }
                        SoundUtils.playSound("advantage-disable.wav");
                        break;
                    }
                    case Augustus: {
                        if (this.enabled) {
                            SoundUtils.playSound("augustus-enable.wav");
                            break;
                        }
                        SoundUtils.playSound("augustus-disable.wav");
                        break;
                    }
                    case Vanilla: {
                        SoundUtils.playSound("minecraft-toggle.wav");
                        break;
                    }
                    case Sigma5: {
                        if (this.enabled) {
                            SoundUtils.playSound("sigma5-enable.wav");
                            break;
                        }
                        SoundUtils.playSound("sigma5-disable.wav");
                        break;
                    }
                    case Note: {
                        if (this.enabled) {
                            SoundUtils.playSound("note-enable.wav");
                            break;
                        }
                        SoundUtils.playSound("note-disable.wav");
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public JsonObject save() {
        return this.save(true);
    }

    public JsonObject save(boolean saveKey) {
        JsonObject object = new JsonObject();
        object.addProperty("toggled", this.isEnabled());
        if (saveKey) {
            object.addProperty("key", this.getKey());
        }
        object.addProperty("hidden", this.isHidden());
        List properties = this.getElements();
        if (!properties.isEmpty()) {
            JsonObject propertiesObject = new JsonObject();
            for (Property property : properties) {
                if (property instanceof NumberProperty) {
                    propertiesObject.addProperty(property.getLabel(), (Number)((NumberProperty)property).getValue());
                    continue;
                }
                if (property instanceof ModeProperty) {
                    ModeProperty ModeProperty2 = (ModeProperty)property;
                    propertiesObject.add(property.getLabel(), new JsonPrimitive(((Enum)ModeProperty2.getValue()).name()));
                    continue;
                }
                if (property instanceof MultiModeProperty) {
                    MultiModeProperty multiSelect = (MultiModeProperty)property;
                    JsonArray array = new JsonArray();
                    for (Enum e : multiSelect.getValues()) {
                        array.add(new JsonPrimitive(e.name()));
                    }
                    propertiesObject.add(property.getLabel(), array);
                    continue;
                }
                if (property.getType() == Boolean.class) {
                    propertiesObject.addProperty(property.getLabel(), (Boolean)property.getValue());
                    continue;
                }
                if (property.getType() == Integer.class) {
                    propertiesObject.addProperty(property.getLabel(), Integer.toHexString((Integer)property.getValue()));
                    continue;
                }
                if (property.getType() != String.class) continue;
                propertiesObject.addProperty(property.getLabel(), (String)property.getValue());
            }
            object.add("Properties", propertiesObject);
        }
        return object;
    }

    @Override
    public void load(JsonObject object) {
        this.load(object, false);
    }

    public void load(JsonObject object, boolean loadKey) {
        if (object.has("toggled")) {
            this.setEnabled(object.get("toggled").getAsBoolean());
        }
        if (loadKey && object.has("key")) {
            this.setKey(object.get("key").getAsInt());
        }
        if (object.has("hidden")) {
            this.setHidden(object.get("hidden").getAsBoolean());
        }
        if (object.has("Properties") && !this.getElements().isEmpty()) {
            JsonObject propertiesObject = object.getAsJsonObject("Properties");
            for (Property property : this.getElements()) {
                if (!propertiesObject.has(property.getLabel())) continue;
                if (property instanceof NumberProperty) {
                    ((NumberProperty)property).setValue(propertiesObject.get(property.getLabel()).getAsDouble());
                    continue;
                }
                if (property instanceof ModeProperty) {
                    Module.findEnumValue(property, propertiesObject);
                    continue;
                }
                if (property instanceof MultiModeProperty) continue;
                if (property.getValue() instanceof Boolean) {
                    property.setValue(propertiesObject.get(property.getLabel()).getAsBoolean());
                    continue;
                }
                if (property.getValue() instanceof Integer) {
                    property.setValue((int)Long.parseLong(propertiesObject.get(property.getLabel()).getAsString(), 16));
                    continue;
                }
                if (!(property.getValue() instanceof String)) continue;
                property.setValue(propertiesObject.get(property.getLabel()).getAsString());
            }
        }
    }

    private static <T extends Enum<T>> void findEnumValue(Property<?> property, JsonObject propertiesObject) {
        ModeProperty ModeProperty2 = (ModeProperty)property;
        String value = propertiesObject.getAsJsonPrimitive(property.getLabel()).getAsString();
        for (Enum possibleValue : ModeProperty2.getValues()) {
            if (!possibleValue.name().equalsIgnoreCase(value)) continue;
            ModeProperty2.setValue(possibleValue);
            break;
        }
    }

    @Generated
    public String getSuffix() {
        return this.suffix;
    }

    @Generated
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}

