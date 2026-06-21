/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.game.MiddleClickEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import cc.advantage.utils.client.Logger;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(label="MCF", category=ModuleCategory.CLIENT, description="Middle Click to exclude players from targeting")
public class MCFModule
extends Module {
    public static final List<Entity> excludedPlayers = new ArrayList<Entity>();
    @EventLink
    public final Listener<MiddleClickEvent> onMiddleClick = event -> {
        EntityPlayer clickedPlayer;
        if (Util.mc.objectMouseOver != null && Util.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && Util.mc.objectMouseOver.entityHit instanceof EntityPlayer && (clickedPlayer = (EntityPlayer)Util.mc.objectMouseOver.entityHit) != Util.mc.thePlayer) {
            if (excludedPlayers.contains(clickedPlayer)) {
                excludedPlayers.remove(clickedPlayer);
                Logger.chatPrint("\u00a7a[MCF] \u00a7fRemoved \u00a7c" + clickedPlayer.getName() + "\u00a7f from exclusion list");
            } else {
                excludedPlayers.add(clickedPlayer);
                Logger.chatPrint("\u00a7a[MCF] \u00a7fAdded \u00a7c" + clickedPlayer.getName() + "\u00a7f to exclusion list");
            }
            event.setCancelled(true);
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> excludedPlayers.clear();

    @Override
    public void onDisable() {
        excludedPlayers.clear();
        super.onDisable();
    }
}

