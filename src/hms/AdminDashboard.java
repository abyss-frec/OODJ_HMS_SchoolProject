package hms.gui;

import hms.model.*;
import hms.service.*;
import hms.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class AdminDashboard extends JFrame {

    private final AdminStaff admin;
    private final UserService userService = new UserService();
    private final WardService wardService = new WardService();
    private final DepartmentService departmentService = new DepartmentService();
    private final InsuranceService insuranceService = new InsuranceService();
    private final SystemConfigService configService = new SystemConfigService();
    private final LabRequestService labRequestService = new LabRequestService();

    public AdminDashboard(AdminStaff admin) {
        this.admin = admin;
        setTitle("Admin Dashboard - " + admin.getFullName());
        setSize(950, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manage Users", buildUsersTab());
        tabs.addTab("Assign Doctors to Managers", buildAssignTab());
        tabs.addTab("Wards / Clinics / Rooms", buildWardsTab());
        tabs.addTab("Insurance Networks", buildInsuranceTab());
        tabs.addTab("Lab / Imaging Scheduling", buildLabRequestsTab());
        tabs.addTab("System Settings", buildSettingsTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Logged in as: " + admin.getFullName() + " (Admin Staff)");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 13));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        bar.add(welcome, BorderLayout.WEST);
        bar.add(logout, BorderLayout.EAST);
        return bar;
    }

    // ================= USERS TAB =================
    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JTabbedPane roleTabs = new JTabbedPane();
        roleTabs.addTab("Admin Staff", buildAdminUsersPanel());
        roleTabs.addTab("Medical Managers", buildManagerUsersPanel());
        roleTabs.addTab("Doctors", buildDoctorUsersPanel());
        roleTabs.addTab("Patients", buildPatientUsersPanel());
        panel.add(roleTabs, BorderLayout.CENTER);
        return panel;
    }

    // -- Admin Staff CRUD --
    private JPanel buildAdminUsersPanel() {
        String[] cols = {"ID", "Username", "Full Name", "Email", "Phone", "Active"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (AdminStaff a : userService.getAllAdmins()) {
                model.addRow(new Object[]{a.getUserId(), a.getUsername(), a.getFullName(), a.getEmail(),
                        a.getPhone(), a.isActive()});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Admin");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            Map<String, String> v = DialogUtils.showForm(this, "Add Admin Staff",
                    new String[]{"Username", "Password", "Full Name", "Email", "Phone"}, null);
            if (v == null) return;
            if (!validCommon(v)) return;
            if (userService.usernameTaken(v.get("Username"))) { DialogUtils.error(this, "Username already exists."); return; }
            userService.createAdmin(v.get("Username"), v.get("Password"), v.get("Full Name"), v.get("Email"), v.get("Phone"));
            refresh.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an admin to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            AdminStaff a = userService.getAllAdmins().stream().filter(x -> x.getUserId().equals(id)).findFirst().orElse(null);
            if (a == null) return;
            Map<String, String> v = DialogUtils.showForm(this, "Edit Admin Staff",
                    new String[]{"Full Name", "Email", "Phone"},
                    new String[]{a.getFullName(), a.getEmail(), a.getPhone()});
            if (v == null) return;
            if (!Validator.isValidEmail(v.get("Email")) || !Validator.isValidPhone(v.get("Phone"))
                    || !Validator.isNotEmpty(v.get("Full Name"))) {
                DialogUtils.error(this, "Please provide a valid name, email and phone.");
                return;
            }
            a.setFullName(v.get("Full Name"));
            a.setEmail(v.get("Email"));
            a.setPhone(v.get("Phone"));
            userService.updateAdmin(a);
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an admin to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (id.equals(admin.getUserId())) { DialogUtils.error(this, "You cannot delete your own account."); return; }
            if (DialogUtils.confirm(this, "Delete this admin account?")) {
                userService.deleteUser(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, addBtn, editBtn, deleteBtn, refreshBtn);
    }

    // -- Medical Manager CRUD --
    private JPanel buildManagerUsersPanel() {
        String[] cols = {"ID", "Username", "Full Name", "Department", "Email", "Phone"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (MedicalManager m : userService.getAllManagers()) {
                Department d = departmentService.findById(m.getDepartmentId());
                model.addRow(new Object[]{m.getUserId(), m.getUsername(), m.getFullName(),
                        d != null ? d.getName() : "-", m.getEmail(), m.getPhone()});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Manager");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            List<Department> depts = departmentService.getAll();
            JComboBox<Department> deptBox = new JComboBox<>(depts.toArray(new Department[0]));
            deptBox.insertItemAt(null, 0);
            deptBox.setSelectedIndex(0);

            JTextField uf = new JTextField(), pf = new JTextField(), nf = new JTextField(),
                    ef = new JTextField(), phf = new JTextField();
            JPanel p = new JPanel(new GridLayout(6, 2, 6, 6));
            p.add(new JLabel("Username:")); p.add(uf);
            p.add(new JLabel("Password:")); p.add(pf);
            p.add(new JLabel("Full Name:")); p.add(nf);
            p.add(new JLabel("Email:")); p.add(ef);
            p.add(new JLabel("Phone:")); p.add(phf);
            p.add(new JLabel("Department:")); p.add(deptBox);

            int res = JOptionPane.showConfirmDialog(this, p, "Add Medical Manager", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            if (!Validator.isNotEmpty(uf.getText()) || !Validator.isNotEmpty(pf.getText())
                    || !Validator.isNotEmpty(nf.getText()) || !Validator.isValidEmail(ef.getText())
                    || !Validator.isValidPhone(phf.getText())) {
                DialogUtils.error(this, "Please fill all fields with valid values.");
                return;
            }
            if (userService.usernameTaken(uf.getText().trim())) { DialogUtils.error(this, "Username already exists."); return; }
            Department d = (Department) deptBox.getSelectedItem();
            userService.createManager(uf.getText().trim(), pf.getText().trim(), nf.getText().trim(),
                    ef.getText().trim(), phf.getText().trim(), d != null ? d.getDeptId() : "");
            refresh.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a manager to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            MedicalManager m = userService.getAllManagers().stream().filter(x -> x.getUserId().equals(id)).findFirst().orElse(null);
            if (m == null) return;
            Map<String, String> v = DialogUtils.showForm(this, "Edit Medical Manager",
                    new String[]{"Full Name", "Email", "Phone"},
                    new String[]{m.getFullName(), m.getEmail(), m.getPhone()});
            if (v == null) return;
            if (!Validator.isValidEmail(v.get("Email")) || !Validator.isValidPhone(v.get("Phone"))
                    || !Validator.isNotEmpty(v.get("Full Name"))) {
                DialogUtils.error(this, "Please provide a valid name, email and phone.");
                return;
            }
            m.setFullName(v.get("Full Name"));
            m.setEmail(v.get("Email"));
            m.setPhone(v.get("Phone"));
            userService.updateManager(m);
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a manager to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this manager account?")) {
                userService.deleteUser(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, addBtn, editBtn, deleteBtn, refreshBtn);
    }

    // -- Doctor CRUD --
    private JPanel buildDoctorUsersPanel() {
        String[] cols = {"ID", "Username", "Full Name", "Department", "Manager", "Fee (RM)", "Shift"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Doctor d : userService.getAllDoctors()) {
                Department dept = departmentService.findById(d.getDepartmentId());
                MedicalManager mgr = userService.getAllManagers().stream()
                        .filter(m -> m.getUserId().equals(d.getManagerId())).findFirst().orElse(null);
                model.addRow(new Object[]{d.getUserId(), d.getUsername(), d.getFullName(),
                        dept != null ? dept.getName() : "-", mgr != null ? mgr.getFullName() : "Unassigned",
                        d.getConsultationFee(), d.getShiftSchedule()});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Doctor");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            List<Department> depts = departmentService.getAll();
            JComboBox<Department> deptBox = new JComboBox<>(depts.toArray(new Department[0]));
            JTextField uf = new JTextField(), pf = new JTextField(), nf = new JTextField(),
                    ef = new JTextField(), phf = new JTextField(),
                    feeF = new JTextField("50.00"), shiftF = new JTextField("Mon-Fri 09:00-17:00");
            JPanel p = new JPanel(new GridLayout(8, 2, 6, 6));
            p.add(new JLabel("Username:")); p.add(uf);
            p.add(new JLabel("Password:")); p.add(pf);
            p.add(new JLabel("Full Name:")); p.add(nf);
            p.add(new JLabel("Email:")); p.add(ef);
            p.add(new JLabel("Phone:")); p.add(phf);
            p.add(new JLabel("Department:")); p.add(deptBox);
            p.add(new JLabel("Consultation Fee (RM):")); p.add(feeF);
            p.add(new JLabel("Shift Schedule:")); p.add(shiftF);

            int res = JOptionPane.showConfirmDialog(this, p, "Add Doctor", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            if (!Validator.isNotEmpty(uf.getText()) || !Validator.isNotEmpty(pf.getText())
                    || !Validator.isNotEmpty(nf.getText()) || !Validator.isValidEmail(ef.getText())
                    || !Validator.isValidPhone(phf.getText()) || !Validator.isPositiveDouble(feeF.getText())
                    || deptBox.getSelectedItem() == null) {
                DialogUtils.error(this, "Please fill all fields with valid values (including a department).");
                return;
            }
            if (userService.usernameTaken(uf.getText().trim())) { DialogUtils.error(this, "Username already exists."); return; }
            Department d = (Department) deptBox.getSelectedItem();
            userService.createDoctor(uf.getText().trim(), pf.getText().trim(), nf.getText().trim(),
                    ef.getText().trim(), phf.getText().trim(), d.getDeptId(), "",
                    Double.parseDouble(feeF.getText().trim()), shiftF.getText().trim());
            refresh.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a doctor to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            Doctor d = userService.getAllDoctors().stream().filter(x -> x.getUserId().equals(id)).findFirst().orElse(null);
            if (d == null) return;
            Map<String, String> v = DialogUtils.showForm(this, "Edit Doctor",
                    new String[]{"Full Name", "Email", "Phone", "Consultation Fee (RM)"},
                    new String[]{d.getFullName(), d.getEmail(), d.getPhone(), String.valueOf(d.getConsultationFee())});
            if (v == null) return;
            if (!Validator.isValidEmail(v.get("Email")) || !Validator.isValidPhone(v.get("Phone"))
                    || !Validator.isNotEmpty(v.get("Full Name")) || !Validator.isPositiveDouble(v.get("Consultation Fee (RM)"))) {
                DialogUtils.error(this, "Please provide valid values.");
                return;
            }
            d.setFullName(v.get("Full Name"));
            d.setEmail(v.get("Email"));
            d.setPhone(v.get("Phone"));
            d.setConsultationFee(Double.parseDouble(v.get("Consultation Fee (RM)")));
            userService.updateDoctor(d);
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a doctor to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this doctor account?")) {
                userService.deleteUser(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, addBtn, editBtn, deleteBtn, refreshBtn);
    }

    // -- Patient CRUD --
    private JPanel buildPatientUsersPanel() {
        String[] cols = {"ID", "Username", "Full Name", "Email", "Phone", "DOB", "Gender"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Patient p : userService.getAllPatients()) {
                model.addRow(new Object[]{p.getUserId(), p.getUsername(), p.getFullName(), p.getEmail(),
                        p.getPhone(), p.getDateOfBirth(), p.getGender()});
            }
        };
        refresh.run();

        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");
        JLabel note = new JLabel("  Patients self-register via the login screen.");

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a patient to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this patient account?")) {
                userService.deleteUser(id);
                refresh.run();
            }
        });
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(note, BorderLayout.WEST);
        JPanel btns = new JPanel();
        btns.add(deleteBtn);
        btns.add(refreshBtn);
        south.add(btns, BorderLayout.EAST);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private boolean validCommon(Map<String, String> v) {
        if (!Validator.isNotEmpty(v.get("Username")) || !Validator.isNotEmpty(v.get("Password"))
                || !Validator.isNotEmpty(v.get("Full Name"))) {
            DialogUtils.error(this, "Username, password and full name are required.");
            return false;
        }
        if (!Validator.isValidEmail(v.get("Email"))) {
            DialogUtils.error(this, "Please enter a valid email address.");
            return false;
        }
        if (!Validator.isValidPhone(v.get("Phone"))) {
            DialogUtils.error(this, "Please enter a valid phone number.");
            return false;
        }
        return true;
    }

    private JPanel crudPanel(JTable table, JButton... buttons) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        for (JButton b : buttons) btnPanel.add(b);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ================= ASSIGN DOCTORS TAB =================
    private JPanel buildAssignTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel info = new JLabel("Assign a Doctor to their supervising Medical Manager:");
        JComboBox<Doctor> doctorBox = new JComboBox<>(userService.getAllDoctors().toArray(new Doctor[0]));
        JComboBox<MedicalManager> managerBox = new JComboBox<>(userService.getAllManagers().toArray(new MedicalManager[0]));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 12));
        form.add(new JLabel("Doctor:")); form.add(doctorBox);
        form.add(new JLabel("Medical Manager:")); form.add(managerBox);

        JButton assignBtn = new JButton("Assign");
        JButton refreshBtn = new JButton("Refresh Lists");
        assignBtn.addActionListener(e -> {
            Doctor d = (Doctor) doctorBox.getSelectedItem();
            MedicalManager m = (MedicalManager) managerBox.getSelectedItem();
            if (d == null || m == null) { DialogUtils.error(this, "Please select both a doctor and a manager."); return; }
            userService.assignDoctorToManager(d.getUserId(), m.getUserId());
            DialogUtils.info(this, d.getFullName() + " has been assigned to " + m.getFullName() + ".");
        });
        refreshBtn.addActionListener(e -> {
            doctorBox.setModel(new DefaultComboBoxModel<>(userService.getAllDoctors().toArray(new Doctor[0])));
            managerBox.setModel(new DefaultComboBoxModel<>(userService.getAllManagers().toArray(new MedicalManager[0])));
        });

        JPanel buttons = new JPanel();
        buttons.add(assignBtn);
        buttons.add(refreshBtn);

        panel.add(info, BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    // ================= WARDS TAB =================
    private JPanel buildWardsTab() {
        String[] cols = {"ID", "Name", "Type", "Capacity", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Ward w : wardService.getAll()) {
                model.addRow(new Object[]{w.getWardId(), w.getName(), w.getType(), w.getCapacity(), w.getStatus()});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Asset");
        JButton editBtn = new JButton("Edit Status");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            JTextField nameF = new JTextField(), capF = new JTextField("1");
            JComboBox<Ward.Type> typeBox = new JComboBox<>(Ward.Type.values());
            JComboBox<Ward.Status> statusBox = new JComboBox<>(Ward.Status.values());
            JPanel p = new JPanel(new GridLayout(4, 2, 6, 6));
            p.add(new JLabel("Name:")); p.add(nameF);
            p.add(new JLabel("Type:")); p.add(typeBox);
            p.add(new JLabel("Capacity:")); p.add(capF);
            p.add(new JLabel("Status:")); p.add(statusBox);
            int res = JOptionPane.showConfirmDialog(this, p, "Add Ward / Room / Lab", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            if (!Validator.isNotEmpty(nameF.getText()) || !Validator.isInt(capF.getText())) {
                DialogUtils.error(this, "Please provide a valid name and capacity.");
                return;
            }
            wardService.create(nameF.getText().trim(), (Ward.Type) typeBox.getSelectedItem(),
                    Integer.parseInt(capF.getText().trim()), (Ward.Status) statusBox.getSelectedItem());
            refresh.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an asset to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            Ward w = wardService.findById(id);
            if (w == null) return;
            JComboBox<Ward.Status> statusBox = new JComboBox<>(Ward.Status.values());
            statusBox.setSelectedItem(w.getStatus());
            int res = JOptionPane.showConfirmDialog(this, statusBox, "Update Status of " + w.getName(),
                    JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            w.setStatus((Ward.Status) statusBox.getSelectedItem());
            wardService.update(w);
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select an asset to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this asset?")) {
                wardService.delete(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, addBtn, editBtn, deleteBtn, refreshBtn);
    }

    // ================= INSURANCE TAB =================
    private JPanel buildInsuranceTab() {
        String[] cols = {"ID", "Name", "Coverage %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (InsurancePlan p : insuranceService.getAll()) {
                model.addRow(new Object[]{p.getPlanId(), p.getName(), p.getCoveragePercentage()});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Plan");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            Map<String, String> v = DialogUtils.showForm(this, "Add Insurance Plan",
                    new String[]{"Name", "Coverage %"}, null);
            if (v == null) return;
            if (!Validator.isNotEmpty(v.get("Name")) || !Validator.isPositiveDouble(v.get("Coverage %"))) {
                DialogUtils.error(this, "Please provide a valid name and coverage percentage.");
                return;
            }
            insuranceService.create(v.get("Name"), Double.parseDouble(v.get("Coverage %")));
            refresh.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a plan to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            InsurancePlan p = insuranceService.findById(id);
            if (p == null) return;
            Map<String, String> v = DialogUtils.showForm(this, "Edit Insurance Plan",
                    new String[]{"Name", "Coverage %"}, new String[]{p.getName(), String.valueOf(p.getCoveragePercentage())});
            if (v == null) return;
            if (!Validator.isNotEmpty(v.get("Name")) || !Validator.isPositiveDouble(v.get("Coverage %"))) {
                DialogUtils.error(this, "Please provide a valid name and coverage percentage.");
                return;
            }
            p.setName(v.get("Name"));
            p.setCoveragePercentage(Double.parseDouble(v.get("Coverage %")));
            insuranceService.update(p);
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a plan to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this insurance plan?")) {
                insuranceService.delete(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, addBtn, editBtn, deleteBtn, refreshBtn);
    }

    // ================= LAB REQUESTS TAB (Admin schedules Doctor requests) =================
    private JPanel buildLabRequestsTab() {
        String[] cols = {"Request ID", "Patient", "Doctor", "Type", "Status", "Date", "Assigned Room"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (LabRequest r : labRequestService.getAll()) {
                Patient p = userService.getAllPatients().stream().filter(x -> x.getUserId().equals(r.getPatientId())).findFirst().orElse(null);
                Doctor d = userService.getAllDoctors().stream().filter(x -> x.getUserId().equals(r.getDoctorId())).findFirst().orElse(null);
                model.addRow(new Object[]{r.getRequestId(), p != null ? p.getFullName() : r.getPatientId(),
                        d != null ? d.getFullName() : r.getDoctorId(), r.getType(), r.getStatus(),
                        r.getDateRequested(), r.getWardId().isEmpty() ? "-" : r.getWardId()});
            }
        };
        refresh.run();

        JButton scheduleBtn = new JButton("Schedule to Room");
        JButton rejectBtn = new JButton("Reject");
        JButton refreshBtn = new JButton("Refresh");

        scheduleBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a request to schedule."); return; }
            String id = (String) model.getValueAt(row, 0);
            List<Ward> labs = wardService.getAll().stream()
                    .filter(w -> w.getType() == Ward.Type.LAB || w.getType() == Ward.Type.IMAGING_ROOM)
                    .toList();
            if (labs.isEmpty()) { DialogUtils.error(this, "No lab/imaging rooms are configured yet."); return; }
            JComboBox<Ward> box = new JComboBox<>(labs.toArray(new Ward[0]));
            int res = JOptionPane.showConfirmDialog(this, box, "Assign Room", JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) return;
            Ward w = (Ward) box.getSelectedItem();
            labRequestService.schedule(id, w.getWardId());
            refresh.run();
        });

        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a request to reject."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Reject this request?")) {
                labRequestService.reject(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        return crudPanel(table, scheduleBtn, rejectBtn, refreshBtn);
    }

    // ================= SETTINGS TAB =================
    private JPanel buildSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        JLabel label = new JLabel("Base Consultation Rate (RM):");
        JTextField rateField = new JTextField(String.valueOf(configService.getBaseConsultationRate()));
        JButton saveBtn = new JButton("Save");

        form.add(label);
        form.add(rateField);
        form.add(new JLabel());
        form.add(saveBtn);

        saveBtn.addActionListener(e -> {
            if (!Validator.isPositiveDouble(rateField.getText())) {
                DialogUtils.error(this, "Please enter a valid rate.");
                return;
            }
            configService.setBaseConsultationRate(Double.parseDouble(rateField.getText().trim()));
            DialogUtils.info(this, "Base consultation rate updated.");
        });

        panel.add(new JLabel("System Configuration", SwingConstants.LEFT), BorderLayout.NORTH);
        panel.add(form, BorderLayout.CENTER);
        return panel;
    }
}
