package su.dreamtime.bots.commands.common;

import su.dreamtime.bots.Client;
import su.dreamtime.bots.Main;

public class CreateNamedConnectionCommand extends CommandResponse{

    public CreateNamedConnectionCommand(Client c, String data) {
        c.setClientName(data);
        Main.getLogger().info("Client " + c + " was registered as " + c.getClientName());
    }
}
