/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.modules.ModuleManager;
import cc.advantage.modules.impl.player.ScaffoldModule;
import cc.advantage.utils.Util;
import cc.advantage.utils.mc.PlayerUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;

@ModuleInfo(label="Clutch", category=ModuleCategory.PLAYER)
public final class ClutchModule
extends Module {
    private final NumberProperty voidDistance = new NumberProperty("Void Distance", 10.0, 5.0, 50.0, 1.0);
    private final NumberProperty blockSearchRadius = new NumberProperty("Block Search Radius", 3.0, 1.0, 5.0, 1.0);
    private final Property<Boolean> autoDisable = new Property<Boolean>("Auto Disable", true);
    private ScaffoldModule scaffoldModule;
    private boolean wasScaffoldEnabled = false;
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        if (this.scaffoldModule != null && this.wasScaffoldEnabled && this.scaffoldModule.isEnabled()) {
            this.scaffoldModule.toggle();
            this.wasScaffoldEnabled = false;
        }
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        boolean isAboveVoid;
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null || Util.mc.thePlayer.isDead) {
            if (this.scaffoldModule != null && this.wasScaffoldEnabled && this.scaffoldModule.isEnabled()) {
                this.scaffoldModule.toggle();
                this.wasScaffoldEnabled = false;
            }
            return;
        }
        boolean bl = isAboveVoid = !PlayerUtils.isBlockUnder((Double)this.voidDistance.getValue(), true);
        if (!isAboveVoid) {
            if (this.wasScaffoldEnabled && this.scaffoldModule != null && this.scaffoldModule.isEnabled()) {
                this.scaffoldModule.toggle();
                this.wasScaffoldEnabled = false;
            }
            return;
        }
        boolean hasNearbyBlocks = this.hasBlocksNearby();
        if (hasNearbyBlocks) {
            if (this.scaffoldModule != null && !this.scaffoldModule.isEnabled()) {
                this.scaffoldModule.toggle();
                this.wasScaffoldEnabled = true;
            }
        } else {
            if (this.wasScaffoldEnabled && this.scaffoldModule != null && this.scaffoldModule.isEnabled()) {
                this.scaffoldModule.toggle();
                this.wasScaffoldEnabled = false;
            }
            if (this.autoDisable.getValue().booleanValue()) {
                this.toggle();
            }
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
        this.scaffoldModule = ModuleManager.getInstance(ScaffoldModule.class);
        this.wasScaffoldEnabled = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.scaffoldModule != null && this.wasScaffoldEnabled && this.scaffoldModule.isEnabled()) {
            this.scaffoldModule.toggle();
            this.wasScaffoldEnabled = false;
        }
    }

    private boolean hasBlocksNearby() {
        int radius = ((Double)this.blockSearchRadius.getValue()).intValue();
        BlockPos playerPos = new BlockPos(Util.mc.thePlayer.posX, Util.mc.thePlayer.posY, Util.mc.thePlayer.posZ);
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -2; y <= 1; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos checkPos;
                    if (x == 0 && y == -1 && z == 0 || Util.mc.theWorld.getBlockState(checkPos = playerPos.add(x, y, z)).getBlock() instanceof BlockAir) continue;
                    return true;
                }
            }
        }
        return false;
    }
}

