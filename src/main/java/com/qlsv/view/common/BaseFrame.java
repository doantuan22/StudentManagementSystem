package com.qlsv.view.common;

import com.qlsv.config.AppConfig;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class BaseFrame extends JFrame {

    protected BaseFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
    }

    protected JPanel createHeader(String userInfo, JButton logoutButton) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        headerPanel.setBackground(new Color(33, 37, 41));

        javax.swing.JLabel titleLabel = new javax.swing.JLabel(AppConfig.getAppName());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);
        javax.swing.JLabel userLabel = new javax.swing.JLabel(userInfo);
        userLabel.setForeground(Color.WHITE);
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
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        JLabel statusLabel = new JLabel(statusText);
        footerPanel.add(statusLabel);
        return footerPanel;
    }

    protected JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(JButton.LEFT);
        return button;
    }
}
