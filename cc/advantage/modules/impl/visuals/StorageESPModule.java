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
import cc.advantage.utils.Util;
import cc.advantage.utils.render.RenderUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;

@ModuleInfo(label="Storage ESP", category=ModuleCategory.VISUALS)
public final class StorageESPModule
extends Module {
    private final Property<Boolean> chests = new Property<Boolean>("Chests", true);
    private final Property<Boolean> enderChests = new Property<Boolean>("Ender Chests", true);
    private final Property<Boolean> throughWalls = new Property<Boolean>("Through Walls", true);
    private final Property<Boolean> filled = new Property<Boolean>("Filled", false);
    private final Property<Boolean> outline = new Property<Boolean>("Outline", true);
    private final NumberProperty lineWidth = new NumberProperty("Line Width", 2.0, () -> this.outline.getValue(), 1.0, 5.0, 0.5);
    private final NumberProperty alpha = new NumberProperty("Alpha", 0.3, 0.1, 1.0, 0.05);
    private final Color espColor = new Color(173, 216, 230);
    @EventLink
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        if (Util.mc.theWorld == null || Util.mc.thePlayer == null) {
            return;
        }
        List<TileEntity> storageBlocks = this.getStorageBlocks();
        if (storageBlocks.isEmpty()) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        if (this.throughWalls.getValue().booleanValue()) {
            GL11.glDisable(2929);
        }
        GL11.glLineWidth(((Double)this.lineWidth.getValue()).floatValue());
        for (TileEntity tileEntity : storageBlocks) {
            this.renderStorageBlock(tileEntity);
        }
        if (this.throughWalls.getValue().booleanValue()) {
            GL11.glEnable(2929);
        }
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    };

    private List<TileEntity> getStorageBlocks() {
        ArrayList<TileEntity> storageBlocks = new ArrayList<TileEntity>();
        for (TileEntity tileEntity : Util.mc.theWorld.loadedTileEntityList) {
            if (!this.isValidStorageBlock(tileEntity)) continue;
            storageBlocks.add(tileEntity);
        }
        return storageBlocks;
    }

    private boolean isValidStorageBlock(TileEntity tileEntity) {
        BlockPos pos = tileEntity.getPos();
        if (pos == null) {
            return false;
        }
        Block block = Util.mc.theWorld.getBlockState(pos).getBlock();
        if (block == null) {
            return false;
        }
        if (tileEntity instanceof TileEntityChest && this.chests.getValue().booleanValue()) {
            return true;
        }
        return tileEntity instanceof TileEntityEnderChest && this.enderChests.getValue() != false;
    }

    private void renderStorageBlock(TileEntity tileEntity) {
        BlockPos pos = tileEntity.getPos();
        if (pos == null) {
            return;
        }
        Block block = Util.mc.theWorld.getBlockState(pos).getBlock();
        if (block == null) {
            return;
        }
        double x = (double)pos.getX() - Util.mc.getRenderManager().renderPosX;
        double y = (double)pos.getY() - Util.mc.getRenderManager().renderPosY;
        double z = (double)pos.getZ() - Util.mc.getRenderManager().renderPosZ;
        AxisAlignedBB boundingBox = block.getSelectedBoundingBox(Util.mc.theWorld, pos);
        if (boundingBox == null) {
            return;
        }
        boundingBox = boundingBox.offset(-Util.mc.getRenderManager().renderPosX, -Util.mc.getRenderManager().renderPosY, -Util.mc.getRenderManager().renderPosZ);
        boundingBox = boundingBox.expand(-0.002, -0.002, -0.002);
        Color renderColor = new Color(this.espColor.getRed(), this.espColor.getGreen(), this.espColor.getBlue(), (int)((Double)this.alpha.getValue() * 255.0));
        if (this.filled.getValue().booleanValue()) {
            this.renderFilledBox(boundingBox, renderColor);
        }
        if (this.outline.getValue().booleanValue()) {
            this.renderOutlinedBox(boundingBox, renderColor);
        }
    }

    private void renderFilledBox(AxisAlignedBB boundingBox, Color color) {
        RenderUtils.drawBlockESP(new BlockPos(boundingBox.minX + Util.mc.getRenderManager().renderPosX, boundingBox.minY + Util.mc.getRenderManager().renderPosY, boundingBox.minZ + Util.mc.getRenderManager().renderPosZ), (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f * 0.3f, 0.0f, 0.0f);
    }

    private void renderOutlinedBox(AxisAlignedBB boundingBox, Color color) {
        RenderUtils.drawBlockESP(new BlockPos(boundingBox.minX + Util.mc.getRenderManager().renderPosX, boundingBox.minY + Util.mc.getRenderManager().renderPosY, boundingBox.minZ + Util.mc.getRenderManager().renderPosZ), (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.0f, (float)color.getAlpha() / 255.0f, ((Double)this.lineWidth.getValue()).floatValue());
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}

