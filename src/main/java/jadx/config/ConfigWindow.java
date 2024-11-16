package jadx.plugins.xvision.config;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;
import jadx.plugins.xvision.XVisionPlugin;

public class ConfigWindow {
    private static final Preferences prefs = Preferences.userNodeForPackage(ConfigWindow.class);
    private final XVisionPlugin plugin;

    public ConfigWindow(XVisionPlugin plugin) {
        this.plugin = plugin;
    }

    public void show() {
        JFrame frame = new JFrame("XVision Configuration");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 200); // Slimmer and wider
        frame.setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10)); // Add padding between components
        JLabel llmLabel = new JLabel("LLM Type:");
        JComboBox<String> llmComboBox = new JComboBox<>(new String[]{"GPT-4", "Claude", "Custom"});
        JLabel apiKeyLabel = new JLabel("API Key:");
        JPasswordField apiKeyField = new JPasswordField();

        // Load saved preferences
        llmComboBox.setSelectedItem(prefs.get("llmType", "GPT-4"));
        apiKeyField.setText(prefs.get("apiKey", ""));

        panel.add(llmLabel);
        panel.add(llmComboBox);
        panel.add(apiKeyLabel);
        panel.add(apiKeyField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            // Save configuration logic
            String selectedLLM = (String) llmComboBox.getSelectedItem();
            String apiKey = new String(apiKeyField.getPassword());

            prefs.put("llmType", selectedLLM);
            prefs.put("apiKey", apiKey);

            // Update plugin preferences
            plugin.updatePreferences(selectedLLM, apiKey);

            frame.dispose();
        });

        panel.add(saveButton);
        frame.add(panel);
        frame.setVisible(true);
    }
}
