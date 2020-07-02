package su.dreamtime.bots.commands.common;

import su.dreamtime.bots.Client;
import su.dreamtime.bots.Main;
import su.dreamtime.bots.commands.CreateVkCommand;
import su.dreamtime.bots.commands.CreateVKConnectionCommand;
import su.dreamtime.bots.commands.SendVkMessageCommand;

public class CommandListener
{
    public static CommandResponse execute(Client c, Command command) {
        switch (command.getName().toUpperCase()) {

            case "CREATE_NAMED_CONNECTION": {
                return new CreateNamedConnectionCommand(c, command.getData());
            }
            case "CONNECT_TO_VK": {
                return new CreateVKConnectionCommand(c, command.getData());
            }
            case "SEND_VK_MESSAGE": {
                return new SendVkMessageCommand(c, command.getData());
            }
            case "CREATE_VK_COMMAND": {
                return new CreateVkCommand(c, command.getData());
            }
            default: {
                Main.getLogger().debug("Command \"" + command.getName() + "\" want to be executed but has no implementation.");
                break;
            }
        }
        return null;
    }
}
