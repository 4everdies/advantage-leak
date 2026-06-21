/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.visuals;

import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;

@ModuleInfo(label="Capes", category=ModuleCategory.VISUALS)
public final class CapesModule
extends Module {
    public static ModeProperty<Cape> cape = new ModeProperty<Cape>("Cape", Cape.Advantage);

    public static enum Cape {
        Advantage,
        Rise,
        Gato,
        Minecon,
        Kitty,
        Blonde,
        Epstein,
        OMG,
        Exhibition,
        Slack;

    }
}

