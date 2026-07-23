package hms.gui;

import hms.model.AdminStaff;
import hms.storage.UserRepository;

import javax.swing.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        seedDefaultAdminIfNeeded();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /** Ensures the system always has at least one admin account to log in with. */
    private static void seedDefaultAdminIfNeeded() {
        UserRepository repo = new UserRepository();
        if (repo.loadAdmins().isEmpty()) {
            AdminStaff admin = new AdminStaff(repo.nextAdminId(), "admin", "admin123",
                    "System Administrator", "admin@apumedical.com", "0300000000",
                    LocalDate.now().toString(), true);
            repo.save(admin);
        }
    }
}
