import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

public class Window {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private StreakTracker tracker;
    private TasksPanel tasksPanel;
    
    public Window() {
        tasksPanel = new TasksPanel();
        tracker = new StreakTracker(tasksPanel);

        JFrame frame = new JFrame("75 HARD");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(500,800);
        frame.getContentPane().setBackground(Theme.getBackgroundColor());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout((new BoxLayout(bottomPanel, BoxLayout.Y_AXIS))); // Stacks components vertiacally on the Panel.
        bottomPanel.setBackground(Theme.getNavigationBarBackgroundColor());
        
        //Icon
        //This is unavailable on GNOME based linux systems.
        ImageIcon icon = new ImageIcon("icon.jpg");
        frame.setIconImage(icon.getImage());

        // Card Layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        //Navigation Bar   
        JSeparator NavigationBar = new JSeparator();
        NavigationBar.setForeground(Theme.getNavigationBarForegroundColor());
        NavigationBar.setPreferredSize(new Dimension(0,2));

        //Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5));

        int buttonSize = frame.getWidth() / 5;
        String[] labels = {"Photos", "Calendar", "Tasks", "Progress", "Settings"};
        
        for (String label : labels) {
            JButton button = new JButton(label);
            button.setPreferredSize(new Dimension(buttonSize, buttonSize));
            button.setBackground(Theme.getNavigationBarBackgroundColor());
            button.setForeground(Theme.getNavigationBarForegroundColor());
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(e -> cardLayout.show(cardPanel,label)); // "When this button is clicked, show the panel with name label in the cardPanel using the CardLayout."
            buttonPanel.add(button);
        }

        cardPanel.add(new PhotosPanel(), "Photos");
        cardPanel.add(new CalendarPanel(), "Calendar");
        cardPanel.add(tasksPanel, "Tasks"); // Already referenced at the top of the file.
        cardPanel.add(new ProgressPanel(), "Progress");
        cardPanel.add(new SettingsPanel(), "Settings");
        frame.add(cardPanel, BorderLayout.CENTER);

        
        bottomPanel.add(NavigationBar);
        bottomPanel.add(buttonPanel);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        cardLayout.show(cardPanel, "Tasks");
        frame.setVisible(true);  
    }
}