/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

@ModuleInfo(label="Auto Play", category=ModuleCategory.CLIENT)
public class AutoPlayModule
extends Module {
    private static final String TARGET_MESSAGE = "Deseja jogar novamente? CLIQUE AQUI!";
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    public long delayMs = 1500L;
    @EventLink
    public final Listener<PacketReceiveEvent> onChat = event -> {
        String message;
        Packet<?> packet = event.getPacket();
        if (packet instanceof S02PacketChat && (message = ((S02PacketChat)packet).getChatComponent().getUnformattedText()).contains(TARGET_MESSAGE)) {
            this.trigger();
        }
    };

    private void trigger() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(String.valueOf((Object)EnumChatFormatting.BLUE) + "[AutoPlay] " + String.valueOf((Object)EnumChatFormatting.GRAY) + "Sending you to the next game in " + String.valueOf((Object)EnumChatFormatting.WHITE) + (double)this.delayMs / 1000.0 + "s..."));
        }
        this.executor.schedule(() -> {
            if (mc.thePlayer != null) {
                mc.thePlayer.sendChatMessage("/playagain");
            }
        }, this.delayMs, TimeUnit.MILLISECONDS);
    }

    public void setDelay(double seconds) {
        this.delayMs = (long)(seconds * 1000.0);
    }
}

