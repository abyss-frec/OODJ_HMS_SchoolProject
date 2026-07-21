package hms.storage;

import hms.model.Ward;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class WardRepository {
    private static final String FILE = "wards.txt";

    public List<Ward> loadAll() {
        List<Ward> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Ward.fromFileLine(line));
        return list;
    }

    public Ward findById(String id) {
        for (Ward w : loadAll()) if (w.getWardId().equals(id)) return w;
        return null;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "WRD", 4); }

    public void save(Ward w) { FileManager.appendLine(FILE, w.toFileLine()); }

    public void update(Ward updated) {
        List<String> lines = new ArrayList<>();
        for (Ward w : loadAll()) lines.add(w.getWardId().equals(updated.getWardId()) ? updated.toFileLine() : w.toFileLine());
        FileManager.writeLines(FILE, lines);
    }

    public void delete(String id) {
        List<String> lines = new ArrayList<>();
        for (Ward w : loadAll()) if (!w.getWardId().equals(id)) lines.add(w.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
