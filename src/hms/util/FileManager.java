package hms.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Helper for reading/writing plain text files used by the HMS */
public class FileManager {

    public static final String DATA_DIR = "data";

    static {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Could not create data directory: " + e.getMessage());
        }
    }

    private FileManager() { }

    public static String path(String fileName) {
        return DATA_DIR + File.separator + fileName;
    }

    /** Reads all non-blank lines from a file. Creates the file if missing. */
    public static List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        File file = new File(path(fileName));
        try {
            if (!file.exists()) {
                file.createNewFile();
                return lines;
            }
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading " + fileName + ": " + e.getMessage());
        }
        return lines;
    }

    /** Overwrites the file with the given lines. */
    public static void writeLines(String fileName, List<String> lines) {
        File file = new File(path(fileName));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing " + fileName + ": " + e.getMessage());
        }
    }

    /** Appends a single line to the file. */
    public static void appendLine(String fileName, String line) {
        File file = new File(path(fileName));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error appending to " + fileName + ": " + e.getMessage());
        }
    }
}
