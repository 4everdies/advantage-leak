/*
 * Decompiled with CFR 0.152.
 */
package de.florianmichael.viamcp;

import cc.advantage.utils.client.ViaMCPFixes;
import com.viaversion.viaversion.api.connection.UserConnection;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.viamcp.gui.AsyncVersionSlider;
import java.io.File;

public class ViaMCP {
    public static final int NATIVE_VERSION = 47;
    public static ViaMCP INSTANCE;
    public UserConnection user;
    private AsyncVersionSlider asyncVersionSlider;

    public static void create() {
        INSTANCE = new ViaMCP();
    }

    public ViaMCP() {
        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File("ViaMCP")).nativeVersion(47).onProtocolReload(protocolVersion -> {
            if (this.getAsyncVersionSlider() != null) {
                this.getAsyncVersionSlider().setVersion(protocolVersion.getVersion());
            }
        }).build();
        ViaMCPFixes.applyNibblesPatches();
        System.setProperty("com.viaversion.handlePingsAsInvAcknowledgements", "true");
    }

    public void initAsyncSlider() {
        this.initAsyncSlider(5, 5, 110, 20);
    }

    public void initAsyncSlider(int x, int y, int width, int height) {
        this.asyncVersionSlider = new AsyncVersionSlider(-1, x, y, Math.max(width, 110), height);
    }

    public AsyncVersionSlider getAsyncVersionSlider() {
        return this.asyncVersionSlider;
    }
}

