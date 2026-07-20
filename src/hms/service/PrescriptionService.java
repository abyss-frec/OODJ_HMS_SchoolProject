package hms.service;

import hms.model.Prescription;
import hms.storage.PrescriptionRepository;

import java.time.LocalDate;
import java.util.List;

public class PrescriptionService {
    private final PrescriptionRepository repo = new PrescriptionRepository();

    public List<Prescription> getForPatient(String patientId) { return repo.findByPatient(patientId); }

    public Prescription issue(String appointmentId, String patientId, String doctorId,
                               String medications, String instructions) {
        Prescription p = new Prescription(repo.nextId(), appointmentId, patientId, doctorId,
                medications, instructions, LocalDate.now().toString());
        repo.save(p);
        return p;
    }
}
