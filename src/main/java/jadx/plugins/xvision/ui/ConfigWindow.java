package jadx.plugins.xvision.config;

import javax.swing.*;
import java.awt.*;
import jadx.plugins.xvision.XVisionPlugin;

public class ConfigWindow {
    private final XVisionPlugin plugin;
    
    private static final int WINDOW_WIDTH = 800; 
    private static final int WINDOW_HEIGHT = 600;

    public ConfigWindow(XVisionPlugin plugin) {
        this.plugin = plugin;
    }

    public void show() {
        JFrame frame = new JFrame("XVision Configuration");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setLocationRelativeTo(null);

        XVisionConfigPanel configPanel = new XVisionConfigPanel(plugin);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            configPanel.savePreferences();
            frame.dispose();
        });

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(configPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack(); 
        frame.setVisible(true);
    }
}