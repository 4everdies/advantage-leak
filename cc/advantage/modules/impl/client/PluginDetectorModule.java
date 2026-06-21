/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.events.impl.packet.PacketReceiveEvent;
import cc.advantage.api.events.impl.player.MotionEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.client.Logger;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.mc.PacketUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;

@ModuleInfo(label="Plugin Detector", category=ModuleCategory.CLIENT)
public final class PluginDetectorModule
extends Module {
    private final Set<String> cachedPlugins = new LinkedHashSet<String>();
    private final Timer clock = new Timer();
    private int step = 0;
    private boolean finished = false;
    private long lastStepTime = 0L;
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> this.toggle();
    @EventLink
    public final Listener<MotionEvent> motionEventListener = event -> {
        if (!event.isPre()) {
            return;
        }
        if (!this.finished && System.currentTimeMillis() - this.lastStepTime > 500L) {
            ++this.step;
            this.sendNextRequest();
            this.lastStepTime = System.currentTimeMillis();
        }
        if (!this.finished && this.clock.hasTimeElapsed(7000.0)) {
            Logger.chatPrint("\u00a7c\u00a7lFailed to detect plugins");
            this.toggle();
            this.finished = true;
        }
    };
    @EventLink
    public final Listener<PacketReceiveEvent> packetReceiveEventListener = event -> {
        String[] commands;
        if (!(event.getPacket() instanceof S3APacketTabComplete)) {
            return;
        }
        for (String command : commands = ((S3APacketTabComplete)event.getPacket()).func_149630_c()) {
            String[] parts = command.split(":");
            if (parts.length <= 1 || parts[0].startsWith("/minecraft")) continue;
            String pluginName = parts[0].replace("/", "");
            this.cachedPlugins.add(pluginName);
        }
        if (this.step >= 3) {
            this.finishDetection();
        }
    };

    private void resetState() {
        this.cachedPlugins.clear();
        this.clock.reset();
        this.step = 0;
        this.finished = false;
        this.lastStepTime = 0L;
    }

    @Override
    public void onEnable() {
        this.resetState();
        this.sendNextRequest();
        this.lastStepTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    private void sendNextRequest() {
        if (this.finished) {
            return;
        }
        switch (this.step) {
            case 0: {
                PacketUtils.sendPacket(new C14PacketTabComplete("/version "));
                break;
            }
            case 1: {
                PacketUtils.sendPacket(new C14PacketTabComplete("/bukkit:version "));
                break;
            }
            case 2: {
                PacketUtils.sendPacket(new C14PacketTabComplete("/"));
                break;
            }
            default: {
                this.finishDetection();
            }
        }
    }

    private void finishDetection() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        if (this.cachedPlugins.isEmpty()) {
            Logger.chatPrint("\u00a76\u00a7lNo plugins found");
        } else {
            String joined = String.join((CharSequence)"\u00a77, \u00a7a", this.cachedPlugins);
            Logger.chatPrint("\u00a76\u00a7lPlugins \u00a77(\u00a7b" + this.cachedPlugins.size() + "\u00a77) \u00a7oTook " + this.clock.getTime() + "ms");
            Logger.chatPrint("\u00a7a" + joined);
        }
        this.toggle();
    }
}

