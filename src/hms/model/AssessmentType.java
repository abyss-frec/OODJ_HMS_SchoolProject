package hms.model;

/**
 * A medical assessment / check-up type designed by a Doctor 
 * classifies a result as NORMAL, WARNING or CRITICAL.
 */
public class AssessmentType {

    private String typeId;
    private String name;
    private String unit;      
    private double normalMin;
    private double normalMax;
    private double criticalMin; // below this => CRITICAL
    private double criticalMax; // above this => CRITICAL
    private String createdByDoctorId;

    public AssessmentType(String typeId, String name, String unit, double normalMin, double normalMax,
                           double criticalMin, double criticalMax, String createdByDoctorId) {
        this.typeId = typeId;
        this.name = name;
        this.unit = unit;
        this.normalMin = normalMin;
        this.normalMax = normalMax;
        this.criticalMin = criticalMin;
        this.criticalMax = criticalMax;
        this.createdByDoctorId = createdByDoctorId;
    }

    public String getTypeId() { return typeId; }
    public void setTypeId(String typeId) { this.typeId = typeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public double getNormalMin() { return normalMin; }
    public void setNormalMin(double normalMin) { this.normalMin = normalMin; }
    public double getNormalMax() { return normalMax; }
    public void setNormalMax(double normalMax) { this.normalMax = normalMax; }
    public double getCriticalMin() { return criticalMin; }
    public void setCriticalMin(double criticalMin) { this.criticalMin = criticalMin; }
    public double getCriticalMax() { return criticalMax; }
    public void setCriticalMax(double criticalMax) { this.criticalMax = criticalMax; }
    public String getCreatedByDoctorId() { return createdByDoctorId; }
    public void setCreatedByDoctorId(String createdByDoctorId) { this.createdByDoctorId = createdByDoctorId; }

    /** Grades a raw result value against this type's normal/critical ranges. */
    public String grade(double value) {
        if (value < criticalMin || value > criticalMax) return "CRITICAL";
        if (value < normalMin || value > normalMax) return "WARNING";
        return "NORMAL";
    }

    public String toFileLine() {
        return String.join("~", typeId, name, unit,
                String.valueOf(normalMin), String.valueOf(normalMax),
                String.valueOf(criticalMin), String.valueOf(criticalMax),
                nullSafe(createdByDoctorId));
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    public static AssessmentType fromFileLine(String line) {
        String[] f = line.split("~", -1);
        return new AssessmentType(f[0], f[1], f[2],
                Double.parseDouble(f[3]), Double.parseDouble(f[4]),
                Double.parseDouble(f[5]), Double.parseDouble(f[6]),
                f.length > 7 ? f[7] : "");
    }

    @Override
    public String toString() {
        return name + " (" + unit + ")";
    }
}
