package jadx.plugins.xvision;

import jadx.api.JadxDecompiler;
import jadx.api.JavaNode;
import jadx.api.JavaClass;
import jadx.api.JavaMethod;
import jadx.api.metadata.ICodeNodeRef;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.gui.JadxGuiContext;
import jadx.api.plugins.options.JadxPluginOptions;
import jadx.api.plugins.options.OptionDescription;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jadx.plugins.xvision.config.ConfigWindow;
import jadx.plugins.xvision.llm.LLMCommunicator;
import jadx.plugins.xvision.utils.XVisionConstants;

public class XVisionPlugin implements JadxPlugin {
    private static final String PLUGIN_NAME = "xVision Plugin";
    private static final Map<String, String> LLM_ENDPOINTS = Map.of(
            "GPT-4", "https://api.openai.com/v1/chat/completions",
            "claud-sonnet-3-5", "https://api.anthropic.com/v1/messages",
            "Your own LLM", "" 
    );

    private static final String DEFAULT_PROMPT_TEMPLATE = """
        Assume the role of an expert Java developer and a security researcher. Analyze the provided Java code and answer following:
        1. What does the code do?
        2. Are there any security issues
        3. Any suspicious or notable patterns

        Code:
        %s
        """;
    private LLMCommunicator llmCommunicator;
    private JadxPluginContext pluginContext;
    private JadxGuiContext guiContext;
    private String apiKey;
    private String selectedLLM;
    private String customEndpoint;
    private final Preferences preferences = Preferences.userNodeForPackage(XVisionPlugin.class);

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
            initializeGUIComponents();
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
            handleError("Failed to save preferences", e);
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
    private void initializeGUIComponents() {
        guiContext.addMenuAction("XVision Config", () -> {
            new ConfigWindow(this).show();
        });
        XVisionContextMenuAction.addToContextMenu(guiContext, this);

        // Load preferences
        selectedLLM = getSelectedLLM();
        apiKey = getApiKey();
        customEndpoint = getCustomEndpoint();

        // Initialize LLMCommunicator instance
        initializeLLMCommunicator();
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
                // List<OptionDescription> options = new ArrayList<>();
                // LLM Type option
                // options.add(new OptionDescription() {
                //     @Override
                //     public String defaultValue() { return "GPT-4"; }
                //     @Override
                //     public String name() { return "xvision-plugin.llmType"; }
                //     @Override
                //     public String description() { return "LLM Type"; }
                //     @Override
                //     public List<String> values() {
                //         return List.of("GPT-4", "GPT-3.5", "Claude", "Custom");
                //     }
                // });
                // Custom Endpoint option
                // options.add(new OptionDescription() {
                //     @Override
                //     public String defaultValue() { return ""; }
                //     @Override
                //     public String name() { return "xvision-plugin.customEndpoint"; }
                //     @Override
                //     public String description() { return "Custom API Endpoint"; }
                //     @Override
                //     public List<String> values() { return List.of(); }
                // });
                // API Key option
                // options.add(new OptionDescription() {
                //     @Override
                //     public String defaultValue() { return ""; }
                //     @Override
                //     public String name() { return "xvision-plugin.apiKey"; }
                //     @Override
                //     public String description() { return "API Key"; }
                //     @Override
                //     public List<String> values() { return List.of(); }
                // });
                // return options;
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
            promptArea.setText(getLastCustomPrompt());
        });

        // Show dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "xVision Plugin", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String prompt = promptArea.getText();
            saveCustomPrompt(prompt);
            return defaultPromptButton.isSelected() ? String.format(DEFAULT_PROMPT_TEMPLATE, code) : prompt;
        }
        return null;
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
                    showAnalysisResult(response);
                } catch (Exception e) {
                    handleError("Error analyzing code", e);
                }
            }
        };
        worker.execute();
    }



    private String sendLLMRequest(String prompt) throws IOException, InterruptedException {
        return llmCommunicator.sendRequest(prompt);
    }


    private String formatRequestBody(String prompt) {
        String escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
        return switch (selectedLLM) {
            case "GPT-4", "GPT-3.5" -> String.format("""
            {
                "model": "%s",
                "messages": [{"role": "user", "content": "%s"}],
                "temperature": 0.7
            }
            """, selectedLLM.equals("GPT-4") ? "gpt-4" : "gpt-3.5-turbo",
                    escapedPrompt);
            case "claud-sonnet-3-5-20241022" -> String.format("""
            {
                "model": "claude-2",
                "messages": [{"role": "user", "content": "%s"}]
            }
            """, escapedPrompt);
            default -> prompt;
        };
    }

    private String parseResponse(String responseBody) {
        try {
            if (selectedLLM.startsWith("GPT")) {
                // Parse the JSON response to extract the content
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                    return message.get("content").getAsString();
                } else {
                    throw new RuntimeException("No choices found in the response: " + responseBody);
                }
            } else if (selectedLLM.equals("Claude")) {
                // Parse the JSON response to extract the content
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray choices = jsonResponse.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                    return message.get("content").getAsString();
                } else {
                    throw new RuntimeException("No choices found in the response: " + responseBody);
                }
            }
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse LLM response: " + responseBody, e);
        }
    }




    private void showAnalysisResult(String analysis) {
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Analysis text area
        JTextArea textArea = new JTextArea(analysis);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(800, 600));

        // Add copy button
        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(analysis);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(null, "Analysis copied to clipboard!");
        });

        // Add components to panel
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        resultPanel.add(copyButton, BorderLayout.SOUTH);

        // Show dialog
        JDialog dialog = new JDialog();
        dialog.setTitle("xVision Analysis Result");
        dialog.setModal(true);
        dialog.setContentPane(resultPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private boolean validateConfiguration() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            showError("API Key is not configured. Please configure it in the plugin settings.");
            return false;
        }

        if (selectedLLM.equals("Custom") && (customEndpoint == null || customEndpoint.trim().isEmpty())) {
            showError("Custom endpoint is not configured. Please configure it in the plugin settings.");
            return false;
        }

        return true;
    }

    private void saveCustomPrompt(String prompt) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(XVisionPlugin.class);
            prefs.put("lastCustomPrompt", prompt);
        } catch (Exception e) {
        }
    }

    private String getLastCustomPrompt() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(XVisionPlugin.class);
            return prefs.get("lastCustomPrompt", "");
        } catch (Exception e) {
            return "";
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void handleError(String message, Exception e) {
        String fullMessage = message + "\n" + e.getMessage();
        e.printStackTrace(); // Log the stack trace for debugging
        showError(fullMessage);
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public String getSelectedLLM() {
        return preferences.get(XVisionConstants.PREF_SELECTED_LLM, XVisionConstants.DEFAULT_LLM);
    }

    public String getApiKey() {
        return preferences.get(XVisionConstants.PREF_API_KEY, XVisionConstants.DEFAULT_API_KEY);
    }
    public String getCustomEndpoint() {
        return preferences.get(XVisionConstants.PREF_CUSTOM_ENDPOINT, XVisionConstants.DEFAULT_CUSTOM_ENDPOINT);
    }
    
}
