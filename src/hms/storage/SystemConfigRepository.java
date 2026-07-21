package hms.storage;

import hms.util.FileManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Simple key=value store for global settings such as the base consultation rate. */
public class SystemConfigRepository {
    private static final String FILE = "system_config.txt";

    public Map<String, String> loadAll() {
        Map<String, String> map = new LinkedHashMap<>();
        for (String line : FileManager.readLines(FILE)) {
            int idx = line.indexOf('=');
            if (idx > 0) {
                map.put(line.substring(0, idx), line.substring(idx + 1));
            }
        }
        if (!map.containsKey("baseConsultationRate")) {
            map.put("baseConsultationRate", "50.00");
        }
        return map;
    }

    public String get(String key, String defaultValue) {
        return loadAll().getOrDefault(key, defaultValue);
    }

    public void set(String key, String value) {
        Map<String, String> map = loadAll();
        map.put(key, value);
        List<String> lines = new java.util.ArrayList<>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            lines.add(e.getKey() + "=" + e.getValue());
        }
        FileManager.writeLines(FILE, lines);
    }
}
