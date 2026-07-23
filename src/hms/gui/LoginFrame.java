package hms.gui;

import hms.model.*;
import hms.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("APU Medical Centre - Hospital Management System");
        setSize(420, 320);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("Hospital Management System", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        JLabel subtitle = new JLabel("APU Medical Centre", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        root.add(titlePanel, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        root.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 8));
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register as Patient");
        buttons.add(loginBtn);
        buttons.add(registerBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> {
            new RegisterFrame(this).setVisible(true);
            setVisible(false);
        });
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            DialogUtils.error(this, "Please enter both username and password.");
            return;
        }

        User user = authService.login(username, password);
        if (user == null) {
            DialogUtils.error(this, "Invalid username/password, or account is inactive.");
            passwordField.setText("");
            return;
        }

        JFrame dashboard = switch (user) {
            case AdminStaff a -> new AdminDashboard(a);
            case MedicalManager m -> new ManagerDashboard(m);
            case Doctor d -> new DoctorDashboard(d);
            case Patient p -> new PatientDashboard(p);
            default -> null;
        };

        if (dashboard != null) {
            dashboard.setVisible(true);
            dispose();
        }
    }
}
