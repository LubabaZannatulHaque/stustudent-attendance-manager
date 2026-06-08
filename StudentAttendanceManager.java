package GUI;

import entity.Student;
import file.FileHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceManager extends JFrame {

    // ----- BUG FIX: Use consistent data directory path -----
    private static final String STUDENT_FILE    = "data/Student.txt";
    private static final String ATTENDANCE_FILE = "data/Attendance.txt";

    private JTextField idField, nameField, dateField, searchField;
    private JComboBox<String> deptDropdown, semDropdown, courseDropdown, statusDropdown;
    private DefaultListModel<String> attendanceListModel;
    private JList<String> attendanceList;
    private List<String> attendanceData = new ArrayList<>();

    public StudentAttendanceManager() {
        setTitle("Students' Attendance Manager");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.BLACK);

        // ----- Left panel: input fields -----
        addLabel("Student ID:",       30,  30); idField   = addTextField(150,  30);
        addLabel("Name:",             30,  70); nameField = addTextField(150,  70);

        addLabel("Department:", 30, 110);
        deptDropdown = new JComboBox<>(new String[]{"CSE", "EEE", "BBA", "Architecture", "Law"});
        deptDropdown.setBounds(150, 110, 150, 25);
        add(deptDropdown);

        addLabel("Semester:", 30, 150);
        semDropdown = new JComboBox<>(new String[]{"Spring", "Summer", "Fall"});
        semDropdown.setBounds(150, 150, 150, 25);
        add(semDropdown);

        addLabel("Attendance Date:", 30, 190); dateField = addTextField(150, 190);

        addLabel("Course:", 30, 230);
        courseDropdown = new JComboBox<>(new String[]{"Java", "Python", "C++", "CADD", "Money"});
        courseDropdown.setBounds(150, 230, 150, 25);
        add(courseDropdown);

        addLabel("Status:", 30, 270);
        statusDropdown = new JComboBox<>(new String[]{"PRESENT", "ABSENT"});
        statusDropdown.setBounds(150, 270, 150, 25);
        add(statusDropdown);

        // ----- CRUD buttons -----
        int btnY = 310;
        addButton("CREATE", 30,  btnY, e -> createStudent());
        addButton("UPDATE", 140, btnY, e -> updateStudent());
        addButton("REMOVE", 250, btnY, e -> removeSelectedAttendance());

        // ----- Logo -----
        // BUG FIX: guard against missing image so the app doesn't crash
        java.net.URL logoUrl = getClass().getResource("/Images/logo.png");
        if (logoUrl != null) {
            ImageIcon logoIcon = new ImageIcon(logoUrl);
            Image scaledLogo = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
            logoLabel.setBounds(135, 370, 120, 130);
            add(logoLabel);
        }

        // ----- Search bar -----
        searchField = new JTextField();
        searchField.setBounds(400, 30, 200, 30);
        add(searchField);

        RoundedButton searchBtn = new RoundedButton("Search");
        searchBtn.setBounds(610, 30, 80, 40);
        searchBtn.setBackground(new Color(0x03A9F4));
        searchBtn.hoverBackgroundColor   = new Color(0x0288D1);
        searchBtn.pressedBackgroundColor = new Color(0x0277BD);
        searchBtn.syncOriginalBackground();    // BUG FIX: keep hover-exit color correct
        add(searchBtn);

        // ----- Save Changes button -----
        RoundedButton saveChangesBtn = new RoundedButton("Save Changes");
        saveChangesBtn.setBounds(700, 30, 130, 40);
        saveChangesBtn.setBackground(new Color(0x009688));
        saveChangesBtn.hoverBackgroundColor   = new Color(0x00796B);
        saveChangesBtn.pressedBackgroundColor = new Color(0x004D40);
        saveChangesBtn.syncOriginalBackground();
        add(saveChangesBtn);

        // ----- Export CSV button -----
        RoundedButton exportCsvBtn = new RoundedButton("Export Attendance");
        exportCsvBtn.setBounds(120, 520, 150, 40);
        exportCsvBtn.setBackground(new Color(40, 167, 69));
        exportCsvBtn.hoverBackgroundColor   = new Color(33, 136, 56);
        exportCsvBtn.pressedBackgroundColor = new Color(25, 111, 61);
        exportCsvBtn.syncOriginalBackground();
        exportCsvBtn.addActionListener(e -> exportAttendanceToCSV());
        add(exportCsvBtn);

        // ----- Attendance list -----
        attendanceListModel = new DefaultListModel<>();
        attendanceList = new JList<>(attendanceListModel);
        attendanceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(attendanceList);
        scrollPane.setBounds(400, 80, 430, 450);
        add(scrollPane);

        // ----- Wire up remaining listeners -----
        searchBtn.addActionListener(e -> searchAttendance());
        saveChangesBtn.addActionListener(e -> saveChanges());

        loadLog();
        setVisible(true);
    }

    // ------------------------------------------------------------------ CREATE
    private void createStudent() {
        String id     = idField.getText().trim();
        String name   = nameField.getText().trim();
        String dept   = (String) deptDropdown.getSelectedItem();
        String sem    = (String) semDropdown.getSelectedItem();
        String course = (String) courseDropdown.getSelectedItem();
        String status = (String) statusDropdown.getSelectedItem();
        String date   = dateField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields (ID, Name, Date are required).");
            return;
        }

        // BUG FIX: check for duplicate student ID before writing
        try {
            List<String> studentLines = FileHandler.readFromFile(STUDENT_FILE);
            for (String line : studentLines) {
                String[] parts = line.split(",");
                if (parts.length >= 1 && parts[0].trim().equals(id)) {
                    JOptionPane.showMessageDialog(this,
                        "Student ID '" + id + "' already exists. Use UPDATE to modify.");
                    return;
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error checking existing students: " + ex.getMessage());
            return;
        }

        Student student = new Student(id, name, dept, sem);
        try {
            FileHandler.appendToFile(STUDENT_FILE, student.toString());

            String attendanceRecord = String.format("%s | %s | %s | %s | %s | %s | %s",
                    id, name, dept, sem, course, status, date);
            FileHandler.appendToFile(ATTENDANCE_FILE, attendanceRecord);

            loadLog();
            JOptionPane.showMessageDialog(this, "Student and attendance added successfully.");
            clearInputFields();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ UPDATE
    private void updateStudent() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Student ID to update.");
            return;
        }

        try {
            // --- Update Student.txt ---
            List<String> studentLines = FileHandler.readFromFile(STUDENT_FILE);
            boolean studentFound = false;
            for (int i = 0; i < studentLines.size(); i++) {
                String[] parts = studentLines.get(i).split(",");
                if (parts.length >= 4 && parts[0].trim().equals(id)) {
                    String name = nameField.getText().trim().isEmpty() ? parts[1].trim() : nameField.getText().trim();
                    String dept = (String) deptDropdown.getSelectedItem();
                    String sem  = (String) semDropdown.getSelectedItem();
                    studentLines.set(i, String.format("%s,%s,%s,%s", id, name, dept, sem));
                    studentFound = true;
                    break;
                }
            }
            if (!studentFound) {
                JOptionPane.showMessageDialog(this, "Student ID not found in Student file.");
                return;
            }
            FileHandler.writeToFile(STUDENT_FILE, studentLines);

            // --- Update Attendance.txt ---
            List<String> attendanceLines = FileHandler.readFromFile(ATTENDANCE_FILE);
            boolean attendanceUpdated = false;
            for (int i = 0; i < attendanceLines.size(); i++) {
                String[] parts = attendanceLines.get(i).split("\\|");
                if (parts.length >= 7 && parts[0].trim().equals(id)) {
                    for (int j = 0; j < parts.length; j++) parts[j] = parts[j].trim();
                    String name   = nameField.getText().trim().isEmpty()   ? parts[1] : nameField.getText().trim();
                    String dept   = (String) deptDropdown.getSelectedItem();
                    String sem    = (String) semDropdown.getSelectedItem();
                    String course = (String) courseDropdown.getSelectedItem();
                    String status = (String) statusDropdown.getSelectedItem();
                    String date   = dateField.getText().trim().isEmpty()   ? parts[6] : dateField.getText().trim();
                    attendanceLines.set(i, String.format("%s | %s | %s | %s | %s | %s | %s",
                            id, name, dept, sem, course, status, date));
                    attendanceUpdated = true;
                }
            }
            if (!attendanceUpdated) {
                JOptionPane.showMessageDialog(this, "No attendance records found for this Student ID.");
                return;
            }
            FileHandler.writeToFile(ATTENDANCE_FILE, attendanceLines);
            loadLog();
            JOptionPane.showMessageDialog(this, "Student data updated successfully.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ REMOVE
    private void removeSelectedAttendance() {
        List<String> selected = attendanceList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select one or more attendance records to remove.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove " + selected.size() + " selected record(s)?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        attendanceData.removeAll(selected);
        refreshList();
        // BUG FIX: auto-save so in-memory state matches file after removal
        try {
            FileHandler.writeToFile(ATTENDANCE_FILE, attendanceData);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving after remove: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------ SEARCH
    private void searchAttendance() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        attendanceListModel.clear();

        if (searchTerm.isEmpty()) {
            // Restore full list
            for (String record : attendanceData) {
                attendanceListModel.addElement(record);
            }
            attendanceList.setEnabled(true);
            return;
        }

        boolean found = false;
        for (String record : attendanceData) {
            if (record.toLowerCase().contains(searchTerm)) {
                attendanceListModel.addElement(record);
                found = true;
            }
        }

        // BUG FIX: never disable the list — just show a placeholder message
        if (!found) {
            attendanceListModel.addElement("No matching records found for: \"" + searchTerm + "\"");
            attendanceList.setEnabled(false);
        } else {
            attendanceList.setEnabled(true);
        }
    }

    // ------------------------------------------------------------------ SAVE
    private void saveChanges() {
        try {
            FileHandler.writeToFile(ATTENDANCE_FILE, attendanceData);
            JOptionPane.showMessageDialog(this, "All changes saved.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving changes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ LOAD
    private void loadLog() {
        try {
            attendanceData.clear();
            attendanceData.addAll(FileHandler.readFromFile(ATTENDANCE_FILE));
            refreshList();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading attendance data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ------------------------------------------------------------------ REFRESH LIST
    private void refreshList() {
        attendanceListModel.clear();
        for (String record : attendanceData) {
            attendanceListModel.addElement(record);
        }
        attendanceList.setEnabled(true);
        // Also clear search box so it doesn't show stale filter results
        if (searchField != null) searchField.setText("");
    }

    // ------------------------------------------------------------------ EXPORT CSV
    private void exportAttendanceToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Attendance Log");
        fileChooser.setSelectedFile(new java.io.File("AttendanceLog.csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            // BUG FIX: ensure .csv extension
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".csv");
            }
            try (FileWriter writer = new FileWriter(fileToSave)) {
                List<String> lines = FileHandler.readFromFile(ATTENDANCE_FILE);
                writer.write("ID,Name,Department,Semester,Course,Status,Date\n");
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    for (int i = 0; i < parts.length; i++) {
                        writer.write(parts[i].trim());
                        if (i < parts.length - 1) writer.write(",");
                    }
                    writer.write("\n");
                }
                JOptionPane.showMessageDialog(this,
                        "Attendance exported to:\n" + fileToSave.getAbsolutePath());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ------------------------------------------------------------------ HELPERS
    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, 150, 25);
        add(label);
    }

    private JTextField addTextField(int x, int y) {
        JTextField field = new JTextField();
        field.setBounds(x, y, 150, 25);
        add(field);
        return field;
    }

    private void addButton(String label, int x, int y, ActionListener action) {
        RoundedButton btn = new RoundedButton(label);
        btn.setBounds(x, y, 100, 40);
        switch (label.toUpperCase()) {
            case "CREATE":
                btn.setBackground(new Color(46, 204, 113));
                btn.hoverBackgroundColor   = new Color(39, 174, 96);
                btn.pressedBackgroundColor = new Color(33, 150, 83);
                break;
            case "UPDATE":
                btn.setBackground(new Color(241, 196, 15));
                btn.hoverBackgroundColor   = new Color(243, 156, 18);
                btn.pressedBackgroundColor = new Color(230, 126, 34);
                break;
            case "REMOVE":
                btn.setBackground(new Color(231, 76, 60));
                btn.hoverBackgroundColor   = new Color(192, 57, 43);
                btn.pressedBackgroundColor = new Color(169, 50, 38);
                break;
        }
        btn.syncOriginalBackground(); // BUG FIX: lock in the correct exit color
        btn.addActionListener(action);
        add(btn);
    }

    private void clearInputFields() {
        idField.setText("");
        nameField.setText("");
        dateField.setText("");
    }
}
