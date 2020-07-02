package su.dreamtime.bots.util;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class JsonParser {
    private static Gson gson = new Gson();

    public static <T> T  parseJson(String json, Class<T> classType) {
        return gson.fromJson(json, classType);
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }

    public static Map<String, Object> parseData(String data) {
        return JsonParser.parseJson(data, new HashMap<String, String>().getClass());
    }
}
