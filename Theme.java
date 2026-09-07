import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Theme {
    public static final String SETTINGS_FILE = "settings.properties";

    private static String getThemeSetting() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("Settings file not found, using default theme");
        }
        return props.getProperty("theme", "dark mode").toLowerCase();
    }


    public static Color getBackgroundColor() {
        String theme = getThemeSetting();
        if (theme.equals("dark mode")) {
            return Color.DARK_GRAY;
        } else if (theme.equals("light mode")) {
            return Color.WHITE;
        }
        return Color.GRAY; // default will be dark mode
    }

    public static Color getForegroundColor() {
        String theme = getThemeSetting();
        if (theme.equals("dark mode")) {
            return Color.WHITE;
        } else if (theme.equals("light mode")) {
            return Color.BLACK;
        }
        return Color.DARK_GRAY; // default will be dark mode
    }

    public static Color getPanelColor() {
        String theme = getThemeSetting();
        if (theme.equals("dark mode")) {
            return Color.DARK_GRAY;
        } else if (theme.equals("light mode")) {
            return Color.LIGHT_GRAY;
        }
        return Color.GRAY; // Default will be dark mode
    }

    public static Color getNavigationBarBackgroundColor() {
        String theme = getThemeSetting();
        
        if (theme.equals("dark mode")) {
            return Color.BLACK;
        } else if (theme.equals("light mode")) {
            return Color.WHITE;
        }
        return Color.BLACK; // Default will be dark mode
    }

    public static Color getNavigationBarForegroundColor() {
        String theme = getThemeSetting();
        
        if (theme.equals("dark mode")) {
            return Color.WHITE;
        } else if (theme.equals("light mode")) {
            return Color.BLACK;
        }
        return Color.WHITE; // Default will be dark mode
    }

    public static Color getCheckBoxColor() {
        String theme = getThemeSetting();
        
        if (theme.equals("dark mode")) {
            return Color.GRAY;
        } else if (theme.equals("light mode")) {
            return Color.WHITE;
        }
        return Color.WHITE; // Default will be dark mode
    }

    public static Color getCheckBoxOutlineColor() {
        String theme = getThemeSetting();
        
        if (theme.equals("dark mode")) {
            return Color.WHITE;
        } else if (theme.equals("light mode")) {
            return Color.BLACK;
        }
        return Color.WHITE; // Default will be dark mode
    }

    public static Color getBuyMeACoffeeText() {
        String theme = getThemeSetting();
        
        if (theme.equals("dark mode")) {
            return Color.GREEN;
        } else if (theme.equals("light mode")) {
            return new Color(2,102,12);
        }
        return Color.WHITE; // Default will be dark mode
    }
}