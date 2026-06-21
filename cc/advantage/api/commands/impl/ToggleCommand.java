/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.Advantage;
import cc.advantage.api.commands.Command;
import cc.advantage.modules.Module;

public class ToggleCommand
extends Command {
    public ToggleCommand() {
        super("toggle", "Toggles a module", ".t or .toggle [module]", "t");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            this.usage();
        } else {
            try {
                String moduleName = String.join((CharSequence)" ", args);
                Module module = Advantage.INSTANCE.getModuleManager().getModule(moduleName);
                if (module != null) {
                    module.toggle();
                    this.sendChatWithPrefix("Toggled " + moduleName + "!");
                } else {
                    this.sendChatWithPrefix("Cannot find module \"" + moduleName + "\"");
                }
            }
            catch (Exception e) {
                this.usage();
            }
        }
    }
}
