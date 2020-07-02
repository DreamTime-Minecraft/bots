package su.dreamtime.bots.bot.vk;

public class VkMessage {
    private Integer userId;
    private Integer chatId;
    private String message;

    public VkMessage(int userId, int chatId, String message) {
        this.userId = userId;
        this.chatId = chatId;
        this.message = message;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "VkMessage{" +
                "userId=" + userId +
                ", chatId=" + chatId +
                ", message='" + message + '\'' +
                '}';
    }
}