package GUI;

import entity.Student;
import file.FileHandler;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import GUI.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceManager extends JFrame {
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

        addLabel("Student ID:", 30, 30); idField = addTextField(150, 30);
        addLabel("Name:", 30, 70); nameField = addTextField(150, 70);

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

        int btnY = 310;

        addButton("CREATE", 30, btnY, e -> createStudent());
        addButton("UPDATE", 140, btnY, e -> updateStudent());
        addButton("REMOVE", 250, btnY, e -> removeSelectedAttendance());

        ImageIcon logoIcon = new ImageIcon(getClass().getResource("/Images/logo.png"));
        Image scaledLogo = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
        logoLabel.setBounds(135, 370, 120, 130);
        add(logoLabel);

        searchField = new JTextField();
        searchField.setBounds(400, 30, 200, 30);
        add(searchField);

        RoundedButton searchBtn = new RoundedButton("Search");
        searchBtn.setBounds(610, 30, 80, 40);
        searchBtn.setBackground(new Color(0x03A9F4));
        searchBtn.hoverBackgroundColor = new Color(0x0288D1);
        searchBtn.pressedBackgroundColor = new Color(0x0277BD);
        add(searchBtn);

        RoundedButton saveChangesBtn = new RoundedButton("Save Changes");
        saveChangesBtn.setBounds(700, 30, 130, 40);
        saveChangesBtn.setBackground(new Color(0x009688));
        saveChangesBtn.hoverBackgroundColor = new Color(0x00796B);
        saveChangesBtn.pressedBackgroundColor = new Color(0x004D40);
        add(saveChangesBtn);

        RoundedButton exportCsvBtn = new RoundedButton("Export Attendance");
        exportCsvBtn.setBounds(120, 520, 150, 40);
        exportCsvBtn.setBackground(new Color(40, 167, 69));
        exportCsvBtn.hoverBackgroundColor = new Color(33, 136, 56);
        exportCsvBtn.pressedBackgroundColor = new Color(25, 111, 61);
        exportCsvBtn.addActionListener(e -> exportAttendanceToCSV());
        add(exportCsvBtn);

        attendanceListModel = new DefaultListModel<>();
        attendanceList = new JList<>(attendanceListModel);
        attendanceList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(attendanceList);
        scrollPane.setBounds(400, 80, 430, 450);
        add(scrollPane);

        searchBtn.addActionListener(e -> searchAttendance());
        saveChangesBtn.addActionListener(e -> saveChanges());

        loadLog();
        setVisible(true);
    }

    private void createStudent() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String dept = (String) deptDropdown.getSelectedItem();
        String sem = (String) semDropdown.getSelectedItem();
        String course = (String) courseDropdown.getSelectedItem();
        String status = (String) statusDropdown.getSelectedItem();
        String date = dateField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || dept.isEmpty() || sem.isEmpty() || date.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all student and attendance details.");
            return;
        }

        Student student = new Student(id, name, dept, sem);

        try {
            FileHandler.appendToFile("file/Student.txt", student.toString());

            String attendanceRecord = String.format("%s | %s | %s | %s | %s | %s | %s",
                    id, name, dept, sem, course, status, date);
            FileHandler.appendToFile("file/Attendance.txt", attendanceRecord);

            loadLog();
            JOptionPane.showMessageDialog(this, "Student and attendance added successfully.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateStudent() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the Student ID to update.");
            return;
        }

        try {
            List<String> studentLines = FileHandler.readFromFile("file/Student.txt");
            boolean studentFound = false;

            for (int i = 0; i < studentLines.size(); i++) {
                String[] parts = studentLines.get(i).split(",");
                if (parts.length >= 4 && parts[0].trim().equals(id)) {
                    String name = nameField.getText().trim().isEmpty() ? parts[1].trim() : nameField.getText().trim();
                    String dept = (String) deptDropdown.getSelectedItem();
                    String sem = (String) semDropdown.getSelectedItem();

                    studentLines.set(i, String.format("%s,%s,%s,%s", id, name, dept, sem));
                    studentFound = true;
                    break;
                }
            }

            if (!studentFound) {
                JOptionPane.showMessageDialog(this, "Student ID not found in Student.txt.");
                return;
            }

            FileHandler.writeToFile("file/Student.txt", studentLines);

            List<String> attendanceLines = FileHandler.readFromFile("file/Attendance.txt");
            boolean attendanceUpdated = false;

            for (int i = 0; i < attendanceLines.size(); i++) {
                String[] parts = attendanceLines.get(i).split("\\|");
                if (parts.length >= 7 && parts[0].trim().equals(id)) {
                    for (int j = 0; j < parts.length; j++) parts[j] = parts[j].trim();

                    String name = nameField.getText().trim().isEmpty() ? parts[1] : nameField.getText().trim();
                    String dept = (String) deptDropdown.getSelectedItem();
                    String sem = (String) semDropdown.getSelectedItem();
                    String course = (String) courseDropdown.getSelectedItem();
                    String status = (String) statusDropdown.getSelectedItem();
                    String date = dateField.getText().trim().isEmpty() ? parts[6] : dateField.getText().trim();

                    attendanceLines.set(i, String.format("%s | %s | %s | %s | %s | %s | %s",
                            id, name, dept, sem, course, status, date));
                    attendanceUpdated = true;
                }
            }

            if (!attendanceUpdated) {
                JOptionPane.showMessageDialog(this, "No attendance records found for this Student ID.");
                return;
            }

            FileHandler.writeToFile("file/Attendance.txt", attendanceLines);
            loadLog();
            JOptionPane.showMessageDialog(this, "Student data updated based on non-empty fields.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void removeSelectedAttendance() {
        List<String> selected = attendanceList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select attendance records to remove.");
            return;
        }
        attendanceData.removeAll(selected);
        refreshList();
    }

    private void searchAttendance() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            refreshList();
            return;
        }

        attendanceListModel.clear();
        for (String record : attendanceData) {
            if (record.toLowerCase().contains(searchTerm)) {
                attendanceListModel.addElement(record);
            }
        }

        if (attendanceListModel.isEmpty()) {
            attendanceListModel.addElement("No matching records found.");
            attendanceList.setEnabled(false);
        } else {
            attendanceList.setEnabled(true);
        }
    }

    private void saveChanges() {
        try {
            FileHandler.writeToFile("file/Attendance.txt", attendanceData);
            JOptionPane.showMessageDialog(this, "All saved.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving changes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLog() {
        try {
            attendanceData.clear();
            attendanceData.addAll(FileHandler.readFromFile("file/Attendance.txt"));
            refreshList();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading attendance data.");
            ex.printStackTrace();
        }
    }

    private void refreshList() {
        attendanceListModel.clear();
        for (String record : attendanceData) {
            attendanceListModel.addElement(record);
        }
        attendanceList.setEnabled(true);
    }

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

        if (label.equalsIgnoreCase("CREATE")) {
            btn.setBackground(new Color(46, 204, 113));
            btn.hoverBackgroundColor = new Color(39, 174, 96);
            btn.pressedBackgroundColor = new Color(33, 150, 83);
        } else if (label.equalsIgnoreCase("UPDATE")) {
            btn.setBackground(new Color(241, 196, 15));
            btn.hoverBackgroundColor = new Color(243, 156, 18);
            btn.pressedBackgroundColor = new Color(230, 126, 34);
        } else if (label.equalsIgnoreCase("REMOVE")) {
            btn.setBackground(new Color(231, 76, 60));
            btn.hoverBackgroundColor = new Color(192, 57, 43);
            btn.pressedBackgroundColor = new Color(169, 50, 38);
        }

        add(btn);
        btn.addActionListener(action);
    }

    public static void main(String[] args) {
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        SwingUtilities.invokeLater(StudentAttendanceManager::new);
    }

    private void exportAttendanceToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Attendance Log");
        fileChooser.setSelectedFile(new java.io.File("AttendanceLog.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();

            try (FileWriter writer = new FileWriter(fileToSave)) {
                List<String> lines = FileHandler.readFromFile("file/Attendance.txt");
                writer.write("ID,Name,Department,Semester,Course,Status,Date\n");

                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    for (int i = 0; i < parts.length; i++) {
                        writer.write(parts[i].trim());
                        if (i < parts.length - 1) writer.write(",");
                    }
                    writer.write("\n");
                }

                JOptionPane.showMessageDialog(this, "Attendance exported to:\n" + fileToSave.getAbsolutePath());

            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

class RoundedButton extends JButton {
    Color hoverBackgroundColor;
    Color pressedBackgroundColor;

    public RoundedButton(String text) {
        super(text);
        super.setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setBackground(new Color(70, 130, 180));
        hoverBackgroundColor = new Color(100, 149, 237);
        pressedBackgroundColor = new Color(65, 105, 225);

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackgroundColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                setBackground(new Color(70, 130, 180));
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                setBackground(pressedBackgroundColor);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                setBackground(hoverBackgroundColor);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        super.paintComponent(g);
    }
}
