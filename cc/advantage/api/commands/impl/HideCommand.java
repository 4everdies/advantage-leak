/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.Advantage;
import cc.advantage.api.commands.Command;
import cc.advantage.modules.Module;

public class HideCommand
extends Command {
    public HideCommand() {
        super("hide", "Hides a module", ".h or .hide [module]", "h");
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            this.usage();
        } else {
            try {
                String moduleName = String.join((CharSequence)" ", args);
                Module module = Advantage.INSTANCE.getModuleManager().getModule(moduleName);
                if (moduleName.equalsIgnoreCase("all")) {
                    if (Advantage.INSTANCE.getModuleManager().getModules().stream().anyMatch(Module::isHidden)) {
                        Advantage.INSTANCE.getModuleManager().getModules().forEach(m -> m.setHidden(false));
                        this.sendChatWithPrefix("Unhid all modules!");
                    } else {
                        Advantage.INSTANCE.getModuleManager().getModules().forEach(m -> m.setHidden(true));
                        this.sendChatWithPrefix("Hid all modules!");
                    }
                } else if (module != null) {
                    module.setHidden(!module.isHidden());
                    if (module.isHidden()) {
                        this.sendChatWithPrefix("Hid " + module.getLabel() + "!");
                    } else {
                        this.sendChatWithPrefix("Unhid " + module.getLabel() + "!");
                    }
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
