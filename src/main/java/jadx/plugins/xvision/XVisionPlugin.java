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
import jadx.plugins.xvision.llm.LLMCommunicator;
import jadx.plugins.xvision.ui.UIManager;
import jadx.plugins.xvision.utils.XVisionConstants;
import jadx.plugins.xvision.utils.CodeExtractor;
import jadx.plugins.xvision.ui.CodeDisplayWindow;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class XVisionPlugin implements JadxPlugin {
    public static final String PLUGIN_NAME = "xVision Plugin";
    public static final String PLUGIN_ID = "xvision-plugin";
    private JadxPluginContext pluginContext;
    private JadxGuiContext guiContext;
    private String selectedLLM;
    private String apiKey;
    private String customEndpoint;
    private LLMCommunicator llmCommunicator;
    private UIManager uiManager;
    private JadxPluginInfo pluginInfo;
    private JadxDecompiler decompiler;
    private boolean renderInSeparateWindow = false;

    public XVisionPlugin(JadxDecompiler decompiler) {
        this.decompiler = decompiler;
        PreferencesManager.initializeJadxArgs(decompiler);
    }

    public XVisionPlugin() {
        // No-argument constructor required by JADX plugin system
    }

    @Override
    public JadxPluginInfo getPluginInfo() {
        return new JadxPluginInfo(
                PLUGIN_ID, 
                PLUGIN_NAME,      
                "xVision: LLM integration for code analysis"
        );
    }

    @Override
    public void init(JadxPluginContext context) {
        this.pluginContext = context;
        if (context.getDecompiler() != null) {
            PreferencesManager.initializeJadxArgs(context.getDecompiler());
        }
        if (context.getGuiContext() != null) {
            this.guiContext = context.getGuiContext();
            uiManager = new UIManager(this);
            uiManager.initializeGUIComponents(guiContext);
        }
        loadPreferences();
    }

    private void loadPreferences() {
        selectedLLM = PreferencesManager.getLLMType();
        apiKey = PreferencesManager.getApiKey();
        customEndpoint = PreferencesManager.getCustomEndpoint();
        initializeLLMCommunicator();
    }

    public JadxPluginContext getContext() {
        return pluginContext;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setCustomEndpoint(String customEndpoint) {
        this.customEndpoint = customEndpoint;
    }
    
    public String getCustomEndpoint() {
        return customEndpoint;
    }
    
    public void setSelectedLLM(String selectedLLM) {
        this.selectedLLM = selectedLLM;
    }
    
    public String getSelectedLLM() {
        return selectedLLM;
    }
       
    public String getDefaultPrompt() {
        return PreferencesManager.getDefaultPrompt();
    }

    public void setDefaultPrompt(String defaultPrompt) {
        PreferencesManager.setDefaultPrompt(defaultPrompt);
    }
    
    public void updatePreferences(String selectedLLM, String apiKey, String customEndpoint) {
        PreferencesManager.setLLMType(selectedLLM);
        PreferencesManager.setApiKey(apiKey);
        PreferencesManager.setCustomEndpoint(customEndpoint);
        initializeLLMCommunicator();
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
            llmCommunicator = new LLMCommunicator.CustomLLMCommunicator(apiKey);
        }
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
                    if (renderInSeparateWindow) {
                        String extractedCode = CodeExtractor.extractJavaCode(response);
                        if (!extractedCode.isEmpty()) {
                            SwingUtilities.invokeLater(() -> {
                                CodeDisplayWindow codeWindow = new CodeDisplayWindow(extractedCode, "Analyzed Code");
                                codeWindow.setVisible(true);
                            });
                        } else {
                            // Show regular response if no code was found to extract
                            uiManager.showAnalysisResult(response);
                        }
                    } else {
                        uiManager.showAnalysisResult(response);
                    }
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
        String defaultPrompt = getDefaultPrompt();

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
        JCheckBox renderSeparateWindowCheckbox = new JCheckBox("Render the code in new window");
        JPanel checkboxPanel = new JPanel(new GridLayout(2, 1));
        checkboxPanel.add(useCustomPromptCheckbox);
        checkboxPanel.add(renderSeparateWindowCheckbox);

        JTextArea customPromptArea = new JTextArea(5, 40);
        customPromptArea.setLineWrap(true);
        customPromptArea.setWrapStyleWord(true);
        JScrollPane customPromptScrollPane = new JScrollPane(customPromptArea);
        customPromptScrollPane.setBorder(BorderFactory.createTitledBorder("Custom Prompt"));
        customPromptScrollPane.setVisible(false);
        
        customPromptPanel.add(checkboxPanel, BorderLayout.NORTH);
        customPromptPanel.add(customPromptScrollPane, BorderLayout.CENTER);

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

        panel.add(codeScrollPane, BorderLayout.CENTER);
        panel.add(customPromptPanel, BorderLayout.SOUTH);


        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel,
                "xVision Plugin - Analyzing Code",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                renderInSeparateWindow = renderSeparateWindowCheckbox.isSelected();
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