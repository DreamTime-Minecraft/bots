package su.dreamtime.bots.commands.common;

import su.dreamtime.bots.Client;
import su.dreamtime.bots.Main;

public class CreateNamedConnectionCommand extends CommandResponse{
    String hello;
    public CreateNamedConnectionCommand(Client c, String data) {
        c.setClientName(data);
        Main.addClient(c);
        Main.getLogger().info("Client " + c + " was connected and registered");
    }
}
