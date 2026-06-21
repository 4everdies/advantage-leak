/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.render.GlintEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.src.Config;
import net.optifine.CustomItems;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;

@ModuleInfo(label="Glint", category=ModuleCategory.VISUALS)
public final class GlintModule
extends Module {
    private final Property<Boolean> glintWeapons = new Property<Boolean>("Glint Weapons", true);
    private final NumberProperty minHue = new NumberProperty("Min Hue", 0.0, 0.0, 360.0, 1.0);
    private final NumberProperty maxHue = new NumberProperty("Max Hue", 360.0, 0.0, 360.0, 1.0);
    private final NumberProperty layers = new NumberProperty("Layers", 4.0, 1.0, 8.0, 1.0);
    @EventLink
    public final Listener<GlintEvent> onGlint = event -> {
        ItemStack itemStack = event.getItemStack();
        Item item = itemStack.getItem();
        if (this.glintWeapons.getValue().booleanValue() && (item instanceof ItemSword || item instanceof ItemAxe)) {
            event.setEnchanted(true);
        }
        event.setCancelled();
        if (event.isEnchanted() && event.isRender()) {
            this.renderEffect(event.getModel());
        }
    };

    public void renderEffect(IBakedModel model) {
        if (!(Config.isCustomItems() && !CustomItems.isUseGlint() || Config.isShaders() && Shaders.isShadowPass)) {
            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(514);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(768, 1);
            Util.mc.getRenderItem().textureManager.bindTexture(RenderItem.RES_ITEM_GLINT);
            if (Config.isShaders() && !Util.mc.getRenderItem().renderItemGui) {
                ShadersRender.renderEnchantedGlintBegin();
            }
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(8.0f, 8.0f, 8.0f);
            float f = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0f / 8.0f;
            GlStateManager.translate(f, 0.0f, 0.0f);
            for (int layer = 1; layer <= ((Double)this.layers.getValue()).intValue(); ++layer) {
                GlStateManager.rotate(-50.0f, 0.0f, 0.0f, 1.0f);
                Util.mc.getRenderItem().renderModel(model, new Color(Color.HSBtoRGB(((float)((Double)this.minHue.getValue()).intValue() + (float)Math.abs(((Double)this.maxHue.getValue()).intValue() - ((Double)this.minHue.getValue()).intValue()) * ((float)layer / ((Double)this.layers.getValue()).floatValue())) / 255.0f, 1.0f, 1.0f)).hashCode());
            }
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableLighting();
            GlStateManager.depthFunc(515);
            GlStateManager.depthMask(true);
            Util.mc.getRenderItem().textureManager.bindTexture(TextureMap.locationBlocksTexture);
            if (Config.isShaders() && !Util.mc.getRenderItem().renderItemGui) {
                ShadersRender.renderEnchantedGlintEnd();
            }
        }
    }
}

