/**
 * Ô nhập bo góc dùng cho màn hình đăng nhập.
 * @deprecated Sử dụng CustomTextField với setRounded(true) thay thế
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

@Deprecated
public class RoundedTextField extends JTextField {
    private final int radius;
    private boolean focused;

    /**
     * Khởi tạo trường bo góc văn bản.
     * @deprecated Sử dụng CustomTextField thay thế
     */
    @Deprecated
    public RoundedTextField(int radius) {
        super();
        this.radius = radius;
        setOpaque(false);
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(8, 14, 8, 14));
        addFocusListener(new FocusAdapter() {
            /**
             * Xử lý focus gained.
             */
            @Override
            public void focusGained(FocusEvent event) {
                focused = true;
                repaint();
            }

            /**
             * Xử lý focus lost.
             */
            @Override
            public void focusLost(FocusEvent event) {
                focused = false;
                repaint();
            }
        });
    }

    /**
     * Xử lý paint component.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(getBackground());
        graphics2D.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        graphics2D.dispose();
        super.paintComponent(graphics);
    }

    /**
     * Xử lý paint border.
     */
    @Override
    protected void paintBorder(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(focused ? AppColors.LOGIN_PRIMARY : AppColors.INPUT_BORDER);
        graphics2D.setStroke(new BasicStroke(focused ? 1.6f : 1f));
        graphics2D.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        graphics2D.dispose();
    }
}
