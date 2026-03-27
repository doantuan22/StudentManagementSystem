package com.qlsv.view.student;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.navigation.AppNavigator;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BaseFrame;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.SidebarMenu;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

public class StudentDashboardFrame extends BaseFrame {

    private final LoginController loginController;
    private final AppNavigator navigator;

    public StudentDashboardFrame(User user, AppNavigator navigator) {
        this(user, navigator, new LoginController());
    }

    StudentDashboardFrame(User user, AppNavigator navigator, LoginController loginController) {
        super("Trang sinh viên");
        this.navigator = navigator;
        this.loginController = loginController;
        initComponents(user);
    }

    private void initComponents(User user) {
        JButton logoutButton = new JButton("Đăng xuất");
        styleHeaderActionButton(logoutButton);
        logoutButton.addActionListener(event -> {
            loginController.logout();
            navigator.showLogin();
            dispose();
        });

        JPanel headerPanel = createHeader(user.getFullName() + " - " + user.getRole().getDisplayName(), logoutButton);

        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);

        StudentHomePanel homePanel = new StudentHomePanel();
        StudentProfilePanel profilePanel = new StudentProfilePanel();
        StudentEnrollmentPanel enrollmentPanel = new StudentEnrollmentPanel();
        StudentRegisteredSubjectsPanel registeredPanel = new StudentRegisteredSubjectsPanel();
        StudentScorePanel scorePanel = new StudentScorePanel();
        StudentSchedulePanel schedulePanel = new StudentSchedulePanel();

        contentPanel.add(homePanel, "home");
        contentPanel.add(profilePanel, "profile");
        contentPanel.add(enrollmentPanel, "enrollment");
        contentPanel.add(registeredPanel, "registered");
        contentPanel.add(scorePanel, "scores");
        contentPanel.add(schedulePanel, "schedule");

        SidebarMenu sidebarMenu = new SidebarMenu(
                "Sinh viên",
                ""
        );
        registerMenuItem(sidebarMenu, "home", "Tổng quan", cardLayout, contentPanel, "home", homePanel);
        registerMenuItem(sidebarMenu, "profile", "Thông tin cá nhân", cardLayout, contentPanel, "profile", profilePanel);
        registerMenuItem(sidebarMenu, "enrollment", "Đăng ký học phần", cardLayout, contentPanel, "enrollment", enrollmentPanel);
        registerMenuItem(sidebarMenu, "registered", "Học phần đã đăng ký", cardLayout, contentPanel, "registered", registeredPanel);
        registerMenuItem(sidebarMenu, "scores", "Xem điểm", cardLayout, contentPanel, "scores", scorePanel);
        registerMenuItem(sidebarMenu, "schedule", "Lịch học", cardLayout, contentPanel, "schedule", schedulePanel);

        sidebarMenu.setActiveItem("home");
        cardLayout.show(contentPanel, "home");

        JPanel bodyPanel = new JPanel(new BorderLayout(18, 12));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        bodyPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        bodyPanel.add(sidebarMenu, BorderLayout.WEST);
        bodyPanel.add(contentPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(createFooter("Trạng thái: Sinh viên đang đăng nhập | Có thể đăng ký học phần, xem điểm và lịch học"), BorderLayout.SOUTH);
    }

    private void registerMenuItem(
            SidebarMenu sidebarMenu,
            String itemKey,
            String text,
            CardLayout cardLayout,
            JPanel contentPanel,
            String cardName,
            BasePanel panel
    ) {
        sidebarMenu.addMenuItem(itemKey, text, () -> {
            panel.reloadData();
            cardLayout.show(contentPanel, cardName);
        });
    }
}
