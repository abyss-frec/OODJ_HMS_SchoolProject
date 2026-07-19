package hms.storage;

import hms.model.Appointment;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {
    private static final String FILE = "appointments.txt";

    public List<Appointment> loadAll() {
        List<Appointment> list = new ArrayList<>();
        for (String line : FileManager.readLines(FILE)) list.add(Appointment.fromFileLine(line));
        return list;
    }

    public Appointment findById(String id) {
        for (Appointment a : loadAll()) if (a.getAppointmentId().equals(id)) return a;
        return null;
    }

    public List<Appointment> findByPatient(String patientId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : loadAll()) if (a.getPatientId().equals(patientId)) list.add(a);
        return list;
    }

    public List<Appointment> findByDoctor(String doctorId) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : loadAll()) if (a.getDoctorId().equals(doctorId)) list.add(a);
        return list;
    }

    public String nextId() { return IdGenerator.nextId(FileManager.readLines(FILE), "APT", 5); }

    public void save(Appointment a) { FileManager.appendLine(FILE, a.toFileLine()); }

    public void update(Appointment updated) {
        List<String> lines = new ArrayList<>();
        for (Appointment a : loadAll())
            lines.add(a.getAppointmentId().equals(updated.getAppointmentId()) ? updated.toFileLine() : a.toFileLine());
        FileManager.writeLines(FILE, lines);
    }
}
