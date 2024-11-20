package jadx.plugins.xvision.config;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;
import jadx.plugins.xvision.XVisionPlugin;

public class ConfigWindow {
    private final XVisionPlugin plugin;
    private final Preferences preferences;
    
    // Define preferred window dimensions
    private static final int WINDOW_WIDTH = 800;  // Increased from 400
    private static final int WINDOW_HEIGHT = 600; // Increased from 200

    public ConfigWindow(XVisionPlugin plugin) {
        this.plugin = plugin;
        this.preferences = Preferences.userNodeForPackage(XVisionPlugin.class);
    }

    public void show() {
        JFrame frame = new JFrame("XVision Configuration");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setMinimumSize(new Dimension(600, 400)); // Set minimum size
        frame.setLocationRelativeTo(null);

        XVisionConfigPanel configPanel = new XVisionConfigPanel(plugin, preferences);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            configPanel.savePreferences();
            frame.dispose();
        });

        // Add some padding around the main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(configPanel, BorderLayout.CENTER);
        
        // Create a panel for the button with some padding
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack(); 
        frame.setVisible(true);
    }
}