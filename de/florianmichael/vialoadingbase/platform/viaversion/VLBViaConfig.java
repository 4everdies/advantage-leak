/*
 * Decompiled with CFR 0.152.
 */
package de.florianmichael.vialoadingbase.platform.viaversion;

import com.viaversion.viaversion.configuration.AbstractViaConfig;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class VLBViaConfig
extends AbstractViaConfig {
    private static final List<String> UNSUPPORTED = Arrays.asList("anti-xray-patch", "quick-move-action-fix", "nms-player-ticking", "velocity-ping-interval", "velocity-ping-save", "velocity-servers", "blockconnection-method", "change-1_9-hitbox", "change-1_14-hitbox", "show-shield-when-sword-in-hand", "left-handed-handling");

    public VLBViaConfig(File configFile, Logger logger) {
        super(configFile, logger);
        this.reload();
    }

    @Override
    protected void handleConfig(Map<String, Object> config) {
    }

    @Override
    public List<String> getUnsupportedOptions() {
        return UNSUPPORTED;
    }
}

