package com.qlsv.view.dialog;

import com.qlsv.view.common.AppColors;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Font;

public class AnalysisResultDialog extends BaseDetailDialog {

    private final JTextArea textArea;

    public AnalysisResultDialog(String title) {
        this(title, createTextArea());
    }

    private AnalysisResultDialog(String title, JTextArea textArea) {
        super(title, textArea, 760, 560);
        this.textArea = textArea;
    }

    public void setAnalysisText(String text) {
        textArea.setText(text == null ? "" : text);
        textArea.setCaretPosition(0);
    }

    public void setDialogTitle(String title) {
        setTitle(title);
    }

    private static JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(AppColors.CARD_BACKGROUND);
        area.setForeground(AppColors.CARD_VALUE_TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        Font baseFont = UIManager.getFont("Label.font");
        area.setFont(baseFont == null ? new Font("Segoe UI", Font.PLAIN, 14) : baseFont.deriveFont(Font.PLAIN, 14f));
        return area;
    }
}
