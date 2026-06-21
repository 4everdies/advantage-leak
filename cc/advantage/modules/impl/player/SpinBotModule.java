/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.player;

import cc.advantage.api.events.impl.game.PreUpdateEvent;
import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;
import cc.advantage.processes.RotationProcess;
import cc.advantage.utils.Util;
import cc.advantage.utils.misc.MovementFix;
import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import org.lwjgl.util.vector.Vector2f;

@ModuleInfo(label="Spin Bot", category=ModuleCategory.PLAYER)
public final class SpinBotModule
extends Module {
    private final ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Fake);
    private final Property<Boolean> movementFix = new Property<Boolean>("Movement Fix", false, () -> this.mode.getValue() == Mode.Server);
    private int yaw;
    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        this.yaw += 10;
        switch (((Mode)((Object)((Object)this.mode.getValue()))).ordinal()) {
            case 0: {
                Util.mc.thePlayer.renderYawOffset = this.yaw;
                break;
            }
            case 1: {
                RotationProcess.setRotations(new Vector2f(this.yaw, Util.mc.thePlayer.rotationPitch), 10.0, this.movementFix.getValue() != false ? MovementFix.NORMAL : MovementFix.OFF);
                break;
            }
            case 2: {
                Util.mc.thePlayer.rotationYaw = this.yaw;
            }
        }
    };

    public static enum Mode {
        Fake,
        Server,
        Player;

    }
}

