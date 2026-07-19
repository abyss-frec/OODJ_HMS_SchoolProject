package hms.service;

import hms.model.*;
import hms.storage.UserRepository;

import java.time.LocalDate;

/** Handles login authentication and self-service patient registration. */
public class AuthService {

    private final UserRepository userRepo = new UserRepository();

    /** @return the authenticated User, or null if credentials are invalid / account inactive. */
    public User login(String username, String password) {
        User u = userRepo.findByUsername(username);
        if (u == null) return null;
        if (!u.getPassword().equals(password)) return null;
        if (!u.isActive()) return null;
        return u;
    }

    public boolean usernameTaken(String username) {
        return userRepo.usernameExists(username);
    }

    /** Public self-registration is only offered to Patients. */
    public Patient registerPatient(String username, String password, String fullName, String email,
                                    String phone, String dob, String gender, String insurancePlanId,
                                    String bloodType) {
        String id = userRepo.nextPatientId();
        Patient p = new Patient(id, username, password, fullName, email, phone,
                LocalDate.now().toString(), true, dob, gender, insurancePlanId, bloodType);
        userRepo.save(p);
        return p;
    }
}
