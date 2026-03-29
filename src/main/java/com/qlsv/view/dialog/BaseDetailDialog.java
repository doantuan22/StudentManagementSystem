/**
 * Hộp thoại cơ sở chi tiết dialog.
 */
package com.qlsv.view.dialog;

import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.AppTheme;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;

public class BaseDetailDialog extends JDialog {

    /**
     * Khởi tạo cơ sở chi tiết.
     */
    public BaseDetailDialog(String title, JComponent content) {
        this(title, content, 800, 600);
    }

    /**
     * Khởi tạo cơ sở chi tiết.
     */
    public BaseDetailDialog(String title, JComponent content, int width, int height) {
        super((Frame) null, title, false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AppColors.CONTENT_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createLineBorder(AppColors.CARD_BORDER));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(content.getBackground());

        add(scrollPane, BorderLayout.CENTER);
        setMinimumSize(new Dimension(640, 420));
        setSize(width, height);
        AppTheme.applyTree(this);
    }

    /**
     * Mở hộp thoại.
     */
    public void openDialog() {
        setLocationRelativeTo(null);
        if (!isVisible()) {
            setVisible(true);
        }
        toFront();
        repaint();
    }
}
