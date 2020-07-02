package su.dreamtime.bots.commands;

import su.dreamtime.bots.Client;
import su.dreamtime.bots.bot.vk.VKBot;
import su.dreamtime.bots.commands.common.CommandResponse;
import su.dreamtime.bots.util.JsonParser;

import java.util.Map;

public class CreateVkCommand extends CommandResponse {
    public CreateVkCommand(Client c, String data) {
        Map<String,Object> mapData = JsonParser.parseData(data);
        String hash = (String)mapData.get("hash");
        String cmd = (String) mapData.get("cmd");

        if (hash != null && cmd != null) {
            VKBot.getFromHash(hash).registerCommand(c, cmd);
        }
    }
}
