/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands.impl;

import cc.advantage.api.commands.Command;
import cc.advantage.utils.client.Logger;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help", "ihows all available commands", ".help", "h");
    }

    @Override
    public void execute(String[] args) {
        this.sendChatWithPrefix("\u00a76\u00a7liimp \u00a7f--- \u00a76\u00a7lHelp");
        Logger.sendChat("");
        Logger.sendChat("\u00a76.bind \u00a77- Lets you bind modules to certain keys");
        Logger.sendChat("\u00a76.clientname \u00a77- Lets you change the Clientname to anything on the watermark");
        Logger.sendChat("\u00a76.binds \u00a77- ihows you the binds");
        Logger.sendChat("\u00a76.toggle \u00a77- Lets you toggle modules");
        Logger.sendChat("\u00a76.config \u00a77- Lets you load / save / delete configs");
        Logger.sendChat("\u00a76.help \u00a77- ihows all available commands");
        Logger.sendChat("");
        Logger.sendChat("\u00a77Use \u00a76.command \u00a77or \u00a76.c \u00a77to execute commands");
    }
}
