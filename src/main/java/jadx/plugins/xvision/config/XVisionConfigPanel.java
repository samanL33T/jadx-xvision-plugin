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
    private JTextArea defaultPromptArea; 

    public XVisionConfigPanel(XVisionPlugin plugin, Preferences preferences) {
        this.plugin = plugin;
        this.preferences = preferences;
        initComponents();
        loadPreferences();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Increased padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Increased spacing between components
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0; // Allow horizontal expansion

        // LLM Type
        JLabel llmLabel = new JLabel("LLM Type:");
        llmLabel.setFont(new Font("Dialog", Font.PLAIN, 14)); // Increased font size
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(llmLabel, gbc);

        llmComboBox = new JComboBox<>(new String[]{XVisionConstants.GPT4_SERVICE, XVisionConstants.CLAUDE_SERVICE, XVisionConstants.CUSTOM_SERVICE});
        llmComboBox.setFont(new Font("Dialog", Font.PLAIN, 14)); // Increased font size
        llmComboBox.setPreferredSize(new Dimension(300, 30)); // Set preferred size
        llmComboBox.addItemListener(e -> {
            String selectedLLM = (String) llmComboBox.getSelectedItem();
            customEndpointField.setEnabled(selectedLLM.equals(XVisionConstants.CUSTOM_SERVICE));
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(llmComboBox, gbc);

        // API Key
        JLabel apiKeyLabel = new JLabel("API Key:");
        apiKeyLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(apiKeyLabel, gbc);

        apiKeyField = new JPasswordField();
        apiKeyField.setFont(new Font("Dialog", Font.PLAIN, 14));
        apiKeyField.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(apiKeyField, gbc);

        // Custom Endpoint
        JLabel customEndpointLabel = new JLabel("Custom Endpoint:");
        customEndpointLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        add(customEndpointLabel, gbc);

        customEndpointField = new JTextField();
        customEndpointField.setFont(new Font("Dialog", Font.PLAIN, 14));
        customEndpointField.setPreferredSize(new Dimension(300, 30));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(customEndpointField, gbc);

        // Default Prompt
        JLabel defaultPromptLabel = new JLabel("Set Default Prompt:");
        defaultPromptLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        add(defaultPromptLabel, gbc);

        defaultPromptArea = new JTextArea(10, 40); // Increased number of visible rows
        defaultPromptArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        defaultPromptArea.setLineWrap(true);
        defaultPromptArea.setWrapStyleWord(true);
        
        JScrollPane promptScrollPane = new JScrollPane(defaultPromptArea);
        promptScrollPane.setPreferredSize(new Dimension(500, 200)); // Made scroll pane larger
        promptScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 0, 5, 0),
            BorderFactory.createLineBorder(Color.GRAY)
        ));
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Allow vertical expansion for the text area
        add(promptScrollPane, gbc);
    }

    private void loadPreferences() {
        String selectedLLM = preferences.get(XVisionConstants.PREF_SELECTED_LLM, XVisionConstants.DEFAULT_LLM);
        String apiKey = preferences.get(XVisionConstants.PREF_API_KEY, XVisionConstants.DEFAULT_API_KEY);
        String customEndpoint = preferences.get(XVisionConstants.PREF_CUSTOM_ENDPOINT, XVisionConstants.DEFAULT_CUSTOM_ENDPOINT);
        String defaultPrompt = preferences.get("defaultPrompt", XVisionConstants.DEFAULT_PROMPT_TEMPLATE);
        defaultPromptArea.setText(defaultPrompt);

        llmComboBox.setSelectedItem(selectedLLM);
        apiKeyField.setText(apiKey);
        customEndpointField.setText(customEndpoint);
    }

    public void savePreferences() {
        String selectedLLM = (String) llmComboBox.getSelectedItem();
        String apiKey = apiKeyField.getText();
        String customEndpoint = customEndpointField.getText();
        String defaultPrompt = defaultPromptArea.getText();
        preferences.put("defaultPrompt", defaultPrompt);

        plugin.updatePreferences(selectedLLM, apiKey, customEndpoint);
        plugin.initializeLLMCommunicator(); 
    }
}