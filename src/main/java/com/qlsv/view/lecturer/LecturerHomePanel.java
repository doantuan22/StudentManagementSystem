/**
 * Màn hình giảng viên cho tổng quan.
 */
package com.qlsv.view.lecturer;

import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class LecturerHomePanel extends BasePanel {

    /**
     * Khởi tạo giảng viên tổng quan.
     */
    public LecturerHomePanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);

        JLabel titleLabel = new JLabel("Chào mừng Giảng viên");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel descriptionLabel = new JLabel("Hệ thống quản lý sinh viên - Không gian làm việc của Giảng viên.");
        descriptionLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JPanel introPanel = new JPanel(new GridLayout(0, 1, 0, 6));
        introPanel.setOpaque(false);
        introPanel.add(titleLabel);
        introPanel.add(descriptionLabel);

        JPanel infoPanel = new JPanel(new GridLayout(1, 1, 20, 20));
        infoPanel.setOpaque(false);
        
        JPanel welcomeCard = createInfoCard("Thông tin", "Sử dụng menu bên trái để quản lý học phần, danh sách sinh viên và nhập điểm.");
        infoPanel.add(welcomeCard);

        add(introPanel, BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }

    /**
     * Tạo card thông tin.
     */
    private JPanel createInfoCard(String title, String content) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(AppColors.CARD_BACKGROUND);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(AppColors.CARD_BORDER, 1),
                javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppColors.CARD_TITLE_TEXT);

        JLabel contentLabel = new JLabel("<html>" + content + "</html>");
        contentLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentLabel, BorderLayout.CENTER);

        return card;
    }
}

