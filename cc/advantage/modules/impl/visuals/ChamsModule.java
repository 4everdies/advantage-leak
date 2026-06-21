/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(label="Chams", category=ModuleCategory.VISUALS)
public final class ChamsModule
extends Module {
    private static final Property<Boolean> tileEntities = new Property<Boolean>("Tile Entities", false);

    public static boolean shouldRender(Entity entity) {
        return entity instanceof EntityPlayer && (!(entity instanceof EntityPlayerSP) || Util.mc.gameSettings.thirdPersonView != 0);
    }

    public static boolean doRenderTileEntities() {
        return tileEntities.getValue();
    }
}

