package hms.storage;

import hms.model.Feedback;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class FeedbackRepository {
    private static final String FILE = "feedback.txt";

    public List<Feedback> loadAll() {
        List<Feedback> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Feedback.fromFileLine(line));
        return list;
    }

    public List<Feedback> findByDoctor(String doctorId) {
        List<Feedback> list = new ArrayList<>();
        for (Feedback f : loadAll()) if (f.getDoctorId().equals(doctorId)) list.add(f);
        return list;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "FBK", 5); }

    public void save(Feedback f) { FileManager.appendLine(FILE, f.toFileLine()); }
}
