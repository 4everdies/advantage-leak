/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.processes;

import cc.advantage.Advantage;
import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.packet.PacketSendEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.modules.impl.client.FpsEnhancerModule;
import cc.advantage.utils.client.Timer;
import cc.advantage.utils.client.ViaMCPFixes;
import cc.advantage.utils.render.DragUtils;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;

public class BackgroundProcess {
    private Timer cfgTimer = new Timer();
    @EventLink
    public final Listener<PreUpdateEvent> preUpdateEventListener = e -> {
        if (this.cfgTimer.hasTimeElapsed(30000.0, true)) {
            if (Advantage.INSTANCE.getModuleManager().getModule(FpsEnhancerModule.class).isEnabled() && FpsEnhancerModule.disableConfigAutoSave.getValue().booleanValue()) {
                return;
            }
            Advantage.INSTANCE.getConfigManager().saveConfig("default");
            Advantage.INSTANCE.getBindsConfig().saveToFile();
        }
        DragUtils.update();
    };
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = e -> {
        ViaMCPFixes.initialized = false;
    };
    @EventLink
    public final Listener<PacketSendEvent> packetSendEventListener = ViaMCPFixes::handleFixedSendPackets;
}

