package hms.storage;

import hms.model.Prescription;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionRepository {
    private static final String FILE = "prescriptions.txt";

    public List<Prescription> loadAll() {
        List<Prescription> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Prescription.fromFileLine(line));
        return list;
    }

    public List<Prescription> findByPatient(String patientId) {
        List<Prescription> list = new ArrayList<>();
        for (Prescription p : loadAll()) if (p.getPatientId().equals(patientId)) list.add(p);
        return list;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "PRX", 5); }

    public void save(Prescription p) { FileManager.appendLine(FILE, p.toFileLine()); }
}
