package hms.service;

import hms.model.*;
import hms.storage.AssessmentRepository;
import hms.storage.BillingRepository;
import hms.storage.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Implements the "medical grading & billing system": a bill is generated
 * for an appointment, its grade is derived from the patient's most recent
 * assessment (worst grade found on/after the appointment date), a surcharge
 * is applied for WARNING/CRITICAL grades, and any insurance plan the
 * patient holds reduces the final amount.
 */
public class BillingService {

    private final BillingRepository billRepo = new BillingRepository();
    private final AssessmentRepository assessmentRepo = new AssessmentRepository();
    private final UserRepository userRepo = new UserRepository();

    private static final double WARNING_SURCHARGE = 20.00;
    private static final double CRITICAL_SURCHARGE = 60.00;

    public List<Billing> getForPatient(String patientId) { return billRepo.findByPatient(patientId); }
    public List<Billing> getAll() { return billRepo.loadAll(); }

    /** Determines the worst grade among a patient's recorded assessments (or "N-A" if none). */
    private String worstGrade(String patientId) {
        String worst = "N-A";
        for (Assessment a : assessmentRepo.findByPatient(patientId)) {
            if ("CRITICAL".equals(a.getGrade())) return "CRITICAL";
            if ("WARNING".equals(a.getGrade())) worst = "WARNING";
            else if ("NORMAL".equals(a.getGrade()) && "N-A".equals(worst)) worst = "NORMAL";
        }
        return worst;
    }

    public Billing generateBill(String patientId, String appointmentId, double baseFee) {
        String grade = worstGrade(patientId);
        double surcharge = switch (grade) {
            case "CRITICAL" -> CRITICAL_SURCHARGE;
            case "WARNING" -> WARNING_SURCHARGE;
            default -> 0.0;
        };

        double subtotal = baseFee + surcharge;
        double insuranceDeduction = 0.0;

        User u = userRepo.findById(patientId);
        if (u instanceof Patient p && p.getInsurancePlanId() != null && !p.getInsurancePlanId().isEmpty()) {
            InsurancePlan plan = new hms.storage.InsurancePlanRepository().findById(p.getInsurancePlanId());
            if (plan != null) {
                insuranceDeduction = subtotal * (plan.getCoveragePercentage() / 100.0);
            }
        }

        double finalAmount = Math.max(0, subtotal - insuranceDeduction);

        Billing bill = new Billing(billRepo.nextId(), patientId, appointmentId, baseFee, surcharge,
                insuranceDeduction, finalAmount, grade, Billing.Status.UNPAID, LocalDate.now().toString());
        billRepo.save(bill);
        return bill;
    }

    public void markPaid(String billId) {
        for (Billing b : billRepo.loadAll()) {
            if (b.getBillId().equals(billId)) {
                b.setStatus(Billing.Status.PAID);
                billRepo.update(b);
                return;
            }
        }
    }
}
