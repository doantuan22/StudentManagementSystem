/**
 * Nút Swing tùy biến theo theme ứng dụng với hỗ trợ nhiều style.
 */
package com.qlsv.view.common;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class CustomButton extends JButton {

    /**
     * Button style variants.
     */
    public enum ButtonStyle {
        PRIMARY(AppColors.BUTTON_PRIMARY),
        SUCCESS(AppColors.BUTTON_SUCCESS),
        WARNING(AppColors.BUTTON_WARNING),
        DANGER(AppColors.BUTTON_DANGER),
        NEUTRAL(AppColors.BUTTON_NEUTRAL),
        LOGIN(AppColors.LOGIN_PRIMARY);

        private final Color color;

        ButtonStyle(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }

    private ButtonStyle style = ButtonStyle.PRIMARY;
    private boolean rounded = false;
    private int radius = AppSpacing.RADIUS_NORMAL;

    /**
     * Khởi tạo nút tùy biến.
     */
    public CustomButton() {
        super();
        init();
    }

    /**
     * Khởi tạo nút tùy biến với text.
     */
    public CustomButton(String text) {
        super(text);
        init();
    }

    /**
     * Khởi tạo nút tùy biến với text và style.
     */
    public CustomButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        init();
    }

    /**
     * Khởi tạo nút tùy biến với text và icon.
     */
    public CustomButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    /**
     * Khởi tạo properties chung.
     */
    private void init() {
        setFont(AppFonts.BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusPainted(false);
        
        if (rounded) {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }
        
        AppTheme.applyTree(this);
    }

    /**
     * Set button style.
     */
    public void setButtonStyle(ButtonStyle style) {
        this.style = style;
        setBackground(style.getColor());
    }

    /**
     * Enable rounded corners (for login screen).
     */
    public void setRounded(boolean rounded) {
        this.rounded = rounded;
        if (rounded) {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
        }
        repaint();
    }

    /**
     * Set border radius (only for rounded buttons).
     */
    public void setRadius(int radius) {
        this.radius = radius;
        repaint();
    }

    /**
     * Paint component với rounded corners nếu cần.
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (!rounded) {
            super.paintComponent(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor = style.getColor();
        if (!isEnabled()) {
            bgColor = AppColors.BUTTON_DISABLED;
        } else if (getModel().isPressed()) {
            bgColor = adjustColor(bgColor, 0.88f);
        } else if (getModel().isRollover()) {
            bgColor = adjustColor(bgColor, 1.07f);
        }

        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2d.dispose();

        super.paintComponent(g);
    }

    /**
     * Adjust color brightness.
     */
    private Color adjustColor(Color color, float factor) {
        int r = Math.min(255, Math.max(0, Math.round(color.getRed() * factor)));
        int g = Math.min(255, Math.max(0, Math.round(color.getGreen() * factor)));
        int b = Math.min(255, Math.max(0, Math.round(color.getBlue() * factor)));
        return new Color(r, g, b);
    }
}
