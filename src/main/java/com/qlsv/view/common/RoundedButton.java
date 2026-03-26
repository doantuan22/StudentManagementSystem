package com.qlsv.view.common;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedButton extends JButton {
    private final int radius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setBackground(AppColors.LOGIN_PRIMARY);
        setForeground(Color.WHITE);
        setFont(getFont().deriveFont(Font.BOLD, 14f));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            graphics2D.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            graphics2D.setColor(AppColors.LOGIN_PRIMARY_HOVER);
        } else {
            graphics2D.setColor(getBackground());
        }

        graphics2D.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        super.paintComponent(graphics2D);
        graphics2D.dispose();
    }
}
