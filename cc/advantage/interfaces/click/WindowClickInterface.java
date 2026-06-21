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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class WindowClickInterface
extends GuiScreen {
    private final CustomFontRenderer font = FontUtils.getFont("bold");
    private final CustomFontRenderer titleFont = FontUtils.getFont("big");
    private int windowX = 100;
    private int windowY = 100;
    private final int windowWidth = 480;
    private final int windowHeight = 300;
    private boolean dragging = false;
    private int dragX;
    private int dragY;
    private static final int SIDEBAR_WIDTH = 130;
    private static final int HEADER_HEIGHT = 35;
    private static final int CONTENT_PADDING = 12;
    private static final int MODULE_ITEM_HEIGHT = 32;
    private static final int CONFIG_ITEM_HEIGHT = 28;
    private static final Color WINDOW_BG = new Color(17, 17, 17, 250);
    private static final Color HEADER_BG = new Color(20, 20, 20, 255);
    private static final Color SIDEBAR_BG = new Color(19, 19, 19, 245);
    private static final Color CONTENT_BG = new Color(24, 24, 24, 240);
    private static Color ACCENT_COLOR = new Color(120, 145, 255);
    private static final Color TEXT_COLOR = new Color(210, 210, 210);
    private static final Color SECONDARY_TEXT = new Color(140, 140, 140);
    private static final Color SEPARATOR_COLOR = new Color(35, 35, 35);
    private static final Color HOVER_COLOR = new Color(32, 32, 32);
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private Module selectedModule = null;
    private Module listeningModule = null;
    private SettingComponent draggingSlider = null;
    private SettingComponent editingString = null;
    private String editingBuffer = "";
    private volatile List<ConfigItem> configList = new ArrayList<ConfigItem>();
    private volatile boolean configsLoading = true;
    private boolean configLoadStarted = false;
    private float moduleScrollOffset = 0.0f;
    private float moduleTargetScroll = 0.0f;
    private float settingScrollOffset = 0.0f;
    private float settingTargetScroll = 0.0f;
    private float configScrollOffset = 0.0f;
    private float configTargetScroll = 0.0f;
    private final Map<Property<?>, SettingComponent> componentCache = new HashMap();

    @Override
    public void initGui() {
        List<Module> modules;
        if (this.selectedModule == null && !(modules = Advantage.INSTANCE.getModuleManager().getModulesForCategory(this.selectedCategory)).isEmpty()) {
            this.selectedModule = modules.get(0);
        }
        if (!this.configLoadStarted) {
            this.configLoadStarted = true;
            this.configsLoading = true;
            new Thread(() -> {
                try {
                    List<String> configs = GitHubConfigFetcher.fetchConfigList();
                    ArrayList<ConfigItem> loadedConfigs = new ArrayList<ConfigItem>(configs.size());
                    for (String configName : configs) {
                        loadedConfigs.add(new ConfigItem(configName));
                    }
                    this.configList = loadedConfigs;
                    this.configsLoading = false;
                }
                catch (Exception e) {
                    Logger.chatError("Failed to load configs: " + e.getMessage());
                    this.configsLoading = false;
                }
            }, "Advantage-Config-Loader").start();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        ACCENT_COLOR = ColorProcess.getColor();
        ClientSettingsModule.renderAnimeImage(this.width, this.height);
        if (this.dragging) {
            this.windowX = WindowClickInterface.clamp(mouseX - this.dragX, 0, Math.max(0, this.width - 480));
            this.windowY = WindowClickInterface.clamp(mouseY - this.dragY, 0, Math.max(0, this.height - 300));
        }
        if (this.draggingSlider != null) {
            this.draggingSlider.updateDrag(mouseX);
        }
        this.moduleScrollOffset = RenderUtils.lerp(this.moduleScrollOffset, this.moduleTargetScroll, 0.18f);
        this.settingScrollOffset = RenderUtils.lerp(this.settingScrollOffset, this.settingTargetScroll, 0.18f);
        this.configScrollOffset = RenderUtils.lerp(this.configScrollOffset, this.configTargetScroll, 0.18f);
        this.drawWindow(mouseX, mouseY);
    }

    private void drawWindow(int mouseX, int mouseY) {
        RenderUtils.drawRoundedRect(this.windowX - 3, this.windowY - 3, 486.0f, 306.0f, 6.0f, new Color(0, 0, 0, 80));
        RenderUtils.drawRoundedRect(this.windowX, this.windowY, 480.0f, 300.0f, 5.0f, WINDOW_BG);
        RenderUtils.drawRoundedRect(this.windowX, this.windowY, 480.0f, 35.0f, 5.0f, HEADER_BG);
        WindowClickInterface.drawRect(this.windowX, this.windowY + 35 - 5, this.windowX + 480, this.windowY + 35, HEADER_BG.getRGB());
        this.titleFont.drawString("Advantage Client", this.windowX + 12, this.windowY + 11, ACCENT_COLOR.getRGB());
        int closeX = this.windowX + 480 - 25;
        int closeY = this.windowY + 8;
        boolean closeHovered = mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18;
        RenderUtils.drawRoundedRect(closeX, closeY, 18.0f, 18.0f, 3.0f, closeHovered ? new Color(220, 60, 60) : new Color(60, 60, 60));
        this.font.drawString("\u00d7", closeX + 5, closeY + 3, Color.WHITE.getRGB());
        WindowClickInterface.drawRect(this.windowX, this.windowY + 35, this.windowX + 480, this.windowY + 35 + 1, SEPARATOR_COLOR.getRGB());
        this.drawSidebar(mouseX, mouseY);
        int sidebarX = this.windowX + 130;
        WindowClickInterface.drawRect(sidebarX, this.windowY + 35, sidebarX + 1, this.windowY + 300, SEPARATOR_COLOR.getRGB());
        int contentX = sidebarX + 1;
        int contentY = this.windowY + 35 + 1;
        int contentWidth = 349;
        int contentHeight = 264;
        if (this.selectedCategory == ModuleCategory.CONFIGS) {
            this.drawConfigList(contentX, contentY, contentWidth, contentHeight, mouseX, mouseY);
        } else {
            int moduleListWidth = (int)((double)contentWidth * 0.35);
            this.drawModuleList(contentX, contentY, moduleListWidth, contentHeight, mouseX, mouseY);
            int settingsX = contentX + moduleListWidth + 1;
            WindowClickInterface.drawRect(settingsX - 1, contentY, settingsX, contentY + contentHeight, SEPARATOR_COLOR.getRGB());
            this.drawSettings(settingsX, contentY, contentWidth - moduleListWidth - 1, contentHeight, mouseX, mouseY);
        }
    }

    private void drawSidebar(int mouseX, int mouseY) {
        int sidebarX = this.windowX;
        int sidebarY = this.windowY + 35 + 1;
        int sidebarHeight = 264;
        RenderUtils.drawRoundedRect(sidebarX, sidebarY, 130.0f, sidebarHeight, 0.0f, SIDEBAR_BG);
        int categoryY = sidebarY + 12;
        for (ModuleCategory category : ModuleCategory.values()) {
            Color bgColor;
            boolean hovered;
            boolean selected = this.selectedCategory == category;
            boolean bl = hovered = mouseX >= sidebarX && mouseX <= sidebarX + 130 && mouseY >= categoryY && mouseY <= categoryY + 28;
            Color color = selected ? new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 40) : (bgColor = hovered ? HOVER_COLOR : new Color(0, 0, 0, 0));
            if (selected || hovered) {
                RenderUtils.drawRoundedRect(sidebarX + 6, categoryY, 118.0f, 28.0f, 3.0f, bgColor);
            }
            if (selected) {
                WindowClickInterface.drawRect(sidebarX + 4, categoryY + 8, sidebarX + 6, categoryY + 20, ACCENT_COLOR.getRGB());
            }
            Color textColor = selected ? ACCENT_COLOR : (hovered ? TEXT_COLOR : SECONDARY_TEXT);
            this.font.drawString(category.name(), sidebarX + 14, categoryY + 9, textColor.getRGB());
            categoryY += 32;
        }
    }

    private void drawModuleList(int x, int y, int width, int height, int mouseX, int mouseY) {
        WindowClickInterface.drawRect(x, y, x + width, y + height, CONTENT_BG.getRGB());
        List<Module> modules = Advantage.INSTANCE.getModuleManager().getModulesForCategory(this.selectedCategory);
        int totalHeight = modules.size() * 32;
        int maxScroll = Math.max(0, totalHeight - (height - 24));
        this.moduleTargetScroll = WindowClickInterface.clamp(this.moduleTargetScroll, 0.0f, (float)maxScroll);
        RenderUtils.startScissor(x, y + 12, width, height - 24);
        int moduleY = y + 12 - (int)this.moduleScrollOffset;
        for (Module module : modules) {
            Color bgColor;
            boolean hovered;
            boolean selected = module == this.selectedModule;
            boolean bl = hovered = mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + 32;
            Color color = selected ? new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 30) : (bgColor = hovered ? HOVER_COLOR : new Color(0, 0, 0, 0));
            if (selected || hovered) {
                RenderUtils.drawRoundedRect(x + 8, moduleY, width - 16, 32.0f, 4.0f, bgColor);
            }
            int indicatorSize = 8;
            int indicatorX = x + 16;
            int indicatorY = moduleY + (32 - indicatorSize) / 2;
            Color indicatorColor = module.isEnabled() ? ACCENT_COLOR : new Color(60, 60, 60);
            RenderUtils.drawCircle((double)indicatorX + (double)indicatorSize / 2.0, (double)indicatorY + (double)indicatorSize / 2.0, (double)indicatorSize / 2.0, indicatorColor.getRGB());
            String name = module == this.listeningModule ? "Press a key..." : module.getLabel();
            Color textColor = module.isEnabled() ? TEXT_COLOR.brighter() : SECONDARY_TEXT;
            this.font.drawString(name, x + 30, moduleY + 11, textColor.getRGB());
            if (module.getKey() != 0 && module != this.listeningModule) {
                String keyName = Keyboard.getKeyName(module.getKey());
                int keyWidth = this.font.getStringWidth(keyName);
                RenderUtils.drawRoundedRect(x + width - keyWidth - 20, moduleY + 8, keyWidth + 10, 16.0f, 3.0f, new Color(40, 40, 40));
                this.font.drawString(keyName, x + width - keyWidth - 15, moduleY + 10, SECONDARY_TEXT.getRGB());
            }
            moduleY += 32;
        }
        RenderUtils.endScissor();
        if (totalHeight > height - 24) {
            this.drawScrollbar(x + width - 8, y + 12, height - 24, totalHeight, this.moduleScrollOffset);
        }
    }

    private void drawConfigList(int x, int y, int width, int height, int mouseX, int mouseY) {
        WindowClickInterface.drawRect(x, y, x + width, y + height, CONTENT_BG.getRGB());
        if (this.configsLoading) {
            String loadingText = "Loading configs...";
            int textWidth = this.font.getStringWidth(loadingText);
            this.font.drawString(loadingText, (float)x + (float)(width - textWidth) / 2.0f, (float)y + (float)height / 2.0f - 5.0f, SECONDARY_TEXT.getRGB());
            return;
        }
        if (this.configList.isEmpty()) {
            String emptyText = "No configs available";
            int textWidth = this.font.getStringWidth(emptyText);
            this.font.drawString(emptyText, (float)x + (float)(width - textWidth) / 2.0f, (float)y + (float)height / 2.0f - 5.0f, SECONDARY_TEXT.getRGB());
            return;
        }
        int totalHeight = this.configList.size() * 28;
        int maxScroll = Math.max(0, totalHeight - (height - 24));
        this.configTargetScroll = WindowClickInterface.clamp(this.configTargetScroll, 0.0f, (float)maxScroll);
        RenderUtils.startScissor(x, y + 12, width, height - 24);
        int configY = y + 12 - (int)this.configScrollOffset;
        for (ConfigItem config : this.configList) {
            boolean buttonHovered;
            boolean hovered;
            boolean bl = hovered = mouseX >= x + 12 && mouseX <= x + width - 12 && mouseY >= configY && mouseY <= configY + 28;
            if (hovered) {
                RenderUtils.drawRoundedRect(x + 12, configY, width - 24, 28.0f, 4.0f, HOVER_COLOR);
            }
            RenderUtils.drawRoundedRect(x + 12 + 8, configY + 6, 16.0f, 16.0f, 3.0f, new Color(40, 40, 40));
            this.font.drawString("C", x + 12 + 13, configY + 9, ACCENT_COLOR.getRGB());
            this.font.drawString(config.name, x + 12 + 32, configY + 9, TEXT_COLOR.getRGB());
            int buttonX = x + width - 12 - 80;
            int buttonY = configY + 6;
            boolean bl2 = buttonHovered = mouseX >= buttonX && mouseX <= buttonX + 75 && mouseY >= buttonY && mouseY <= buttonY + 16;
            Color buttonColor = config.downloading ? new Color(80, 80, 80) : (buttonHovered ? ACCENT_COLOR.brighter() : ACCENT_COLOR);
            RenderUtils.drawRoundedRect(buttonX, buttonY, 75.0f, 16.0f, 3.0f, buttonColor);
            String buttonText = config.downloading ? "Loading..." : "Download";
            int textWidth = this.font.getStringWidth(buttonText);
            this.font.drawString(buttonText, (float)buttonX + (float)(75 - textWidth) / 2.0f, buttonY + 4, Color.WHITE.getRGB());
            configY += 28;
        }
        RenderUtils.endScissor();
        if (totalHeight > height - 24) {
            this.drawScrollbar(x + width - 8, y + 12, height - 24, totalHeight, this.configScrollOffset);
        }
    }

    private void drawSettings(int x, int y, int width, int height, int mouseX, int mouseY) {
        WindowClickInterface.drawRect(x, y, x + width, y + height, CONTENT_BG.getRGB());
        if (this.selectedModule == null) {
            String text = "Select a module";
            int textWidth = this.font.getStringWidth(text);
            this.font.drawString(text, (float)x + (float)(width - textWidth) / 2.0f, (float)y + (float)height / 2.0f - 5.0f, SECONDARY_TEXT.getRGB());
            return;
        }
        int headerY = y + 12;
        this.titleFont.drawString(this.selectedModule.getLabel(), x + 12, headerY, TEXT_COLOR.getRGB());
        if (this.selectedModule.getDescription() != null && !this.selectedModule.getDescription().isEmpty()) {
            this.font.drawString(this.selectedModule.getDescription(), x + 12, headerY + 16, SECONDARY_TEXT.getRGB());
        }
        WindowClickInterface.drawRect(x + 12, headerY + 32, x + width - 12, headerY + 33, SEPARATOR_COLOR.getRGB());
        int settingsY = headerY + 40;
        int availableHeight = height - settingsY + y - 12;
        List properties = this.selectedModule.getElements();
        int totalHeight = 0;
        for (Property property : properties) {
            if (!property.isAvailable()) continue;
            totalHeight += this.getPropertyHeight(property) + 8;
        }
        int maxScroll = Math.max(0, totalHeight - availableHeight);
        this.settingTargetScroll = WindowClickInterface.clamp(this.settingTargetScroll, 0.0f, (float)maxScroll);
        RenderUtils.startScissor(x, settingsY, width, availableHeight);
        int currentY = settingsY - (int)this.settingScrollOffset;
        for (Property property : properties) {
            if (!property.isAvailable()) continue;
            int propHeight = this.getPropertyHeight(property);
            this.drawProperty(property, x + 12, currentY, width - 24, mouseX, mouseY);
            currentY += propHeight + 8;
        }
        RenderUtils.endScissor();
        if (totalHeight > availableHeight) {
            this.drawScrollbar(x + width - 8, settingsY, availableHeight, totalHeight, this.settingScrollOffset);
        }
    }

    private void drawScrollbar(int x, int y, int height, int totalHeight, float scrollOffset) {
        int scrollbarWidth = 6;
        RenderUtils.drawRoundedRect(x, y, scrollbarWidth, height, 3.0f, new Color(30, 30, 30, 100));
        float trackHeight = Math.max(1, height);
        float thumbSize = Math.max(20.0f, trackHeight / (float)totalHeight * trackHeight);
        float maxScroll = Math.max(0, totalHeight - height);
        float thumbPos = maxScroll > 0.0f ? scrollOffset / maxScroll * ((float)height - thumbSize) : 0.0f;
        RenderUtils.drawRoundedRect(x + 1, (float)y + thumbPos, scrollbarWidth - 2, thumbSize, 2.0f, ACCENT_COLOR);
    }

    private int getPropertyHeight(Property<?> property) {
        SettingComponent component = this.findOrCreateComponent(property);
        if (property instanceof ModeProperty && component.dropdownOpen) {
            return 30 + ((ModeProperty)property).getValues().length * 20;
        }
        if (property instanceof NumberProperty) {
            return 36;
        }
        return 26;
    }

    private void drawProperty(Property<?> property, int x, int y, int width, int mouseX, int mouseY) {
        SettingComponent component = this.findOrCreateComponent(property);
        this.font.drawString(property.getLabel(), x, y + 2, TEXT_COLOR.getRGB());
        if (property.getType() == Boolean.class) {
            this.drawBooleanProperty(property, x, y, width);
        } else if (property instanceof NumberProperty) {
            this.drawNumberProperty((NumberProperty)property, component, x, y, width, mouseX, mouseY);
        } else if (property instanceof ModeProperty) {
            this.drawModeProperty((ModeProperty)property, component, x, y, width, mouseX, mouseY);
        } else if (property.getType() == String.class) {
            this.drawStringProperty(property, component, x, y, width);
        }
    }

    private void drawBooleanProperty(Property<?> property, int x, int y, int width) {
        boolean value = (Boolean)property.getValue();
        int switchWidth = 32;
        int switchHeight = 16;
        int switchX = x + width - switchWidth;
        int switchY = y + 3;
        Color bgColor = value ? ACCENT_COLOR.darker() : new Color(50, 50, 50);
        RenderUtils.drawRoundedRect(switchX, switchY, switchWidth, switchHeight, 8.0f, bgColor);
        int knobSize = 12;
        int knobX = value ? switchX + switchWidth - knobSize - 2 : switchX + 2;
        Color knobColor = value ? Color.WHITE : new Color(100, 100, 100);
        RenderUtils.drawCircle((double)knobX + (double)knobSize / 2.0, (double)switchY + (double)switchHeight / 2.0, (double)knobSize / 2.0, knobColor.getRGB());
    }

    private void drawNumberProperty(NumberProperty property, SettingComponent component, int x, int y, int width, int mouseX, int mouseY) {
        String valueStr = this.formatNumber((Double)property.getValue());
        int valueWidth = this.font.getStringWidth(valueStr);
        this.font.drawString(valueStr, x + width - valueWidth, y + 2, ACCENT_COLOR.getRGB());
        int sliderY = y + 20;
        int sliderHeight = 6;
        double percent = ((Double)property.getValue() - property.getMin()) / (property.getMax() - property.getMin());
        RenderUtils.drawRoundedRect(x, sliderY, width, sliderHeight, 3.0f, new Color(40, 40, 40));
        RenderUtils.drawRoundedRect(x, sliderY, (float)((double)width * percent), sliderHeight, 3.0f, ACCENT_COLOR);
        int thumbSize = 12;
        int thumbX = (int)((double)x + (double)width * percent - (double)thumbSize / 2.0);
        boolean thumbHovered = mouseX >= thumbX && mouseX <= thumbX + thumbSize && mouseY >= sliderY - 3 && mouseY <= sliderY + sliderHeight + 3;
        Color thumbColor = this.draggingSlider == component || thumbHovered ? ACCENT_COLOR.brighter() : ACCENT_COLOR;
        RenderUtils.drawCircle((double)thumbX + (double)thumbSize / 2.0, (double)sliderY + (double)sliderHeight / 2.0, (double)thumbSize / 2.0, thumbColor.getRGB());
        component.componentX = x;
        component.componentWidth = width;
    }

    private void drawModeProperty(ModeProperty<?> property, SettingComponent component, int x, int y, int width, int mouseX, int mouseY) {
        String value = ((Enum)property.getValue()).toString();
        int valueWidth = this.font.getStringWidth(value);
        String arrow = component.dropdownOpen ? "\u25b2" : "\u25bc";
        int arrowWidth = this.font.getStringWidth(arrow);
        this.font.drawString(arrow, x + width - arrowWidth, y + 2, SECONDARY_TEXT.getRGB());
        this.font.drawString(value, x + width - valueWidth - arrowWidth - 8, y + 2, ACCENT_COLOR.getRGB());
        if (component.dropdownOpen) {
            Enum[] values2;
            int dropdownY = y + 22;
            for (Enum val : values2 = property.getValues()) {
                boolean hovered;
                boolean selected = val.equals(property.getValue());
                boolean bl = hovered = mouseX >= x && mouseX <= x + width && mouseY >= dropdownY && mouseY <= dropdownY + 20;
                Color bgColor = selected ? new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(), ACCENT_COLOR.getBlue(), 50) : (hovered ? new Color(40, 40, 40) : new Color(30, 30, 30));
                RenderUtils.drawRoundedRect(x, dropdownY, width, 20.0f, 3.0f, bgColor);
                this.font.drawString(val.toString(), x + 8, dropdownY + 6, selected ? ACCENT_COLOR.getRGB() : TEXT_COLOR.getRGB());
                dropdownY += 20;
            }
        }
    }

    private void drawStringProperty(Property<?> property, SettingComponent component, int x, int y, int width) {
        Color valueColor;
        Object displayValue;
        if (this.editingString == component) {
            displayValue = this.editingBuffer + (System.currentTimeMillis() % 1000L < 500L ? "|" : "");
            valueColor = ACCENT_COLOR.brighter();
        } else {
            String value = (String)property.getValue();
            displayValue = value.isEmpty() ? "Click to edit" : value;
            valueColor = value.isEmpty() ? SECONDARY_TEXT : ACCENT_COLOR;
        }
        int boxWidth = width - this.font.getStringWidth(property.getLabel()) - 8;
        int boxX = x + width - boxWidth;
        Color boxColor = this.editingString == component ? new Color(40, 40, 40) : new Color(35, 35, 35);
        RenderUtils.drawRoundedRect(boxX, y, boxWidth, 18.0f, 3.0f, boxColor);
        int textWidth = this.font.getStringWidth((String)displayValue);
        this.font.drawString((String)displayValue, (float)boxX + (float)(boxWidth - textWidth) / 2.0f, y + 5, valueColor.getRGB());
    }

    private String formatNumber(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(value % 1.0 == 0.0 ? 0 : 2, RoundingMode.HALF_UP);
        return bd.stripTrailingZeros().toPlainString();
    }

    private SettingComponent findOrCreateComponent(Property<?> property) {
        return this.componentCache.computeIfAbsent(property, SettingComponent::new);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.clearTransientState();
        int closeX = this.windowX + 480 - 25;
        int closeY = this.windowY + 8;
        if (mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (mouseX >= this.windowX && mouseX <= this.windowX + 480 && mouseY >= this.windowY && mouseY <= this.windowY + 35) {
            this.dragging = true;
            this.dragX = mouseX - this.windowX;
            this.dragY = mouseY - this.windowY;
            return;
        }
        int sidebarY = this.windowY + 35 + 1 + 12;
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mouseX >= this.windowX && mouseX <= this.windowX + 130 && mouseY >= sidebarY && mouseY <= sidebarY + 28) {
                List<Module> modules;
                this.selectedCategory = category;
                this.selectedModule = null;
                this.listeningModule = null;
                this.moduleTargetScroll = 0.0f;
                this.settingTargetScroll = 0.0f;
                this.configTargetScroll = 0.0f;
                if (this.selectedCategory != ModuleCategory.CONFIGS && !(modules = Advantage.INSTANCE.getModuleManager().getModulesForCategory(this.selectedCategory)).isEmpty()) {
                    this.selectedModule = modules.get(0);
                }
                return;
            }
            sidebarY += 32;
        }
        int contentX = this.windowX + 130 + 1;
        int contentY = this.windowY + 35 + 1;
        if (this.selectedCategory == ModuleCategory.CONFIGS) {
            this.handleConfigClick(contentX, contentY, mouseX, mouseY, mouseButton);
        } else {
            this.handleModuleClick(contentX, contentY, mouseX, mouseY, mouseButton);
            this.handleSettingsClick(mouseX, mouseY);
        }
    }

    private void handleConfigClick(int contentX, int contentY, int mouseX, int mouseY, int mouseButton) {
        if (this.configsLoading || this.configList.isEmpty()) {
            return;
        }
        int configY = contentY + 12 - (int)this.configScrollOffset;
        for (ConfigItem config : this.configList) {
            int buttonX = contentX + 480 - 130 - 1 - 12 - 80;
            int buttonY = configY + 6;
            if (mouseX >= buttonX && mouseX <= buttonX + 75 && mouseY >= buttonY && mouseY <= buttonY + 16 && mouseButton == 0 && !config.downloading) {
                config.downloading = true;
                new Thread(() -> {
                    boolean success = GitHubConfigFetcher.downloadAndLoadConfig(config.name);
                    config.downloading = false;
                    if (success) {
                        Logger.chatPrint("\u00a7aConfig '" + config.name + "' loaded successfully!");
                    } else {
                        Logger.chatPrint("\u00a7cFailed to load config '" + config.name + "'.");
                    }
                }).start();
                return;
            }
            configY += 28;
        }
    }

    private void handleModuleClick(int contentX, int contentY, int mouseX, int mouseY, int mouseButton) {
        int moduleListWidth = 122;
        if (mouseX < contentX || mouseX > contentX + moduleListWidth) {
            return;
        }
        List<Module> modules = Advantage.INSTANCE.getModuleManager().getModulesForCategory(this.selectedCategory);
        int moduleY = contentY + 12 - (int)this.moduleScrollOffset;
        for (Module module : modules) {
            if (mouseY >= moduleY && mouseY <= moduleY + 32) {
                if (mouseButton == 1) {
                    this.selectedModule = module;
                    this.settingTargetScroll = 0.0f;
                } else if (mouseButton == 0) {
                    module.toggle();
                } else if (mouseButton == 2) {
                    this.listeningModule = module;
                }
                return;
            }
            moduleY += 32;
        }
    }

    private void handleSettingsClick(int mouseX, int mouseY) {
        if (this.selectedModule == null) {
            return;
        }
        int contentX = this.windowX + 130 + 1;
        int moduleListWidth = 122;
        int settingsX = contentX + moduleListWidth + 1;
        int contentY = this.windowY + 35 + 1;
        int headerY = contentY + 12;
        int settingsY = headerY + 40;
        int settingsWidth = 349 - moduleListWidth - 1;
        int currentY = settingsY - (int)this.settingScrollOffset;
        for (Property property : this.selectedModule.getElements()) {
            int boxWidth;
            int boxX;
            if (!property.isAvailable()) continue;
            SettingComponent component = this.findOrCreateComponent(property);
            int propHeight = this.getPropertyHeight(property);
            if (property.getType() == Boolean.class) {
                int switchX = settingsX + settingsWidth - 12 - 32;
                int switchY = currentY + 3;
                if (mouseX >= switchX && mouseX <= switchX + 32 && mouseY >= switchY && mouseY <= switchY + 16) {
                    property.setValueObj((Boolean)property.getValue() == false);
                    return;
                }
            } else if (property instanceof NumberProperty) {
                NumberProperty numProp = (NumberProperty)property;
                int sliderY = currentY + 20;
                if (mouseX >= component.componentX && mouseX <= component.componentX + component.componentWidth && mouseY >= sliderY - 3 && mouseY <= sliderY + 9) {
                    this.draggingSlider = component;
                    this.updateSlider(numProp, component, mouseX);
                    return;
                }
            } else if (property instanceof ModeProperty) {
                ModeProperty modeProp = (ModeProperty)property;
                if (component.dropdownOpen) {
                    int dropdownY = currentY + 22;
                    for (Enum value : modeProp.getValues()) {
                        if (mouseX >= settingsX + 12 && mouseX <= settingsX + settingsWidth - 12 && mouseY >= dropdownY && mouseY <= dropdownY + 20) {
                            modeProp.setValueObj(value);
                            component.dropdownOpen = false;
                            return;
                        }
                        dropdownY += 20;
                    }
                }
                if (mouseX >= settingsX + 12 && mouseX <= settingsX + settingsWidth - 12 && mouseY >= currentY && mouseY <= currentY + 18) {
                    component.dropdownOpen = !component.dropdownOpen;
                    return;
                }
            } else if (property.getType() == String.class && mouseX >= (boxX = settingsX + settingsWidth - 12 - (boxWidth = settingsWidth - 24 - this.font.getStringWidth(property.getLabel()) - 8)) && mouseX <= boxX + boxWidth && mouseY >= currentY && mouseY <= currentY + 18) {
                this.editingString = component;
                this.editingBuffer = (String)property.getValue();
                return;
            }
            currentY += propHeight + 8;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.dragging = false;
        this.draggingSlider = null;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int scrollAmount = wheel > 0 ? -20 : 20;
            int contentX = this.windowX + 130 + 1;
            if (this.selectedCategory == ModuleCategory.CONFIGS) {
                this.configTargetScroll += (float)scrollAmount;
            } else {
                int moduleListWidth = 122;
                if (mouseX >= contentX && mouseX <= contentX + moduleListWidth) {
                    this.moduleTargetScroll += (float)scrollAmount;
                } else {
                    this.settingTargetScroll += (float)scrollAmount;
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
            } else if (WindowClickInterface.isCtrlKeyDown()) {
                if (keyCode == Keyboard.KEY_V) {
                    String clipboard = GuiScreen.getClipboardString();
                    if (!clipboard.isEmpty()) {
                        StringBuilder builder = new StringBuilder(this.editingBuffer.length() + clipboard.length());
                        builder.append(this.editingBuffer);
                        for (char c : clipboard.toCharArray()) {
                            if (!ChatAllowedCharacters.isAllowedCharacter(c)) continue;
                            builder.append(c);
                        }
                        this.editingBuffer = builder.toString();
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

    private void updateSlider(NumberProperty property, SettingComponent component, int mouseX) {
        if (component.componentWidth <= 0) {
            return;
        }
        double percent = WindowClickInterface.clamp((double)(mouseX - component.componentX) / (double)component.componentWidth, 0.0, 1.0);
        double range = property.getMax() - property.getMin();
        double rawValue = range == 0.0 ? property.getMin() : property.getMin() + range * percent;
        double increment = property.getIncrement();
        if (increment > 0.0) {
            rawValue = (double)Math.round(rawValue / increment) * increment;
        }
        double finalValue = WindowClickInterface.clamp(rawValue, property.getMin(), property.getMax());
        property.setValue(finalValue);
    }

    private void clearTransientState() {
        this.dragging = false;
        this.draggingSlider = null;
        if (this.editingString != null) {
            this.editingString = null;
            this.editingBuffer = "";
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class SettingComponent {
        private final Property<?> property;
        private boolean dropdownOpen = false;
        private int componentX = 0;
        private int componentWidth = 0;

        public SettingComponent(Property<?> property) {
            this.property = property;
        }

        public void updateDrag(int mouseX) {
            Property<?> property = this.property;
            if (property instanceof NumberProperty) {
                NumberProperty numProp = (NumberProperty)property;
                if (this.componentWidth <= 0) {
                    return;
                }
                double percent = WindowClickInterface.clamp((double)(mouseX - this.componentX) / (double)this.componentWidth, 0.0, 1.0);
                double range = numProp.getMax() - numProp.getMin();
                double rawValue = range == 0.0 ? numProp.getMin() : numProp.getMin() + range * percent;
                double increment = numProp.getIncrement();
                if (increment > 0.0) {
                    rawValue = (double)Math.round(rawValue / increment) * increment;
                }
                double finalValue = WindowClickInterface.clamp(rawValue, numProp.getMin(), numProp.getMax());
                numProp.setValue(finalValue);
            }
        }
    }

    private static class ConfigItem {
        private final String name;
        private boolean downloading = false;

        public ConfigItem(String name) {
            this.name = name;
        }
    }
}

