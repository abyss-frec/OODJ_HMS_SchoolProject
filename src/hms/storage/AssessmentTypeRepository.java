package hms.storage;

import hms.model.AssessmentType;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class AssessmentTypeRepository {
    private static final String FILE = "assessment_types.txt";

    public List<AssessmentType> loadAll() {
        List<AssessmentType> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(AssessmentType.fromFileLine(line));
        return list;
    }

    public AssessmentType findById(String id) {
        for (AssessmentType t : loadAll()) if (t.getTypeId().equals(id)) return t;
        return null;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "AST", 3); }

    public void save(AssessmentType t) { FileManager.appendLine(FILE, t.toFileLine()); }

    public void update(AssessmentType updated) {
        List<String> lines = new ArrayList<>();
        for (AssessmentType t : loadAll())
            lines.add(t.getTypeId().equals(updated.getTypeId()) ? updated.toFileLine() : t.toFileLine());
        FileManager.writeLines(FILE, lines);
    }

    public void delete(String id) {
        List<String> lines = new ArrayList<>();
        for (AssessmentType t : loadAll()) if (!t.getTypeId().equals(id)) lines.add(t.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
