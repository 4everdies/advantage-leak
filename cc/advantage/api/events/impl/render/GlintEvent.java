/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.api.events.impl.render;

import cc.advantage.api.events.CancellableEvent;
import lombok.Generated;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;

public final class GlintEvent
extends CancellableEvent {
    private boolean enchanted;
    private boolean render;
    private ItemStack itemStack;
    private IBakedModel model;

    @Generated
    public boolean isEnchanted() {
        return this.enchanted;
    }

    @Generated
    public boolean isRender() {
        return this.render;
    }

    @Generated
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Generated
    public IBakedModel getModel() {
        return this.model;
    }

    @Generated
    public void setEnchanted(boolean enchanted) {
        this.enchanted = enchanted;
    }

    @Generated
    public void setRender(boolean render) {
        this.render = render;
    }

    @Generated
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Generated
    public void setModel(IBakedModel model) {
        this.model = model;
    }

    @Generated
    public GlintEvent(boolean enchanted, boolean render, ItemStack itemStack, IBakedModel model) {
        this.enchanted = enchanted;
        this.render = render;
        this.itemStack = itemStack;
        this.model = model;
    }
}
