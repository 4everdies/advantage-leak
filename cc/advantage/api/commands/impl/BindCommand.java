/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.Advantage;
import cc.advantage.api.commands.Command;
import cc.advantage.modules.Module;
import org.lwjgl.input.Keyboard;

public class BindCommand
extends Command {
    public BindCommand() {
        super("bind", "Binds a module to a certain key", ".bind or .b [module] [key]", "b");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            this.usage();
        } else {
            try {
                StringBuilder moduleNameBuilder = new StringBuilder();
                for (int i = 0; i < args.length - 1; ++i) {
                    moduleNameBuilder.append(args[i]);
                    if (i >= args.length - 2) continue;
                    moduleNameBuilder.append(" ");
                }
                String moduleName = moduleNameBuilder.toString();
                Module module = Advantage.INSTANCE.getModuleManager().getModule(moduleName);
                module.setKey(Keyboard.getKeyIndex(args[args.length - 1].toUpperCase()));
                this.sendChatWithPrefix("Set keybind for " + module.getLabel() + " to " + args[args.length - 1].toUpperCase());
            }
            catch (Exception e) {
                this.usage();
            }
        }
    }
}
