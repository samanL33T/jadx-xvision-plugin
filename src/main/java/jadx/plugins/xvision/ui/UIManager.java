package jadx.plugins.xvision.ui;

import jadx.api.plugins.gui.JadxGuiContext;
import jadx.plugins.xvision.XVisionPlugin;
import jadx.plugins.xvision.config.ConfigWindow;
import jadx.plugins.xvision.utils.XVisionConstants;
import jadx.plugins.xvision.ui.XVisionContextMenuAction;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.prefs.Preferences;

public class UIManager {
    private final XVisionPlugin plugin;

    public UIManager(XVisionPlugin plugin) {
        this.plugin = plugin;
    }

    public void initializeGUIComponents(JadxGuiContext guiContext) {
        guiContext.addMenuAction("XVision Config", () -> {
            new ConfigWindow(plugin).show();
        });

        XVisionContextMenuAction.addToContextMenu(guiContext, plugin);
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void handleError(String message, Exception e) {
        String fullMessage = message + "\n" + e.getMessage();
        e.printStackTrace(); // Log the stack trace for debugging
        showError(fullMessage);
    }

    public void saveCustomPrompt(String prompt) {
        try {
            Preferences prefs = Preferences.userNodeForPackage(XVisionPlugin.class);
            prefs.put("lastCustomPrompt", prompt);
        } catch (Exception e) {
            handleError("Failed to save custom prompt", e);
        }
    }

    public String getLastCustomPrompt() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(XVisionPlugin.class);
            return prefs.get("lastCustomPrompt", "");
        } catch (Exception e) {
            handleError("Failed to retrieve custom prompt", e);
            return "";
        }
    }

    public void showAnalysisResult(String analysis) {
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
}