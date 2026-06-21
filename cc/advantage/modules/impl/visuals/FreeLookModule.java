/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.events.impl.world.WorldLoadEvent;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.utils.Util;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import lombok.Generated;

@ModuleInfo(label="Free Look", category=ModuleCategory.VISUALS)
public final class FreeLookModule
extends Module {
    private float cameraYaw;
    private float cameraPitch;
    private int prevThirdPersonView;
    private boolean enableLook = false;
    @EventLink
    public final Listener<WorldLoadEvent> worldLoadEventListener = event -> {
        this.enableLook = false;
    };
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (Util.mc.thePlayer != null && !this.enableLook) {
            this.prevThirdPersonView = Util.mc.gameSettings.thirdPersonView;
            Util.mc.gameSettings.thirdPersonView = 1;
            this.cameraYaw = Util.mc.thePlayer.rotationYaw;
            this.cameraPitch = Util.mc.thePlayer.rotationPitch;
            this.enableLook = true;
        }
    };

    @Override
    public void onDisable() {
        this.enableLook = false;
        Util.mc.gameSettings.thirdPersonView = this.prevThirdPersonView;
        super.onDisable();
    }

    public boolean freeLooked() {
        return this.enableLook;
    }

    @Generated
    public float getCameraYaw() {
        return this.cameraYaw;
    }

    @Generated
    public float getCameraPitch() {
        return this.cameraPitch;
    }

    @Generated
    public void setCameraYaw(float cameraYaw) {
        this.cameraYaw = cameraYaw;
    }

    @Generated
    public void setCameraPitch(float cameraPitch) {
        this.cameraPitch = cameraPitch;
    }
}

