package hms.storage;

import hms.model.Billing;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class BillingRepository {
    private static final String FILE = "billing.txt";

    public List<Billing> loadAll() {
        List<Billing> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Billing.fromFileLine(line));
        return list;
    }

    public List<Billing> findByPatient(String patientId) {
        List<Billing> list = new ArrayList<>();
        for (Billing b : loadAll()) if (b.getPatientId().equals(patientId)) list.add(b);
        return list;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "BIL", 5); }

    public void save(Billing b) { FileManager.appendLine(FILE, b.toFileLine()); }

    public void update(Billing updated) {
        List<String> lines = new ArrayList<>();
        for (Billing b : loadAll())
            lines.add(b.getBillId().equals(updated.getBillId()) ? updated.toFileLine() : b.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
