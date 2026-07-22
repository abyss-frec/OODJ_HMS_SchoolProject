package hms.storage;

import hms.model.*;
import hms.util.FileManager;
import hms.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for all User subtypes. Each role is stored in its own
 * text file, but this class exposes a unified API so callers (services/GUI)
 * do not need to know about individual files.
 */
public class UserRepository {

    private static final String ADMIN_FILE = "admin_staff.txt";
    private static final String MANAGER_FILE = "medical_managers.txt";
    private static final String DOCTOR_FILE = "doctors.txt";
    private static final String PATIENT_FILE = "patients.txt";

    // Load Unique text files for each role
    public List<AdminStaff> loadAdmins() {
        List<AdminStaff> list = new ArrayList<>();
        for (String line : FileManager.readLines(ADMIN_FILE)) {
            list.add(AdminStaff.fromFileLine(line));
        }
        return list;
    }

    public List<MedicalManager> loadManagers() {
        List<MedicalManager> list = new ArrayList<>();
        for (String line : FileManager.readLines(MANAGER_FILE)) {
            list.add(MedicalManager.fromFileLine(line));
        }
        return list;
    }

    public List<Doctor> loadDoctors() {
        List<Doctor> list = new ArrayList<>();
        for (String line : FileManager.readLines(DOCTOR_FILE)) {
            list.add(Doctor.fromFileLine(line));
        }
        return list;
    }

    public List<Patient> loadPatients() {
        List<Patient> list = new ArrayList<>();
        for (String line : FileManager.readLines(PATIENT_FILE)) {
            list.add(Patient.fromFileLine(line));
        }
        return list;
    }

    /** Loads every user across all four roles */
    public List<User> loadAllUsers() {
        List<User> all = new ArrayList<>();
        all.addAll(loadAdmins());
        all.addAll(loadManagers());
        all.addAll(loadDoctors());
        all.addAll(loadPatients());
        return all;
    }

    // Find the specific user
    public User findByUsername(String username) {
        for (User u : loadAllUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public User findById(String userId) {
        for (User u : loadAllUsers()) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    // ID generation
    public String nextAdminId() { return IdGenerator.nextId(FileManager.readLines(ADMIN_FILE), "ADM", 4); }
    public String nextManagerId() { return IdGenerator.nextId(FileManager.readLines(MANAGER_FILE), "MGR", 4); }
    public String nextDoctorId() { return IdGenerator.nextId(FileManager.readLines(DOCTOR_FILE), "DOC", 4); }
    public String nextPatientId() { return IdGenerator.nextId(FileManager.readLines(PATIENT_FILE), "PAT", 4); }

    // Save (SQL insert)
    public void save(AdminStaff a) { FileManager.appendLine(ADMIN_FILE, a.toFileLine()); }
    public void save(MedicalManager m) { FileManager.appendLine(MANAGER_FILE, m.toFileLine()); }
    public void save(Doctor d) { FileManager.appendLine(DOCTOR_FILE, d.toFileLine()); }
    public void save(Patient p) { FileManager.appendLine(PATIENT_FILE, p.toFileLine()); }

    // Update user role information
    public void update(AdminStaff a) { replace(ADMIN_FILE, loadAdmins(), a); }
    public void update(MedicalManager m) { replaceManager(m); }
    public void update(Doctor d) { replaceDoctor(d); }
    public void update(Patient p) { replacePatient(p); }

    private void replace(String file, List<AdminStaff> list, AdminStaff updated) {
        List<String> lines = new ArrayList<>();
        for (AdminStaff a : list) {
            lines.add(a.getUserId().equals(updated.getUserId()) ? updated.toFileLine() : a.toFileLine());
        }
        FileManager.writeLines(file, lines);
    }

    private void replaceManager(MedicalManager updated) {
        List<String> lines = new ArrayList<>();
        for (MedicalManager m : loadManagers()) {
            lines.add(m.getUserId().equals(updated.getUserId()) ? updated.toFileLine() : m.toFileLine());
        }
        FileManager.writeLines(MANAGER_FILE, lines);
    }

    private void replaceDoctor(Doctor updated) {
        List<String> lines = new ArrayList<>();
        for (Doctor d : loadDoctors()) {
            lines.add(d.getUserId().equals(updated.getUserId()) ? updated.toFileLine() : d.toFileLine());
        }
        FileManager.writeLines(DOCTOR_FILE, lines);
    }

    private void replacePatient(Patient updated) {
        List<String> lines = new ArrayList<>();
        for (Patient p : loadPatients()) {
            lines.add(p.getUserId().equals(updated.getUserId()) ? updated.toFileLine() : p.toFileLine());
        }
        FileManager.writeLines(PATIENT_FILE, lines);
    }

    // Delete
    public void deleteAdmin(String userId) {
        List<String> lines = new ArrayList<>();
        for (AdminStaff a : loadAdmins()) if (!a.getUserId().equals(userId)) lines.add(a.toFileLine());
        FileManager.writeLines(ADMIN_FILE, lines);
    }

    public void deleteManager(String userId) {
        List<String> lines = new ArrayList<>();
        for (MedicalManager m : loadManagers()) if (!m.getUserId().equals(userId)) lines.add(m.toFileLine());
        FileManager.writeLines(MANAGER_FILE, lines);
    }

    public void deleteDoctor(String userId) {
        List<String> lines = new ArrayList<>();
        for (Doctor d : loadDoctors()) if (!d.getUserId().equals(userId)) lines.add(d.toFileLine());
        FileManager.writeLines(DOCTOR_FILE, lines);
    }

    public void deletePatient(String userId) {
        List<String> lines = new ArrayList<>();
        for (Patient p : loadPatients()) if (!p.getUserId().equals(userId)) lines.add(p.toFileLine());
        FileManager.writeLines(PATIENT_FILE, lines);
    }

    /** Deletes a user of any role by ID (used by admin only). */
    public void deleteAny(String userId) {
        User u = findById(userId);
        if (u == null) return;
        if (u instanceof AdminStaff) deleteAdmin(userId);
        else if (u instanceof MedicalManager) deleteManager(userId);
        else if (u instanceof Doctor) deleteDoctor(userId);
        else if (u instanceof Patient) deletePatient(userId);
    }
}
