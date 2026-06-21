/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.events.impl.render.ShaderEvent;
import cc.advantage.api.font.CustomFontRenderer;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.modules.impl.player.StealerModule;
import cc.advantage.processes.ColorProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

@ModuleInfo(label="Watermark", category=ModuleCategory.VISUALS)
public final class WatermarkModule
extends Module {
    public static final ModeProperty<Type> type = new ModeProperty<Type>("Client Watermark Type", Type.Simple);
    public static final Property<Boolean> info = new Property<Boolean>("Watermark Info", true, () -> type.getValue() != Type.GameSense && type.getValue() != Type.Logo && type.getValue() != Type.Island && type.getValue() != Type.Tutorial2017 && type.getValue() != Type.Wurst && type.getValue() != Type.Nursultan);
    public static final Property<String> name = new Property<String>("Client Name", "Advantage");
    public static String customName = "Advantage";
    private long lastServerTime;
    private long lastUpdateTime;
    private String currentServer;
    private float islandWidth;
    private float islandHeight;
    private ResourceLocation clientLogo;
    private ResourceLocation wurstLogo = new ResourceLocation("advantage/images/wurst.png");
    private float stealerWidth = 200.0f;
    private float stealerHeight = 28.0f;
    private final Map<Integer, ChestItemAnimation> chestItemAnimations = new HashMap<Integer, ChestItemAnimation>();
    private int lastChestSize = 0;
    private boolean wasInChest = false;
    @EventLink
    public Listener<Render2DEvent> render2DEventListener = e -> {
        this.setSuffix(((Type)((Object)((Object)type.getValue()))).toString());
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        ScaledResolution sr = new ScaledResolution(Util.mc);
        String clientName = customName = name.getValue();
        if (type.getValue() != Type.Logo && type.getValue() != Type.Island && type.getValue() != Type.Tutorial2017 && type.getValue() != Type.Wurst) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("hh:mm a");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            Object text = clientName;
            if (type.getValue() == Type.Exhibition) {
                if (!clientName.isEmpty()) {
                    text = String.valueOf(clientName.charAt(0)) + String.valueOf((Object)EnumChatFormatting.GRAY) + clientName.substring(1) + " ";
                    if (info.getValue().booleanValue()) {
                        text = String.valueOf(clientName.charAt(0)) + String.valueOf((Object)EnumChatFormatting.GRAY) + clientName.substring(1) + " " + String.valueOf((Object)EnumChatFormatting.WHITE) + Advantage.VERSION + String.valueOf((Object)EnumChatFormatting.GRAY) + " [" + String.valueOf((Object)EnumChatFormatting.WHITE) + strDate + String.valueOf((Object)EnumChatFormatting.GRAY) + "]" + String.valueOf((Object)EnumChatFormatting.GRAY) + " [FPS: " + String.valueOf((Object)EnumChatFormatting.WHITE) + Util.mc.getDebugFPS() + String.valueOf((Object)EnumChatFormatting.GRAY) + "]";
                    }
                }
                fr.drawStringWithShadow((String)text, 2.0f, 2.0f, ColorProcess.getColor().getRGB());
            } else if (type.getValue() == Type.Astolfo) {
                if (!clientName.isEmpty()) {
                    text = String.valueOf(clientName.charAt(0)) + String.valueOf((Object)EnumChatFormatting.WHITE) + clientName.substring(1) + " ";
                }
                fr.drawStringWithShadow((String)text, 2.0f, 2.0f, ColorProcess.getColor().getRGB());
            } else if (type.getValue() == Type.Simple) {
                text = info.getValue() != false ? clientName + String.valueOf((Object)EnumChatFormatting.WHITE) + " " + String.valueOf((Object)EnumChatFormatting.WHITE) + Advantage.VERSION : clientName;
                fr.drawStringWithShadow((String)text, 2.0f, 2.0f, ColorProcess.getColor().getRGB());
            } else if (type.getValue() == Type.GameSense) {
                String serverInfo = Util.mc.getCurrentServerData() != null ? (Util.mc.getCurrentServerData().serverIP.toLowerCase().contains("liquidproxy") ? "LiquidProxy" : Util.mc.getCurrentServerData().serverIP) : "Singleplayer";
                text = String.format(String.valueOf((Object)EnumChatFormatting.WHITE) + "%s v%s | %d FPS | %s", clientName, Advantage.VERSION, Minecraft.getDebugFPS(), serverInfo);
                RenderUtils.drawBorderedRect(0.0f, 0.5f, fr.getStringWidth((String)text) + 4, 7 * sr.getScaleFactor(), 2.0f, new Color(0, 0, 0, 100).getRGB(), ColorProcess.getColor().getRGB(), true, false, false, false);
                fr.drawStringWithShadow((String)text, 2.0f, 3.0f, ColorProcess.getColor().getRGB());
            }
        } else if (type.getValue() == Type.Logo) {
            RenderUtils.drawImage(new ResourceLocation("advantage/images/Advantage.png"), 2.0f, 2.0f, 78.5f, 62.5f);
        } else if (type.getValue() == Type.Island) {
            this.renderDynamicIsland(sr, clientName);
        } else if (type.getValue() == Type.Tutorial2017) {
            RenderUtils.drawRect(2.0f, 2.0f, fr.getStringWidth(clientName) + 4, fr.FONT_HEIGHT + 2, new Color(0, 0, 0, 100));
            fr.drawString(clientName, 4.0f, 4.0f, 0x5555FF);
            RenderUtils.drawRect(2.0f, 15.0f, fr.getStringWidth("FPS: " + Util.mc.getDebugFPS()) + 4, fr.FONT_HEIGHT + 2, new Color(0, 0, 0, 100));
            fr.drawString("FPS: " + Util.mc.getDebugFPS(), 4.0f, 17.0f, -1);
        } else if (type.getValue() == Type.Wurst) {
            RenderUtils.drawRect(0.0f, 10.0f, 223.0f, 21.0f, new Color(255, 255, 255, 100));
            RenderUtils.drawImage(this.wurstLogo, 0.0f, 10.0f, 89.17647f, 22.588236f);
            Util.mc.fontRendererObj.drawString("v7.46.1 MC1.8.9 (outdated)", 92, 17, Color.BLACK.getRGB());
        }
        if (type.getValue() == Type.Nursultan) {
            int ping = 67;
            NetworkPlayerInfo playerInfo = Util.mc.getNetHandler().getPlayerInfo(Util.mc.thePlayer.getUniqueID());
            if (playerInfo != null && playerInfo.getResponseTime() != 0) {
                ping = playerInfo.getResponseTime();
            }
            String skibidi = clientName + String.valueOf((Object)EnumChatFormatting.WHITE) + " - " + Util.mc.getDebugFPS() + " FPS - " + ping + "ms";
            RenderUtils.drawRoundOutline(2.0f, 2.0f, fr.getStringWidth(skibidi) + 4, fr.FONT_HEIGHT + 4, 4.0f, 0.05f, new Color(0, 0, 0, 130), ColorProcess.getColor());
            fr.drawStringWithShadow(skibidi, 4.0f, 4.0f, ColorProcess.getColor().getRGB());
        }
    };
    @EventLink
    public Listener<ShaderEvent> shaderEventListener = e -> {
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        ScaledResolution sr = new ScaledResolution(Util.mc);
        String clientName = customName;
        if (type.getValue() != Type.Logo && type.getValue() != Type.Island && type.getValue() != Type.Tutorial2017 && type.getValue() != Type.Wurst) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("hh:mm a");
            Date now = new Date();
            String strDate = sdfDate.format(now);
            Object text = clientName;
            if (type.getValue() == Type.Exhibition) {
                if (!clientName.isEmpty()) {
                    text = String.valueOf(clientName.charAt(0)) + String.valueOf((Object)EnumChatFormatting.GRAY) + clientName.substring(1) + " ";
                    if (info.getValue().booleanValue()) {
                        text = String.valueOf(clientName.charAt(0)) + String.valueOf((Object)EnumChatFormatting.GRAY) + clientName.substring(1) + " " + String.valueOf((Object)EnumChatFormatting.WHITE) + Advantage.VERSION + String.valueOf((Object)EnumChatFormatting.GRAY) + " [" + String.valueOf((Object)EnumChatFormatting.WHITE) + strDate + String.valueOf((Object)EnumChatFormatting.GRAY) + "]" + String.valueOf((Object)EnumChatFormatting.GRAY) + " [FPS: " + String.valueOf((Object)EnumChatFormatting.WHITE) + Util.mc.getDebugFPS() + String.valueOf((Object)EnumChatFormatting.GRAY) + "]";
                    }
                }
                fr.drawStringWithShadow((String)text, 2.0f, 2.0f, ColorProcess.getColor().getRGB());
            } else if (type.getValue() == Type.Astolfo) {
                if (!clientName.isEmpty()) {
                    text = String.valueOf(clientName.charAt(0)) + String.valueOf((Object)EnumChatFormatting.WHITE) + clientName.substring(1) + " ";
                }
                fr.drawStringWithShadow((String)text, 2.0f, 2.0f, ColorProcess.getColor().getRGB());
            } else if (type.getValue() == Type.Simple) {
                text = info.getValue() != false ? clientName + String.valueOf((Object)EnumChatFormatting.WHITE) + " " + String.valueOf((Object)EnumChatFormatting.WHITE) + Advantage.VERSION : clientName;
                fr.drawStringWithShadow((String)text, 2.0f, 2.0f, ColorProcess.getColor().getRGB());
            } else if (type.getValue() == Type.GameSense) {
                String serverInfo = Util.mc.getCurrentServerData() != null ? (Util.mc.getCurrentServerData().serverIP.toLowerCase().contains("liquidproxy") ? "LiquidProxy" : Util.mc.getCurrentServerData().serverIP) : "Singleplayer";
                text = String.format(String.valueOf((Object)EnumChatFormatting.WHITE) + "%s v%s | %d FPS | %s", clientName, Advantage.VERSION, Minecraft.getDebugFPS(), serverInfo);
                RenderUtils.drawBorderedRect(0.0f, 0.5f, fr.getStringWidth((String)text) + 4, 7 * sr.getScaleFactor(), 2.0f, new Color(0, 0, 0, 100).getRGB(), ColorProcess.getColor().getRGB(), true, false, false, false);
                fr.drawStringWithShadow((String)text, 2.0f, 3.0f, ColorProcess.getColor().getRGB());
            }
        } else if (type.getValue() == Type.Logo) {
            RenderUtils.drawImage(new ResourceLocation("advantage/images/Advantage.png"), 2.0f, 2.0f, 78.5f, 62.5f);
        } else if (type.getValue() == Type.Island) {
            this.renderDynamicIsland(sr, clientName);
        } else if (type.getValue() == Type.Tutorial2017) {
            RenderUtils.drawRect(2.0f, 2.0f, fr.getStringWidth(clientName) + 4, fr.FONT_HEIGHT + 2, new Color(0, 0, 0, 100));
            fr.drawString(clientName, 4.0f, 4.0f, 0x5555FF);
            RenderUtils.drawRect(2.0f, 15.0f, fr.getStringWidth("FPS: " + Util.mc.getDebugFPS()) + 4, fr.FONT_HEIGHT + 2, new Color(0, 0, 0, 100));
            fr.drawString("FPS: " + Util.mc.getDebugFPS(), 4.0f, 17.0f, -1);
        } else if (type.getValue() == Type.Wurst) {
            RenderUtils.drawRect(0.0f, 10.0f, 223.0f, 21.0f, new Color(255, 255, 255, 100));
            RenderUtils.drawImage(this.wurstLogo, 0.0f, 10.0f, 89.17647f, 22.588236f);
            Util.mc.fontRendererObj.drawString("v7.46.1 MC1.8.9 (outdated)", 92, 17, Color.BLACK.getRGB());
        }
        if (type.getValue() == Type.Nursultan) {
            int ping = 67;
            NetworkPlayerInfo playerInfo = Util.mc.getNetHandler().getPlayerInfo(Util.mc.thePlayer.getUniqueID());
            if (playerInfo != null && playerInfo.getResponseTime() != 0) {
                ping = playerInfo.getResponseTime();
            }
            String skibidi = clientName + String.valueOf((Object)EnumChatFormatting.WHITE) + " - " + Util.mc.getDebugFPS() + " FPS - " + ping + "ms";
            RenderUtils.drawRoundOutline(2.0f, 2.0f, fr.getStringWidth(skibidi) + 4, fr.FONT_HEIGHT + 4, 6.0f, 0.05f, new Color(0, 0, 0, 130), ColorProcess.getColor());
            fr.drawStringWithShadow(skibidi, 4.0f, 4.0f, ColorProcess.getColor().getRGB());
        }
    };
    private float scaffoldWidth = 200.0f;
    private float scaffoldHeight = 28.0f;
    private int lastBlockCount = 0;
    private float blockCountDisplay = 0.0f;
    private long lastBPSUpdate = 0L;
    private float currentBPS = 0.0f;
    private float displayBPS = 0.0f;

    private void renderDynamicIsland(ScaledResolution sr, String clientName) {
        boolean scaffoldEnabled;
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        if (System.currentTimeMillis() - this.lastServerTime > 5000L) {
            this.updateServerInfo();
            this.lastServerTime = System.currentTimeMillis();
        }
        int centerX = sr.getScaledWidth() / 2;
        int yPos = 8;
        StealerModule stealer = Advantage.INSTANCE.getModuleManager().getModule(StealerModule.class);
        boolean stealerActive = stealer != null && stealer.isEnabled() && Util.mc.currentScreen instanceof GuiChest;
        ScaffoldModule scaffold = Advantage.INSTANCE.getModuleManager().getModule(ScaffoldModule.class);
        boolean bl = scaffoldEnabled = scaffold != null && scaffold.isEnabled();
        if (stealerActive) {
            this.renderStealerIsland(sr, centerX, yPos);
        } else if (scaffoldEnabled) {
            this.renderScaffoldIsland(sr, centerX, yPos);
        } else {
            this.renderNormalIsland(sr, centerX, yPos, clientName);
        }
    }

    private void renderNormalIsland(ScaledResolution sr, int centerX, int yPos, String clientName) {
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        String serverText = this.currentServer != null ? this.currentServer : "Loading...";
        String versionText = Advantage.VERSION;
        String fpsText = Util.mc.getDebugFPS() + " fps";
        int contentPadding = 12;
        int logoSize = 30;
        int textSpacing = 8;
        int dotWidth = fr.getStringWidth("\u2022");
        int textWidth = fr.getStringWidth(serverText) + textSpacing + dotWidth + textSpacing + fr.getStringWidth(versionText) + textSpacing + dotWidth + textSpacing + fr.getStringWidth(fpsText);
        int totalWidth = logoSize + contentPadding * 2 + textWidth + 8;
        int height = 28;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        float smoothTime = 0.2f;
        float alpha = 1.0f - (float)Math.exp(-deltaTime / smoothTime);
        this.islandHeight = RenderUtils.lerp(this.islandHeight, height, alpha);
        this.islandWidth = RenderUtils.lerp(this.islandWidth, totalWidth, alpha);
        float startX = (float)centerX - this.islandWidth / 2.0f;
        RenderUtils.drawRoundedRect(startX, yPos, this.islandWidth, this.islandHeight, 14.0f, new Color(20, 20, 25, 200));
        RenderUtils.drawRoundOutline(startX, yPos, this.islandWidth, this.islandHeight, 14.0f, 0.5f, new Color(0, 0, 0, 0), new Color(255, 255, 255, 30));
        this.clientLogo = new ResourceLocation("advantage/images/Advantage.png");
        float logoX = startX + (float)contentPadding;
        float logoWidth = logoSize;
        float logoHeight = (float)logoSize / 1.256f;
        float logoY = (float)yPos + (this.islandHeight - logoHeight) / 2.0f;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtils.drawImage(this.clientLogo, logoX, logoY, logoWidth, logoHeight);
        GlStateManager.disableBlend();
        float textX = logoX + (float)logoSize + 10.0f;
        float textY = (float)yPos + (this.islandHeight - (float)fr.FONT_HEIGHT) / 1.8f;
        fr.drawString(serverText, (int)textX, (int)textY, ColorProcess.getColor().getRGB());
        fr.drawString("\u2022", (int)(textX += (float)(fr.getStringWidth(serverText) + textSpacing)), (int)textY, new Color(100, 100, 110).getRGB());
        fr.drawString(versionText, (int)(textX += (float)(dotWidth + textSpacing)), (int)textY, new Color(150, 150, 160).getRGB());
        fr.drawString("\u2022", (int)(textX += (float)(fr.getStringWidth(versionText) + textSpacing)), (int)textY, new Color(100, 100, 110).getRGB());
        fr.drawString(fpsText, (int)(textX += (float)(dotWidth + textSpacing)), (int)textY, new Color(180, 180, 190).getRGB());
    }

    private void renderScaffoldIsland(ScaledResolution sr, int centerX, int yPos) {
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        int currentBlockCount = 0;
        int totalBlocks = 0;
        for (int i = 0; i < Util.mc.thePlayer.inventory.mainInventory.length; ++i) {
            ItemStack stack = Util.mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || !(stack.getItem() instanceof ItemBlock)) continue;
            totalBlocks += stack.stackSize;
        }
        currentBlockCount = Util.mc.thePlayer.inventory.getCurrentItem() != null && Util.mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock ? totalBlocks : totalBlocks;
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastBPSUpdate > 50L) {
            double dx = Util.mc.thePlayer.posX - Util.mc.thePlayer.lastTickPosX;
            double dz = Util.mc.thePlayer.posZ - Util.mc.thePlayer.lastTickPosZ;
            double distance = Math.sqrt(dx * dx + dz * dz) * 20.0;
            this.currentBPS = (float)distance;
            this.lastBPSUpdate = currentTime;
        }
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        float smoothTime = 0.15f;
        float alpha = 1.0f - (float)Math.exp(-deltaTime / smoothTime);
        this.blockCountDisplay = RenderUtils.lerp(this.blockCountDisplay, currentBlockCount, alpha * 2.0f);
        if (this.lastBlockCount == 0 || currentBlockCount > this.lastBlockCount) {
            this.lastBlockCount = currentBlockCount;
        }
        this.displayBPS = RenderUtils.lerp(this.displayBPS, this.currentBPS, alpha);
        int contentPadding = 12;
        int logoSize = 30;
        int textSpacing = 8;
        int progressBarWidth = 80;
        int progressBarHeight = 4;
        String blocksText = (int)this.blockCountDisplay + "/" + this.lastBlockCount;
        String bpsText = String.format("%.1f BPS", Float.valueOf(this.displayBPS));
        int dotWidth = fr.getStringWidth("\u2022");
        int textWidth = logoSize + 10 + fr.getStringWidth(blocksText) + textSpacing + progressBarWidth + textSpacing + dotWidth + textSpacing + fr.getStringWidth(bpsText);
        int targetWidth = contentPadding * 2 + textWidth + 8;
        int targetHeight = 36;
        this.scaffoldHeight = RenderUtils.lerp(this.scaffoldHeight, targetHeight, alpha);
        this.scaffoldWidth = RenderUtils.lerp(this.scaffoldWidth, targetWidth, alpha);
        float startX = (float)centerX - this.scaffoldWidth / 2.0f;
        RenderUtils.drawRoundedRect(startX, yPos, this.scaffoldWidth, this.scaffoldHeight, 14.0f, new Color(20, 20, 25, 200));
        RenderUtils.drawRoundOutline(startX, yPos, this.scaffoldWidth, this.scaffoldHeight, 14.0f, 0.5f, new Color(0, 0, 0, 0), new Color(255, 255, 255, 30));
        this.clientLogo = new ResourceLocation("advantage/images/Advantage.png");
        float logoX = startX + (float)contentPadding;
        float logoWidth = logoSize;
        float logoHeight = (float)logoSize / 1.256f;
        float logoY = (float)yPos + (this.scaffoldHeight - logoHeight) / 2.0f;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtils.drawImage(this.clientLogo, logoX, logoY, logoWidth, logoHeight);
        GlStateManager.disableBlend();
        float textX = logoX + (float)logoSize + 10.0f;
        float textY = (float)yPos + (this.scaffoldHeight - (float)fr.FONT_HEIGHT) / 1.8f;
        fr.drawString(blocksText, (int)textX, (int)textY, ColorProcess.getColor().getRGB());
        float progressY = textY + (float)(fr.FONT_HEIGHT / 2) - (float)(progressBarHeight / 2);
        float progress = this.lastBlockCount > 0 ? (float)currentBlockCount / (float)this.lastBlockCount : 0.0f;
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        RenderUtils.drawRoundedRect(textX += (float)(fr.getStringWidth(blocksText) + textSpacing), progressY, progressBarWidth, progressBarHeight, 2.0f, new Color(40, 40, 45, 180));
        if (progress > 0.0f) {
            float fillWidth = (float)progressBarWidth * progress;
            Color progressColor = progress < 0.2f ? new Color(255, 100, 100) : (progress < 0.5f ? new Color(255, 255, 100) : ColorProcess.getColor());
            RenderUtils.drawRoundedRect(textX, progressY, fillWidth, progressBarHeight, 2.0f, progressColor);
        }
        fr.drawString("\u2022", (int)(textX += (float)(progressBarWidth + textSpacing)), (int)textY, new Color(100, 100, 110).getRGB());
        Color bpsColor = this.displayBPS > 8.0f ? new Color(100, 255, 100) : (this.displayBPS > 4.0f ? new Color(255, 255, 100) : new Color(180, 180, 190));
        fr.drawString(bpsText, (int)(textX += (float)(dotWidth + textSpacing)), (int)textY, bpsColor.getRGB());
    }

    private void renderStealerIsland(ScaledResolution sr, int centerX, int yPos) {
        ChestItemAnimation anim;
        CustomFontRenderer fr = FontUtils.getCurrentFont();
        if (!(Util.mc.currentScreen instanceof GuiChest)) {
            this.wasInChest = false;
            this.chestItemAnimations.clear();
            return;
        }
        GuiChest guiChest = (GuiChest)Util.mc.currentScreen;
        ContainerChest chest = (ContainerChest)Util.mc.thePlayer.openContainer;
        IInventory inventory = chest.getLowerChestInventory();
        int chestSize = inventory.getSizeInventory();
        boolean isDoubleChest = chestSize > 27;
        int rows = isDoubleChest ? 6 : 3;
        int columns = 9;
        if (!this.wasInChest || this.lastChestSize != chestSize) {
            this.chestItemAnimations.clear();
            this.lastChestSize = chestSize;
        }
        this.wasInChest = true;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        float smoothTime = 0.15f;
        float alpha = 1.0f - (float)Math.exp(-deltaTime / smoothTime);
        for (int i = 0; i < chestSize; ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!this.chestItemAnimations.containsKey(i)) {
                if (stack == null) continue;
                this.chestItemAnimations.put(i, new ChestItemAnimation(stack));
                continue;
            }
            anim = this.chestItemAnimations.get(i);
            if (stack == null && anim.lastStack != null) {
                anim.removing = true;
                continue;
            }
            if (stack == null) continue;
            anim.lastStack = stack;
            anim.removing = false;
            anim.opacity = 1.0f;
            anim.scale = 1.0f;
        }
        Iterator<Map.Entry<Integer, ChestItemAnimation>> iterator2 = this.chestItemAnimations.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<Integer, ChestItemAnimation> entry = iterator2.next();
            anim = entry.getValue();
            if (!anim.removing) continue;
            anim.opacity = Math.max(0.0f, anim.opacity - deltaTime * 4.0f);
            anim.scale = Math.min(1.5f, anim.scale + deltaTime * 3.0f);
            if (!(anim.opacity <= 0.0f)) continue;
            iterator2.remove();
        }
        int contentPadding = 12;
        int logoSize = 30;
        int textSpacing = 8;
        int itemSize = 16;
        int itemSpacing = 2;
        Object chestName = inventory.getDisplayName().getFormattedText();
        if (((String)chestName).length() > 20) {
            chestName = ((String)chestName).substring(0, 20) + "...";
        }
        int chestGridWidth = columns * itemSize + (columns - 1) * itemSpacing;
        int chestGridHeight = rows * itemSize + (rows - 1) * itemSpacing;
        int textWidth = Math.max(fr.getStringWidth((String)chestName), chestGridWidth);
        int targetWidth = contentPadding * 2 + logoSize + 10 + textWidth + 8;
        int targetHeight = contentPadding * 2 + Math.max(logoSize, fr.FONT_HEIGHT + 8 + chestGridHeight);
        this.stealerHeight = RenderUtils.lerp(this.stealerHeight, targetHeight, alpha);
        this.stealerWidth = RenderUtils.lerp(this.stealerWidth, targetWidth, alpha);
        float startX = (float)centerX - this.stealerWidth / 2.0f;
        RenderUtils.drawRoundedRect(startX, yPos, this.stealerWidth, this.stealerHeight, 14.0f, new Color(20, 20, 25, 200));
        RenderUtils.drawRoundOutline(startX, yPos, this.stealerWidth, this.stealerHeight, 14.0f, 0.5f, new Color(0, 0, 0, 0), new Color(255, 255, 255, 30));
        this.clientLogo = new ResourceLocation("advantage/images/Advantage.png");
        float logoX = startX + (float)contentPadding;
        float logoWidth = logoSize;
        float logoHeight = (float)logoSize / 1.256f;
        float logoY = yPos + contentPadding;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtils.drawImage(this.clientLogo, logoX, logoY, logoWidth, logoHeight);
        GlStateManager.disableBlend();
        float textX = logoX + (float)logoSize + 10.0f;
        float textY = yPos + contentPadding;
        fr.drawString((String)chestName, (int)textX, (int)textY, ColorProcess.getColor().getRGB());
        float gridX = textX;
        float gridY = textY + (float)fr.FONT_HEIGHT + 6.0f;
        if (chestGridWidth < fr.getStringWidth((String)chestName)) {
            gridX += (float)((fr.getStringWidth((String)chestName) - chestGridWidth) / 2);
        }
        this.renderChestGrid(inventory, gridX, gridY, rows, columns, itemSize, itemSpacing);
    }

    private void renderChestGrid(IInventory inventory, float startX, float startY, int rows, int columns, int itemSize, int itemSpacing) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        for (int row = 0; row < rows; ++row) {
            int slotIndex;
            for (int col = 0; col < columns && (slotIndex = row * columns + col) < inventory.getSizeInventory(); ++col) {
                float slotX = startX + (float)(col * (itemSize + itemSpacing));
                float slotY = startY + (float)(row * (itemSize + itemSpacing));
                RenderUtils.drawRoundedRect(slotX, slotY, itemSize, itemSize, 2.0f, new Color(30, 30, 35, 150));
                ItemStack stack = inventory.getStackInSlot(slotIndex);
                ChestItemAnimation anim = this.chestItemAnimations.get(slotIndex);
                if (anim == null || anim.lastStack == null) continue;
                GlStateManager.pushMatrix();
                float centerX = slotX + (float)itemSize / 2.0f;
                float centerY = slotY + (float)itemSize / 2.0f;
                GlStateManager.translate(centerX, centerY, 0.0f);
                GlStateManager.scale(anim.scale, anim.scale, 1.0f);
                GlStateManager.translate(-centerX, -centerY, 0.0f);
                GlStateManager.color(1.0f, 1.0f, 1.0f, anim.opacity);
                try {
                    Util.mc.getRenderItem().renderItemAndEffectIntoGUI(anim.lastStack, (int)slotX, (int)slotY);
                    if (anim.lastStack.stackSize > 1) {
                        String stackSize = String.valueOf(anim.lastStack.stackSize);
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.disableBlend();
                        Util.mc.fontRendererObj.drawStringWithShadow(stackSize, slotX + (float)itemSize - (float)Util.mc.fontRendererObj.getStringWidth(stackSize) - 1.0f, slotY + (float)itemSize - 7.0f, (int)(255.0f * anim.opacity) << 24 | 0xFFFFFF);
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                        GlStateManager.enableBlend();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                GlStateManager.popMatrix();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }

    private void updateServerInfo() {
        this.currentServer = Util.mc.getCurrentServerData() != null ? (Util.mc.getCurrentServerData().serverIP.toLowerCase().contains("liquidproxy") ? "LiquidProxy" : Util.mc.getCurrentServerData().serverIP) : "Singleplayer";
    }

    private static class ChestItemAnimation {
        float opacity = 1.0f;
        float scale = 1.0f;
        boolean removing = false;
        ItemStack lastStack;

        ChestItemAnimation(ItemStack stack) {
            this.lastStack = stack;
        }
    }

    public static enum Type {
        Simple,
        Exhibition,
        Astolfo,
        GameSense,
        Logo,
        Island,
        Tutorial2017,
        Wurst,
        Nursultan;

    }
}

