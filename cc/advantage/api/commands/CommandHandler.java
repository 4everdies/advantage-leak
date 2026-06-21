/*
 * Decompiled with CFR 0.152.
 */
package cc.advantage.api.commands;

import cc.advantage.api.commands.Command;
import cc.advantage.utils.client.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHandler {
    public static String CHAT_PREFIX = ".";
    public List<Command> commands = new ArrayList<Command>();

    public boolean execute(String txt) {
        if (!txt.startsWith(CHAT_PREFIX)) {
            return false;
        }
        String[] arguments = txt.substring(1).split(" ");
        for (Command command : this.commands) {
            if (!command.getName().equalsIgnoreCase(arguments[0]) && !Arrays.stream(command.getOtherPrefixes()).anyMatch(p -> p.equalsIgnoreCase(arguments[0]))) continue;
            command.execute(Arrays.copyOfRange(arguments, 1, arguments.length));
            return true;
        }
        Logger.chatPrint("Command doesnt exist");
        return false;
    }

    public List<Command> getCommands() {
        return this.commands;
    }

    public Command getCommand(Class<? extends Command> command) {
        return this.getCommands().stream().filter(com -> command == com.getClass()).findFirst().orElse(null);
    }
}
