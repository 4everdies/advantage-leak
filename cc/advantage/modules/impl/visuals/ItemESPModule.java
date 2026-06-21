/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.Render3DEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Item ESP", category=ModuleCategory.VISUALS)
public final class ItemESPModule
extends Module {
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        for (Entity entity : Util.mc.theWorld.getLoadedEntityList()) {
            if (!(entity instanceof EntityItem)) continue;
            EntityItem entityItem = (EntityItem)entity;
            Object enhancement = "";
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, entityItem.getEntityItem()) != 0) {
                enhancement = "\u00a7b Protection:\u00a7c" + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, entityItem.getEntityItem());
            }
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, entityItem.getEntityItem()) != 0) {
                enhancement = "\u00a7b Sharpness:\u00a7c" + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, entityItem.getEntityItem());
            }
            if (entityItem.getEntityItem().getItem() == Items.golden_apple && entityItem.getEntityItem().getItem().hasEffect(entityItem.getEntityItem())) {
                enhancement = "\u00a7c Enchanted";
            }
            String var3 = entityItem.getEntityItem().stackSize > 1 ? "\u00a7f x" + entityItem.getEntityItem().stackSize : "";
            float partialTicks = Util.mc.timer.renderPartialTicks;
            double interpolatedX = entityItem.lastTickPosX + (entityItem.posX - entityItem.lastTickPosX) * (double)partialTicks;
            double interpolatedY = entityItem.lastTickPosY + (entityItem.posY - entityItem.lastTickPosY) * (double)partialTicks;
            double interpolatedZ = entityItem.lastTickPosZ + (entityItem.posZ - entityItem.lastTickPosZ) * (double)partialTicks;
            double diffX = Util.mc.thePlayer.lastTickPosX + (Util.mc.thePlayer.posX - Util.mc.thePlayer.lastTickPosX) * (double)partialTicks - interpolatedX;
            double diffY = Util.mc.thePlayer.lastTickPosY + (Util.mc.thePlayer.posY - Util.mc.thePlayer.lastTickPosY) * (double)partialTicks - interpolatedY;
            double diffZ = Util.mc.thePlayer.lastTickPosZ + (Util.mc.thePlayer.posZ - Util.mc.thePlayer.lastTickPosZ) * (double)partialTicks - interpolatedZ;
            double dist = MathHelper.sqrt_double(diffX * diffX + diffY * diffY + diffZ * diffZ);
            GlStateManager.pushMatrix();
            ItemESPModule.drawText(entityItem.getEntityItem().getDisplayName() + var3 + (String)enhancement, -1, interpolatedX, interpolatedY, interpolatedZ, dist);
            GlStateManager.popMatrix();
        }
    };

    public static void drawText(String value, int textColor, double posX, double posY, double posZ, double dist) {
        posX -= Util.mc.getRenderManager().viewerPosX;
        posY -= Util.mc.getRenderManager().viewerPosY;
        posZ -= Util.mc.getRenderManager().viewerPosZ;
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY + 1.0f, (float)posZ);
        GlStateManager.rotate(-Util.mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float)(Util.mc.gameSettings.thirdPersonView == 2 ? -1 : 1) * Util.mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        float scale = Math.min(Math.max(0.02266667f, (float)((double)0.0015f * dist)), 0.07f);
        GlStateManager.scale(-scale, -scale, -scale);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        int textWidth = Util.mc.fontRendererObj.getStringWidth(value);
        Util.mc.fontRendererObj.drawString(value, -textWidth / 2 + (int)(scale * 3.5f), (int)(-(123.805f * scale - 2.47494f)), textColor);
        Util.mc.fontRendererObj.drawString(value, -textWidth / 2 + (int)(scale * 3.5f) - 1, (int)(-(123.805f * scale - 2.47494f)), Color.BLACK.getRGB());
        Util.mc.fontRendererObj.drawString(value, -textWidth / 2 + (int)(scale * 3.5f) + 1, (int)(-(123.805f * scale - 2.47494f)), Color.BLACK.getRGB());
        Util.mc.fontRendererObj.drawString(value, -textWidth / 2 + (int)(scale * 3.5f), (int)(-(123.805f * scale - 2.47494f)) - 1, Color.BLACK.getRGB());
        Util.mc.fontRendererObj.drawString(value, -textWidth / 2 + (int)(scale * 3.5f), (int)(-(123.805f * scale - 2.47494f)) + 1, Color.BLACK.getRGB());
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }
}

