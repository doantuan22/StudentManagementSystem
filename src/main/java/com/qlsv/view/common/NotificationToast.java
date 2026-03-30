/**
 * Hệ thống notification toast nhẹ, hiển thị thông báo tự động ẩn.
 */
package com.qlsv.view.common;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.util.concurrent.atomic.AtomicInteger;

public final class NotificationToast {

    private static final int TOAST_WIDTH = 360;
    private static final int TOAST_HEIGHT = 56;
    private static final int TOAST_MARGIN = AppSpacing.PADDING_NORMAL;
    private static final int DISPLAY_DURATION_MS = 3000;
    private static final AtomicInteger yOffset = new AtomicInteger(TOAST_MARGIN);

    private NotificationToast() {
    }

    /**
     * Hiển thị notification success.
     */
    public static void showSuccess(Component parent, String message) {
        show(parent, message, NotificationType.SUCCESS);
    }

    /**
     * Hiển thị notification error.
     */
    public static void showError(Component parent, String message) {
        show(parent, message, NotificationType.ERROR);
    }

    /**
     * Hiển thị notification info.
     */
    public static void showInfo(Component parent, String message) {
        show(parent, message, NotificationType.INFO);
    }

    /**
     * Hiển thị notification warning.
     */
    public static void showWarning(Component parent, String message) {
        show(parent, message, NotificationType.WARNING);
    }

    /**
     * Hiển thị toast notification.
     */
    private static void show(Component parent, String message, NotificationType type) {
        Window window = javax.swing.SwingUtilities.getWindowAncestor(parent);
        if (window == null) {
            return;
        }

        JPanel toastPanel = createToastPanel(message, type);
        javax.swing.JWindow toastWindow = new javax.swing.JWindow(window);
        toastWindow.setContentPane(toastPanel);
        toastWindow.setSize(TOAST_WIDTH, TOAST_HEIGHT);
        toastWindow.setAlwaysOnTop(true);

        // Position ở góc trên bên phải
        Point windowLocation = window.getLocationOnScreen();
        Dimension windowSize = window.getSize();
        int currentOffset = yOffset.getAndAdd(TOAST_HEIGHT + AppSpacing.SM);
        
        int x = windowLocation.x + windowSize.width - TOAST_WIDTH - TOAST_MARGIN;
        int y = windowLocation.y + currentOffset;
        toastWindow.setLocation(x, y);

        toastWindow.setVisible(true);

        // Tự động ẩn sau DISPLAY_DURATION_MS
        Timer timer = new Timer(DISPLAY_DURATION_MS, event -> {
            toastWindow.dispose();
            yOffset.addAndGet(-(TOAST_HEIGHT + AppSpacing.SM));
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Tạo toast panel.
     */
    private static JPanel createToastPanel(String message, NotificationType type) {
        JPanel panel = new JPanel(new BorderLayout(AppSpacing.MD, 0));
        panel.setOpaque(true);
        panel.setBackground(type.backgroundColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(type.borderColor, 1),
                BorderFactory.createEmptyBorder(
                        AppSpacing.MD,
                        AppSpacing.PADDING_NORMAL,
                        AppSpacing.MD,
                        AppSpacing.PADDING_NORMAL
                )
        ));

        JLabel iconLabel = new JLabel(type.icon);
        iconLabel.setFont(AppFonts.H3);
        iconLabel.setForeground(type.iconColor);
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setPreferredSize(new Dimension(32, 32));

        JLabel messageLabel = new JLabel("<html>" + message + "</html>");
        messageLabel.setFont(AppFonts.BODY);
        messageLabel.setForeground(type.textColor);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(messageLabel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Enum notification type.
     */
    private enum NotificationType {
        SUCCESS("✓", new Color(240, 253, 244), new Color(34, 197, 94), new Color(22, 163, 74), new Color(21, 128, 61)),
        ERROR("✕", new Color(254, 242, 242), new Color(239, 68, 68), new Color(220, 38, 38), new Color(185, 28, 28)),
        INFO("ℹ", new Color(239, 246, 255), new Color(59, 130, 246), new Color(37, 99, 235), new Color(29, 78, 216)),
        WARNING("⚠", new Color(254, 252, 232), new Color(234, 179, 8), new Color(202, 138, 4), new Color(161, 98, 7));

        final String icon;
        final Color backgroundColor;
        final Color borderColor;
        final Color iconColor;
        final Color textColor;

        NotificationType(String icon, Color backgroundColor, Color borderColor, Color iconColor, Color textColor) {
            this.icon = icon;
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.iconColor = iconColor;
            this.textColor = textColor;
        }
    }
}
