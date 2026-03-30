/**
 * Khối hiển thị chi tiết dạng cặp nhãn và giá trị.
 */
package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class DetailSectionPanel extends JPanel {

    private final JLabel titleLabel;
    private final JPanel bodyPanel = new JPanel(new BorderLayout());

    /**
     * Khởi tạo chi tiết phần.
     */
    public DetailSectionPanel(String title, String emptyMessage) {
        setLayout(new BorderLayout(0, 14));
        setOpaque(true);
        setBackground(AppColors.CARD_BACKGROUND);
        Border outerBorder = AppTheme.createCardBorder();
        setBorder(outerBorder);

        titleLabel = new JLabel(title);
        AppTheme.styleSectionTitle(titleLabel);

        bodyPanel.setOpaque(false);
        add(titleLabel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);

        showMessage(emptyMessage);
    }

    /**
     * Cập nhật phần title.
     */
    public void setSectionTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Hiển thị thông báo.
     */
    public void showMessage(String message) {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(24, 12, 24, 12));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.ITALIC, 14f));
        messageLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        messagePanel.add(messageLabel, BorderLayout.CENTER);

        bodyPanel.removeAll();
        bodyPanel.add(messagePanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Hiển thị trường.
     */
    public void showFields(String[][] fields) {
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 14, 14));
        gridPanel.setOpaque(false);

        for (String[] field : fields) {
            String label = field.length > 0 ? field[0] : "";
            String value = field.length > 1 ? field[1] : "";
            gridPanel.add(createFieldCard(label, value));
        }

        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(gridPanel);

        bodyPanel.removeAll();
        bodyPanel.add(container, BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    /**
     * Tạo card trường.
     */
    private JPanel createFieldCard(String label, String value) {
        JPanel cardPanel = new JPanel(new BorderLayout(0, 6));
        cardPanel.setOpaque(true);
        cardPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD, 12.5f));
        labelComponent.setForeground(AppColors.CARD_TITLE_TEXT);

        JLabel valueComponent = new JLabel(toHtml(value));
        valueComponent.setFont(valueComponent.getFont().deriveFont(Font.PLAIN, 13.5f));
        valueComponent.setForeground(AppColors.CARD_VALUE_TEXT);

        cardPanel.add(labelComponent, BorderLayout.NORTH);
        cardPanel.add(valueComponent, BorderLayout.CENTER);
        return cardPanel;
    }

    /**
     * Xử lý to html.
     */
    private String toHtml(String value) {
        String escapedValue = value == null ? "" : value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
        return "<html><div style='width:280px;line-height:1.4;'>" + escapedValue + "</div></html>";
    }
}
