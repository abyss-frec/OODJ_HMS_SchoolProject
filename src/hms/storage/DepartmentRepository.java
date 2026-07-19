package hms.storage;

import hms.model.Department;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class DepartmentRepository {
    private static final String FILE = "departments.txt";

    public List<Department> loadAll() {
        List<Department> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Department.fromFileLine(line));
        return list;
    }

    public Department findById(String id) {
        for (Department d : loadAll()) if (d.getDeptId().equals(id)) return d;
        return null;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "DPT", 3); }

    public void save(Department d) { FileManager.appendLine(FILE, d.toFileLine()); }

    public void update(Department updated) {
        List<String> lines = new ArrayList<>();
        for (Department d : loadAll())
            lines.add(d.getDeptId().equals(updated.getDeptId()) ? updated.toFileLine() : d.toFileLine());
        FileManager.writeLines(FILE, lines);
    }

    public void delete(String id) {
        List<String> lines = new ArrayList<>();
        for (Department d : loadAll()) if (!d.getDeptId().equals(id)) lines.add(d.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
