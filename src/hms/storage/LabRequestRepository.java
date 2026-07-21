package hms.storage;

import hms.model.LabRequest;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class LabRequestRepository {
    private static final String FILE = "lab_requests.txt";

    public List<LabRequest> loadAll() {
        List<LabRequest> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(LabRequest.fromFileLine(line));
        return list;
    }

    public List<LabRequest> findByDoctor(String doctorId) {
        List<LabRequest> list = new ArrayList<>();
        for (LabRequest r : loadAll()) if (r.getDoctorId().equals(doctorId)) list.add(r);
        return list;
    }

    public List<LabRequest> findByPatient(String patientId) {
        List<LabRequest> list = new ArrayList<>();
        for (LabRequest r : loadAll()) if (r.getPatientId().equals(patientId)) list.add(r);
        return list;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "LRQ", 4); }

    public void save(LabRequest r) { FileManager.appendLine(FILE, r.toFileLine()); }

    public void update(LabRequest updated) {
        List<String> lines = new ArrayList<>();
        for (LabRequest r : loadAll())
            lines.add(r.getRequestId().equals(updated.getRequestId()) ? updated.toFileLine() : r.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
