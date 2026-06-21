/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.interfaces.click;

import cc.advantage.Advantage;
import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.impl.client.ClientSettingsModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.client.Logger;
import cc.advantage.utils.misc.GitHubConfigFetcher;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class ClickInterface
extends GuiScreen {
    private final List<CategoryPanel> panels = new ArrayList<CategoryPanel>();
    private final CustomFontRenderer font = FontUtils.getFont("advantage");
    private Module listeningModule = null;
    private SettingComponent draggingSlider = null;
    private SettingComponent editingString = null;
    private String editingBuffer = "";
    private static final Color BG_COLOR = new Color(22, 22, 22, 220);
    private static final Color PANEL_BG = new Color(28, 28, 28, 230);
    private static Color ACCENT_COLOR = new Color(120, 145, 255);
    private static final Color TEXT_COLOR = new Color(210, 210, 210);
    private static final Color HOVER_COLOR = new Color(45, 45, 45);
    private static final int PANEL_WIDTH = 120;
    private static final int PANEL_SPACING = 12;
    private static final int TOP_MARGIN = 48;

    @Override
    public void initGui() {
        this.panels.clear();
        int x = 20;
        for (ModuleCategory category : ModuleCategory.values()) {
            CategoryPanel panel = new CategoryPanel(category, x, 48);
            this.panels.add(panel);
            x += 132;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        ACCENT_COLOR = ColorProcess.getColor();
        ClientSettingsModule.renderAnimeImage(this.width, this.height);
        if (this.draggingSlider != null) {
            this.draggingSlider.updateDrag(mouseX);
        }
        for (CategoryPanel panel : this.panels) {
            panel.render(mouseX, mouseY);
            panel.updateDrag(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (CategoryPanel panel : this.panels) {
            if (!panel.mouseClicked(mouseX, mouseY, mouseButton)) continue;
            return;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.draggingSlider = null;
        for (CategoryPanel panel : this.panels) {
            panel.mouseReleased(mouseX, mouseY, state);
            panel.clampToScreen();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            boolean handledByPanel = false;
            for (CategoryPanel panel : this.panels) {
                if (mouseX < panel.x || mouseX > panel.x + panel.width || mouseY < panel.y + panel.headerHeight || mouseY > panel.y + this.height) continue;
                panel.handleScroll(mouseX, mouseY, wheel);
                handledByPanel = true;
                break;
            }
            if (!handledByPanel) {
                int scrollAmount = wheel > 0 ? 18 : -18;
                for (CategoryPanel panel : this.panels) {
                    panel.x += scrollAmount;
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.editingString != null) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                this.editingString = null;
                this.editingBuffer = "";
            } else if (keyCode == Keyboard.KEY_RETURN) {
                this.editingString.property.setValueObj(this.editingBuffer);
                this.editingString = null;
                this.editingBuffer = "";
            } else if (keyCode == Keyboard.KEY_BACK) {
                if (!this.editingBuffer.isEmpty()) {
                    this.editingBuffer = this.editingBuffer.substring(0, this.editingBuffer.length() - 1);
                }
            } else if (ClickInterface.isCtrlKeyDown()) {
                if (keyCode == Keyboard.KEY_V) {
                    String clipboard = GuiScreen.getClipboardString();
                    if (clipboard != null && !clipboard.isEmpty()) {
                        for (char c : clipboard.toCharArray()) {
                            if (!ChatAllowedCharacters.isAllowedCharacter(c)) continue;
                            this.editingBuffer = this.editingBuffer + c;
                        }
                    }
                } else if (keyCode == Keyboard.KEY_A) {
                    this.editingBuffer = "";
                } else if (keyCode == Keyboard.KEY_C) {
                    GuiScreen.setClipboardString(this.editingBuffer);
                }
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                this.editingBuffer = this.editingBuffer + typedChar;
            }
            return;
        }
        if (this.listeningModule != null) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
                this.listeningModule.setKey(Keyboard.KEY_NONE);
            } else {
                this.listeningModule.setKey(keyCode);
            }
            this.listeningModule = null;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public class SettingComponent {
        private final Property<?> property;
        private boolean dropdownOpen = false;
        private int dragStartX = 0;
        private int componentX = 0;
        private int componentWidth = 0;

        public SettingComponent(Property<?> property) {
            this.property = property;
        }

        public int getHeight() {
            if (this.property instanceof ModeProperty && this.dropdownOpen) {
                return 16 + ((ModeProperty)this.property).getValues().length * 12;
            }
            return 17;
        }

        public void render(int x, int y, int width, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16;
            Color bgColor = hovered ? new Color(36, 36, 36, 230) : PANEL_BG;
            Gui.drawRect(x, y, x + width, y + 16, bgColor.getRGB());
            if (this.property.getType() == Boolean.class) {
                this.renderBooleanSetting(x, y, width);
            } else if (this.property instanceof NumberProperty) {
                this.renderNumberSetting(x, y, width, mouseX, mouseY);
            } else if (this.property instanceof ModeProperty) {
                this.renderModeSetting(x, y, width, mouseX, mouseY);
            } else if (this.property.getType() == String.class) {
                this.renderStringSetting(x, y, width, mouseX, mouseY);
            }
        }

        private void renderStringSetting(int x, int y, int width, int mouseX, int mouseY) {
            Color valueColor;
            Object displayValue;
            ClickInterface.this.font.drawString(this.property.getLabel(), x + 4, y + 2, TEXT_COLOR.getRGB());
            if (ClickInterface.this.editingString == this) {
                displayValue = ClickInterface.this.editingBuffer + (System.currentTimeMillis() % 1000L < 500L ? "_" : "");
                valueColor = ACCENT_COLOR.brighter();
            } else {
                String value = (String)this.property.getValue();
                displayValue = value.isEmpty() ? "..." : value;
                valueColor = ACCENT_COLOR;
            }
            int valueWidth = ClickInterface.this.font.getStringWidth((String)displayValue);
            ClickInterface.this.font.drawString((String)displayValue, x + width - valueWidth - 6, y + 2, valueColor.getRGB());
            int underlineY = y + 13;
            Color underlineColor = ClickInterface.this.editingString == this ? ACCENT_COLOR : new Color(80, 80, 80);
            Gui.drawRect(x + 6, underlineY, x + width - 6, underlineY + 1, underlineColor.getRGB());
        }

        private void renderBooleanSetting(int x, int y, int width) {
            boolean value = (Boolean)this.property.getValue();
            int switchWidth = 18;
            int switchHeight = 8;
            int switchX = x + width - switchWidth - 6;
            int switchY = y + 4;
            Color bgColor = value ? ACCENT_COLOR.darker() : new Color(70, 70, 70);
            Gui.drawRect(switchX, switchY, switchX + switchWidth, switchY + switchHeight, bgColor.getRGB());
            int knobSize = 6;
            int knobX = value ? switchX + switchWidth - knobSize - 1 : switchX + 1;
            Color knobColor = value ? ACCENT_COLOR.brighter() : new Color(140, 140, 140);
            Gui.drawRect(knobX, switchY + 1, knobX + knobSize, switchY + switchHeight - 1, knobColor.getRGB());
            ClickInterface.this.font.drawString(this.property.getLabel(), x + 4, y + 3, TEXT_COLOR.getRGB());
        }

        private void renderNumberSetting(int x, int y, int width, int mouseX, int mouseY) {
            NumberProperty numProp = (NumberProperty)this.property;
            double value = (Double)numProp.getValue();
            double min = numProp.getMin();
            double max = numProp.getMax();
            double percent = max - min > 0.0 ? (value - min) / (max - min) : 0.0;
            String valueStr = this.formatNumber(value);
            ClickInterface.this.font.drawString(this.property.getLabel(), x + 4, y + 2, TEXT_COLOR.getRGB());
            int valueWidth = ClickInterface.this.font.getStringWidth(valueStr);
            ClickInterface.this.font.drawString(valueStr, x + width - valueWidth - 6, y + 2, ACCENT_COLOR.getRGB());
            int sliderY = y + 12;
            int sliderHeight = 3;
            int sliderPadding = 6;
            int sliderLeft = x + sliderPadding;
            int sliderRight = x + width - sliderPadding;
            boolean sliderHovered = mouseX >= sliderLeft && mouseX <= sliderRight && mouseY >= sliderY - 3 && mouseY <= sliderY + sliderHeight + 3;
            Gui.drawRect(sliderLeft, sliderY, sliderRight, sliderY + sliderHeight, new Color(60, 60, 60).getRGB());
            int filledWidth = (int)((double)(sliderRight - sliderLeft) * percent);
            Gui.drawRect(sliderLeft, sliderY, sliderLeft + filledWidth, sliderY + sliderHeight, ACCENT_COLOR.getRGB());
            if (ClickInterface.this.draggingSlider == this || sliderHovered) {
                int thumbX = sliderLeft + filledWidth;
                int thumbSize = 6;
                Color thumbColor = ClickInterface.this.draggingSlider == this ? ACCENT_COLOR.brighter() : ACCENT_COLOR;
                Gui.drawRect(thumbX - thumbSize / 2, sliderY - 3, thumbX + thumbSize / 2, sliderY + sliderHeight + 3, thumbColor.getRGB());
            }
        }

        private void renderModeSetting(int x, int y, int width, int mouseX, int mouseY) {
            ModeProperty modeProp = (ModeProperty)this.property;
            String displayText = this.property.getLabel() + ": " + String.valueOf(modeProp.getValue());
            ClickInterface.this.font.drawString(displayText, x + 4, y + 3, TEXT_COLOR.getRGB());
            String arrow = this.dropdownOpen ? "\u25b2" : "\u25bc";
            ClickInterface.this.font.drawString(arrow, x + width - 10, y + 3, new Color(160, 160, 160).getRGB());
            if (this.dropdownOpen) {
                int optionY = y + 16;
                for (Enum value : modeProp.getValues()) {
                    boolean hovered;
                    boolean selected = value.equals(modeProp.getValue());
                    boolean bl = hovered = mouseX >= x + 2 && mouseX <= x + width - 2 && mouseY >= optionY && mouseY <= optionY + 12;
                    Color color = selected ? ACCENT_COLOR : (hovered ? HOVER_COLOR.brighter() : new Color(34, 34, 34, 220));
                    Gui.drawRect(x + 2, optionY, x + width - 2, optionY + 12, color.getRGB());
                    ClickInterface.this.font.drawString(value.toString(), x + 6, optionY + 2, TEXT_COLOR.getRGB());
                    optionY += 12;
                }
            }
        }

        private String formatNumber(double value) {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(value % 1.0 == 0.0 ? 0 : 2, RoundingMode.HALF_UP);
            return bd.stripTrailingZeros().toPlainString();
        }

        public boolean mouseClicked(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
            if (this.property.getType() == Boolean.class) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16) {
                    this.property.setValueObj((Boolean)this.property.getValue() == false);
                    return true;
                }
            } else if (this.property.getType() == String.class) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16) {
                    ClickInterface.this.editingString = this;
                    ClickInterface.this.editingBuffer = (String)this.property.getValue();
                    return true;
                }
            } else if (this.property instanceof NumberProperty) {
                int sliderY = y + 12;
                int sliderPadding = 6;
                int sliderLeft = x + sliderPadding;
                int sliderRight = x + width - sliderPadding;
                int sliderTop = sliderY - 4;
                int sliderBottom = sliderY + 7;
                if (mouseX >= sliderLeft && mouseX <= sliderRight && mouseY >= sliderTop && mouseY <= sliderBottom) {
                    ClickInterface.this.draggingSlider = this;
                    this.dragStartX = mouseX;
                    this.componentX = x;
                    this.componentWidth = width;
                    this.updateSlider(x, width, mouseX);
                    return true;
                }
            } else if (this.property instanceof ModeProperty) {
                ModeProperty modeProp = (ModeProperty)this.property;
                if (this.dropdownOpen) {
                    int optionY = y + 16;
                    for (Enum value : modeProp.getValues()) {
                        if (mouseX >= x + 2 && mouseX <= x + width - 2 && mouseY >= optionY && mouseY <= optionY + 12) {
                            modeProp.setValueObj(value);
                            this.dropdownOpen = false;
                            return true;
                        }
                        optionY += 12;
                    }
                }
                if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16) {
                    this.dropdownOpen = !this.dropdownOpen;
                    return true;
                }
            }
            return false;
        }

        public void mouseReleased(int mouseX, int mouseY) {
        }

        public void updateDrag(int mouseX) {
            if (this.property instanceof NumberProperty && ClickInterface.this.draggingSlider == this) {
                this.updateSlider(this.componentX, this.componentWidth, mouseX);
            }
        }

        private void updateSlider(int x, int width, int mouseX) {
            if (this.property instanceof NumberProperty) {
                NumberProperty numProp = (NumberProperty)this.property;
                int sliderPadding = 6;
                double percent = Math.max(0.0, Math.min(1.0, (double)(mouseX - (x + sliderPadding)) / (double)(width - sliderPadding * 2)));
                double range = numProp.getMax() - numProp.getMin();
                double rawValue = numProp.getMin() + range * percent;
                double increment = numProp.getIncrement();
                if (increment > 0.0) {
                    rawValue = (double)Math.round(rawValue / increment) * increment;
                }
                double finalValue = Math.max(numProp.getMin(), Math.min(numProp.getMax(), rawValue));
                numProp.setValue(finalValue);
            }
        }
    }

    private class CategoryPanel {
        private final ModuleCategory category;
        private int x;
        private int y;
        private int width = 120;
        private int headerHeight = 18;
        private boolean dragging = false;
        private int dragX;
        private int dragY;
        private float scrollOffset = 0.0f;
        private float targetScroll = 0.0f;
        private boolean draggingScrollbar = false;
        private float scrollbarDragStartY;
        private float scrollbarStartScroll;
        private final List<ModuleButton> modules = new ArrayList<ModuleButton>();
        private final List<ConfigButton> configs = new ArrayList<ConfigButton>();

        public CategoryPanel(ModuleCategory category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
            if (category == ModuleCategory.CONFIGS) {
                new Thread(() -> {
                    List<String> configList = GitHubConfigFetcher.fetchConfigList();
                    for (String configName : configList) {
                        this.configs.add(new ConfigButton(configName, this));
                    }
                }).start();
            } else {
                for (Module module : Advantage.INSTANCE.getModuleManager().getModulesForCategory(category)) {
                    this.modules.add(new ModuleButton(module, this));
                }
            }
        }

        public void render(int mouseX, int mouseY) {
            if (this.dragging) {
                this.x = mouseX - this.dragX;
                this.y = mouseY - this.dragY;
            }
            this.scrollOffset = RenderUtils.lerp(this.scrollOffset, this.targetScroll, 0.18f);
            int totalHeight = 0;
            if (this.category == ModuleCategory.CONFIGS) {
                totalHeight = this.configs.size() * 16;
            } else {
                for (ModuleButton mb : this.modules) {
                    totalHeight += mb.getTotalHeight();
                }
            }
            int maxVisibleHeight = ClickInterface.this.height - this.y - this.headerHeight - 20;
            int maxScroll = Math.max(0, totalHeight - Math.max(0, maxVisibleHeight));
            this.targetScroll = Math.max(0.0f, Math.min(this.targetScroll, (float)maxScroll));
            this.scrollOffset = Math.max(0.0f, Math.min(this.scrollOffset, (float)maxScroll));
            Gui.drawRect(this.x, this.y, this.x + this.width, this.y + this.headerHeight, PANEL_BG.getRGB());
            Gui.drawRect(this.x, this.y + this.headerHeight - 1, this.x + this.width, this.y + this.headerHeight, new Color(40, 40, 40).getRGB());
            ClickInterface.this.font.drawString(this.category.name(), this.x + 6, this.y + 4, TEXT_COLOR.getRGB());
            int bodyHeight = Math.min(totalHeight, maxVisibleHeight);
            Gui.drawRect(this.x, this.y + this.headerHeight, this.x + this.width, this.y + this.headerHeight + bodyHeight, BG_COLOR.getRGB());
            RenderUtils.startScissor(this.x, this.y + this.headerHeight, this.width, bodyHeight);
            if (this.category == ModuleCategory.CONFIGS) {
                int configY = this.y + this.headerHeight - (int)this.scrollOffset;
                for (ConfigButton cb : this.configs) {
                    cb.render(this.x, configY, this.width, mouseX, mouseY);
                    configY += cb.getTotalHeight();
                }
            } else {
                int moduleY = this.y + this.headerHeight - (int)this.scrollOffset;
                for (ModuleButton mb : this.modules) {
                    mb.render(this.x, moduleY, this.width, mouseX, mouseY);
                    moduleY += mb.getTotalHeight();
                }
            }
            RenderUtils.endScissor();
            if (totalHeight > maxVisibleHeight) {
                this.drawScrollbar(this.y + this.headerHeight, bodyHeight, totalHeight, maxScroll);
            }
        }

        private void drawScrollbar(int startY, int visibleHeight, int totalHeight, int maxScroll) {
            int scrollbarX = this.x + this.width - 6;
            int scrollbarWidth = 6;
            Gui.drawRect(scrollbarX, startY, scrollbarX + scrollbarWidth, startY + visibleHeight, new Color(30, 30, 30, 180).getRGB());
            float thumbSize = Math.max(24.0f, (float)visibleHeight / (float)totalHeight * (float)visibleHeight);
            float thumbPos = maxScroll > 0 ? this.scrollOffset / (float)maxScroll * ((float)visibleHeight - thumbSize) : 0.0f;
            int y1 = (int)((float)startY + thumbPos);
            int y2 = (int)((float)startY + thumbPos + thumbSize);
            Gui.drawRect(scrollbarX + 1, y1, scrollbarX + scrollbarWidth - 1, y2, ACCENT_COLOR.getRGB());
            Gui.drawRect(scrollbarX, y1, scrollbarX + 1, y2, new Color(0, 0, 0, 120).getRGB());
            Gui.drawRect(scrollbarX + scrollbarWidth - 1, y1, scrollbarX + scrollbarWidth, y2, new Color(0, 0, 0, 120).getRGB());
        }

        public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.headerHeight && mouseButton == 0) {
                this.dragging = true;
                this.dragX = mouseX - this.x;
                this.dragY = mouseY - this.y;
                return true;
            }
            int totalHeight = 0;
            if (this.category == ModuleCategory.CONFIGS) {
                totalHeight = this.configs.size() * 16;
            } else {
                for (ModuleButton mb : this.modules) {
                    totalHeight += mb.getTotalHeight();
                }
            }
            int maxVisibleHeight = ClickInterface.this.height - this.y - this.headerHeight - 20;
            int maxScroll = Math.max(0, totalHeight - Math.max(0, maxVisibleHeight));
            if (totalHeight > maxVisibleHeight) {
                int scrollbarX = this.x + this.width - 6;
                int scrollbarWidth = 6;
                float thumbSize = Math.max(24.0f, (float)maxVisibleHeight / (float)totalHeight * (float)maxVisibleHeight);
                float thumbPos = maxScroll > 0 ? this.scrollOffset / (float)maxScroll * ((float)maxVisibleHeight - thumbSize) : 0.0f;
                int y1 = (int)((float)(this.y + this.headerHeight) + thumbPos);
                int y2 = (int)((float)(this.y + this.headerHeight) + thumbPos + thumbSize);
                if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth && mouseY >= y1 && mouseY <= y2 && mouseButton == 0) {
                    this.draggingScrollbar = true;
                    this.scrollbarDragStartY = mouseY;
                    this.scrollbarStartScroll = this.scrollOffset;
                    return true;
                }
            }
            if (this.category == ModuleCategory.CONFIGS) {
                int configY = this.y + this.headerHeight - (int)this.scrollOffset;
                for (ConfigButton cb : this.configs) {
                    if (cb.mouseClicked(this.x, configY, this.width, mouseX, mouseY, mouseButton)) {
                        return true;
                    }
                    configY += cb.getTotalHeight();
                }
            } else {
                int moduleY = this.y + this.headerHeight - (int)this.scrollOffset;
                int maxY = this.y + this.headerHeight + (ClickInterface.this.height - this.y - this.headerHeight - 20);
                for (ModuleButton mb : this.modules) {
                    int moduleHeight = mb.getTotalHeight();
                    if (moduleY + moduleHeight > this.y + this.headerHeight && moduleY < maxY && mb.mouseClicked(this.x, moduleY, this.width, mouseX, mouseY, mouseButton)) {
                        return true;
                    }
                    moduleY += moduleHeight;
                }
            }
            return false;
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            this.dragging = false;
            this.draggingScrollbar = false;
            for (ModuleButton mb : this.modules) {
                mb.mouseReleased(mouseX, mouseY, state);
            }
        }

        public void handleScroll(int mouseX, int mouseY, int wheel) {
            int scrollAmount = wheel > 0 ? 15 : -15;
            this.targetScroll -= (float)scrollAmount;
        }

        public void updateDrag(int mouseX, int mouseY) {
            if (this.draggingScrollbar) {
                int totalHeight = 0;
                if (this.category == ModuleCategory.CONFIGS) {
                    totalHeight = this.configs.size() * 16;
                } else {
                    for (ModuleButton mb : this.modules) {
                        totalHeight += mb.getTotalHeight();
                    }
                }
                int maxVisibleHeight = ClickInterface.this.height - this.y - this.headerHeight - 20;
                int maxScroll = Math.max(0, totalHeight - Math.max(0, maxVisibleHeight));
                if (maxScroll > 0) {
                    float thumbTrack = (float)maxVisibleHeight - Math.max(24.0f, (float)maxVisibleHeight / (float)totalHeight * (float)maxVisibleHeight);
                    if (thumbTrack <= 0.0f) {
                        return;
                    }
                    float dy = (float)mouseY - this.scrollbarDragStartY;
                    float scrollDelta = dy / thumbTrack * (float)maxScroll;
                    this.targetScroll = Math.max(0.0f, Math.min((float)maxScroll, this.scrollbarStartScroll + scrollDelta));
                }
            }
        }

        public void clampToScreen() {
            int minX = -this.width + 30;
            int maxX = ClickInterface.this.width - 30;
            if (this.x < minX) {
                this.x = minX;
            }
            if (this.x > maxX) {
                this.x = maxX;
            }
        }
    }

    private class ConfigButton {
        private final String configName;
        private final CategoryPanel parent;

        public ConfigButton(String configName, CategoryPanel parent) {
            this.configName = configName;
            this.parent = parent;
        }

        public int getTotalHeight() {
            return 16;
        }

        public void render(int x, int y, int width, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16;
            Color bgColor = hovered ? HOVER_COLOR : BG_COLOR;
            Gui.drawRect(x, y, x + width, y + 16, bgColor.getRGB());
            ClickInterface.this.font.drawString(this.configName, x + 4, y + 4, TEXT_COLOR.getRGB());
            ClickInterface.this.font.drawString("\u2193", x + width - 10, y + 4, ACCENT_COLOR.getRGB());
        }

        public boolean mouseClicked(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
            boolean hovered;
            boolean bl = hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16;
            if (hovered && mouseButton == 0) {
                new Thread(() -> {
                    boolean success = GitHubConfigFetcher.downloadAndLoadConfig(this.configName);
                    if (success) {
                        Logger.chatPrint("Config '" + this.configName + "' downloaded and loaded.");
                    } else {
                        Logger.chatPrint("Failed to download config '" + this.configName + "'.");
                    }
                }).start();
                return true;
            }
            return false;
        }
    }

    private class ModuleButton {
        private final Module module;
        private final CategoryPanel parent;
        private boolean expanded = false;
        private final List<SettingComponent> settings = new ArrayList<SettingComponent>();

        public ModuleButton(Module module, CategoryPanel parent) {
            this.module = module;
            this.parent = parent;
            for (Property property : module.getElements()) {
                this.settings.add(new SettingComponent(property));
            }
        }

        public int getTotalHeight() {
            int h = 16;
            if (this.expanded) {
                for (SettingComponent s : this.settings) {
                    if (!s.property.isAvailable()) continue;
                    h += s.getHeight();
                }
            }
            return h;
        }

        public void render(int x, int y, int width, int mouseX, int mouseY) {
            boolean hovered;
            boolean bl = hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16;
            Color bgColor = this.module.isEnabled() ? ACCENT_COLOR.darker() : (hovered ? HOVER_COLOR : BG_COLOR);
            Gui.drawRect(x, y, x + width, y + 16, bgColor.getRGB());
            String name = this.module == ClickInterface.this.listeningModule ? "Listening..." : this.module.getLabel();
            Color textColor = this.module.isEnabled() ? Color.WHITE : TEXT_COLOR;
            ClickInterface.this.font.drawString(name, x + 4, y + 4, textColor.getRGB());
            if (this.module.getKey() != 0 && this.module != ClickInterface.this.listeningModule) {
                String keyName = Keyboard.getKeyName(this.module.getKey());
                int keyWidth = ClickInterface.this.font.getStringWidth(keyName);
                int textX = x + width - keyWidth - (this.settings.isEmpty() ? 6 : 18);
                ClickInterface.this.font.drawString(keyName, textX, y + 4, new Color(160, 160, 160, 200).getRGB());
            }
            if (!this.settings.isEmpty()) {
                String arrow = this.expanded ? "\u25bc" : "\u25b6";
                ClickInterface.this.font.drawString(arrow, x + width - 10, y + 4, new Color(160, 160, 160).getRGB());
            }
            if (this.expanded) {
                int settingY = y + 16;
                for (SettingComponent s : this.settings) {
                    if (!s.property.isAvailable()) continue;
                    s.render(x, settingY, width, mouseX, mouseY);
                    settingY += s.getHeight();
                }
            }
        }

        public boolean mouseClicked(int x, int y, int width, int mouseX, int mouseY, int mouseButton) {
            boolean hovered;
            boolean bl = hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 16;
            if (hovered) {
                if (mouseButton == 0) {
                    this.module.toggle();
                } else if (mouseButton == 1) {
                    if (!this.settings.isEmpty()) {
                        this.expanded = !this.expanded;
                    }
                } else if (mouseButton == 2) {
                    ClickInterface.this.listeningModule = this.module;
                }
                return true;
            }
            if (this.expanded) {
                int settingY = y + 16;
                for (SettingComponent s : this.settings) {
                    if (!s.property.isAvailable()) continue;
                    if (s.mouseClicked(x, settingY, width, mouseX, mouseY, mouseButton)) {
                        return true;
                    }
                    settingY += s.getHeight();
                }
            }
            return false;
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            if (this.expanded) {
                for (SettingComponent s : this.settings) {
                    s.mouseReleased(mouseX, mouseY);
                }
            }
        }
    }
}

