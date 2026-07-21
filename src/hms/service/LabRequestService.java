package hms.service;

import hms.model.LabRequest;
import hms.storage.LabRequestRepository;

import java.time.LocalDate;
import java.util.List;

public class LabRequestService {
    private final LabRequestRepository repo = new LabRequestRepository();

    public List<LabRequest> getAll() { return repo.loadAll(); }
    public List<LabRequest> getForDoctor(String doctorId) { return repo.findByDoctor(doctorId); }
    public List<LabRequest> getForPatient(String patientId) { return repo.findByPatient(patientId); }

    public LabRequest create(String patientId, String doctorId, LabRequest.Type type) {
        LabRequest r = new LabRequest(repo.nextId(), patientId, doctorId, type, LabRequest.Status.PENDING,
                LocalDate.now().toString(), "", "");
        repo.save(r);
        return r;
    }

    /** Admin Staff schedules the request to a lab/imaging room. */
    public void schedule(String requestId, String wardId) {
        LabRequest r = findById(requestId);
        if (r == null) return;
        r.setWardId(wardId);
        r.setStatus(LabRequest.Status.SCHEDULED);
        repo.update(r);
    }

    public void complete(String requestId, String resultNotes) {
        LabRequest r = findById(requestId);
        if (r == null) return;
        r.setResultNotes(resultNotes);
        r.setStatus(LabRequest.Status.COMPLETED);
        repo.update(r);
    }

    public void reject(String requestId) {
        LabRequest r = findById(requestId);
        if (r == null) return;
        r.setStatus(LabRequest.Status.REJECTED);
        repo.update(r);
    }

    private LabRequest findById(String id) {
        for (LabRequest r : repo.loadAll()) if (r.getRequestId().equals(id)) return r;
        return null;
    }
}
