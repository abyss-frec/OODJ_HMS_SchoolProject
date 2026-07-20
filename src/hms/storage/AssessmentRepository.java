package hms.storage;

import hms.model.Assessment;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class AssessmentRepository {
    private static final String FILE = "assessments.txt";

    public List<Assessment> loadAll() {
        List<Assessment> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Assessment.fromFileLine(line));
        return list;
    }

    public List<Assessment> findByPatient(String patientId) {
        List<Assessment> list = new ArrayList<>();
        for (Assessment a : loadAll()) if (a.getPatientId().equals(patientId)) list.add(a);
        return list;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "ASM", 5); }

    public void save(Assessment a) { FileManager.appendLine(FILE, a.toFileLine()); }
}
