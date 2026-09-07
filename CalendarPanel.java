import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class CalendarPanel extends JPanel{
    
    public static final String SETTINGS_FILE = "settings.properties";
    private boolean calendarNumbersOn() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Settings file not found, using default for calendar_numbers");
        }

        String value = props.getProperty("calendar_numbers", "off");

        return value.equalsIgnoreCase("on");
    }
    
    // This adjusts the settings for each box.
    /*
    * currentDay is the day of the challenge.
    * currentBox is the box number the loop is currently on.
     */
    private JButton createProgressBox(int boxSize, int currentDay, int currentBox) {
        JButton box;
        
        if (calendarNumbersOn()) {
            box = new JButton(String.valueOf(currentBox)); // convert int to String
            box.setForeground(Color.ORANGE);
        } else {
            box = new JButton();
        }

        box.setPreferredSize(new Dimension(boxSize,boxSize));
        box.setMaximumSize(new Dimension(boxSize,boxSize));
        box.setMinimumSize(new Dimension(boxSize,boxSize));
        box.setContentAreaFilled(true);
        box.setBorder((BorderFactory.createLineBorder(Color.WHITE)));

        if (currentBox < currentDay) {
            box.setBackground(new Color (108,155,212)); // Completed
        }
        else if (currentBox > currentDay) {
            box.setBackground(new Color(18,57,105));
        }
        else {
            box.setBackground(Color.WHITE);
        }

        // Creating the notepad
        box.addActionListener(e -> { 
            JFrame noteFrame = new JFrame("Notes for Day " + currentBox);
            noteFrame.setSize(400,400);

            JTextArea textArea = new JTextArea(10,30); 
            JScrollPane scrollPane = new JScrollPane(textArea);

            // The file will save on close and load on open.
            File notesFolder = new File("Notes");
            if (!notesFolder.exists()) {
                notesFolder.mkdir();
            }
            
            File noteFile = new File(notesFolder, "note_day" + currentBox + ".txt");

            // Load text box when the window opens
            if (noteFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(noteFile))) {
                    textArea.read(reader, null);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            // Save text file when window closes
            noteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent we) {
                    try (FileWriter writer = new FileWriter(noteFile)) {
                        writer.write(textArea.getText());
                        System.out.println("Note closed and saved.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                
            });

            noteFrame.add(scrollPane, BorderLayout.CENTER);
            noteFrame.setVisible(true);
        });

        return box;
    }

    
    public int getDay() {
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
    
    
    public CalendarPanel() {
        setBackground(Theme.getPanelColor());
        setLayout(new BorderLayout());

        // Title
        JLabel title = new JLabel("Calendar", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Theme.getForegroundColor());
        title.setOpaque(true);
        title.setBackground(Theme.getNavigationBarBackgroundColor());

        add(title, BorderLayout.NORTH);

        // Progress boxes with notes
        JPanel boxBackgroundPanel = new JPanel();
        boxBackgroundPanel.setBackground(Theme.getPanelColor());
        boxBackgroundPanel.setLayout(new BoxLayout(boxBackgroundPanel, BoxLayout.Y_AXIS));
        add(boxBackgroundPanel, BorderLayout.CENTER);

        JPanel calendarGrid = new JPanel(new GridBagLayout());
        calendarGrid.setBackground(Theme.getPanelColor());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 0, 3, 0); 
        gbc.fill = GridBagConstraints.NONE;  

        int boxSize = 50;
        int currentDay = getDay();

        // First 70 boxes
        for (int i = 0; i < 70; i++) {
            gbc.gridx = i % 7;
            gbc.gridy = i / 7;
            JButton box = createProgressBox(boxSize, currentDay, i + 1);
            calendarGrid.add(box, gbc);
        }

        // Last 5 boxes
        for (int i = 0; i < 5; i++) {
            gbc.gridx = i;
            gbc.gridy = 10; // 11th row
            JButton box = createProgressBox(boxSize, currentDay, 71 + i);
            calendarGrid.add(box, gbc);
        }

        // Add 2 blanks
        for (int i = 5; i < 7; i++) {
            gbc.gridx = i;
            gbc.gridy = 10;
            JPanel empty = new JPanel();
            empty.setPreferredSize(new Dimension(boxSize, boxSize));
            empty.setBackground(Theme.getPanelColor());
            calendarGrid.add(empty, gbc);
        }

        boxBackgroundPanel.add(calendarGrid);

    }
}