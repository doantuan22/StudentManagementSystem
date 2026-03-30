/**
 * Ô nhập tùy biến theo theme ứng dụng với hỗ trợ rounded corners.
 */
package com.qlsv.view.common;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class CustomTextField extends JTextField {

    private boolean rounded = false;
    private int radius = AppSpacing.RADIUS_NORMAL;
    private boolean focused = false;

    /**
     * Khởi tạo trường tùy biến văn bản.
     */
    public CustomTextField() {
        super();
        init();
    }

    /**
     * Khởi tạo trường tùy biến văn bản với columns.
     */
    public CustomTextField(int columns) {
        super(columns);
        init();
    }

    /**
     * Khởi tạo properties chung.
     */
    private void init() {
        setFont(AppFonts.INPUT);
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                focused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                focused = false;
                repaint();
            }
        });
        
        AppTheme.applyTree(this);
    }

    /**
     * Enable rounded corners (for login screen).
     */
    public void setRounded(boolean rounded) {
        this.rounded = rounded;
        if (rounded) {
            setOpaque(false);
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(AppSpacing.SM, AppSpacing.MD + 2, AppSpacing.SM, AppSpacing.MD + 2));
        }
        repaint();
    }

    /**
     * Set border radius (only for rounded fields).
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
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2d.dispose();

        super.paintComponent(g);
    }

    /**
     * Paint border với rounded corners nếu cần.
     */
    @Override
    protected void paintBorder(Graphics g) {
        if (!rounded) {
            super.paintBorder(g);
            return;
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(focused ? AppColors.INPUT_BORDER_FOCUS : AppColors.INPUT_BORDER);
        g2d.setStroke(new BasicStroke(focused ? 1.6f : 1f));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2d.dispose();
    }
}
