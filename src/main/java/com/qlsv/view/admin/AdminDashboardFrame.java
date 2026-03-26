package com.qlsv.view.admin;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.view.auth.LoginFrame;
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

public class AdminDashboardFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public AdminDashboardFrame(User user) {
        super("Trang quản trị");
        initComponents(user);
    }

    private void initComponents(User user) {
        JButton logoutButton = new JButton("Đăng xuất");
        configureHeaderButton(logoutButton);
        logoutButton.addActionListener(event -> {
            loginController.logout();
            new LoginFrame().setVisible(true);
            dispose();
        });

        JPanel headerPanel = createHeader(user.getFullName() + " - " + user.getRole().getDisplayName(), logoutButton);

        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        AdminHomePanel homePanel = new AdminHomePanel();
        StudentManagementPanel studentManagementPanel = new StudentManagementPanel();
        LecturerManagementPanel lecturerManagementPanel = new LecturerManagementPanel();
        FacultyManagementPanel facultyManagementPanel = new FacultyManagementPanel();
        ClassRoomManagementPanel classRoomManagementPanel = new ClassRoomManagementPanel();
        RoomManagementPanel roomManagementPanel = new RoomManagementPanel();
        SubjectManagementPanel subjectManagementPanel = new SubjectManagementPanel();
        CourseSectionManagementPanel courseSectionManagementPanel = new CourseSectionManagementPanel();
        EnrollmentManagementPanel enrollmentManagementPanel = new EnrollmentManagementPanel();
        ScoreManagementPanel scoreManagementPanel = new ScoreManagementPanel();
        ScheduleManagementPanel scheduleManagementPanel = new ScheduleManagementPanel();
        ReportManagementPanel reportManagementPanel = new ReportManagementPanel();

        contentPanel.add(homePanel, "home");
        contentPanel.add(studentManagementPanel, "students");
        contentPanel.add(lecturerManagementPanel, "lecturers");
        contentPanel.add(facultyManagementPanel, "faculties");
        contentPanel.add(classRoomManagementPanel, "classes");
        contentPanel.add(roomManagementPanel, "rooms");
        contentPanel.add(subjectManagementPanel, "subjects");
        contentPanel.add(courseSectionManagementPanel, "sections");
        contentPanel.add(enrollmentManagementPanel, "enrollments");
        contentPanel.add(scoreManagementPanel, "scores");
        contentPanel.add(scheduleManagementPanel, "schedules");
        contentPanel.add(reportManagementPanel, "reports");
        contentPanel.setBackground(AppColors.CONTENT_BACKGROUND);

        SidebarMenu sidebarMenu = new SidebarMenu(
                "Quản trị hệ thống",
                "Truy cập nhanh các màn hình quản lý dành cho quản trị viên."
        );
        registerMenuItem(sidebarMenu, "home", "Tổng quan", cardLayout, contentPanel, "home", homePanel);
        registerMenuItem(sidebarMenu, "students", "Quản lý sinh viên", cardLayout, contentPanel, "students", studentManagementPanel);
        registerMenuItem(sidebarMenu, "lecturers", "Quản lý giảng viên", cardLayout, contentPanel, "lecturers", lecturerManagementPanel);
        registerMenuItem(sidebarMenu, "faculties", "Quản lý khoa", cardLayout, contentPanel, "faculties", facultyManagementPanel);
        registerMenuItem(sidebarMenu, "classes", "Quản lý lớp", cardLayout, contentPanel, "classes", classRoomManagementPanel);
        registerMenuItem(sidebarMenu, "rooms", "Quản lý phòng", cardLayout, contentPanel, "rooms", roomManagementPanel);
        registerMenuItem(sidebarMenu, "subjects", "Quản lý môn học", cardLayout, contentPanel, "subjects", subjectManagementPanel);
        registerMenuItem(sidebarMenu, "sections", "Quản lý học phần", cardLayout, contentPanel, "sections", courseSectionManagementPanel);
        registerMenuItem(sidebarMenu, "enrollments", "Quản lý đăng ký", cardLayout, contentPanel, "enrollments", enrollmentManagementPanel);
        registerMenuItem(sidebarMenu, "scores", "Quản lý điểm", cardLayout, contentPanel, "scores", scoreManagementPanel);
        registerMenuItem(sidebarMenu, "schedules", "Quản lý lịch học", cardLayout, contentPanel, "schedules", scheduleManagementPanel);
        registerMenuItem(sidebarMenu, "reports", "Báo cáo", cardLayout, contentPanel, "reports", reportManagementPanel);
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
        add(createFooter("Trạng thái: Quản trị viên đang đăng nhập | Bảng điều khiển đã sẵn sàng"), BorderLayout.SOUTH);
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
