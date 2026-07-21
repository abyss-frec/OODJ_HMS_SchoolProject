package hms.storage;

import hms.model.InsurancePlan;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class InsurancePlanRepository {
    private static final String FILE = "insurance_plans.txt";

    public List<InsurancePlan> loadAll() {
        List<InsurancePlan> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(InsurancePlan.fromFileLine(line));
        return list;
    }

    public InsurancePlan findById(String id) {
        for (InsurancePlan p : loadAll()) if (p.getPlanId().equals(id)) return p;
        return null;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "INS", 3); }

    public void save(InsurancePlan p) { FileManager.appendLine(FILE, p.toFileLine()); }

    public void update(InsurancePlan updated) {
        List<String> lines = new ArrayList<>();
        for (InsurancePlan p : loadAll())
            lines.add(p.getPlanId().equals(updated.getPlanId()) ? updated.toFileLine() : p.toFileLine());
        FileManager.writeLines(FILE, lines);
    }

    public void delete(String id) {
        List<String> lines = new ArrayList<>();
        for (InsurancePlan p : loadAll()) if (!p.getPlanId().equals(id)) lines.add(p.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
