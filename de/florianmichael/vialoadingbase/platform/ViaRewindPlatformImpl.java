/*
 * Decompiled with CFR 0.152.
 */
package de.florianmichael.vialoadingbase.platform;

import com.viaversion.viarewind.api.ViaRewindPlatform;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import java.io.File;
import java.util.logging.Logger;

public class ViaRewindPlatformImpl
implements ViaRewindPlatform {
    private final File directory;

    public ViaRewindPlatformImpl(File directory) {
        this.directory = directory;
        this.init(new File(this.directory, "viarewind.yml"));
    }

    @Override
    public Logger getLogger() {
        return ViaLoadingBase.LOGGER;
    }

    @Override
    public File getDataFolder() {
        return this.directory;
    }
}

