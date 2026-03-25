package com.qlsv.view.admin;

import com.qlsv.view.common.BasePanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;

public class AdminHomePanel extends BasePanel {

    public AdminHomePanel() {
        JLabel titleLabel = new JLabel("Tổng quan quản trị");
        titleLabel.setFont(titleLabel.getFont().deriveFont(20f));

        JPanel introPanel = new JPanel(new GridLayout(0, 1, 0, 8));
        introPanel.add(titleLabel);
        introPanel.add(new JLabel("Trang tổng quan dùng để kiểm tra nhanh quy mô dữ liệu và truy cập các mô-đun quản trị."));
        introPanel.add(new JLabel("Dữ liệu bên dưới được nạp từ cơ sở dữ liệu mỗi lần mở bảng điều khiển."));

        add(introPanel, BorderLayout.NORTH);
        add(new SystemStatisticsPanel(), BorderLayout.CENTER);
    }
}
