/**
 * Màn hình quản trị cho tổng quan.
 */
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

    /**
     * Khởi tạo tổng quan quản trị.
     */
    public AdminHomePanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Bảng điều khiển quản trị");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel descriptionLabel = new JLabel("Thống kê tổng quan dữ liệu hệ thống.");
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JLabel noteLabel = new JLabel("Thông tin được cập nhật theo thời gian thực từ cơ sở dữ liệu.");
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

    /**
     * Làm mới dữ liệu đang hiển thị.
     */
    @Override
    public void reloadData() {
        statisticsPanel.reloadStatistics();
    }

    /**
     * Thiết lập nút primary.
     */
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
