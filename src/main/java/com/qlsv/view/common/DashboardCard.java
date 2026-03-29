/**
 * Thẻ thống kê dùng trong các màn hình dashboard.
 */
package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class DashboardCard extends JPanel {

    private final JLabel valueLabel = new JLabel("-");

    /**
     * Khởi tạo card dashboard.
     */
    public DashboardCard(String title, Color accentColor) {
        setLayout(new BorderLayout(0, 14));
        setOpaque(true);
        setBackground(AppColors.CARD_BACKGROUND);
        setPreferredSize(new Dimension(220, 136));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(4, 0, 0, 0, accentColor),
                        BorderFactory.createEmptyBorder(18, 18, 18, 18)
                )
        ));

        JLabel titleLabel = new JLabel(toHtml(title));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 30f));
        valueLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel hintLabel = new JLabel("Giá trị hiện tại");
        hintLabel.setFont(hintLabel.getFont().deriveFont(Font.PLAIN, 12.5f));
        hintLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel contentPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        contentPanel.setOpaque(false);
        contentPanel.add(titleLabel);
        contentPanel.add(valueLabel);
        contentPanel.add(hintLabel);

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Cập nhật value.
     */
    public void setValue(String value) {
        valueLabel.setText(value == null || value.isBlank() ? "-" : value);
    }

    /**
     * Xử lý to html.
     */
    private String toHtml(String text) {
        return "<html><div style='width:180px;line-height:1.35;'>" + text + "</div></html>";
    }
}
