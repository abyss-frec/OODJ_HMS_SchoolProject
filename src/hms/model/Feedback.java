package hms.model;

/** A patient's rating and comment for a doctor / clinic visit. */
public class Feedback {

    private String feedbackId;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private int rating; // 1-5
    private String comments;
    private String dateSubmitted;

    public Feedback(String feedbackId, String patientId, String doctorId, String appointmentId,
                     int rating, String comments, String dateSubmitted) {
        this.feedbackId = feedbackId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.rating = rating;
        this.comments = comments;
        this.dateSubmitted = dateSubmitted;
    }

    public String getFeedbackId() { return feedbackId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentId() { return appointmentId; }
    public int getRating() { return rating; }
    public String getComments() { return comments; }
    public String getDateSubmitted() { return dateSubmitted; }

    public String toFileLine() {
        return String.join("~", feedbackId, patientId, doctorId, appointmentId,
                String.valueOf(rating), comments.replace("~", ";"), dateSubmitted);
    }

    public static Feedback fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Feedback(f[0], f[1], f[2], f[3], Integer.parseInt(f[4]), f[5], f[6]);
    }
}
