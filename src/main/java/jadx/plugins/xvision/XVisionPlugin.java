package jadx.plugins.xvision;

import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.api.JavaNode;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.gui.JadxGuiContext;
import jadx.api.plugins.options.JadxPluginOptions;
import jadx.api.plugins.options.OptionDescription;
import jadx.plugins.xvision.llm.LLMCommunicator;
import jadx.plugins.xvision.ui.UIManager;
import jadx.plugins.xvision.utils.XVisionConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class XVisionPlugin implements JadxPlugin {
    private static final String PLUGIN_NAME = "xVision Plugin";
    private static final String DEFAULT_PROMPT_TEMPLATE = """
            Assume the role of an expert Java developer and a security researcher. Analyze the provided Java code and answer following:
            1. What does the code do?
            2. Are there any security issues
            3. Any suspicious or notable patterns

            Code:
            %s
            """;

    private JadxPluginContext pluginContext;
    private JadxGuiContext guiContext;
    private Preferences preferences;
    private String selectedLLM;
    private String apiKey;
    private String customEndpoint;
    private LLMCommunicator llmCommunicator;
    private UIManager uiManager;
    private JadxPluginInfo pluginInfo;

    public XVisionPlugin() {
        preferences = Preferences.userNodeForPackage(XVisionPlugin.class);
    }

    @Override
    public JadxPluginInfo getPluginInfo() {
        return new JadxPluginInfo(
                "xvision-plugin",
                PLUGIN_NAME,
                "xVision: LLM integration for code analysis",
                "0.1.0"
        );
    }

    @Override
    public void init(JadxPluginContext context) {
        this.pluginContext = context;
        if (context.getGuiContext() != null) {
            this.guiContext = context.getGuiContext();
            uiManager = new UIManager(this);
            uiManager.initializeGUIComponents(guiContext);
        }
        context.registerOptions(getOptions());
    }

    public JadxPluginContext getContext() {
        return pluginContext;
    }

    public void updatePreferences(String selectedLLM, String apiKey, String customEndpoint) {
        this.selectedLLM = selectedLLM;
        this.apiKey = apiKey;
        this.customEndpoint = customEndpoint;

        preferences.put(XVisionConstants.PREF_SELECTED_LLM, selectedLLM);
        preferences.put(XVisionConstants.PREF_API_KEY, apiKey);
        preferences.put(XVisionConstants.PREF_CUSTOM_ENDPOINT, customEndpoint);
        try {
            preferences.flush();
        } catch (Exception e) {
            uiManager.handleError("Failed to save preferences", e);
        }
    }

    public String getCode(JavaNode node) {
        if (node instanceof JavaMethod) {
            return ((JavaMethod) node).getCodeStr();
        } else if (node instanceof JavaClass) {
            return ((JavaClass) node).getCode();
        }
        return null;
    }

    public void initializeLLMCommunicator() {
        if (selectedLLM.equals(XVisionConstants.GPT4_SERVICE)) {
            llmCommunicator = new LLMCommunicator.GPT4Communicator(apiKey);
        } else if (selectedLLM.equals(XVisionConstants.CLAUDE_SERVICE)) {
            llmCommunicator = new LLMCommunicator.ClaudeCommunicator(apiKey);
        } else if (selectedLLM.equals(XVisionConstants.CUSTOM_SERVICE)) {
            llmCommunicator = new LLMCommunicator.CustomLLMCommunicator(customEndpoint);
        } else {
            throw new IllegalArgumentException("Invalid LLM type: " + selectedLLM);
        }
    }

    public JadxPluginOptions getOptions() {
        return new JadxPluginOptions() {
            @Override
            public List<OptionDescription> getOptionsDescriptions() {
                return List.of();
            }

            @Override
            public void setOptions(Map<String, String> options) {
                Preferences prefs = Preferences.userNodeForPackage(XVisionPlugin.class);
                selectedLLM = options.getOrDefault("xvision-plugin.llmType", prefs.get("llmType", "GPT-4"));
                customEndpoint = options.getOrDefault("xvision-plugin.customEndpoint", prefs.get("customEndpoint", ""));
                apiKey = options.getOrDefault("xvision-plugin.apiKey", prefs.get("apiKey", ""));

                // Save preferences
                prefs.put("llmType", selectedLLM);
                prefs.put("customEndpoint", customEndpoint);
                prefs.put("apiKey", apiKey);
            }
        };
    }

    public void analyzeCode(String code) {
        if (!validateConfiguration()) return;
        String prompt = showPromptDialog(code);
        if (prompt == null) return;
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return sendLLMRequest(prompt);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    uiManager.showAnalysisResult(response);
                } catch (Exception e) {
                    uiManager.handleError("Error analyzing code", e);
                }
            }
        };
        worker.execute();
    }

    private String sendLLMRequest(String prompt) throws IOException, InterruptedException {
        return llmCommunicator.sendRequest(prompt);
    }

    private String showPromptDialog(String code) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Create radio buttons for prompt selection
        JRadioButton defaultPromptButton = new JRadioButton("Default Prompt");
        JRadioButton customPromptButton = new JRadioButton("Custom Prompt");
        ButtonGroup promptGroup = new ButtonGroup();
        promptGroup.add(defaultPromptButton);
        promptGroup.add(customPromptButton);
        defaultPromptButton.setSelected(true);

        // Create text area for prompt
        JTextArea promptArea = new JTextArea(10, 40);
        promptArea.setText(String.format(DEFAULT_PROMPT_TEMPLATE, code));
        promptArea.setEnabled(false);

        // Create code preview area
        JTextArea codePreview = new JTextArea(10, 40);
        codePreview.setText(code);
        codePreview.setEditable(false);

        // Create scroll panes
        JScrollPane promptScrollPane = new JScrollPane(promptArea);
        promptScrollPane.setBorder(BorderFactory.createTitledBorder("Prompt"));

        JScrollPane codeScrollPane = new JScrollPane(codePreview);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Code Preview"));

        // Add components to main panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(defaultPromptButton);
        topPanel.add(customPromptButton);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(promptScrollPane, BorderLayout.CENTER);
        panel.add(codeScrollPane, BorderLayout.SOUTH);

        // Add listeners
        defaultPromptButton.addActionListener(e -> {
            promptArea.setText(String.format(DEFAULT_PROMPT_TEMPLATE, code));
            promptArea.setEnabled(false);
        });
        customPromptButton.addActionListener(e -> {
            promptArea.setEnabled(true);
            promptArea.setText(uiManager.getLastCustomPrompt());
        });

        // Show dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "xVision Plugin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String prompt = promptArea.getText();
            uiManager.saveCustomPrompt(prompt);
            return defaultPromptButton.isSelected() ? String.format(DEFAULT_PROMPT_TEMPLATE, code) : prompt;
        }
        return String.format(DEFAULT_PROMPT_TEMPLATE, code);
    }

    private boolean validateConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            uiManager.showError("API Key is not configured. Please configure it in the plugin settings.");
            return false;
        }

        if (selectedLLM.equals("Custom") && (customEndpoint == null || customEndpoint.trim().isEmpty())) {
            uiManager.showError("Custom endpoint is not configured. Please configure it in the plugin settings.");
            return false;
        }

        return true;
    }
}