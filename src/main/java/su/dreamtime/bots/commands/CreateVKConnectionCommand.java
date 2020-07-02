package su.dreamtime.bots.commands;

import su.dreamtime.bots.Client;
import su.dreamtime.bots.bot.vk.VKBot;
import su.dreamtime.bots.commands.common.CommandResponse;
import su.dreamtime.bots.util.JsonParser;

import java.util.Map;

public class CreateVKConnectionCommand extends CommandResponse {

    private String hash;
    private Boolean success;
    public CreateVKConnectionCommand(Client c, String data) {
        Map<String, Object> mapData = JsonParser.parseData(data);
        if (mapData != null) {
            Integer groupId = Integer.parseInt((String) mapData.get("groupId"));
            String accessToken = (String) mapData.get("token");

            if (groupId != null && accessToken != null) {
                hash = VKBot.hashBot(groupId, accessToken);
                VKBot.create(groupId, accessToken);
                c.setVkHash(hash);
            }
        }


        if (hash != null) {
            success = true;
        } else {
            success = false;
        }

    }

}
