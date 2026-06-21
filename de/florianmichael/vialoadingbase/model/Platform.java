/*
 * Decompiled with CFR 0.152.
 */
package de.florianmichael.vialoadingbase.model;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Platform {
    public static int COUNT = 0;
    private final String name;
    private final BooleanSupplier load;
    private final Runnable executor;
    private final Consumer<List<ProtocolVersion>> versionCallback;

    public Platform(String name, BooleanSupplier load, Runnable executor) {
        this(name, load, executor, null);
    }

    public Platform(String name, BooleanSupplier load, Runnable executor, Consumer<List<ProtocolVersion>> versionCallback) {
        this.name = name;
        this.load = load;
        this.executor = executor;
        this.versionCallback = versionCallback;
    }

    public String getName() {
        return this.name;
    }

    public void createProtocolPath() {
        if (this.versionCallback != null) {
            this.versionCallback.accept(ViaLoadingBase.PROTOCOLS);
        }
    }

    public void build(Logger logger) {
        if (this.load.getAsBoolean()) {
            try {
                this.executor.run();
                logger.info("Loaded Platform " + this.name);
                ++COUNT;
            }
            catch (Throwable t) {
                logger.severe("An error occurred while loading Platform " + this.name + ":");
                t.printStackTrace();
            }
            return;
        }
        logger.severe("Platform " + this.name + " is not present");
    }
}

