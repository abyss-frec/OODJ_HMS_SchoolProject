package hms.util;

import java.util.List;

/**
 * Generates the next ID for a given prefix based on the highest
 */
public class IdGenerator {

    private IdGenerator() { }

    public static String nextId(List<String> existingLines, String prefix, int digits) {
        int max = 0;
        for (String line : existingLines) {
            String[] f = line.split("~", -1);
            if (f.length == 0) continue;
            String id = f[0];
            if (id.startsWith(prefix)) {
                String numPart = id.substring(prefix.length());
                try {
                    int n = Integer.parseInt(numPart);
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) { }
            }
        }
        int next = max + 1;
        StringBuilder sb = new StringBuilder(String.valueOf(next));
        while (sb.length() < digits) {
            sb.insert(0, "0");
        }
        return prefix + sb;
    }
}
