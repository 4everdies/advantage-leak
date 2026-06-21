/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.render.FontUtils;
import cc.advantage.utils.render.GlUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.HashSet;
import java.util.Set;
import kotlin.collections.ArraysKt;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

@ModuleInfo(label="Bed Plates", category=ModuleCategory.VISUALS)
public final class BedPlatesModule
extends Module {
    private static final int MAX_SIZE = 8;
    public final NumberProperty distance = new NumberProperty("Distance", 50.0, 10.0, 75.0, 1.0);
    public final NumberProperty updateRate = new NumberProperty("Update Rate", 1000.0, 250.0, 5000.0, 250.0);
    public final NumberProperty layers = new NumberProperty("Layers", 5.0, 1.0, 10.0, 1.0);
    private final BlockPos[] beds = new BlockPos[8];
    private final Set<Block>[] bedBlocks = new Set[8];
    private final Set<BlockPos> retardedList = new HashSet<BlockPos>();
    private final Timer timer = new Timer();
    private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = event -> {
        if (!this.timer.hasTimeElapsed((Double)this.updateRate.getValue())) {
            return;
        }
        this.clearBeds();
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        int radius = ((Double)this.distance.getValue()).intValue();
        int ind = 0;
        BlockPos center = new BlockPos(Util.mc.thePlayer);
        for (int y = radius; y >= -radius; --y) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    boolean found;
                    this.mutable.set(center);
                    BlockPos.MutableBlockPos pos = this.mutable.move(x, y, z);
                    Block bl = Util.mc.theWorld.getBlockState(pos).getBlock();
                    if (this.retardedList.contains(pos) || ind >= 8 || !bl.equals(Blocks.bed) || !(found = this.find(((Vec3i)pos).getX(), ((Vec3i)pos).getY(), ((Vec3i)pos).getZ(), ind))) continue;
                    this.retardedList.add(pos.north());
                    this.retardedList.add(pos.south());
                    this.retardedList.add(pos.east());
                    this.retardedList.add(pos.west());
                    ++ind;
                }
            }
        }
        this.timer.reset();
    };
    @EventLink
    public final Listener<Render2DEvent> render2DEventListener = event -> {
        int index = 0;
        for (BlockPos blockPos : this.beds) {
            if (blockPos == null || this.beds[index] == null) continue;
            ScaledResolution sr = new ScaledResolution(Util.mc);
            Util.mc.entityRenderer.setupCameraTransform(event.getPartialTicks(), 0);
            double x = (double)blockPos.getX() - Util.mc.getRenderManager().viewerPosX;
            double y = (double)blockPos.getY() - Util.mc.getRenderManager().viewerPosY;
            double z = (double)blockPos.getZ() - Util.mc.getRenderManager().viewerPosZ;
            AxisAlignedBB bb = new AxisAlignedBB(x, y - 1.0, z, x, y + 1.0, z);
            double[][] vectors = new double[][]{{bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ}, {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ}, {bb.maxX, bb.minY, bb.minZ}, {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}};
            float[] position = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f};
            for (double[] vec : vectors) {
                float[] projection = GlUtils.project2D((float)vec[0], (float)vec[1], (float)vec[2], sr.getScaleFactor());
                if (projection == null || !(projection[2] >= 0.0f) || !(projection[2] < 1.0f)) continue;
                float pX = projection[0];
                float pY = projection[1];
                position[0] = Math.min(position[0], pX);
                position[1] = Math.min(position[1], pY);
                position[2] = Math.max(position[2], pX);
                position[3] = Math.max(position[3], pY);
            }
            Util.mc.entityRenderer.setupOverlayRendering();
            float width = this.bedBlocks[index].size() * 20 + 4;
            float posX = position[0] - width / 2.0f;
            float posY = position[1];
            FontUtils.getCurrentFont().drawCenteredString((int)Util.mc.thePlayer.getDistance(blockPos) + "m", posX + width / 2.0f, posY + 4.0f, -1);
            float curX = posX + 4.0f;
            for (Block block : this.bedBlocks[index]) {
                ItemStack stack = new ItemStack(block);
                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.disableAlpha();
                GlStateManager.clear(256);
                Util.mc.getRenderItem().zLevel = -150.0f;
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableTexture2D();
                GlStateManager.disableAlpha();
                GlStateManager.disableBlend();
                GlStateManager.enableBlend();
                GlStateManager.enableAlpha();
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                Util.mc.getRenderItem().renderItemIntoGUI(stack, (int)curX, (int)(posY + (float)FontUtils.getCurrentFont().getHeight() + 8.0f));
                Util.mc.getRenderItem().renderItemOverlayIntoGUI(Util.mc.fontRendererObj, stack, (int)curX, (int)(posY + (float)FontUtils.getCurrentFont().getHeight() + 8.0f), null);
                Util.mc.getRenderItem().zLevel = 0.0f;
                GlStateManager.enableAlpha();
                RenderHelper.disableStandardItemLighting();
                GlStateManager.popMatrix();
                curX += 20.0f;
            }
            Util.mc.entityRenderer.setupOverlayRendering();
            ++index;
        }
    };
    private static final Set<Block> targetBlocks = Set.of(Blocks.wool, Blocks.stained_hardened_clay, Blocks.stained_glass, Blocks.planks, Blocks.log, Blocks.log2, Blocks.end_stone, Blocks.obsidian, Blocks.bedrock);

    private void clearBeds() {
        for (int i = 0; i < 8; ++i) {
            this.beds[i] = null;
            this.bedBlocks[i] = new HashSet<Block>();
        }
    }

    @Override
    public void onEnable() {
        this.clearBeds();
        this.retardedList.clear();
    }

    private boolean find(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block bed = Util.mc.theWorld.getBlockState(bedPos).getBlock();
        this.bedBlocks[index].clear();
        this.beds[index] = null;
        if (ArraysKt.contains(this.beds, bedPos)) {
            return false;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int layer = ((Double)this.layers.getValue()).intValue();
        for (int yOffset = 0; yOffset <= layer; ++yOffset) {
            for (int xOffset = -layer; xOffset <= layer; ++xOffset) {
                for (int zOffset = -layer; zOffset <= layer; ++zOffset) {
                    pos.set(bedPos);
                    pos.move(xOffset, yOffset, zOffset);
                    Block blockAtOffset = Util.mc.theWorld.getBlockState(pos).getBlock();
                    if (!targetBlocks.contains(blockAtOffset)) continue;
                    this.bedBlocks[index].add(blockAtOffset);
                }
            }
        }
        if (bed.equals(Blocks.bed)) {
            this.beds[index] = bedPos;
            return true;
        }
        return false;
    }
}

