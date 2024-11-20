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
        String defaultPrompt = preferences.get(XVisionConstants.PREF_DEFAULT_PROMPT, XVisionConstants.DEFAULT_PROMPT_TEMPLATE);

        if (!defaultPrompt.contains("%s")) {
            defaultPrompt = defaultPrompt + "\n\n%s";
        }
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea codePreview = new JTextArea(10, 40);
        codePreview.setText(code);
        codePreview.setEditable(false);
        JScrollPane codeScrollPane = new JScrollPane(codePreview);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Code Preview"));


        JPanel customPromptPanel = new JPanel(new BorderLayout(5, 5));
        JCheckBox useCustomPromptCheckbox = new JCheckBox("Add custom prompt");
        JTextArea customPromptArea = new JTextArea(5, 40);
        customPromptArea.setLineWrap(true);
        customPromptArea.setWrapStyleWord(true);
        JScrollPane customPromptScrollPane = new JScrollPane(customPromptArea);
        customPromptScrollPane.setBorder(BorderFactory.createTitledBorder("Custom Prompt"));
        customPromptScrollPane.setVisible(false);


        useCustomPromptCheckbox.addActionListener(e -> {
            boolean isSelected = useCustomPromptCheckbox.isSelected();
            customPromptScrollPane.setVisible(isSelected);
            if (isSelected) {
                customPromptArea.requestFocus();
            }

            panel.revalidate();
            panel.repaint();
            SwingUtilities.getWindowAncestor(panel).pack();
        });

        customPromptPanel.add(useCustomPromptCheckbox, BorderLayout.NORTH);
        customPromptPanel.add(customPromptScrollPane, BorderLayout.CENTER);

        panel.add(codeScrollPane, BorderLayout.CENTER);
        panel.add(customPromptPanel, BorderLayout.SOUTH);


        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel,
                "xVision Plugin - Analyzing Code",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                // If custom prompt is checked but empty, show error and continue loop
                if (useCustomPromptCheckbox.isSelected() && customPromptArea.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                        "Custom prompt is required when checkbox is selected.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    continue;
                }


                if (useCustomPromptCheckbox.isSelected()) {
                    String customPrompt = customPromptArea.getText();
                    if (!customPrompt.contains("%s")) {
                        customPrompt = customPrompt + "\n\n%s";
                    }
                    return String.format(customPrompt, code);
                } else {
                    return String.format(defaultPrompt, code);
                }
            }
            return null;
        }
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