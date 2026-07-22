package hms.service;

import hms.model.*;
import hms.storage.*;

import java.util.*;

/** Produces the analytical hospital reports (hospital metrics & revenue summaries). */
public class ReportService {

    private final AppointmentRepository appointmentRepo = new AppointmentRepository();
    private final BillingRepository billingRepo = new BillingRepository();
    private final AssessmentRepository assessmentRepo = new AssessmentRepository();
    private final UserRepository userRepo = new UserRepository();
    private final FeedbackRepository feedbackRepo = new FeedbackRepository();

    public String buildSummaryReport() {
        List<Appointment> appts = appointmentRepo.loadAll();
        List<Billing> bills = billingRepo.loadAll();
        List<Assessment> assessments = assessmentRepo.loadAll();
        List<Doctor> doctors = userRepo.loadDoctors();
        List<Patient> patients = userRepo.loadPatients();

        StringBuilder sb = new StringBuilder();
        sb.append("===== APU MEDICAL CENTRE - ANALYTICAL REPORT =====\n\n");

        sb.append("-- Hospital Metrics --\n");
        sb.append("Total Registered Patients : ").append(patients.size()).append('\n');
        sb.append("Total Doctors             : ").append(doctors.size()).append('\n');
        sb.append("Total Appointments        : ").append(appts.size()).append('\n');

        Map<Appointment.Status, Long> byStatus = new EnumMap<>(Appointment.Status.class);
        for (Appointment.Status s : Appointment.Status.values()) byStatus.put(s, 0L);
        for (Appointment a : appts) byStatus.merge(a.getStatus(), 1L, Long::sum);
        for (Map.Entry<Appointment.Status, Long> e : byStatus.entrySet()) {
            sb.append("   ").append(e.getKey()).append(" : ").append(e.getValue()).append('\n');
        }

        sb.append("\n-- Medical Grading Distribution --\n");
        Map<String, Long> byGrade = new LinkedHashMap<>();
        for (Assessment a : assessments) byGrade.merge(a.getGrade(), 1L, Long::sum);
        if (byGrade.isEmpty()) {
            sb.append("   No assessment results recorded yet.\n");
        } else {
            for (Map.Entry<String, Long> e : byGrade.entrySet()) {
                sb.append("   ").append(e.getKey()).append(" : ").append(e.getValue()).append('\n');
            }
        }

        sb.append("\n-- Revenue Summary --\n");
        double totalBilled = 0, totalPaid = 0, totalUnpaid = 0;
        for (Billing b : bills) {
            totalBilled += b.getFinalAmount();
            if (b.getStatus() == Billing.Status.PAID) totalPaid += b.getFinalAmount();
            else totalUnpaid += b.getFinalAmount();
        }
        sb.append(String.format("   Total Billed   : RM %.2f%n", totalBilled));
        sb.append(String.format("   Total Paid     : RM %.2f%n", totalPaid));
        sb.append(String.format("   Total Unpaid   : RM %.2f%n", totalUnpaid));

        sb.append("\n-- Doctor Workload & Ratings --\n");
        for (Doctor d : doctors) {
            long count = appts.stream().filter(a -> a.getDoctorId().equals(d.getUserId())).count();
            List<Feedback> fb = feedbackRepo.findByDoctor(d.getUserId());
            double avgRating = fb.isEmpty() ? 0.0 : fb.stream().mapToInt(Feedback::getRating).average().orElse(0.0);
            sb.append(String.format("   %s (%s) - Appointments: %d, Avg Rating: %.1f/5%n",
                    d.getFullName(), d.getUserId(), count, avgRating));
        }

        return sb.toString();
    }
}
