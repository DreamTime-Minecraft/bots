package su.dreamtime.bots.commands.common;

public class Command {
    private String name;
    private String data;
    public boolean response = false;
    public Command(String name, String data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public boolean isResponse() {
        return response;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }
}
