/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.properties.impl.NumberProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;

@ModuleInfo(label="Aspect Ratio", category=ModuleCategory.VISUALS)
public final class AspectRatioModule
extends Module {
    public static NumberProperty aspect = new NumberProperty("Aspect", 1.0, 0.1f, 5.0, 0.1f);
}

