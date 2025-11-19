package util;

import java.util.HashMap;
import java.util.Map;

/**
 * Very small JSON helper used for pretty-printing payloads in the dashboard and
 * logs. It is not a full JSON implementation but suffices for key/value maps.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    public static String toJson(Map<String, String> map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append('"').append(entry.getKey()).append('"').append(": ")
                    .append('"').append(entry.getValue()).append('"');
            first = false;
        }
        builder.append("}");
        return builder.toString();
    }

    public static Map<String, String> fromJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isEmpty()) {
            return map;
        }
        String cleaned = json.trim();
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) {
            return map;
        }
        String[] entries = cleaned.split(",");
        for (String entry : entries) {
            String[] kv = entry.split(":");
            if (kv.length == 2) {
                String key = kv[0].replace("\"", "").trim();
                String value = kv[1].replace("\"", "").trim();
                map.put(key, value);
            }
        }
        return map;
    }
}
