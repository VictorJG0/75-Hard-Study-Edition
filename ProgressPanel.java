import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ProgressPanel extends JPanel {
    private static final int DAYS = 75;

    // ================= DATA STORAGE ========================
    private List<Integer> screenTimes = new ArrayList<>();
    private List<Integer> screenTimeDays = new ArrayList<>();

    private List<Double> studyHours = new ArrayList<>();
    private List<Integer> studyHourDays = new ArrayList<>();

    private List<Integer> pagesRead = new ArrayList<>();
    private List<Integer> pagesReadDays = new ArrayList<>();

    // ================= SETTINGS & STREAK ===================
    public static final String SETTINGS_FILE = "settings.properties";

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
        } catch (IOException e) { e.printStackTrace(); }
        return day;
    }

    public static boolean isDailyScreenTimeUpdateOn() { return getSetting("daily_screentime_update", "on"); }
    public static boolean isDailyStudyHoursUpdateOn() { return getSetting("daily_study_hours_update", getLegacySetting("daily_fat_update", "on")); }
    public static boolean isDailyPagesReadUpdateOn() { return getSetting("daily_pages_read_update", getLegacySetting("daily_muscle_update", "on")); }

    private static boolean getSetting(String key, String defaultValue) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
        } catch (IOException e) { e.printStackTrace(); return false; }
        String value = props.getProperty(key, defaultValue);
        return value.equalsIgnoreCase("on");
    }

    private static String getLegacySetting(String legacyKey, String defaultValue) {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            props.load(in);
        } catch (IOException e) { return defaultValue; }
        return props.getProperty(legacyKey, defaultValue);
    }

    // ================= FILE LOADING / SAVING =================
    private void loadMetric(String filename, List<Double> values, List<Integer> days) {
        values.clear();
        days.clear();
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length != 2) continue;
                int day = Integer.parseInt(parts[0].trim());
                double val = Double.parseDouble(parts[1].trim());
                days.add(day);
                values.add(val);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveMetric(String filename, List<Double> values, List<Integer> days) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < values.size(); i++) {
                writer.println(days.get(i) + "=" + values.get(i));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private int parseWholeNumber(String valueText) {
        String trimmed = valueText.trim();
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException e) {
            double numericValue = Double.parseDouble(trimmed);
            return (int) Math.round(numericValue);
        }
    }

    private void loadIntegerMetric(String filename, List<Integer> values, List<Integer> days) {
        values.clear();
        days.clear();
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length != 2) continue;
                int day = Integer.parseInt(parts[0].trim());
                int val = parseWholeNumber(parts[1]);
                days.add(day);
                values.add(val);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveIntegerMetric(String filename, List<Integer> values, List<Integer> days) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < values.size(); i++) {
                writer.println(days.get(i) + "=" + values.get(i));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadAllData() {
        loadIntegerMetric("screenTimeStats.txt", screenTimes, screenTimeDays);
        loadMetric("studyHoursStats.txt", studyHours, studyHourDays);
        loadIntegerMetric("pagesReadStats.txt", pagesRead, pagesReadDays);
    }

    private void saveScreenTimes() { saveIntegerMetric("screenTimeStats.txt", screenTimes, screenTimeDays); }
    private void saveStudyHours() { saveMetric("studyHoursStats.txt", studyHours, studyHourDays); }
    private void savePagesRead() { saveIntegerMetric("pagesReadStats.txt", pagesRead, pagesReadDays); }

    // ================= INNER GRAPH PANEL ====================
    private static class GraphPanel extends JPanel {
        private List<? extends Number> values;
        private String title;
        private List<Integer> days;

        public GraphPanel(List<? extends Number> values, List<Integer> days, String title) {
            this.values = values;
            this.days = days;
            this.title = title;
            setPreferredSize(new java.awt.Dimension(400, 150));
            setBackground(Theme.getNavigationBarForegroundColor());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (values == null || values.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padding = 40;
            int graphHeight = h - 2 * padding;
            int graphWidth = w - 2 * padding;

            g2.setColor(Theme.getNavigationBarForegroundColor());
            g2.fillRect(0, 0, w, h);

            g2.setColor(Theme.getNavigationBarBackgroundColor());
            g2.drawString(title, 10, 20);

            double max = values.stream().mapToDouble(Number::doubleValue).max().orElse(1);
            double min = values.stream().mapToDouble(Number::doubleValue).min().orElse(0);
            if (max == min) max += 1;

            g2.drawLine(padding, h - padding, w - padding, h - padding); // X axis
            g2.drawLine(padding, padding, padding, h - padding);         // Y axis

            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            for (int i = 0; i <= 5; i++) {
                double val = min + i * (max - min) / 5;
                int y = h - padding - (int)((val - min) / (max - min) * graphHeight);
                g2.drawString(String.format("%.1f", val), 5, y + 5);
            }

            int n = values.size();
            for (int i = 0; i < n; i++) {
                int x = padding + (i * graphWidth) / Math.max(1, n - 1);
                g2.drawString(String.valueOf(days.get(i)), x - 5, h - padding + 15);
            }

            int prevX = -1, prevY = -1;
            g2.setColor(Color.RED);
            for (int i = 0; i < n; i++) {
                double val = values.get(i).doubleValue();
                int x = padding + (i * graphWidth) / Math.max(1, n - 1);
                int y = h - padding - (int)((val - min) / (max - min) * graphHeight);
                g2.fillOval(x - 3, y - 3, 6, 6);
                if (i > 0) g2.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }
    }

    // ========== GRAPH + INPUT PANEL WRAPPER =================
    private class GraphWithInputPanel extends JPanel {
        public GraphWithInputPanel(String metric, List<? extends Number> values, List<Integer> days, String title) {
            setLayout(new BorderLayout());
            setBackground(Theme.getPanelColor());

            GraphPanel graphPanel = new GraphPanel(values, days, title);
            add(graphPanel, BorderLayout.CENTER);

            JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            inputPanel.setBackground(Theme.getPanelColor());
            JTextField inputField = new JTextField(8);
            JButton addButton = new JButton("Add");

            JLabel label = new JLabel("Daily " + title + ":");
            label.setForeground(Theme.getForegroundColor());

            inputPanel.add(label);
            inputPanel.add(inputField);
            inputPanel.add(addButton);
            add(inputPanel, BorderLayout.SOUTH);

            addButton.addActionListener(e -> {
                try {
                    int today = getDay();

                    switch (metric) {
                        case "screen_time": {
                            int value = Integer.parseInt(inputField.getText().trim());
                            if (value < 0 || value > 1000) {
                                JOptionPane.showMessageDialog(this, "Value must be between 0 and 1000 minutes.");
                                return;
                            }
                            if (!screenTimeDays.isEmpty() && screenTimeDays.get(screenTimeDays.size()-1) == today)
                                screenTimes.set(screenTimes.size()-1, value);
                            else { screenTimes.add(value); screenTimeDays.add(today); }
                            saveScreenTimes();
                            break;
                        }
                        case "study_hours": {
                            double value = Double.parseDouble(inputField.getText().trim());
                            if (value < 0 || value > 24) {
                                JOptionPane.showMessageDialog(this, "Value must be between 0 and 24 hours.");
                                return;
                            }
                            if (!studyHourDays.isEmpty() && studyHourDays.get(studyHourDays.size()-1) == today)
                                studyHours.set(studyHours.size()-1, value);
                            else { studyHours.add(value); studyHourDays.add(today); }
                            saveStudyHours();
                            break;
                        }
                        case "pages_read": {
                            int value = Integer.parseInt(inputField.getText().trim());
                            if (value < 0 || value > 1000) {
                                JOptionPane.showMessageDialog(this, "Value must be between 0 and 1000 pages.");
                                return;
                            }
                            if (!pagesReadDays.isEmpty() && pagesReadDays.get(pagesReadDays.size()-1) == today)
                                pagesRead.set(pagesRead.size()-1, value);
                            else { pagesRead.add(value); pagesReadDays.add(today); }
                            savePagesRead();
                            break;
                        }
                        default:
                            throw new IllegalArgumentException("Unknown metric: " + metric);
                    }

                    inputField.setText("");
                    graphPanel.repaint();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid whole number for screen time and pages read.");
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            });
        }
    }

    // ================= CONSTRUCTOR ===========================
    public ProgressPanel() {
        setBackground(Theme.getPanelColor());
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Progress", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setBackground(Theme.getNavigationBarBackgroundColor());
        title.setOpaque(true);
        title.setForeground(Theme.getNavigationBarForegroundColor());
        add(title, BorderLayout.NORTH);

        JPanel backgroundPanel = new JPanel();
        backgroundPanel.setBackground(Theme.getPanelColor());
        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
        add(backgroundPanel, BorderLayout.CENTER);

        loadAllData();

        if (isDailyScreenTimeUpdateOn())
            backgroundPanel.add(new GraphWithInputPanel("screen_time", screenTimes, screenTimeDays, "Screen Time (mins)"));
        if (isDailyStudyHoursUpdateOn())
            backgroundPanel.add(new GraphWithInputPanel("study_hours", studyHours, studyHourDays, "Study Hours"));
        if (isDailyPagesReadUpdateOn())
            backgroundPanel.add(new GraphWithInputPanel("pages_read", pagesRead, pagesReadDays, "Pages Read"));
    }
}