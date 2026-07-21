package hms.model;

/** A patient's booking with a doctor at a given consultation room and time. */
public class Appointment {

    public enum Status { BOOKED, COMPLETED, CANCELLED, RESCHEDULED }

    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String wardId;      // consultation room
    private String dateTime;
    private Status status;
    private String vitals;            // logged by doctor
    private String consultationNotes; // logged by doctor

    public Appointment(String appointmentId, String patientId, String doctorId, String wardId,
                        String dateTime, Status status, String vitals, String consultationNotes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.wardId = wardId;
        this.dateTime = dateTime;
        this.status = status;
        this.vitals = vitals;
        this.consultationNotes = consultationNotes;
    }

    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getVitals() { return vitals; }
    public void setVitals(String vitals) { this.vitals = vitals; }
    public String getConsultationNotes() { return consultationNotes; }
    public void setConsultationNotes(String consultationNotes) { this.consultationNotes = consultationNotes; }

    public String toFileLine() {
        return String.join("~", appointmentId, patientId, doctorId, wardId, dateTime, status.name(),
                nullSafe(vitals), nullSafe(consultationNotes));
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    public static Appointment fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Appointment(f[0], f[1], f[2], f[3], f[4], Status.valueOf(f[5]),
                f.length > 6 ? f[6] : "", f.length > 7 ? f[7] : "");
    }
}
