package hms.model;

/** A request from a Doctor to Admin Staff for a lab test, X-ray, or imaging. */
public class LabRequest {

    public enum Type { LAB_TEST, XRAY, IMAGING }
    public enum Status { PENDING, SCHEDULED, COMPLETED, REJECTED }

    private String requestId;
    private String patientId;
    private String doctorId;
    private Type type;
    private Status status;
    private String dateRequested;
    private String wardId;      // lab/imaging room assigned by admin, may be ""
    private String resultNotes;

    public LabRequest(String requestId, String patientId, String doctorId, Type type, Status status,
                       String dateRequested, String wardId, String resultNotes) {
        this.requestId = requestId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.type = type;
        this.status = status;
        this.dateRequested = dateRequested;
        this.wardId = wardId;
        this.resultNotes = resultNotes;
    }

    public String getRequestId() { return requestId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public Type getType() { return type; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getDateRequested() { return dateRequested; }
    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getResultNotes() { return resultNotes; }
    public void setResultNotes(String resultNotes) { this.resultNotes = resultNotes; }

    public String toFileLine() {
        return String.join("~", requestId, patientId, doctorId, type.name(), status.name(),
                dateRequested, nullSafe(wardId), nullSafe(resultNotes));
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    public static LabRequest fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new LabRequest(f[0], f[1], f[2], Type.valueOf(f[3]), Status.valueOf(f[4]), f[5],
                f.length > 6 ? f[6] : "", f.length > 7 ? f[7] : "");
    }
}
