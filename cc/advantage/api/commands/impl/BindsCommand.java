/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.Advantage;
import cc.advantage.api.commands.Command;
import org.lwjgl.input.Keyboard;

public class BindsCommand
extends Command {
    public BindsCommand() {
        super("binds", "Lists binds", ".binds .bs", "bs");
    }

    @Override
    public void execute(String[] args) {
        StringBuilder sb = new StringBuilder();
        Advantage.INSTANCE.getModuleManager().getModules().forEach(module -> {
            int key = module.getKey();
            if (key != 0) {
                String keyName = Keyboard.getKeyName(key);
                sb.append("\n").append(module.getLabel()).append(" | ").append(keyName);
            }
        });
        this.sendChatWithPrefix(sb.toString());
    }
}
