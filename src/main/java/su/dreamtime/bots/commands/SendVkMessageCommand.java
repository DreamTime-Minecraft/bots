package su.dreamtime.bots.commands;

import su.dreamtime.bots.Client;
import su.dreamtime.bots.Main;
import su.dreamtime.bots.bot.vk.VKBot;
import su.dreamtime.bots.bot.vk.VkMessage;
import su.dreamtime.bots.commands.common.CommandResponse;
import su.dreamtime.bots.util.JsonParser;

public class SendVkMessageCommand extends CommandResponse {

    public SendVkMessageCommand(Client c, String data) {
        VkMessage msg = JsonParser.parseJson(data, VkMessage.class);
        if (msg == null || msg.getChatId() == null){
            return;
        }
        if (msg.getChatId() == -1 || msg.getUserId() == -1) {
            Main.getLogger().info(msg);
        }
        try {
            Integer userId = msg.getUserId();
            if (userId == null) {
                userId = 0;
            }
            VKBot.getFromHash(c.getVkHash()).sendMessage(userId, msg.getChatId(), msg.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
