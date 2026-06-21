/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.ClickEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.events.impl.world.TickEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.EvictingList;
import cc.advantage.utils.render.DragUtils;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

@ModuleInfo(label="Info Display", category=ModuleCategory.VISUALS)
public class InfoDisplayModule
extends Module {
    private final ModeProperty<DisplayMode> mode = new ModeProperty<DisplayMode>("Mode", DisplayMode.Draggable);
    private final Property<Boolean> cps = new Property<Boolean>("CPS", true);
    private final Property<Boolean> fps = new Property<Boolean>("FPS", true);
    private final Property<Boolean> bps = new Property<Boolean>("BPS", true);
    private final Property<Boolean> version = new Property<Boolean>("Version", true, () -> this.mode.getValue() == DisplayMode.Classic);
    private final Property<Boolean> username = new Property<Boolean>("Username", true, () -> this.mode.getValue() == DisplayMode.Classic);
    private static final int BUBBLE_HEIGHT = 15;
    private static final int BUBBLE_PADDING = 2;
    private static final int RADIUS = 5;
    private static boolean positionInitialized = false;
    private int cpsValue = 0;
    private double bpsValue = 0.0;
    private final EvictingList<Boolean> clicks = new EvictingList(20);
    private boolean clicked;
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> this.drawInfoBubbles();
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> this.drawInfoBubbles();
    @EventLink
    public final Listener<ClickEvent> onClick = event -> {
        this.clicked = true;
    };
    @EventLink
    public final Listener<TickEvent> onPreMotionEvent = event -> {
        this.cpsValue = 0;
        this.clicks.add(this.clicked);
        this.clicks.forEach(click -> {
            if (click.booleanValue()) {
                ++this.cpsValue;
            }
        });
        this.clicked = false;
    };

    private void initializePositions(ScaledResolution sr) {
        if (!positionInitialized) {
            int yOffset = 50;
            int xPos = 10;
            if (this.cps.getValue().booleanValue() && !DragUtils.components.containsKey("InfoDisplay_CPS")) {
                DragUtils.components.put("InfoDisplay_CPS", new DragUtils.DraggableComponent(xPos, yOffset));
            }
            if (this.cps.getValue().booleanValue()) {
                yOffset += 20;
            }
            if (this.fps.getValue().booleanValue() && !DragUtils.components.containsKey("InfoDisplay_FPS")) {
                DragUtils.components.put("InfoDisplay_FPS", new DragUtils.DraggableComponent(xPos, yOffset));
            }
            if (this.fps.getValue().booleanValue()) {
                yOffset += 20;
            }
            if (this.bps.getValue().booleanValue() && !DragUtils.components.containsKey("InfoDisplay_BPS")) {
                DragUtils.components.put("InfoDisplay_BPS", new DragUtils.DraggableComponent(xPos, yOffset));
            }
            positionInitialized = true;
        }
    }

    private void drawInfoBubbles() {
        ScaledResolution sr = new ScaledResolution(Util.mc);
        if (Util.mc.thePlayer != null) {
            double deltaX = Util.mc.thePlayer.posX - Util.mc.thePlayer.prevPosX;
            double deltaZ = Util.mc.thePlayer.posZ - Util.mc.thePlayer.prevPosZ;
            this.bpsValue = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20.0;
        }
        if (this.mode.getValue() == DisplayMode.Classic) {
            this.drawClassic(sr);
        } else {
            this.initializePositions(sr);
            boolean isInChat = Util.mc.currentScreen instanceof GuiChat;
            if (this.cps.getValue().booleanValue()) {
                this.drawBubble("CPS", String.valueOf(this.cpsValue), "InfoDisplay_CPS", isInChat);
            }
            if (this.fps.getValue().booleanValue()) {
                int fpsValue = Util.mc.getDebugFPS();
                this.drawBubble("FPS", String.valueOf(fpsValue), "InfoDisplay_FPS", isInChat);
            }
            if (this.bps.getValue().booleanValue()) {
                this.drawBubble("BPS", String.format("%.2f", this.bpsValue), "InfoDisplay_BPS", isInChat);
            }
        }
    }

    private void drawClassic(ScaledResolution sr) {
        int yOffset = sr.getScaledHeight() - 2;
        int xPos = 2;
        int lineHeight = FontUtils.getCurrentFont().getHeight() + 2;
        ArrayList<Object> lines = new ArrayList<Object>();
        if (this.bps.getValue().booleanValue()) {
            lines.add(String.format("BPS: %.2f", this.bpsValue));
        }
        if (this.fps.getValue().booleanValue()) {
            int fpsValue = Util.mc.getDebugFPS();
            lines.add("FPS: " + fpsValue);
        }
        if (this.cps.getValue().booleanValue()) {
            lines.add("CPS: " + this.cpsValue);
        }
        if (this.version.getValue().booleanValue()) {
            lines.add("Version: " + Advantage.VERSION);
        }
        if (this.username.getValue().booleanValue()) {
            lines.add("Username: " + Util.mc.getSession().getUsername());
        }
        for (int i = lines.size() - 1; i >= 0; --i) {
            FontUtils.getCurrentFont().drawStringWithShadow((String)lines.get(i), xPos, yOffset - (lines.size() - i) * lineHeight, Color.WHITE.getRGB());
        }
    }

    private void drawBubble(String label, String value, String componentKey, boolean isInChat) {
        DragUtils.DraggableComponent component = DragUtils.components.get(componentKey);
        if (component == null) {
            return;
        }
        String displayText = label + ": " + value;
        int textWidth = FontUtils.getCurrentFont().getStringWidth(displayText);
        int bubbleWidth = textWidth + 4;
        component.setWidth(bubbleWidth);
        component.setHeight(15.0);
        GlStateManager.pushMatrix();
        GlStateManager.translate(component.getX(), component.getY(), 0.0);
        Color bgColor = new Color(ColorProcess.getColor().darker().getRed(), ColorProcess.getColor().darker().getGreen(), ColorProcess.getColor().darker().getBlue(), isInChat ? 60 : 100);
        RenderUtils.drawRoundedRect(0.0f, 0.0f, bubbleWidth, 15.0f, 5.0f, bgColor);
        int textY = (15 - FontUtils.getCurrentFont().getHeight()) / 2;
        FontUtils.getCurrentFont().drawStringWithShadow(displayText, 2.0f, textY, Color.WHITE.getRGB());
        GlStateManager.popMatrix();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        positionInitialized = false;
    }

    public static enum DisplayMode {
        Draggable,
        Classic;

    }
}

