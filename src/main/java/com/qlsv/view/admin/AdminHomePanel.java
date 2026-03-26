package com.qlsv.view.admin;

import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

public class AdminHomePanel extends BasePanel {

    private final SystemStatisticsPanel statisticsPanel = new SystemStatisticsPanel();

    public AdminHomePanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Tổng quan quản trị");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel descriptionLabel = new JLabel("Theo dõi nhanh quy mô dữ liệu hiện tại trong hệ thống.");
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JLabel noteLabel = new JLabel("Các chỉ số bên dưới giữ nguyên nguồn dữ liệu đang được lấy lên từ hệ thống.");
        noteLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel introPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        introPanel.setOpaque(false);
        introPanel.add(titleLabel);
        introPanel.add(descriptionLabel);
        introPanel.add(noteLabel);

        JButton reloadButton = new JButton("Tải lại thống kê");
        configurePrimaryButton(reloadButton);
        reloadButton.addActionListener(event -> statisticsPanel.reloadStatistics());

        JPanel headerPanel = new JPanel(new BorderLayout(16, 12));
        headerPanel.setOpaque(false);
        headerPanel.add(introPanel, BorderLayout.CENTER);
        JPanel buttonWrapper = new JPanel();
        buttonWrapper.setOpaque(false); 
        buttonWrapper.add(reloadButton);

        headerPanel.add(buttonWrapper, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(statisticsPanel, BorderLayout.CENTER);
    }

    private void configurePrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.BUTTON_PRIMARY);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(168, 42));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
    }
}
