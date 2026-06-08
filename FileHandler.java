package file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    // Append a single line to a file (for adding new records)
    public static void appendToFile(String filePath, String content) throws IOException {
        ensureFileExists(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(content);
            bw.newLine();
        }
    }

    // Overwrite the file with the entire list (for update/remove operations)
    public static void writeToFile(String filePath, List<String> lines) throws IOException {
        ensureFileExists(filePath);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, false))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        }
    }

    // Read all lines from a file into a new List<String>
    public static List<String> readFromFile(String filePath) throws IOException {
        ensureFileExists(filePath);
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line.trim());
                }
            }
        }
        return lines;
    }

    // Ensure file and its parent directories exist
    private static void ensureFileExists(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            file.createNewFile();
        }
    }
}
