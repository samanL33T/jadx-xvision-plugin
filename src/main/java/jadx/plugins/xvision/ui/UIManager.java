package jadx.plugins.xvision.ui;

import jadx.api.plugins.gui.JadxGuiContext;
import jadx.plugins.xvision.XVisionPlugin;
import jadx.plugins.xvision.config.ConfigWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class UIManager {
    private final XVisionPlugin plugin;

    public UIManager(XVisionPlugin plugin) {
        this.plugin = plugin;
    }

    public void initializeGUIComponents(JadxGuiContext guiContext) {
        guiContext.addMenuAction("XVision Config", this::showConfigWindow);

        XVisionContextMenuAction.addToContextMenu(guiContext, plugin);
    }

    public void showConfigWindow() {
        new ConfigWindow(plugin).show();
    }

    public JDialog getProcessingDialog(String msg) {
        JDialog dialog = new JDialog((Frame)null, "Processing...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(200, 100);
        dialog.setLayout(new FlowLayout());

        JLabel label = new JLabel(msg);
        dialog.add(label);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        dialog.add(progressBar, BorderLayout.CENTER);

        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);

        return dialog;
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void handleError(String message, Exception e) {
        String fullMessage = message + "\n" + e.getMessage();
        e.printStackTrace();
        showError(fullMessage);
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

        JButton copyButton = new JButton("Copy to Clipboard");
        copyButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(analysis);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(null, "Analysis copied to clipboard!");
        });


        resultPanel.add(scrollPane, BorderLayout.CENTER);
        resultPanel.add(copyButton, BorderLayout.SOUTH);

        JDialog dialog = new JDialog();
        dialog.setTitle("xVision Analysis Result");
        dialog.setModal(true);
        dialog.setContentPane(resultPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }
}