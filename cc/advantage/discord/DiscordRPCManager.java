/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.discord;

import cc.advantage.Advantage;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;

public class DiscordRPCManager {
    private static final String APPLICATION_ID = "1511465411172896898";
    private static boolean running = false;
    private static long startTime = 0L;

    public static void start() {
        if (running) {
            return;
        }
        try {
            DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(user -> {
                String advantageUser = Advantage.getLoggedUsername();
                String name = advantageUser != null ? advantageUser : Minecraft.getMinecraft().getSession().getUsername();
                System.out.println("Discord RPC connected for " + name);
            }).build();
            DiscordRPC.discordInitialize(APPLICATION_ID, handlers, true);
            DiscordRPC.discordRegister(APPLICATION_ID, "");
            running = true;
            startTime = System.currentTimeMillis() / 1000L;
            new Thread(() -> {
                while (running) {
                    DiscordRPC.discordRunCallbacks();
                    DiscordRPCManager.updatePresence();
                    try {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
            }, "Discord-RPC-Thread").start();
            System.out.println("Discord RPC started successfully.");
        }
        catch (Exception e) {
            System.err.println("Error starting Discord RPC: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (!running) {
            return;
        }
        DiscordRPC.discordShutdown();
        running = false;
        System.out.println("Discord RPC shut down.");
    }

    private static void updatePresence() {
        Object state;
        Object details;
        if (!running) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null && mc.thePlayer != null) {
            String serverIP = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "Local World";
            details = "Playing on " + serverIP;
            state = "Health: " + (int)mc.thePlayer.getHealth() + "/" + (int)mc.thePlayer.getMaxHealth();
        } else {
            details = "In the Main Menu";
            state = "Selecting options...";
        }
        DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder((String)state);
        presence.setDetails((String)details);
        presence.setStartTimestamps(startTime);
        presence.setBigImage("advantage_logo", "Advantage Client");
        DiscordRPC.discordUpdatePresence(presence.build());
    }
}

