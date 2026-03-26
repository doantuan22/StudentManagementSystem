package com.qlsv.view.admin;

import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BasePanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

public class RoleManagementPanel extends BasePanel {

    public RoleManagementPanel() {
        setOpaque(true);
        setBackground(AppColors.CONTENT_BACKGROUND);

        JPanel cardPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        cardPanel.setOpaque(true);
        cardPanel.setBackground(AppColors.CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppColors.CARD_BORDER),
                BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        JLabel titleLabel = new JLabel("Quản lý vai trò");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(AppColors.CARD_VALUE_TEXT);

        JLabel statusLabel = new JLabel("Màn hình này đang được cập nhật để hoàn thiện luồng phân quyền.");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 13.5f));
        statusLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        JLabel noteLabel = new JLabel("Tạm thời chưa đưa module này vào luồng demo chính để tránh gây rối trải nghiệm.");
        noteLabel.setFont(noteLabel.getFont().deriveFont(Font.PLAIN, 13f));
        noteLabel.setForeground(AppColors.CARD_MUTED_TEXT);

        cardPanel.add(titleLabel);
        cardPanel.add(statusLabel);
        cardPanel.add(noteLabel);

        add(cardPanel, BorderLayout.NORTH);
    }
}
