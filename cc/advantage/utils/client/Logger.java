/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.utils.client;

import cc.advantage.utils.Util;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Logger
extends Util {
    public static void chatPrint(boolean prefix, String message) {
        if (Logger.mc.thePlayer != null) {
            if (prefix) {
                message = String.valueOf((Object)EnumChatFormatting.DARK_BLUE) + "Advantage" + String.valueOf((Object)EnumChatFormatting.WHITE) + " | " + String.valueOf((Object)EnumChatFormatting.WHITE) + (String)message;
            }
            Logger.mc.thePlayer.addChatMessage(new ChatComponentText((String)message));
        }
    }

    public static void chatError(String message) {
        if (Logger.mc.thePlayer != null) {
            Logger.mc.thePlayer.addChatMessage(new ChatComponentText("Error | " + message));
        }
    }

    public static void chatPrint(String prefix, EnumChatFormatting color, String message) {
        if (Logger.mc.thePlayer != null) {
            message = "\u00a77[\u00a7" + color.formattingCode + "\u00a7l" + prefix.toUpperCase() + "\u00a7r\u00a77]\u00a7r \u00a7" + color.formattingCode + (String)message;
            Logger.mc.thePlayer.addChatMessage(new ChatComponentText((String)message));
        }
    }

    public static void chatPrint(Object o) {
        Logger.chatPrint(true, String.valueOf(o));
    }

    public static void sendChat(String message) {
        if (Logger.mc.thePlayer != null) {
            Logger.mc.thePlayer.sendChatMessage(message);
        }
    }
}

