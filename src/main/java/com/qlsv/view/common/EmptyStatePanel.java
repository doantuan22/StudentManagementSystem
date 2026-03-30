/**
 * Panel hiển thị trạng thái không có dữ liệu với icon và message.
 */
package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class EmptyStatePanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel descriptionLabel;

    /**
     * Khởi tạo empty state panel với message mặc định.
     */
    public EmptyStatePanel() {
        this("Không có dữ liệu", "Chưa có dữ liệu để hiển thị.");
    }

    /**
     * Khởi tạo empty state panel với title và description.
     */
    public EmptyStatePanel(String title, String description) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(AppColors.CARD_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(
                        AppSpacing.PADDING_LARGE * 2,
                        AppSpacing.PADDING_LARGE,
                        AppSpacing.PADDING_LARGE * 2,
                        AppSpacing.PADDING_LARGE
                )
        ));

        // Icon panel
        EmptyIconPanel iconPanel = new EmptyIconPanel();
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(AppFonts.H3);
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Description
        descriptionLabel = new JLabel(description, SwingConstants.CENTER);
        descriptionLabel.setFont(AppFonts.BODY);
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalGlue());
        add(iconPanel);
        add(Box.createVerticalStrut(AppSpacing.PADDING_NORMAL));
        add(titleLabel);
        add(Box.createVerticalStrut(AppSpacing.COMPONENT_GAP));
        add(descriptionLabel);
        add(Box.createVerticalGlue());
    }

    /**
     * Cập nhật title.
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Cập nhật description.
     */
    public void setDescription(String description) {
        descriptionLabel.setText(description);
    }

    /**
     * Panel vẽ icon empty state đơn giản.
     */
    private static class EmptyIconPanel extends JPanel {
        private static final int SIZE = 64;

        EmptyIconPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(SIZE, SIZE));
            setMaximumSize(new Dimension(SIZE, SIZE));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = (getWidth() - SIZE) / 2;
            int y = (getHeight() - SIZE) / 2;

            // Vẽ hộp trống
            g2d.setColor(AppColors.INPUT_BORDER);
            g2d.setStroke(new java.awt.BasicStroke(2f));
            g2d.drawRoundRect(x + 8, y + 8, SIZE - 16, SIZE - 16, 8, 8);

            // Vẽ 3 đường ngang trong hộp
            g2d.drawLine(x + 16, y + 20, x + SIZE - 16, y + 20);
            g2d.drawLine(x + 16, y + 32, x + SIZE - 16, y + 32);
            g2d.drawLine(x + 16, y + 44, x + SIZE - 24, y + 44);

            g2d.dispose();
        }
    }
}

