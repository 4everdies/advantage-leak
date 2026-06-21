/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.Advantage;
import cc.advantage.api.commands.Command;
import cc.advantage.api.config.Config;
import cc.advantage.utils.client.Logger;

public class ConfigCommand
extends Command {
    public ConfigCommand() {
        super("config", "Saves or Loads Configs.", ".config save/load/remove [config] or .config list or .config binds save/load", "c");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            this.usage();
            return;
        }
        String command = Advantage.requireNonNull(args[0]);
        if (command.equalsIgnoreCase("binds")) {
            if (args.length != 2) {
                this.usage();
                return;
            }
            String subCommand = Advantage.requireNonNull(args[1]);
            if (subCommand.equalsIgnoreCase("save")) {
                Advantage.INSTANCE.getBindsConfig().saveToFile();
                Logger.chatPrint("Binds have been saved.");
            } else if (subCommand.equalsIgnoreCase("load")) {
                Advantage.INSTANCE.getBindsConfig().loadFromFile();
                Logger.chatPrint("Binds have been loaded.");
            } else {
                this.usage();
            }
        }
        if (command.equalsIgnoreCase("save")) {
            if (args.length != 2) {
                this.usage();
                return;
            }
            name = Advantage.requireNonNull(args[1]);
            Advantage.INSTANCE.getConfigManager().saveConfig(name);
            Logger.chatPrint("Config '" + name + "' has been saved.");
        } else if (command.equalsIgnoreCase("load")) {
            if (args.length != 2) {
                this.usage();
                return;
            }
            name = Advantage.requireNonNull(args[1]);
            Advantage.INSTANCE.getConfigManager().loadConfig(name);
            Logger.chatPrint("Config '" + name + "' has been loaded.");
        } else if (command.equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                this.usage();
                return;
            }
            name = Advantage.requireNonNull(args[1]);
            Advantage.INSTANCE.getConfigManager().deleteConfig(name);
            Logger.chatPrint("Config '" + name + "' has been removed.");
        } else if (command.equalsIgnoreCase("list")) {
            if (args.length != 1) {
                this.usage();
                return;
            }
            Logger.chatPrint("Available Configs:");
            for (Config config : Advantage.INSTANCE.getConfigManager().getElements()) {
                Logger.chatPrint(config.getName());
            }
        } else {
            this.usage();
        }
    }
}
