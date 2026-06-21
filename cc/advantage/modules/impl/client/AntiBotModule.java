/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.NonNull
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import com.mojang.authlib.GameProfile;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;

@ModuleInfo(label="Anti Bot", category=ModuleCategory.CLIENT)
public class AntiBotModule
extends Module {
    public ModeProperty<Mode> modeProperty = new ModeProperty<Mode>("Mode", Mode.TabList);
    public static final List<EntityLivingBase> botList = new ArrayList<EntityLivingBase>();
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        this.setSuffix(((Mode)((Object)((Object)this.modeProperty.getValue()))).toString());
        botList.clear();
        switch (((Mode)((Object)((Object)this.modeProperty.getValue()))).ordinal()) {
            case 1: {
                Util.mc.theWorld.playerEntities.forEach(player -> {
                    if (player != Util.mc.thePlayer && !player.moved) {
                        botList.add((EntityLivingBase)player);
                    }
                });
                break;
            }
            case 0: {
                List<String> tabList = AntiBotModule.getTablist();
                Util.mc.theWorld.playerEntities.forEach(player -> {
                    if (player != Util.mc.thePlayer && !tabList.contains(player.getName())) {
                        botList.add((EntityLivingBase)player);
                    }
                });
            }
        }
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        if (!Util.mc.theWorld.playerEntities.isEmpty()) {
            Util.mc.theWorld.playerEntities.forEach(player -> botList.clear());
        }
    };

    @NonNull
    private static List<String> getTablist() {
        return Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().parallelStream().map(NetworkPlayerInfo::getGameProfile).filter(profile -> profile.getId() != Minecraft.getMinecraft().thePlayer.getUniqueID()).map(GameProfile::getName).collect(Collectors.toList());
    }

    @Override
    public void onDisable() {
        botList.clear();
        super.onDisable();
    }

    private static enum Mode {
        TabList("Tab List"),
        NPC("NPC");

        public final String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

