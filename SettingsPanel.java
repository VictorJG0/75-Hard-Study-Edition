import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;


public class SettingsPanel extends JPanel{

     public Path returnAndClearFolders() {
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
        fileChooser.setSelectedFile(new File("return_package.zip"));

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

    public static final String SETTINGS_FILE = "settings.properties";
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
    
    
    
    private static class SettingsManager {
        private final Properties props = new Properties();
        private final String fileName;

        public SettingsManager(String fileName) {
            this.fileName = fileName;
            load();
        }

        private void load() {
            try (FileInputStream in = new FileInputStream(fileName)) {
                props.load(in);
            } catch (IOException ignored) { 
            }
        }

        private void save() {
            try (FileOutputStream out = new FileOutputStream(fileName)) {
                props.store(out, "User settings");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    
        public String get(String key, String def) {
            return props.getProperty(key, def);
        }

        public String getWithLegacy(String key, String legacyKey, String def) {
            return props.getProperty(key, props.getProperty(legacyKey, def));
        }

        public void set(String key, String value) {
            props.setProperty(key, value);
            save();
        }

        public void bindRadioGroup(String key, ButtonGroup group, String defaultValue) {
            String saved = get(key, defaultValue);
            Enumeration<AbstractButton> buttons = group.getElements();
            while (buttons.hasMoreElements()) {
                AbstractButton btn = buttons.nextElement();
                if (btn.getText().equals(saved)) {
                    btn.setSelected(true);
                }
                btn.addActionListener(e -> set(key, btn.getText()));
            }
        }

        public void bindRadioGroupWithLegacy(String key, String legacyKey, ButtonGroup group, String defaultValue) {
            String saved = getWithLegacy(key, legacyKey, defaultValue);
            Enumeration<AbstractButton> buttons = group.getElements();
            while (buttons.hasMoreElements()) {
                AbstractButton btn = buttons.nextElement();
                if (btn.getText().equals(saved)) {
                    btn.setSelected(true);
                }
                btn.addActionListener(e -> set(key, btn.getText()));
            }
        }
    
    }
    private final SettingsManager settings;

    public SettingsPanel() {
        settings = new SettingsManager("settings.properties");

        setBackground(Theme.getNavigationBarBackgroundColor());
        setLayout(new BorderLayout());

        JPanel mainBackground = new JPanel(new BorderLayout());
        mainBackground.setBackground(Theme.getNavigationBarBackgroundColor());

        // Title
        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Theme.getNavigationBarForegroundColor());
        mainBackground.add(title, BorderLayout.NORTH);

        // Content panel stacked vertically
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.getPanelColor());

        // Settings
        JLabel screentime = new JLabel("Log daily screen time (mins)?: ");
        screentime.setFont(new Font("Arial", Font.PLAIN, 18));
        screentime.setForeground(Theme.getForegroundColor());
        contentPanel.add(screentime);
        
        JRadioButton screentimeSetting0 = new JRadioButton("off");
        JRadioButton screentimeSetting1 = new JRadioButton("on");
        ButtonGroup screentimeGroup = new ButtonGroup();
        screentimeGroup.add(screentimeSetting0);
        screentimeGroup.add(screentimeSetting1);
        screentimeSetting0.setBackground(Theme.getPanelColor());
        screentimeSetting1.setBackground(Theme.getPanelColor());
        screentimeSetting0.setForeground(Theme.getForegroundColor());
        screentimeSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(screentimeSetting0);
        contentPanel.add(screentimeSetting1);
        settings.bindRadioGroup("daily_screentime_update", screentimeGroup, "on");

        
        JLabel studyHours = new JLabel("Log daily study hours?: ");
        studyHours.setFont(new Font("Arial", Font.PLAIN, 18));
        studyHours.setForeground((Theme.getForegroundColor()));
        contentPanel.add(studyHours);

        JRadioButton studyHoursSetting0 = new JRadioButton("off");
        JRadioButton studyHoursSetting1 = new JRadioButton("on");
        ButtonGroup studyHoursGroup = new ButtonGroup();
        studyHoursGroup.add(studyHoursSetting0);
        studyHoursGroup.add(studyHoursSetting1);
        studyHoursSetting0.setBackground(Theme.getPanelColor());
        studyHoursSetting1.setBackground(Theme.getPanelColor());
        studyHoursSetting0.setForeground(Theme.getForegroundColor());
        studyHoursSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(studyHoursSetting0);
        contentPanel.add(studyHoursSetting1);
        settings.bindRadioGroupWithLegacy("daily_study_hours_update", "daily_fat_update", studyHoursGroup, "on");


        JLabel pagesRead = new JLabel("Log daily pages read?: ");
        pagesRead.setFont(new Font("Arial", Font.PLAIN, 18));
        pagesRead.setForeground((Theme.getForegroundColor()));
        contentPanel.add(pagesRead);

        JRadioButton pagesReadSetting0 = new JRadioButton("off");
        JRadioButton pagesReadSetting1 = new JRadioButton("on");
        ButtonGroup pagesReadGroup = new ButtonGroup();
        pagesReadGroup.add(pagesReadSetting0);
        pagesReadGroup.add(pagesReadSetting1);
        pagesReadSetting0.setBackground(Theme.getPanelColor());
        pagesReadSetting1.setBackground(Theme.getPanelColor());
        pagesReadSetting0.setForeground(Theme.getForegroundColor());
        pagesReadSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(pagesReadSetting0);
        contentPanel.add(pagesReadSetting1);
        settings.bindRadioGroupWithLegacy("daily_pages_read_update", "daily_muscle_update", pagesReadGroup, "on");


        JLabel calendarNums = new JLabel("Calendar numbers: ");
        calendarNums.setFont(new Font("Arial", Font.PLAIN, 18));
        calendarNums.setForeground(Theme.getForegroundColor());
        contentPanel.add(calendarNums);

        JRadioButton numsSetting0 = new JRadioButton("off");
        JRadioButton numsSetting1 = new JRadioButton("on");
        ButtonGroup numGroup = new ButtonGroup();
        numGroup.add(numsSetting0);
        numGroup.add(numsSetting1);
        numsSetting0.setBackground(Theme.getPanelColor());
        numsSetting1.setBackground(Theme.getPanelColor());
        numsSetting0.setForeground(Theme.getForegroundColor());
        numsSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(numsSetting0);
        contentPanel.add(numsSetting1);
        settings.bindRadioGroup("calendar_numbers", numGroup, "on");


        JLabel photos = new JLabel("In app photo panel (store photos in the app?): ");
        photos.setFont(new Font("Arial", Font.PLAIN, 18));
        photos.setForeground(Theme.getForegroundColor());
        contentPanel.add(photos);

        JRadioButton photosSetting0 = new JRadioButton("off");
        JRadioButton photosSetting1 = new JRadioButton("on");
        ButtonGroup photosGroup = new ButtonGroup();
        photosGroup.add(photosSetting0);
        photosGroup.add(photosSetting1);
        photosSetting0.setBackground(Theme.getPanelColor());
        photosSetting1.setBackground(Theme.getPanelColor());
        photosSetting0.setForeground(Theme.getForegroundColor());
        photosSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(photosSetting0);
        contentPanel.add(photosSetting1);
        settings.bindRadioGroup("photos_panel", photosGroup, "on");
        

        JLabel returnOnFail = new JLabel("Return photos + notes when challenge failed: ");
        returnOnFail.setFont(new Font("Arial", Font.PLAIN, 18));
        returnOnFail.setForeground(Theme.getForegroundColor());
        contentPanel.add(returnOnFail);

        JRadioButton returnOnFailSetting0 = new JRadioButton("off");
        JRadioButton returnOnFailSetting1 = new JRadioButton("on");
        ButtonGroup returnOnFailGroup = new ButtonGroup();
        returnOnFailGroup.add(returnOnFailSetting0);
        returnOnFailGroup.add(returnOnFailSetting1);
        returnOnFailSetting0.setBackground(Theme.getPanelColor());
        returnOnFailSetting1.setBackground(Theme.getPanelColor());
        returnOnFailSetting0.setForeground(Theme.getForegroundColor());
        returnOnFailSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(returnOnFailSetting0);
        contentPanel.add(returnOnFailSetting1);
        settings.bindRadioGroup("return_on_fail", returnOnFailGroup, "on");


        JLabel returnOnCompletion = new JLabel("Return photos + notes when challenge completed: ");
        returnOnCompletion.setFont(new Font("Arial", Font.PLAIN, 18));
        returnOnCompletion.setForeground(Theme.getForegroundColor());
        contentPanel.add(returnOnCompletion);

        JRadioButton returnOnCompletionSetting0 = new JRadioButton("off");
        JRadioButton returnOnCompletionSetting1 = new JRadioButton("on");
        ButtonGroup returnOnCompletionGroup = new ButtonGroup();
        returnOnCompletionGroup.add(returnOnCompletionSetting0);
        returnOnCompletionGroup.add(returnOnCompletionSetting1);
        returnOnCompletionSetting0.setBackground(Theme.getPanelColor());
        returnOnCompletionSetting1.setBackground(Theme.getPanelColor());
        returnOnCompletionSetting0.setForeground(Theme.getForegroundColor());
        returnOnCompletionSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(returnOnCompletionSetting0);
        contentPanel.add(returnOnCompletionSetting1);
        settings.bindRadioGroup("return_on_completion", returnOnCompletionGroup, "on");


        JLabel theme = new JLabel("Light mode or dark mode : ");
        theme.setFont(new Font("Arial", Font.PLAIN, 18));
        theme.setForeground(Theme.getForegroundColor());
        contentPanel.add(theme);
        
        JRadioButton themeSetting0 = new JRadioButton("light mode");
        JRadioButton themeSetting1 = new JRadioButton("dark mode");
        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(themeSetting0);
        themeGroup.add(themeSetting1);
        themeSetting0.setBackground(Theme.getPanelColor());
        themeSetting1.setBackground(Theme.getPanelColor());
        themeSetting0.setForeground(Theme.getForegroundColor());
        themeSetting1.setForeground(Theme.getForegroundColor());
        contentPanel.add(themeSetting0);
        contentPanel.add(themeSetting1);
        settings.bindRadioGroup("theme", themeGroup, "dark mode");




        /*
         * All of theses settings need to be at the bottom of the settings panel
         * If new setting are going to be added they will most likely need to be put above this point
         * Unless its going to be a setting that will be rarely accessed.
         */
        JLabel reset = new JLabel("Reset back to day one (This counts as a fail.) ");
        JLabel resetWarning = new JLabel("Warning: Progress resets upon pressing button (there is no confirm screen!)");
        reset.setFont(new Font("Arial", Font.PLAIN, 18));
        resetWarning.setFont(new Font("Arial", Font.ITALIC, 12));
        reset.setForeground(Color.RED);
        resetWarning.setForeground(Color.RED);
        contentPanel.add(reset);
        contentPanel.add(resetWarning);
        
        JButton resetButton = new JButton("reset");
        resetButton.setBackground(Theme.getNavigationBarForegroundColor());
        resetButton.setForeground(Theme.getNavigationBarBackgroundColor());
        JPanel resetPanel = new JPanel((new FlowLayout(FlowLayout.CENTER, 0, 0)));
        resetPanel.setBackground((Theme.getPanelColor()));
        resetPanel.add(resetButton);

        resetButton.addActionListener(e -> {
        try {
            Properties props = new Properties();

            FileInputStream in = new FileInputStream("Streak.txt");
            props.load(in);
            in.close();
            props.setProperty("streak", "1");
            try (FileWriter writer = new FileWriter("Streak.txt")) {
                for (String key : props.stringPropertyNames()) {
                    writer.write(key + "=" + props.getProperty(key) + "\n");
                }
            }

            if (returnOnFailSetting()) {
                Path zipFile = returnAndClearFolders();
                promptDownloadDesktop(zipFile);
            } else {
                clearFolder(Paths.get("Photos"));
                clearFolder(Paths.get("Notes"));
            }

            clearProgressDataFiles();
            System.out.println("Streak reset to 1!");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        });   
        contentPanel.add(resetPanel, BorderLayout.SOUTH);


        JLabel donate = new JLabel("Buy me a coffee?: ");
        JLabel donateMessage = new JLabel("If this app helped you in any way at all consider donating.");
        JLabel donateMessage2 = new JLabel("All donations are appreciated, thank you :)");
        donate.setFont(new Font("Arial", Font.PLAIN, 18));
        donateMessage.setFont(new Font("Arial", Font.ITALIC, 12));
        donateMessage2.setFont(new Font("Arial", Font.ITALIC, 12));
        donate.setForeground(Theme.getBuyMeACoffeeText());
        donateMessage.setForeground(Theme.getBuyMeACoffeeText());
        donateMessage2.setForeground(Theme.getBuyMeACoffeeText());
        contentPanel.add(donate);
        contentPanel.add(donateMessage);
        contentPanel.add(donateMessage2);

        JButton donateButton = new JButton("donate");
        donateButton.setBackground(Theme.getNavigationBarForegroundColor());
        donateButton.setForeground(Theme.getNavigationBarBackgroundColor());

        donateButton.addActionListener(e -> {
        String url = "https://buymeacoffee.com/vjgutowskir";

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI(url));
            } 
            // In case the browser is not supported on each system.
            else {
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", url});
                } else if (os.contains("nix") || os.contains("nux")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", url});
                } else {
                    throw new UnsupportedOperationException("No method to open browser on this OS");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        });
        JPanel donatePanel = new JPanel((new FlowLayout(FlowLayout.CENTER, 0, 0)));
        donatePanel.setBackground((Theme.getPanelColor()));
        donatePanel.add(donateButton);
        contentPanel.add(donatePanel, BorderLayout.SOUTH);

        

        mainBackground.add(contentPanel, BorderLayout.CENTER);

        // Scroll support
        JScrollPane scrollPane = new JScrollPane(mainBackground);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.DARK_GRAY);

        add(scrollPane, BorderLayout.CENTER);
    } 
}