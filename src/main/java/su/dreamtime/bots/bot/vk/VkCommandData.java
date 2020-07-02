package su.dreamtime.bots.bot.vk;

public class VkCommandData {
    private String line;
    private Integer chatId;
    private Integer userId;
    private String firstName;
    private String lastName;

    public VkCommandData(String firstName, String lastName, String line, Integer userId, Integer chatId) {
        this.line = line;
        this.chatId = chatId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getLine() {
        return line;
    }

    public Integer getChatId() {
        return chatId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public void setUserId(Integer userId) { this.userId = userId; }

}
