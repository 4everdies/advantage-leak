/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.render;

import cc.advantage.api.config.Serializable;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

public class DragUtils
implements Serializable {
    public static final Map<String, DraggableComponent> components = new HashMap<String, DraggableComponent>();
    private static String draggingComponent = null;
    private static double dragStartX;
    private static double dragStartY;
    private static final Minecraft mc;

    public static void update() {
        if (DragUtils.mc.currentScreen instanceof GuiChat) {
            DragUtils.handleDragging();
        } else {
            draggingComponent = null;
        }
    }

    private static void handleDragging() {
        ScaledResolution sr = new ScaledResolution(mc);
        int mouseX = Mouse.getX() * sr.getScaledWidth() / DragUtils.mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / DragUtils.mc.displayHeight - 1;
        boolean isLeftMouseDown = Mouse.isButtonDown(0);
        for (Map.Entry<String, DraggableComponent> entry : components.entrySet()) {
            DraggableComponent draggableComponent = entry.getValue();
            if (draggableComponent.getWidth() <= 1.0 && draggableComponent.getHeight() <= 1.0) continue;
            double x = draggableComponent.getX();
            double y = draggableComponent.getY();
            double width = draggableComponent.getWidth();
            double height = draggableComponent.getHeight();
            Gui.drawRect((int)x - 2, (int)y - 2, (int)(x + width) + 2, (int)(y + height) + 2, new Color(120, 120, 120, 70).getRGB());
            if (x > (double)sr.getScaledWidth()) {
                draggableComponent.setX((double)sr.getScaledWidth() - width);
            }
            if (!(y > (double)sr.getScaledHeight())) continue;
            draggableComponent.setY((double)sr.getScaledHeight() - height);
        }
        if (draggingComponent != null) {
            if (isLeftMouseDown) {
                DraggableComponent component = components.get(draggingComponent);
                component.setX((double)mouseX - dragStartX);
                component.setY((double)mouseY - dragStartY);
            } else {
                draggingComponent = null;
            }
        } else if (isLeftMouseDown) {
            ArrayList<Map.Entry<String, DraggableComponent>> reversed = new ArrayList<Map.Entry<String, DraggableComponent>>(components.entrySet());
            Collections.reverse(reversed);
            for (Map.Entry entry : reversed) {
                DraggableComponent component = (DraggableComponent)entry.getValue();
                if (component.getWidth() <= 1.0 && component.getHeight() <= 1.0) continue;
                double x = component.getX();
                double y = component.getY();
                double width = component.getWidth();
                double height = component.getHeight();
                if (((String)entry.getKey()).equals("Arraylist")) {
                    x = component.getX() - component.getWidth();
                }
                if (!((double)mouseX >= x - 2.0) || !((double)mouseX <= x + width + 2.0) || !((double)mouseY >= y - 2.0) || !((double)mouseY <= y + height + 2.0)) continue;
                draggingComponent = (String)entry.getKey();
                dragStartX = (double)mouseX - component.getX();
                dragStartY = (double)mouseY - component.getY();
                break;
            }
        }
    }

    @Override
    public JsonObject save() {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, DraggableComponent> entry : components.entrySet()) {
            object.add(entry.getKey(), entry.getValue().save());
        }
        return object;
    }

    @Override
    public void load(JsonObject object) {
        for (Map.Entry<String, DraggableComponent> entry : components.entrySet()) {
            if (!object.has(entry.getKey())) continue;
            entry.getValue().load(object.getAsJsonObject(entry.getKey()));
        }
    }

    static {
        mc = Minecraft.getMinecraft();
    }

    public static class DraggableComponent {
        private double x;
        private double y;
        private double width;
        private double height;

        public DraggableComponent(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double getX() {
            return this.x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return this.y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getWidth() {
            return this.width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return this.height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public JsonObject save() {
            JsonObject object = new JsonObject();
            object.addProperty("x", this.x);
            object.addProperty("y", this.y);
            return object;
        }

        public void load(JsonObject object) {
            if (object.has("x")) {
                this.x = object.get("x").getAsDouble();
            }
            if (object.has("y")) {
                this.y = object.get("y").getAsDouble();
            }
        }
    }
}

