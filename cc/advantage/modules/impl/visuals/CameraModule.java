/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.properties.Property;
import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;

@ModuleInfo(label="Camera", category=ModuleCategory.VISUALS)
public final class CameraModule
extends Module {
    public static ModeProperty<AnimationMode> mode = new ModeProperty<AnimationMode>("Style", AnimationMode.Old);
    public static NumberProperty x = new NumberProperty("X", 0.0, -2.0, 2.0, 0.05f);
    public static NumberProperty y = new NumberProperty("Y", 0.0, -2.0, 2.0, 0.05f);
    public static NumberProperty z = new NumberProperty("Z", 0.0, -2.0, 2.0, 0.05f);
    public static NumberProperty scale = new NumberProperty("Scale", 1.0, 0.1, 2.0, 0.1);
    public static NumberProperty slowdown = new NumberProperty("Slowdown", 1.0, 1.0, 15.0, 1.0);
    public static Property<Boolean> fluxSwing = new Property<Boolean>("Flux Swing", false);
    public static Property<Boolean> dontResetBlock = new Property<Boolean>("Dont Reset Block", true);
    public static Property<Boolean> swingEating = new Property<Boolean>("Swing While Eating", false);
    public static Property<Boolean> noHurtCamera = new Property<Boolean>("No Hurt Camera", true);
    public static Property<Boolean> noSneakCamera = new Property<Boolean>("No Sneak Camera", false);
    public static Property<Boolean> noFireOverlay = new Property<Boolean>("No Fire Overlay", true);
    public static Property<Boolean> noBlindness = new Property<Boolean>("No Blindness", true);

    public CameraModule() {
        this.toggle();
    }

    public static enum AnimationMode {
        Slide,
        Old,
        Exhibition,
        Novoline,
        Spin,
        Noov,
        Smooth,
        Leaked;

    }
}

