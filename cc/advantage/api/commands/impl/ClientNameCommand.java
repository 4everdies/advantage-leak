/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.api.commands.Command;
import cc.advantage.modules.impl.visuals.WatermarkModule;

public class ClientNameCommand
extends Command {
    public ClientNameCommand() {
        super("clientname", "Changes the client name in watermarks", ".clientname [name]", "cn");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            this.usage();
        } else {
            String newName;
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 0; i < args.length; ++i) {
                nameBuilder.append(args[i]);
                if (i >= args.length - 1) continue;
                nameBuilder.append(" ");
            }
            WatermarkModule.customName = newName = nameBuilder.toString();
            this.sendChatWithPrefix("Set client name to: " + newName);
        }
    }
}
