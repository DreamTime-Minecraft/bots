package su.dreamtime.bots.commands.common;

import com.google.gson.annotations.SerializedName;

public abstract class CommandResponse {;

    /** Переменная служащая для того, чтобы ответ не был null*/
    @SerializedName("KLJfdsfd")
    protected String KLJfdsfd = "fdsfd";

    public CommandResponse(String data){

    }

    protected CommandResponse() {
    }

    @Override
    public String toString() {
        return "CommandResponse{" +
                "KLJfdsfd='" + KLJfdsfd + '\'' +
                '}';
    }
}
