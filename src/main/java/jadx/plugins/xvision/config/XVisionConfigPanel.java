package jadx.plugins.xvision.config;

import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import jadx.plugins.xvision.XVisionPlugin;
import jadx.plugins.xvision.utils.XVisionConstants;

public class XVisionConfigPanel extends JPanel {
    private final XVisionPlugin plugin;
    private final Preferences preferences;
    private JComboBox<String> llmComboBox;
    private JTextField apiKeyField;
    private JTextField customEndpointField;

    public XVisionConfigPanel(XVisionPlugin plugin, Preferences preferences) {
        this.plugin = plugin;
        this.preferences = preferences;
        initComponents();
        loadPreferences();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel llmLabel = new JLabel("LLM Type:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(llmLabel, gbc);

        llmComboBox = new JComboBox<>(new String[]{XVisionConstants.GPT4_SERVICE, XVisionConstants.CLAUDE_SERVICE, XVisionConstants.CUSTOM_SERVICE});
        llmComboBox.addItemListener(e -> {
            String selectedLLM = (String) llmComboBox.getSelectedItem();
            customEndpointField.setEnabled(selectedLLM.equals(XVisionConstants.CUSTOM_SERVICE));
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(llmComboBox, gbc);

        JLabel apiKeyLabel = new JLabel("API Key:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(apiKeyLabel, gbc);

        apiKeyField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(apiKeyField, gbc);

        JLabel customEndpointLabel = new JLabel("Custom Endpoint:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(customEndpointLabel, gbc);

        customEndpointField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(customEndpointField, gbc);
    }

    private void loadPreferences() {
        String selectedLLM = preferences.get(XVisionConstants.PREF_SELECTED_LLM, XVisionConstants.DEFAULT_LLM);
        String apiKey = preferences.get(XVisionConstants.PREF_API_KEY, XVisionConstants.DEFAULT_API_KEY);
        String customEndpoint = preferences.get(XVisionConstants.PREF_CUSTOM_ENDPOINT, XVisionConstants.DEFAULT_CUSTOM_ENDPOINT);

        llmComboBox.setSelectedItem(selectedLLM);
        apiKeyField.setText(apiKey);
        customEndpointField.setText(customEndpoint);
    }

    public void savePreferences() {
        String selectedLLM = (String) llmComboBox.getSelectedItem();
        String apiKey = apiKeyField.getText();
        String customEndpoint = customEndpointField.getText();

        plugin.updatePreferences(selectedLLM, apiKey, customEndpoint);
        plugin.initializeLLMCommunicator(); 
    }
}