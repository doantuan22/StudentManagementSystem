/**
 * Panel hiển thị trạng thái đang tải dữ liệu với spinner animation.
 */
package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class LoadingPanel extends JPanel {

    private final JLabel messageLabel;
    private final SpinnerPanel spinnerPanel;

    /**
     * Khởi tạo loading panel với message mặc định.
     */
    public LoadingPanel() {
        this("Đang tải dữ liệu...");
    }

    /**
     * Khởi tạo loading panel với message tùy chỉnh.
     */
    public LoadingPanel(String message) {
        setLayout(new BorderLayout(0, AppSpacing.PADDING_NORMAL));
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

        spinnerPanel = new SpinnerPanel();
        
        messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(AppFonts.BODY_LARGE);
        messageLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        add(spinnerPanel, BorderLayout.CENTER);
        add(messageLabel, BorderLayout.SOUTH);
    }

    /**
     * Cập nhật message.
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Bắt đầu animation.
     */
    public void start() {
        spinnerPanel.start();
    }

    /**
     * Dừng animation.
     */
    public void stop() {
        spinnerPanel.stop();
    }

    /**
     * Panel vẽ spinner animation đơn giản.
     */
    private static class SpinnerPanel extends JPanel {
        private int angle = 0;
        private Timer timer;
        private static final int SIZE = 40;
        private static final int ARC_LENGTH = 90;

        SpinnerPanel() {
            setOpaque(false);
            setPreferredSize(new java.awt.Dimension(SIZE, SIZE));
            timer = new Timer(50, e -> {
                angle = (angle + 15) % 360;
                repaint();
            });
            timer.start();
        }

        void start() {
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        void stop() {
            if (timer.isRunning()) {
                timer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = (getWidth() - SIZE) / 2;
            int y = (getHeight() - SIZE) / 2;

            g2d.setColor(AppColors.INPUT_BORDER);
            g2d.drawArc(x, y, SIZE, SIZE, 0, 360);

            g2d.setColor(AppColors.BUTTON_PRIMARY);
            g2d.setStroke(new java.awt.BasicStroke(3f));
            g2d.drawArc(x, y, SIZE, SIZE, angle, ARC_LENGTH);

            g2d.dispose();
        }
    }
}

