import GUI.StudentAttendanceManager;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Setup dark theme for dialogs
        UIManager.put("Panel.background", Color.BLACK);
        UIManager.put("OptionPane.background", Color.BLACK);
        UIManager.put("OptionPane.messageForeground", Color.WHITE);

        SwingUtilities.invokeLater(StudentAttendanceManager::new);
    }
}
