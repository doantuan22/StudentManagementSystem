package com.qlsv.view.lecturer;

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
import java.awt.Cursor;

public class LecturerDashboardFrame extends BaseFrame {

    private final LoginController loginController;
    private final AppNavigator navigator;

    public LecturerDashboardFrame(User user, AppNavigator navigator) {
        this(user, navigator, new LoginController());
    }

    LecturerDashboardFrame(User user, AppNavigator navigator, LoginController loginController) {
        super("Bảng điều khiển - Giảng viên");
        this.navigator = navigator;
        this.loginController = loginController;
        initComponents(user);
    }

    private void initComponents(User user) {
        JButton logoutButton = new JButton("Đăng xuất");
        configureHeaderButton(logoutButton);
        logoutButton.addActionListener(event -> {
            loginController.logout();
            navigator.showLogin();
            dispose();
        });

        JPanel headerPanel = createHeader(user.getFullName() + " - " + user.getRole().getDisplayName(), logoutButton);

        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        LecturerProfilePanel profilePanel = new LecturerProfilePanel();
        LecturerCourseSectionPanel sectionPanel = new LecturerCourseSectionPanel();
        LecturerStudentListPanel studentListPanel = new LecturerStudentListPanel();
        LecturerScorePanel scorePanel = new LecturerScorePanel();
        LecturerSchedulePanel schedulePanel = new LecturerSchedulePanel();
        contentPanel.add(profilePanel, "profile");
        contentPanel.add(sectionPanel, "sections");
        contentPanel.add(studentListPanel, "students");
        contentPanel.add(scorePanel, "scores");
        contentPanel.add(schedulePanel, "schedule");
        contentPanel.setBackground(AppColors.CONTENT_BACKGROUND);

        SidebarMenu sidebarMenu = new SidebarMenu(
                "Giảng viên",
                ""
        );
        registerMenuItem(sidebarMenu, "profile", "Thông tin cá nhân", cardLayout, contentPanel, "profile", profilePanel);
        registerMenuItem(sidebarMenu, "sections", "Học phần phụ trách", cardLayout, contentPanel, "sections", sectionPanel);
        registerMenuItem(sidebarMenu, "students", "Danh sách sinh viên", cardLayout, contentPanel, "students", studentListPanel);
        registerMenuItem(sidebarMenu, "scores", "Nhập/Xem điểm ", cardLayout, contentPanel, "scores", scorePanel);
        registerMenuItem(sidebarMenu, "schedule", "Lịch dạy", cardLayout, contentPanel, "schedule", schedulePanel);
        sidebarMenu.setActiveItem("profile");
        cardLayout.show(contentPanel, "profile");

        JPanel bodyPanel = new JPanel(new BorderLayout(18, 12));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        bodyPanel.setBackground(AppColors.CONTENT_BACKGROUND);
        bodyPanel.add(sidebarMenu, BorderLayout.WEST);
        bodyPanel.add(contentPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
        add(createFooter("Trạng thái: Giảng viên đang đăng nhập | Hệ thống quản lý đào tạo."), BorderLayout.SOUTH);
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

    private void configureHeaderButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.SIDEBAR_BUTTON_HOVER);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }
}
