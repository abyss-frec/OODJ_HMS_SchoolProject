package hms.service;

import hms.model.*;
import hms.storage.UserRepository;

import java.time.LocalDate;
import java.util.List;

/** Used by Admin Staff to create/read/update/delete users of any role. */
public class UserService {

    private final UserRepository userRepo = new UserRepository();

    public List<User> getAllUsers() { return userRepo.loadAllUsers(); }
    public List<Doctor> getAllDoctors() { return userRepo.loadDoctors(); }
    public List<MedicalManager> getAllManagers() { return userRepo.loadManagers(); }
    public List<Patient> getAllPatients() { return userRepo.loadPatients(); }
    public List<AdminStaff> getAllAdmins() { return userRepo.loadAdmins(); }

    public AdminStaff createAdmin(String username, String password, String fullName, String email, String phone) {
        AdminStaff a = new AdminStaff(userRepo.nextAdminId(), username, password, fullName, email, phone,
                LocalDate.now().toString(), true);
        userRepo.save(a);
        return a;
    }

    public MedicalManager createManager(String username, String password, String fullName, String email,
                                         String phone, String departmentId) {
        MedicalManager m = new MedicalManager(userRepo.nextManagerId(), username, password, fullName, email,
                phone, LocalDate.now().toString(), true, departmentId);
        userRepo.save(m);
        return m;
    }

    public Doctor createDoctor(String username, String password, String fullName, String email, String phone,
                                String departmentId, String managerId, double consultationFee, String shift) {
        Doctor d = new Doctor(userRepo.nextDoctorId(), username, password, fullName, email, phone,
                LocalDate.now().toString(), true, departmentId, managerId, consultationFee, shift);
        userRepo.save(d);
        return d;
    }

    public Patient createPatient(String username, String password, String fullName, String email, String phone,
                                  String dob, String gender, String insurancePlanId, String bloodType) {
        Patient p = new Patient(userRepo.nextPatientId(), username, password, fullName, email, phone,
                LocalDate.now().toString(), true, dob, gender, insurancePlanId, bloodType);
        userRepo.save(p);
        return p;
    }

    public void updateAdmin(AdminStaff a) { userRepo.update(a); }
    public void updateManager(MedicalManager m) { userRepo.update(m); }
    public void updateDoctor(Doctor d) { userRepo.update(d); }
    public void updatePatient(Patient p) { userRepo.update(p); }

    public void deleteUser(String userId) { userRepo.deleteAny(userId); }

    public boolean usernameTaken(String username) { return userRepo.usernameExists(username); }

    /** Assigns a doctor to a Medical Manager (Admin Staff function). */
    public void assignDoctorToManager(String doctorId, String managerId) {
        Doctor d = userRepo.loadDoctors().stream()
                .filter(x -> x.getUserId().equals(doctorId)).findFirst().orElse(null);
        if (d != null) {
            d.setManagerId(managerId);
            userRepo.update(d);
        }
    }
}
