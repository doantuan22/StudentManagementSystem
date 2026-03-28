package com.qlsv.view.common;

import com.qlsv.config.AppConfig;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class BaseFrame extends JFrame {

    protected BaseFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
        getContentPane().setBackground(AppColors.CONTENT_BACKGROUND);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        AppTheme.applyTree(this);
    }

    protected JPanel createHeader(String userInfo, JButton logoutButton) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        headerPanel.setBackground(AppColors.SIDEBAR_BACKGROUND);

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(AppConfig.getAppName());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        javax.swing.JLabel userLabel = new javax.swing.JLabel(userInfo);
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(userLabel.getFont().deriveFont(13.5f));
        rightPanel.add(userLabel);
        if (logoutButton != null) {
            rightPanel.add(logoutButton);
        }

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        return headerPanel;
    }

    protected JPanel createFooter(String statusText) {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        footerPanel.setOpaque(true);
        footerPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(4, 16, 8, 16)
        ));
        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setForeground(AppColors.CARD_MUTED_TEXT);
        footerPanel.add(statusLabel);
        return footerPanel;
    }

    protected JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(JButton.LEFT);
        return button;
    }

    protected void styleHeaderActionButton(JButton button) {
        button.setBackground(AppColors.SIDEBAR_BUTTON_HOVER);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }
}
