package com.qlsv.view.admin;

import com.qlsv.view.common.BasePanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class AdminHomePanel extends BasePanel {

    public AdminHomePanel() {
        JLabel titleLabel = new JLabel("Tong quan quan tri");
        titleLabel.setFont(titleLabel.getFont().deriveFont(20f));

        JPanel introPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        introPanel.add(titleLabel);
        introPanel.add(new JLabel("Trang tong quan dung de kiem tra nhanh quy mo du lieu va truy cap cac module quan tri."));
        introPanel.add(new JLabel("Du lieu ben duoi duoc nap tu database moi lan mo dashboard."));

        add(introPanel, BorderLayout.NORTH);
        add(new SystemStatisticsPanel(), BorderLayout.CENTER);
    }
}
