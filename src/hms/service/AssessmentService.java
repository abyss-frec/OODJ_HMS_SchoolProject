package hms.service;

import hms.model.Assessment;
import hms.model.AssessmentType;
import hms.storage.AssessmentRepository;
import hms.storage.AssessmentTypeRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles both the design of assessment/check-up TYPES (by Doctors) and the
 * keying-in of actual assessment RESULTS, applying the medical grading
 * system (NORMAL / WARNING / CRITICAL) via AssessmentType.grade().
 */
public class AssessmentService {

    private final AssessmentTypeRepository typeRepo = new AssessmentTypeRepository();
    private final AssessmentRepository assessmentRepo = new AssessmentRepository();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ---- Assessment Types ----
    public List<AssessmentType> getAllTypes() { return typeRepo.loadAll(); }

    public AssessmentType createType(String name, String unit, double normalMin, double normalMax,
                                      double criticalMin, double criticalMax, String doctorId) {
        AssessmentType t = new AssessmentType(typeRepo.nextId(), name, unit, normalMin, normalMax,
                criticalMin, criticalMax, doctorId);
        typeRepo.save(t);
        return t;
    }

    public void updateType(AssessmentType t) { typeRepo.update(t); }
    public void deleteType(String id) { typeRepo.delete(id); }
    public AssessmentType findType(String id) { return typeRepo.findById(id); }

    // ---- Assessment Results ----
    public List<Assessment> getResultsForPatient(String patientId) { return assessmentRepo.findByPatient(patientId); }

    public Assessment recordResult(String patientId, String doctorId, String typeId, double value, String notes) {
        AssessmentType type = typeRepo.findById(typeId);
        String grade = type != null ? type.grade(value) : "UNKNOWN";
        Assessment a = new Assessment(assessmentRepo.nextId(), patientId, doctorId, typeId,
                LocalDateTime.now().format(FMT), value, grade, notes);
        assessmentRepo.save(a);
        return a;
    }
}
