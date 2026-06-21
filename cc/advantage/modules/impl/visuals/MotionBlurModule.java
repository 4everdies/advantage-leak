/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;

@ModuleInfo(label="Motion Blur", category=ModuleCategory.VISUALS)
public final class MotionBlurModule
extends Module {
    public NumberProperty blurAmount = new NumberProperty("Blur Amount", 7.0, 0.0, 10.0, 0.1);
}

