package com.qlsv.view.student;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.view.auth.LoginFrame;
import com.qlsv.view.common.BaseFrame;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;

public class StudentDashboardFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public StudentDashboardFrame(User user) {
        super("Student Dashboard");
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
        contentPanel.add(new StudentProfilePanel(), "profile");
        contentPanel.add(new StudentEnrollmentPanel(), "enrollment");
        contentPanel.add(new StudentRegisteredSubjectsPanel(), "registered");
        contentPanel.add(new StudentScorePanel(), "scores");
        contentPanel.add(new StudentSchedulePanel(), "schedule");

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        menuPanel.add(buildMenuButton("Thong tin ca nhan", cardLayout, contentPanel, "profile"));
        menuPanel.add(buildMenuButton("Dang ky hoc phan", cardLayout, contentPanel, "enrollment"));
        menuPanel.add(buildMenuButton("Hoc phan da dang ky", cardLayout, contentPanel, "registered"));
        menuPanel.add(buildMenuButton("Xem diem", cardLayout, contentPanel, "scores"));
        menuPanel.add(buildMenuButton("Lich hoc", cardLayout, contentPanel, "schedule"));

        JPanel bodyPanel = new JPanel(new BorderLayout(12, 12));
        bodyPanel.add(menuPanel, BorderLayout.WEST);
        bodyPanel.add(contentPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(createFooter("Trang thai: STUDENT dang dang nhap | Co the dang ky hoc phan, xem diem va lich hoc"), BorderLayout.SOUTH);
    }

    private JButton buildMenuButton(String text, CardLayout cardLayout, JPanel contentPanel, String cardName) {
        JButton button = createMenuButton(text);
        button.addActionListener(event -> cardLayout.show(contentPanel, cardName));
        return button;
    }
}
