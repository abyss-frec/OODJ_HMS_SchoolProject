package hms.gui;

import hms.model.Department;
import hms.model.Doctor;
import hms.model.MedicalManager;
import hms.service.DepartmentService;
import hms.service.ReportService;
import hms.service.UserService;
import hms.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ManagerDashboard extends JFrame {

    private MedicalManager manager;
    private final UserService userService = new UserService();
    private final DepartmentService departmentService = new DepartmentService();
    private final ReportService reportService = new ReportService();

    public ManagerDashboard(MedicalManager manager) {
        this.manager = manager;
        setTitle("Medical Manager Dashboard - " + manager.getFullName());
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", buildProfileTab());
        tabs.addTab("Manage Departments", buildDepartmentsTab());
        tabs.addTab("Doctor Shift Roster", buildRosterTab());
        tabs.addTab("Hospital Reports", buildReportsTab());

        JPanel root = new JPanel(new BorderLayout());
        root.add(topBar(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel topBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel welcome = new JLabel("Logged in as: " + manager.getFullName() + " (Medical Manager)");
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

    // ================= PROFILE TAB =================
    private JPanel buildProfileTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 12));
        JTextField nameF = new JTextField(manager.getFullName());
        JTextField emailF = new JTextField(manager.getEmail());
        JTextField phoneF = new JTextField(manager.getPhone());
        JTextField passF = new JPasswordField();
        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Email:")); form.add(emailF);
        form.add(new JLabel("Phone:")); form.add(phoneF);
        form.add(new JLabel("New Password (leave blank to keep current):")); form.add(passF);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> {
            if (!Validator.isNotEmpty(nameF.getText()) || !Validator.isValidEmail(emailF.getText())
                    || !Validator.isValidPhone(phoneF.getText())) {
                DialogUtils.error(this, "Please provide a valid name, email and phone.");
                return;
            }
            manager.setFullName(nameF.getText().trim());
            manager.setEmail(emailF.getText().trim());
            manager.setPhone(phoneF.getText().trim());
            if (!passF.getText().isEmpty()) {
                manager.setPassword(passF.getText().trim());
            }
            userService.updateManager(manager);
            DialogUtils.info(this, "Profile updated.");
        });

        panel.add(form, BorderLayout.CENTER);
        panel.add(saveBtn, BorderLayout.SOUTH);
        return panel;
    }

    // ================= DEPARTMENTS TAB =================
    private JPanel buildDepartmentsTab() {
        String[] cols = {"ID", "Name", "Description", "Head Manager"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            for (Department d : departmentService.getAll()) {
                var head = userService.getAllManagers().stream()
                        .filter(m -> m.getUserId().equals(d.getHeadManagerId())).findFirst().orElse(null);
                model.addRow(new Object[]{d.getDeptId(), d.getName(), d.getDescription(),
                        head != null ? head.getFullName() : "-"});
            }
        };
        refresh.run();

        JButton addBtn = new JButton("Add Department");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> {
            Map<String, String> v = DialogUtils.showForm(this, "Create Clinical Department",
                    new String[]{"Name", "Description"}, null);
            if (v == null) return;
            if (!Validator.isNotEmpty(v.get("Name"))) {
                DialogUtils.error(this, "Department name is required.");
                return;
            }
            departmentService.create(v.get("Name"), v.get("Description"), manager.getUserId());
            refresh.run();
        });

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a department to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            Department d = departmentService.findById(id);
            if (d == null) return;
            Map<String, String> v = DialogUtils.showForm(this, "Edit Department",
                    new String[]{"Name", "Description"}, new String[]{d.getName(), d.getDescription()});
            if (v == null) return;
            if (!Validator.isNotEmpty(v.get("Name"))) {
                DialogUtils.error(this, "Department name is required.");
                return;
            }
            d.setName(v.get("Name"));
            d.setDescription(v.get("Description"));
            departmentService.update(d);
            refresh.run();
        });

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a department to delete."); return; }
            String id = (String) model.getValueAt(row, 0);
            if (DialogUtils.confirm(this, "Delete this department?")) {
                departmentService.delete(id);
                refresh.run();
            }
        });

        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel btns = new JPanel();
        btns.add(addBtn); btns.add(editBtn); btns.add(deleteBtn); btns.add(refreshBtn);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    // ================= ROSTER TAB =================
    private JPanel buildRosterTab() {
        String[] cols = {"ID", "Doctor", "Department", "Shift Schedule"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        Runnable refresh = () -> {
            model.setRowCount(0);
            List<Doctor> myDoctors = userService.getAllDoctors().stream()
                    .filter(d -> d.getManagerId() != null && d.getManagerId().equals(manager.getUserId()))
                    .toList();
            for (Doctor d : myDoctors) {
                Department dept = departmentService.findById(d.getDepartmentId());
                model.addRow(new Object[]{d.getUserId(), d.getFullName(), dept != null ? dept.getName() : "-",
                        d.getShiftSchedule()});
            }
        };
        refresh.run();

        JLabel note = new JLabel("  Showing doctors assigned to you by Admin Staff.");
        JButton editBtn = new JButton("Edit Shift Schedule");
        JButton refreshBtn = new JButton("Refresh");

        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { DialogUtils.error(this, "Select a doctor to edit."); return; }
            String id = (String) model.getValueAt(row, 0);
            Doctor d = userService.getAllDoctors().stream().filter(x -> x.getUserId().equals(id)).findFirst().orElse(null);
            if (d == null) return;
            String newShift = JOptionPane.showInputDialog(this, "New shift schedule for " + d.getFullName() + ":",
                    d.getShiftSchedule());
            if (newShift == null) return;
            if (!Validator.isNotEmpty(newShift)) { DialogUtils.error(this, "Shift schedule cannot be empty."); return; }
            d.setShiftSchedule(newShift.trim());
            userService.updateDoctor(d);
            refresh.run();
        });
        refreshBtn.addActionListener(e -> refresh.run());

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(note, BorderLayout.WEST);
        JPanel btns = new JPanel();
        btns.add(editBtn); btns.add(refreshBtn);
        south.add(btns, BorderLayout.EAST);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    // ================= REPORTS TAB =================
    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setText(reportService.buildSummaryReport());

        JButton refreshBtn = new JButton("Refresh Report");
        refreshBtn.addActionListener(e -> area.setText(reportService.buildSummaryReport()));

        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }
}
