package hms.model;

/** Physical hospital asset: consultation room, inpatient ward, lab, or imaging room. */
public class Ward {

    public enum Type { CONSULTATION_ROOM, INPATIENT_WARD, LAB, IMAGING_ROOM }
    public enum Status { AVAILABLE, OCCUPIED, MAINTENANCE }

    private String wardId;
    private String name;
    private Type type;
    private int capacity;
    private Status status;

    public Ward(String wardId, String name, Type type, int capacity, Status status) {
        this.wardId = wardId;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.status = status;
    }

    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String toFileLine() {
        return String.join("~", wardId, name, type.name(), String.valueOf(capacity), status.name());
    }

    public static Ward fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new Ward(f[0], f[1], Type.valueOf(f[2]), Integer.parseInt(f[3]), Status.valueOf(f[4]));
    }

    @Override
    public String toString() {
        return wardId + " - " + name + " (" + type + ", " + status + ")";
    }
}
