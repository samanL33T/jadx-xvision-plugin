package jadx.plugins.xvision.ui;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class CodeDisplayWindow extends JFrame {
    private final RSyntaxTextArea codeTextArea;

    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 800;

    public CodeDisplayWindow(String code, String title) {
        super("xVision - " + title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        codeTextArea = new RSyntaxTextArea(40, 120);
        codeTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        codeTextArea.setCodeFoldingEnabled(true);
        codeTextArea.setAntiAliasingEnabled(true);
        codeTextArea.setText(code);
        codeTextArea.setEditable(false);
        
        RTextScrollPane scrollPane = new RTextScrollPane(codeTextArea);
        getContentPane().add(scrollPane);
        
        // Add toolbar with actions
        JToolBar toolBar = new JToolBar();
        JButton copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> {
            codeTextArea.selectAll();
            codeTextArea.copy();
            codeTextArea.select(0, 0);
        });
        toolBar.add(copyButton);
        
        getContentPane().add(toolBar, BorderLayout.NORTH);

        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        
        pack();
        setLocationRelativeTo(null);
    }
}