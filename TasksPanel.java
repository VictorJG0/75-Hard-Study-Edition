import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class TasksPanel extends JPanel{
    private JLabel title;
    private boolean suppressSave = false;
    
    private boolean DoomscrollTask;
    private boolean StudyTask;
    private boolean ReadingTask;
    private boolean DeadlineTask;
    private boolean NotesTask;
    private boolean NoskipTask;

    private JToggleButton dietCheckbox;
    private JToggleButton indoorCheckbox;
    private JToggleButton outdoorCheckbox;
    private JToggleButton waterCheckbox;
    private JToggleButton readingCheckbox;
    private JToggleButton pictureCheckbox;

    
    private boolean allTasks;
    public boolean completedAllTasks() {
        allTasks = DoomscrollTask && StudyTask && ReadingTask && DeadlineTask && NotesTask && NoskipTask;
        return allTasks;
    }

    public void resetAllTasks() {
        suppressSave = true;
        DoomscrollTask = StudyTask = ReadingTask = DeadlineTask = NotesTask = NoskipTask = false;

        if (dietCheckbox != null) dietCheckbox.setSelected(false);
        if (indoorCheckbox != null) indoorCheckbox.setSelected(false);
        if (outdoorCheckbox != null) outdoorCheckbox.setSelected(false);
        if (waterCheckbox != null) waterCheckbox.setSelected(false);
        if (readingCheckbox != null) readingCheckbox.setSelected(false);
        if (pictureCheckbox != null) pictureCheckbox.setSelected(false);

        suppressSave = false;
        saveTaskStatesToFIle();
    }

    public void reloadFromFile() {
        loadTaskStatesFromFile();
        suppressSave = true;
        if (dietCheckbox != null) dietCheckbox.setSelected(DoomscrollTask);
        if (indoorCheckbox != null) indoorCheckbox.setSelected(StudyTask);
        if (outdoorCheckbox != null) outdoorCheckbox.setSelected(ReadingTask);
        if (waterCheckbox != null) waterCheckbox.setSelected(DeadlineTask);
        if (readingCheckbox != null) readingCheckbox.setSelected(NotesTask);
        if (pictureCheckbox != null) pictureCheckbox.setSelected(NoskipTask);
        suppressSave = false;
    }


    // Setter methods. (We only need the picture task because its the only one that can be toggled with something other than a checkbox.)
    public boolean setNoskipTask(boolean pictureCheckbox) {
        return pictureCheckbox; // This doesnt work yet!!!!!!
    }

    // Getter methods
    public boolean getDoomscrollTask() {return DoomscrollTask;}
    public boolean getStudyTask() {return StudyTask;}
    public boolean getReadingTask() {return ReadingTask;}
    public boolean getDeadlineTask() {return DeadlineTask;}
    public boolean getNotesTask() {return NotesTask;}
    public boolean getNoskipTask() {return NoskipTask;}
    public boolean getAllTasks() {return allTasks;}
    

    // This is used as the 'default setting' for eachcheckbox; aswell as creating it.
    private JToggleButton createStyledCheckbox(int checkboxSize){
        JToggleButton checkbox = new JToggleButton();
        checkbox.setSelected(false); // Starts off not selected
        checkbox.setPreferredSize(new Dimension(checkboxSize, checkboxSize)); // Making sure the checkbox is a square
        checkbox.setMaximumSize(new Dimension(checkboxSize,checkboxSize));    //
        checkbox.setMinimumSize(new Dimension(checkboxSize,checkboxSize));    //
        checkbox.setContentAreaFilled(false);
        checkbox.setOpaque(true);
        checkbox.setBackground(Theme.getCheckBoxColor());
        checkbox.setBorder(BorderFactory.createLineBorder(Theme.getCheckBoxOutlineColor()));

        checkbox.addChangeListener(e -> {
        if (checkbox.isSelected()) {
            checkbox.setBackground(new Color(78,110,84));
        } else {
            checkbox.setBackground(Theme.getCheckBoxColor());
            }
        });
        
        return checkbox;
    }

    public TasksPanel() {
        // Loads saved tasks first
        loadTaskStatesFromFile();
        
        setBackground(Theme.getBackgroundColor()); // The colour of the bar at the top.
        setLayout(new BorderLayout()); // This lets us control where we actually place stuff.

        // Title
        title = new JLabel("Tasks: Day " + getDay(), SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(Theme.getForegroundColor());
        title.setOpaque(true);
        title.setBackground(Theme.getNavigationBarBackgroundColor());
        
        add(title, BorderLayout.NORTH);

        // Checkboxes and their labels
        JPanel checkboxPanel = new JPanel();
        checkboxPanel.setBackground(Theme.getPanelColor());
        checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
        int checkboxSize = 90;

        String[] TaskNames = {"No doomscrolling (8am - 5pm)",
                              "1+ hours of studying",
                              "30+ minute of reading",
                              "Check over your deadlines and plan your day",
                              "Be caught up with your notes",
                              "Dont skip any lectures or tutorials"};
        
        // Creating the checkboxes                      
        dietCheckbox = createStyledCheckbox(checkboxSize);
        dietCheckbox.setSelected(DoomscrollTask);

        indoorCheckbox = createStyledCheckbox(checkboxSize);
        indoorCheckbox.setSelected(StudyTask);

        outdoorCheckbox = createStyledCheckbox(checkboxSize);
        outdoorCheckbox.setSelected(ReadingTask);

        waterCheckbox = createStyledCheckbox(checkboxSize);
        waterCheckbox.setSelected(DeadlineTask);

        readingCheckbox = createStyledCheckbox(checkboxSize);
        readingCheckbox.setSelected(NotesTask);

        pictureCheckbox = createStyledCheckbox(checkboxSize);
        pictureCheckbox.setSelected(NoskipTask);

        JToggleButton[] checkboxes = {dietCheckbox, indoorCheckbox, outdoorCheckbox, waterCheckbox, readingCheckbox, pictureCheckbox };

        // Togglling the tasks variables + Testing them in terminal
        dietCheckbox.addActionListener(e -> { DoomscrollTask = dietCheckbox.isSelected(); 
                                              System.out.println("Diet task: " + DoomscrollTask);
                                              if (!suppressSave) saveTaskStatesToFIle();
                                              completedAllTasks();
                                              System.out.println("All tasks completed: " + allTasks);});

        indoorCheckbox.addActionListener(e -> {StudyTask = indoorCheckbox.isSelected();
                                              System.out.println("Indoor task: " + StudyTask);
                                              if (!suppressSave) saveTaskStatesToFIle();
                                              completedAllTasks();
                                              System.out.println("All tasks completed: " + allTasks);});

        outdoorCheckbox.addActionListener(e -> {ReadingTask = outdoorCheckbox.isSelected();
                                              System.out.println("Outdoor task: " + ReadingTask);
                                              if (!suppressSave) saveTaskStatesToFIle();
                                              completedAllTasks();
                                              System.out.println("All tasks completed: " + allTasks);});

        waterCheckbox.addActionListener(e -> { DeadlineTask = waterCheckbox.isSelected();
                                              System.out.println("Water task: " + DeadlineTask);
                                              if (!suppressSave) saveTaskStatesToFIle();
                                              completedAllTasks();
                                              System.out.println("All tasks completed: " + allTasks);});

        readingCheckbox.addActionListener(e -> {NotesTask = readingCheckbox.isSelected();
                                              System.out.println("Reading task: " + NotesTask);
                                              if (!suppressSave) saveTaskStatesToFIle();
                                              completedAllTasks();
                                              System.out.println("All tasks completed: " + allTasks);});

        pictureCheckbox.addActionListener(e -> {NoskipTask = pictureCheckbox.isSelected();
                                              System.out.println("Picture task: " + NoskipTask);
                                              if (!suppressSave) saveTaskStatesToFIle();
                                              completedAllTasks();
                                              System.out.println("All tasks completed: " + allTasks);});
    
        // Adding the checkboxes to panel
        checkboxPanel.add(dietCheckbox);
        checkboxPanel.add(indoorCheckbox);
        checkboxPanel.add(outdoorCheckbox);
        checkboxPanel.add(waterCheckbox);
        checkboxPanel.add(readingCheckbox);
        checkboxPanel.add(pictureCheckbox);

        add(checkboxPanel, BorderLayout.CENTER);

        // Adding the task descriptors next to the checkboxes
        for (int i = 0; i < TaskNames.length; i++ ) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.setBackground(Theme.getPanelColor());

            JLabel label = new JLabel(TaskNames[i]);
            label.setForeground(Theme.getForegroundColor());
            label.setFont(new Font("Dialog", Font.PLAIN, 20));
            row.add(checkboxes[i]);
            row.add(label);
            checkboxPanel.add(row);
        }
    }

    // Get the day number
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

    public void updateDayLabel(int day) { // What does this do???
        if (title != null) {
            title.setText("Tasks: Day " + day);
        }
    }

    // Handling saving the data from the tasks
    
    public void saveTaskStatesToFIle() {
        Properties props = new Properties();
        props.setProperty("DoomscrollTask", String.valueOf(DoomscrollTask));
        props.setProperty("StudyTask", String.valueOf(StudyTask));
        props.setProperty("ReadingTask", String.valueOf(ReadingTask));
        props.setProperty("DeadlineTask", String.valueOf(DeadlineTask));
        props.setProperty("NotesTask", String.valueOf(NotesTask));
        props.setProperty("NoskipTask", String.valueOf(NoskipTask));

        try (FileOutputStream out = new FileOutputStream("tasks.properties")) {
            props.store(out, "Task Completion States");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTaskStatesFromFile() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("tasks.properties")) {
            props.load(in);
            DoomscrollTask = Boolean.parseBoolean(props.getProperty("DoomscrollTask", "false"));
            StudyTask = Boolean.parseBoolean(props.getProperty("StudyTask", "false"));
            ReadingTask = Boolean.parseBoolean(props.getProperty("ReadingTask", "false"));
            DeadlineTask = Boolean.parseBoolean(props.getProperty("DeadlineTask", "false"));
            NotesTask = Boolean.parseBoolean(props.getProperty("NotesTask", "false"));
            NoskipTask = Boolean.parseBoolean(props.getProperty("NoskipTask", "false"));
        } catch (IOException e) {
            System.out.println("No previous save found, starting fresh.");
        }
    }
}