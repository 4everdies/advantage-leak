/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.render.Render2DEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

@ModuleInfo(label="Nick Hider", category=ModuleCategory.VISUALS)
public class NickHiderModule
extends Module {
    private final Property<String> fakeNameProp = new Property<String>("Fake Name", "x0lumie");
    private String fakeName = " ";
    @EventLink
    public Listener<PacketReceiveEvent> onPacketReceive = event -> {
        if (Util.mc.thePlayer == null) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof S02PacketChat) {
            S02PacketChat wrapper = (S02PacketChat)packet;
            IChatComponent iChatComponent = wrapper.getChatComponent();
            if (iChatComponent instanceof ChatComponentText) {
                String newMessage = iChatComponent.getFormattedText().replace(Util.mc.thePlayer.getGameProfile().getName(), this.fakeName);
                ChatComponentText newChatComponentText = new ChatComponentText(newMessage);
                wrapper.setChatComponent(newChatComponentText);
            }
            event.setPacket(wrapper);
        }
    };
    @EventLink
    public Listener<Render2DEvent> onRender2D = event -> {
        if (Util.mc.thePlayer == null || Util.mc.theWorld == null) {
            return;
        }
        this.fakeName = String.valueOf((Object)EnumChatFormatting.RED) + String.valueOf((Object)EnumChatFormatting.BOLD) + this.fakeNameProp.getValue();
        for (NetworkPlayerInfo player : Util.mc.getNetHandler().getPlayerInfoMap()) {
            if (player.getGameProfile().getName().length() < 3 || player.getDisplayName() == null) continue;
            player.setDisplayName(new ChatComponentText(player.getDisplayName().getFormattedText().replaceFirst(Util.mc.thePlayer.getGameProfile().getName(), this.fakeName)));
        }
    };
}

