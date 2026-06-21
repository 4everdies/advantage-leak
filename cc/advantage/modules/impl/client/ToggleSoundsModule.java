/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.modules.impl.client;

import cc.advantage.api.properties.impl.ModeProperty;
import cc.advantage.modules.Module;
import cc.advantage.modules.ModuleCategory;
import cc.advantage.modules.ModuleInfo;

@ModuleInfo(label="Toggle Sounds", category=ModuleCategory.CLIENT)
public class ToggleSoundsModule
extends Module {
    public static ModeProperty<Mode> mode = new ModeProperty<Mode>("Mode", Mode.Advantage);

    public static enum Mode {
        Advantage("Advantage"),
        Augustus("Augustus"),
        Vanilla("Vanilla"),
        Sigma5("Sigma 5.0"),
        Note("Note");

        public String name;

        private Mode(String name) {
            this.name = name;
        }

        public String toString() {
            return this.name;
        }
    }
}

