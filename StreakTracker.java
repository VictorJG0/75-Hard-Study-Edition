import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class StreakTracker {
    private TasksPanel tasksPanel; // Reference to TaskPanel to be able to use all the public variables
    private static final String STREAK_FILE = "Streak.txt";
    private LocalDate lastDayCompleted;
    private int streakCount;

    private Path returnAndClearFolders() {
        try {
            Path photosFolder = Paths.get("Photos");
            Path notesFolder = Paths.get("Notes");

            Path outputZip = Paths.get("return_package.zip");

            try (FileOutputStream fos = new FileOutputStream(outputZip.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                if (Files.exists(photosFolder)) {
                    zipFolder(photosFolder, zos, photosFolder.getFileName().toString() + "/");
                }
                if (Files.exists(notesFolder)) {
                    zipFolder(notesFolder, zos, notesFolder.getFileName().toString() + "/");
                }
            }

            clearFolder(photosFolder);
            clearFolder(notesFolder);

            System.out.println("Folders zipped and cleared.");
            return outputZip;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearFolder(Path folder) throws IOException {
        if (Files.exists(folder)) {
            Files.walk(folder)
                .sorted(Comparator.reverseOrder())
                .filter(path -> !path.equals(folder)) // keep root folder
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    private void zipFolder(Path folder, ZipOutputStream zos, String baseName) throws IOException {
        Files.walk(folder).forEach(path -> {
            try {
                if (!Files.isDirectory(path)) {
                    String entryName = baseName + folder.relativize(path).toString().replace("\\", "/");
                    zos.putNextEntry(new ZipEntry(entryName));
                    Files.copy(path, zos);
                    zos.closeEntry();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void promptDownloadDesktop(Path zipFile) {
        if (zipFile == null || !Files.exists(zipFile)) {
            JOptionPane.showMessageDialog(null, "No data to return.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save your exported data");
        fileChooser.setSelectedFile(new File("75Hard_Progress.zip"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try {
                Files.copy(zipFile, saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(null, "Your data has been saved to: " + saveFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to save file: " + e.getMessage());
            }
        }
    }

    
    public int getStreakCount() {
        return streakCount;
    }


    private void saveStreak() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(STREAK_FILE))) { // THIS WILL DELETE ALL THE DATA CURRENTLY STORED IN THE FILE
            writer.println("lastCompleted=" + lastDayCompleted);
            writer.println("streak=" + streakCount);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing Streak.txt");
        }
    }

    private void clearProgressDataFiles() {
        String[] progressFiles = {
            "screenTimeStats.txt",
            "studyHoursStats.txt",
            "pagesReadStats.txt",
            "weightStats.txt",
            "fatStats.txt",
            "muscleMassStats.txt"
        };

        for (String fileName : progressFiles) {
            try {
                Files.deleteIfExists(Paths.get(fileName));
            } catch (IOException e) {
                System.err.println("Failed to delete " + fileName + ": " + e.getMessage());
            }
        }
    }
    
    public static final String SETTINGS_FILE = "settings.properties";
    
    private boolean returnOnFailSetting() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Settings file not found, using default for calendar_numbers");
        }

        String value = props.getProperty("return_on_fail", "on");

        return value.equalsIgnoreCase("on");
    }
    
    private boolean returnOnCompletionSetting() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Settings file not found, using default for calendar_numbers");
        }

        String value = props.getProperty("return_on_completion", "on");

        return value.equalsIgnoreCase("on");
    }
    
    
    public void LoadStreak(){
        try {
            // Read file lines
            List<String> lines = Files.readAllLines(Paths.get(STREAK_FILE)); // Puts every line from Streak.txt into a List
            for (String line : lines) {
                String[] parts = line.split("=");
                if (parts.length != 2) continue; // If the lines doesn't have 2 parts seperated by the '=' then skip it. (formatted badly)

                if (parts[0].equals("lastCompleted")) {
                    lastDayCompleted = LocalDate.parse(parts[1]);

                } else if (parts[0].equals("streak")) {
                    streakCount = Integer.parseInt(parts[1]);
                }
            }

            tasksPanel.reloadFromFile(); // Always refresh task booleans before making decisions.
        
            // Compare dates
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);

            if (lastDayCompleted == null) {
                streakCount = 1;
                lastDayCompleted = today;
                tasksPanel.resetAllTasks();
                tasksPanel.updateDayLabel(streakCount);
                saveStreak();
                return;
            }
            
            if (lastDayCompleted.equals(today)) {
                tasksPanel.updateDayLabel(streakCount);
                System.out.println("Streak not changed, you either have more time or its Day 1.");
                return;
            }

            if (lastDayCompleted != null && lastDayCompleted.equals(yesterday)) {
                if (tasksPanel.completedAllTasks()) {
                    tasksPanel.resetAllTasks();
                    streakCount++;
                    tasksPanel.updateDayLabel(streakCount);
                    lastDayCompleted = today;

                    if (streakCount > 75) {
                        if (returnOnCompletionSetting()) {
                            Path zipFile = returnAndClearFolders();
                            promptDownloadDesktop(zipFile);
                        } else {
                            clearFolder(Paths.get("Photos"));
                            clearFolder(Paths.get("Notes"));
                        }

                        tasksPanel.resetAllTasks();
                        streakCount = 1;
                        lastDayCompleted = today;
                        System.out.println("You have completed the challenge, Well done for all of your hard work!");
                        tasksPanel.updateDayLabel(streakCount);
                        saveStreak();
                    } else {
                        saveStreak();
                        System.out.println("All tasks were completed yesterday, increasing the day by 1.");
                    }
                    return;
                }

                if (returnOnFailSetting()) {
                    Path zipFile = returnAndClearFolders();
                    promptDownloadDesktop(zipFile);
                } else {
                    clearFolder(Paths.get("Photos"));
                    clearFolder(Paths.get("Notes"));
                }

                clearProgressDataFiles();
                tasksPanel.resetAllTasks();
                streakCount = 1;
                tasksPanel.updateDayLabel(streakCount);
                lastDayCompleted = today;
                saveStreak();
                System.out.println("Some of yesterdays tasks weren't completed, resetting back to day 1.");
            }
            else if (!today.equals(lastDayCompleted)) { // This is also a fail
                if (returnOnFailSetting()) {
                        Path zipFile = returnAndClearFolders();
                        promptDownloadDesktop(zipFile);
                    } else {
                        clearFolder(Paths.get("Photos"));
                        clearFolder(Paths.get("Notes"));
                    }

                clearProgressDataFiles();
                tasksPanel.resetAllTasks();
                streakCount = 1;
                lastDayCompleted = today;
                System.out.println("A day was missed, resetting back to day 1.");
                tasksPanel.updateDayLabel(streakCount);
                saveStreak();
            }


            saveStreak();
        
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load streak data. Make sure streak.txt exists and is valid.");
        }
    }

    public StreakTracker(TasksPanel tasksPanel) {
        this.tasksPanel = tasksPanel; // Storing the tasks panel reference
        LoadStreak();
    }
}