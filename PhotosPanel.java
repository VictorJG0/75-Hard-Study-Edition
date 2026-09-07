import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PhotosPanel extends JPanel {
    private static final String SETTINGS_FILE = "settings.properties";

    private JPanel photoPanel;
    private JLabel dayLabel;
    private int currentDay;
    private JButton previousButton;
    private JButton nextButton;

    public static boolean isPhotoSettingEnabled() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        String value = props.getProperty("photos_panel", "on");
        return value.equalsIgnoreCase("on");
    }

    public static int getDay() {
        int day = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader("Streak.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("streak=")) {
                    day = Integer.parseInt(line.substring("streak=".length()));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return day;
    }

    private void uploadPhoto() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            File photosDir = new File("Photos");
            if (!photosDir.exists()) photosDir.mkdir();

            File destFile = new File(photosDir, "photo" + currentDay + ".jpg");
            try {
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showPhotoForDay(currentDay);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showPhotoForDay(int day) {
        currentDay = day;
        dayLabel.setText("Day " + currentDay);

        Path photoPath = Paths.get("Photos", "photo" + currentDay + ".jpg");
        photoPanel.removeAll();

        if (Files.exists(photoPath)) {
            ImageIcon icon = new ImageIcon(photoPath.toString());
            Image scaled = icon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
            JLabel picLabel = new JLabel(new ImageIcon(scaled));
            picLabel.setHorizontalAlignment(JLabel.CENTER);
            picLabel.setVerticalAlignment(JLabel.CENTER);
            photoPanel.add(picLabel);
        } else {
            JLabel noPhoto = new JLabel("No photo for this day", SwingConstants.CENTER);
            noPhoto.setFont(new Font("Arial", Font.PLAIN, 16));
            noPhoto.setForeground(Theme.getNavigationBarBackgroundColor());
            photoPanel.add(noPhoto);
        }

        photoPanel.revalidate();
        photoPanel.repaint();

        int today = getDay();

        previousButton.setEnabled(currentDay > 1);

        nextButton.setEnabled(currentDay < today);
    }


    public PhotosPanel() {
        setLayout(new BorderLayout());

        if (!isPhotoSettingEnabled()) {
            JPanel centerPanel = new JPanel();
            centerPanel.setBackground(Theme.getNavigationBarForegroundColor());
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            centerPanel.add(Box.createVerticalGlue());

            JLabel title = new JLabel("Welcome to the photos page!", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.PLAIN, 32));
            title.setAlignmentX(CENTER_ALIGNMENT);

            JLabel titleDescription = new JLabel("(You can enable this feature in settings.)", SwingConstants.CENTER);
            titleDescription.setFont(new Font("Arial", Font.PLAIN, 16));
            titleDescription.setAlignmentX(CENTER_ALIGNMENT);

            centerPanel.add(title);
            centerPanel.add(Box.createVerticalStrut(10));
            centerPanel.add(titleDescription);
            centerPanel.add(Box.createVerticalGlue());

            add(centerPanel, BorderLayout.CENTER);
            return;
        }

        // Title
        JLabel title = new JLabel("Photos", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Theme.getNavigationBarForegroundColor());
        title.setOpaque(true);
        title.setBackground(Theme.getNavigationBarBackgroundColor());
        title.setPreferredSize(new Dimension(0, 45));
        add(title, BorderLayout.NORTH);

        JPanel wrapper = new JPanel();
        wrapper.setBackground(Theme.getPanelColor());
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(Box.createVerticalStrut(20));

        // Day label
        dayLabel = new JLabel("Day " + getDay(), SwingConstants.CENTER);
        dayLabel.setFont(new Font("Arial", Font.BOLD, 28));
        dayLabel.setForeground(Theme.getNavigationBarForegroundColor());
        dayLabel.setAlignmentX(CENTER_ALIGNMENT);
        wrapper.add(dayLabel);
        wrapper.add(Box.createVerticalStrut(10));

        // Photo panel
        photoPanel = new JPanel();
        photoPanel.setBackground(Theme.getNavigationBarForegroundColor());
        photoPanel.setPreferredSize(new Dimension(400, 400));
        photoPanel.setMaximumSize(new Dimension(400, 400));
        photoPanel.setAlignmentX(CENTER_ALIGNMENT);
        wrapper.add(photoPanel);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
        buttonPanel.setOpaque(false);

        previousButton = new JButton("Previous");
        previousButton.setBackground(Theme.getNavigationBarForegroundColor());
        previousButton.setForeground(Theme.getNavigationBarBackgroundColor());
        previousButton.setPreferredSize(new Dimension(100, 50));
        previousButton.addActionListener(e -> showPhotoForDay(currentDay - 1));

        JButton uploadButton = new JButton("Upload Photo");
        uploadButton.setBackground(Theme.getNavigationBarForegroundColor());
        uploadButton.setForeground(Theme.getNavigationBarBackgroundColor());
        uploadButton.setPreferredSize(new Dimension(120, 50));
        uploadButton.addActionListener(e -> uploadPhoto());

        nextButton = new JButton("Next");
        nextButton.setBackground(Theme.getNavigationBarForegroundColor());
        nextButton.setForeground(Theme.getNavigationBarBackgroundColor());
        nextButton.setPreferredSize(new Dimension(100, 50));
        nextButton.addActionListener(e -> showPhotoForDay(currentDay + 1));

        buttonPanel.add(previousButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(nextButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        wrapper.add(Box.createVerticalGlue());
        wrapper.add(buttonPanel);

        add(wrapper, BorderLayout.CENTER);

        currentDay = getDay();
        showPhotoForDay(currentDay);
    }
}