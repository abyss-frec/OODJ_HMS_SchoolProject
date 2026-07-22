package hms.service;

import hms.model.Appointment;
import hms.storage.AppointmentRepository;

import java.util.List;

/** Booking, rescheduling, cancelling appointments, and doctor consultation logging. */
public class AppointmentService {

    private final AppointmentRepository repo = new AppointmentRepository();

    public List<Appointment> getForPatient(String patientId) { return repo.findByPatient(patientId); }
    public List<Appointment> getForDoctor(String doctorId) { return repo.findByDoctor(doctorId); }
    public Appointment findById(String id) { return repo.findById(id); }

    /** Checks whether a doctor already has an appointment at the exact same time. */
    public boolean isSlotTaken(String doctorId, String dateTime) {
        for (Appointment a : repo.findByDoctor(doctorId)) {
            if (a.getDateTime().equals(dateTime)
                    && (a.getStatus() == Appointment.Status.BOOKED || a.getStatus() == Appointment.Status.RESCHEDULED)) {
                return true;
            }
        }
        return false;
    }

    public Appointment book(String patientId, String doctorId, String wardId, String dateTime) {
        Appointment a = new Appointment(repo.nextId(), patientId, doctorId, wardId, dateTime,
                Appointment.Status.BOOKED, "", "");
        repo.save(a);
        return a;
    }

    public void reschedule(String appointmentId, String newDateTime) {
        Appointment a = repo.findById(appointmentId);
        if (a == null) return;
        a.setDateTime(newDateTime);
        a.setStatus(Appointment.Status.RESCHEDULED);
        repo.update(a);
    }

    public void cancel(String appointmentId) {
        Appointment a = repo.findById(appointmentId);
        if (a == null) return;
        a.setStatus(Appointment.Status.CANCELLED);
        repo.update(a);
    }

    /** Doctor logs vitals + consultation notes and marks the appointment complete. */
    public void completeConsultation(String appointmentId, String vitals, String notes) {
        Appointment a = repo.findById(appointmentId);
        if (a == null) return;
        a.setVitals(vitals);
        a.setConsultationNotes(notes);
        a.setStatus(Appointment.Status.COMPLETED);
        repo.update(a);
    }
}
