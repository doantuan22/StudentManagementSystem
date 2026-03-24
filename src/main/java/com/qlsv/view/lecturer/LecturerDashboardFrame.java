package com.qlsv.view.lecturer;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.view.auth.LoginFrame;
import com.qlsv.view.common.BaseFrame;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;

public class LecturerDashboardFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public LecturerDashboardFrame(User user) {
        super("Lecturer Dashboard");
        initComponents(user);
    }

    private void initComponents(User user) {
        JButton logoutButton = new JButton("Dang xuat");
        logoutButton.addActionListener(event -> {
            loginController.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel headerPanel = createHeader(user.getFullName() + " - " + user.getRole().getDisplayName(), logoutButton);

        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        contentPanel.add(new LecturerProfilePanel(), "profile");
        contentPanel.add(new LecturerCourseSectionPanel(), "sections");
        contentPanel.add(new LecturerStudentListPanel(), "students");
        contentPanel.add(new LecturerScorePanel(), "scores");
        contentPanel.add(new LecturerSchedulePanel(), "schedule");

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        menuPanel.add(buildMenuButton("Thong tin ca nhan", cardLayout, contentPanel, "profile"));
        menuPanel.add(buildMenuButton("Hoc phan phu trach", cardLayout, contentPanel, "sections"));
        menuPanel.add(buildMenuButton("Sinh vien hoc phan", cardLayout, contentPanel, "students"));
        menuPanel.add(buildMenuButton("Nhap / xem diem", cardLayout, contentPanel, "scores"));
        menuPanel.add(buildMenuButton("Lich day", cardLayout, contentPanel, "schedule"));

        JPanel bodyPanel = new JPanel(new BorderLayout(12, 12));
        bodyPanel.add(menuPanel, BorderLayout.WEST);
        bodyPanel.add(contentPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(createFooter("Trang thai: LECTURER dang dang nhap | Dang quan ly hoc phan duoc phan cong"), BorderLayout.SOUTH);
    }

    private JButton buildMenuButton(String text, CardLayout cardLayout, JPanel contentPanel, String cardName) {
        JButton button = createMenuButton(text);
        button.addActionListener(event -> cardLayout.show(contentPanel, cardName));
        return button;
    }
}
