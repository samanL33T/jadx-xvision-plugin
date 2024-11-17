package jadx.plugins.xvision.config;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;
import jadx.plugins.xvision.XVisionPlugin;

public class ConfigWindow {
    private final XVisionPlugin plugin;
    private final Preferences preferences;

    public ConfigWindow(XVisionPlugin plugin) {
        this.plugin = plugin;
        this.preferences = Preferences.userNodeForPackage(XVisionPlugin.class);
    }

    public void show() {
        JFrame frame = new JFrame("XVision Configuration");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLocationRelativeTo(null);

        XVisionConfigPanel configPanel = new XVisionConfigPanel(plugin, preferences);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            configPanel.savePreferences();
            frame.dispose();
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(configPanel, BorderLayout.CENTER);
        mainPanel.add(saveButton, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}
