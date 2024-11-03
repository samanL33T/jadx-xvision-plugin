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
    private JadxPluginContext context;
    private JadxGuiContext guiContext;
    private String apiKey;
    private String selectedLLM;
    private String customEndpoint;

    @Override
    public JadxPluginInfo getPluginInfo() {
        return new JadxPluginInfo(
                "xvision-plugin",
                PLUGIN_NAME,
                "LLM integration for code analysis",
                "0.0.1"
        );
    }

    @Override
    public void init(JadxPluginContext context) {
        this.context = context;
        if (context.getGuiContext() != null) {
            this.guiContext = context.getGuiContext();
            initializeGUIComponents();
        }
        context.registerOptions(getOptions());
    }

    private String getCode(ICodeNodeRef node) {
        JadxDecompiler decompiler = context.getDecompiler();
        JavaNode javaNode = decompiler.getJavaNodeByRef(node);
        if (javaNode instanceof JavaClass) {
            return ((JavaClass) javaNode).getCode();
        } else if (javaNode instanceof JavaMethod) {
            JavaMethod javaMethod = (JavaMethod) javaNode;
            return javaMethod.getDeclaringClass().getCode();
        }
        return null;
    }


    private void initializeGUIComponents() {
        guiContext.addMenuAction("Analyze with " + PLUGIN_NAME, () -> {
            try {
                // Get the current node and its code
                ICodeNodeRef node = guiContext.getEnclosingNodeUnderCaret();
                if (node != null) {
                    String code = getCode(node);
                    if (code != null && !code.isEmpty()) {
                        analyzeCode(code);
                    } else {
                        showError("No code available in the selected node");
                    }
                } else {
                    showError("Please place the cursor inside a code block to analyze");
                }
            } catch (Exception e) {
                handleError("Failed to get code", e);
            }
        });
    }

    public JadxPluginOptions getOptions() {
        return new JadxPluginOptions() {
            @Override
            public List<OptionDescription> getOptionsDescriptions() {
                List<OptionDescription> options = new ArrayList<>();
                // LLM Type option
                options.add(new OptionDescription() {
                    @Override
                    public String defaultValue() { return "GPT-4"; }
                    @Override
                    public String name() { return "xvision-plugin.llmType"; }
                    @Override
                    public String description() { return "LLM Type"; }
                    @Override
                    public List<String> values() {
                        return List.of("GPT-4", "GPT-3.5", "Claude", "Custom");
                    }
                });
                // Custom Endpoint option
                options.add(new OptionDescription() {
                    @Override
                    public String defaultValue() { return ""; }
                    @Override
                    public String name() { return "xvision-plugin.customEndpoint"; }
                    @Override
                    public String description() { return "Custom API Endpoint"; }
                    @Override
                    public List<String> values() { return List.of(); }
                });
                // API Key option
                options.add(new OptionDescription() {
                    @Override
                    public String defaultValue() { return ""; }
                    @Override
                    public String name() { return "xvision-plugin.apiKey"; }
                    @Override
                    public String description() { return "API Key"; }
                    @Override
                    public List<String> values() { return List.of(); }
                });
                return options;
            }
            @Override
            public void setOptions(Map<String, String> options) {
                selectedLLM = options.get("xvision-plugin.llmType");
                customEndpoint = options.get("xvision-plugin.customEndpoint");
                apiKey = options.get("xvision-plugin.apiKey");
            }
        };
    }


    private String showPromptDialog(String code) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // Create radio buttons for prompt selection
        JRadioButton defaultPromptButton = new JRadioButton("Use default prompt", true);
        JRadioButton customPromptButton = new JRadioButton("Use custom prompt");
        ButtonGroup group = new ButtonGroup();
        group.add(defaultPromptButton);
        group.add(customPromptButton);
        // Create prompt text area
        JTextArea promptArea = new JTextArea(10, 50);
        promptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        promptArea.setWrapStyleWord(true);
        promptArea.setLineWrap(true);
        promptArea.setText(String.format(DEFAULT_PROMPT_TEMPLATE, code));
        promptArea.setEnabled(false);
        // Add scroll pane for prompt area
        JScrollPane promptScrollPane = new JScrollPane(promptArea);
        promptScrollPane.setBorder(BorderFactory.createTitledBorder("Prompt"));
        // Create top panel for radio buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(defaultPromptButton);
        topPanel.add(customPromptButton);
        // Add code preview
        JTextArea codePreview = new JTextArea(10, 50);
        codePreview.setText(code);
        codePreview.setEditable(false);
        codePreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane codeScrollPane = new JScrollPane(codePreview);
        codeScrollPane.setBorder(BorderFactory.createTitledBorder("Code Preview"));
        // Add components to main panel
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
        });
        // Show dialog
        int result = JOptionPane.showConfirmDialog(null, panel,
                "xVision Plugin",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String prompt = promptArea.getText();
            saveCustomPrompt(prompt);
            return defaultPromptButton.isSelected() ? String.format(DEFAULT_PROMPT_TEMPLATE, code) : prompt;
        }
        return null;
    }


    private void analyzeCode(String code) {
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
        String endpoint = selectedLLM.equals("Custom") ? customEndpoint : LLM_ENDPOINTS.get(selectedLLM);
        String requestBody = formatRequestBody(prompt);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("API request failed with status code: " + response.statusCode() +
                        "\nResponse: " + response.body());
            }
            return parseResponse(response.body());
        } catch (Exception e) {
            e.printStackTrace(); // Log the stack trace for debugging
            throw new IOException("Failed to communicate with LLM API", e);
        }
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

}
