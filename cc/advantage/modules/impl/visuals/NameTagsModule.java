/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.impl.client.AntiBotModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Name Tags", category=ModuleCategory.VISUALS)
public final class NameTagsModule
extends Module {
    private final Property<Boolean> showArmor = new Property<Boolean>("Show Armor", true);
    private final Property<Boolean> showHealth = new Property<Boolean>("Show Health", true);
    private final Property<Boolean> background = new Property<Boolean>("Background", true);
    private final Property<Boolean> throughWalls = new Property<Boolean>("Through Walls", true);
    private final NumberProperty scale = new NumberProperty("Scale", 1.0, 0.5, 2.0, 0.1);
    private final Property<Boolean> distance = new Property<Boolean>("Show Distance", false);
    private final Property<Boolean> rawName = new Property<Boolean>("Raw Name", false);
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        for (EntityPlayer player : Util.mc.theWorld.playerEntities) {
            if (AntiBotModule.botList.contains(player)) {
                return;
            }
            if (player == Util.mc.thePlayer || player.isDead || player.isInvisible()) continue;
            this.renderNameTag(player);
        }
    };

    private void renderNameTag(EntityPlayer player) {
        float partialTicks = Util.mc.timer.renderPartialTicks;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks - Util.mc.getRenderManager().renderPosX;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks - Util.mc.getRenderManager().renderPosY;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks - Util.mc.getRenderManager().renderPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y += (double)player.height + 0.5, z);
        GlStateManager.rotate(-Util.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(Util.mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        float scaleFactor = (float)(0.0266666691750288 * (Double)this.scale.getValue());
        GlStateManager.scale(-scaleFactor, -scaleFactor, scaleFactor);
        if (this.throughWalls.getValue().booleanValue()) {
            GL11.glDisable(2929);
        }
        String name = player.getName();
        if (this.rawName.getValue().booleanValue()) {
            name = player.getDisplayName().getFormattedText();
        }
        float health = player.getHealth();
        int armorValue = player.getTotalArmorValue();
        double distanceToPlayer = Util.mc.thePlayer.getDistanceToEntity(player);
        StringBuilder displayText = new StringBuilder();
        displayText.append("\u00a77").append(this.stripColorCodes(name));
        if (this.distance.getValue().booleanValue()) {
            displayText.append(" \u00a78[\u00a77").append(String.format("%.1f", distanceToPlayer)).append("\u00a78]");
        }
        if (this.showHealth.getValue().booleanValue()) {
            displayText.append(" \u00a78| \u00a7a").append(String.format("%.1f", Float.valueOf(health)));
        }
        String finalText = displayText.toString();
        FontRenderer fontRenderer = Util.mc.fontRendererObj;
        int textWidth = fontRenderer.getStringWidth(this.stripColorCodes(finalText));
        int textHeight = 8;
        int padding = 2;
        int bgWidth = textWidth + padding * 2;
        int bgHeight = textHeight + padding * 2;
        int armorHeight = 0;
        if (this.showArmor.getValue().booleanValue() && armorValue > 0) {
            armorHeight = 10;
            bgHeight += armorHeight;
        }
        if (this.background.getValue().booleanValue()) {
            RenderUtils.drawRoundedRect(-bgWidth / 2, -bgHeight, bgWidth, bgHeight, 2.0f, new Color(0, 0, 0, 150));
        }
        fontRenderer.drawString(finalText, -textWidth / 2, -bgHeight + padding, -1, true);
        if (this.showArmor.getValue().booleanValue() && armorValue > 0) {
            this.drawArmor(player, -bgWidth / 2 + padding, -bgHeight + textHeight + padding * 2, bgWidth - padding * 2);
        }
        if (this.throughWalls.getValue().booleanValue()) {
            GL11.glEnable(2929);
        }
        GlStateManager.popMatrix();
    }

    private void drawArmor(EntityPlayer player, float x, float y, float width) {
        ItemStack[] armorInventory = player.inventory.armorInventory;
        int armorCount = 0;
        for (ItemStack stack : armorInventory) {
            if (stack == null) continue;
            ++armorCount;
        }
        if (armorCount == 0) {
            return;
        }
        float itemWidth = 8.0f;
        float totalWidth = (float)armorCount * itemWidth;
        float startX = x + (width - totalWidth) / 2.0f;
        int index = 0;
        for (int i = 0; i < armorInventory.length; ++i) {
            ItemStack stack = armorInventory[i];
            if (stack == null) continue;
            this.drawArmorItem(stack, startX + (float)index * itemWidth, y, itemWidth);
            ++index;
        }
    }

    private void drawArmorItem(ItemStack stack, float x, float y, float size) {
        GlStateManager.pushMatrix();
        RenderUtils.drawRect(x, y, size, size, new Color(30, 30, 30, 200));
        if (stack.isItemDamaged()) {
            float durability = 1.0f - (float)stack.getItemDamage() / (float)stack.getMaxDamage();
            Color durabilityColor = this.getDurabilityColor(durability);
            RenderUtils.drawRect(x, y + size - 2.0f, size, 2.0f, Color.BLACK);
            RenderUtils.drawRect(x, y + size - 2.0f, size * durability, 2.0f, durabilityColor);
        }
        Color armorColor = this.getArmorColor(stack);
        RenderUtils.drawRect(x + 1.0f, y + 1.0f, size - 2.0f, size - 4.0f, armorColor);
        if (stack.stackSize > 1) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            String count = String.valueOf(stack.stackSize);
            Util.mc.fontRendererObj.drawStringWithShadow(count, (x + size - 2.0f) * 2.0f - (float)Util.mc.fontRendererObj.getStringWidth(count), (y + size - 6.0f) * 2.0f, -1);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private Color getArmorColor(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)stack.getItem();
            switch (armor.getArmorMaterial()) {
                case LEATHER: {
                    return new Color(160, 101, 64);
                }
                case CHAIN: {
                    return new Color(195, 195, 195);
                }
                case IRON: {
                    return new Color(215, 215, 215);
                }
                case GOLD: {
                    return new Color(249, 225, 58);
                }
                case DIAMOND: {
                    return new Color(81, 196, 196);
                }
            }
            return Color.WHITE;
        }
        return Color.GRAY;
    }

    private Color getDurabilityColor(float durability) {
        if ((double)durability > 0.7) {
            return Color.GREEN;
        }
        if ((double)durability > 0.3) {
            return Color.YELLOW;
        }
        return Color.RED;
    }

    private String stripColorCodes(String text) {
        return text.replaceAll("\u00a7.", "");
    }
}

