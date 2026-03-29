/**
 * Khung giao diện quản trị cho dashboard.
 */
package com.qlsv.view.admin;

import com.qlsv.controller.LoginController;
import com.qlsv.controller.UserController;
import com.qlsv.model.User;
import com.qlsv.navigation.AppNavigator;
import com.qlsv.utils.DialogUtil;
import com.qlsv.view.auth.ChangePasswordDialog;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BaseFrame;
import com.qlsv.view.common.BasePanel;
import com.qlsv.view.common.SidebarMenu;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;

public class AdminDashboardFrame extends BaseFrame {

    private final LoginController loginController;
    private final UserController userController = new UserController();
    private final AppNavigator navigator;

    /**
     * Khởi tạo admin dashboard.
     */
    public AdminDashboardFrame(User user, AppNavigator navigator) {
        this(user, navigator, new LoginController());
    }

    /**
     * Khởi tạo admin dashboard.
     */
    AdminDashboardFrame(User user, AppNavigator navigator, LoginController loginController) {
        super("Bảng điều khiển - Quản trị");
        this.navigator = navigator;
        this.loginController = loginController;
        initComponents(user);
    }

    /**
     * Khởi tạo các thành phần giao diện cho khung quản trị viên chính.
     */
    private void initComponents(User user) {
        JButton changePasswordButton = new JButton("Đổi MK");
        configureHeaderButton(changePasswordButton);
        changePasswordButton.addActionListener(event -> openAdminChangePasswordDialog());

        JButton logoutButton = new JButton("Đăng xuất");
        configureHeaderButton(logoutButton);
        logoutButton.addActionListener(event -> {
            loginController.logout();
            navigator.showLogin();
            dispose();
        });

        JPanel headerPanel = createHeader(user.getFullName() + " - " + user.getRole().getDisplayName(), logoutButton);
        attachHeaderAction(headerPanel, changePasswordButton, true);

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
                "Trang quản trị",
                "");
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.DASHBOARD, "home", "Tổng quan", cardLayout, contentPanel, "home", homePanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.USER_GROUP, "students", "Quản lý sinh viên", cardLayout, contentPanel, "students", studentManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.LECTURERS, "lecturers", "Quản lý giảng viên", cardLayout, contentPanel, "lecturers", lecturerManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.BUILDING, "faculties", "Quản lý khoa", cardLayout, contentPanel, "faculties", facultyManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.CLASSES, "classes", "Quản lý lớp", cardLayout, contentPanel, "classes", classRoomManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.ROOMS, "rooms", "Quản lý phòng", cardLayout, contentPanel, "rooms", roomManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.BOOK, "subjects", "Quản lý môn học", cardLayout, contentPanel, "subjects", subjectManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.SECTIONS, "sections", "Quản lý học phần", cardLayout, contentPanel, "sections", courseSectionManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.ENROLLMENT, "enrollments", "Quản lý đăng ký", cardLayout, contentPanel, "enrollments", enrollmentManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.SCORES, "scores", "Quản lý điểm", cardLayout, contentPanel, "scores", scoreManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.SCHEDULE, "schedules", "Quản lý lịch học", cardLayout, contentPanel, "schedules", scheduleManagementPanel);
        registerMenuItem(sidebarMenu, com.qlsv.utils.IconUtil.IconType.REPORTS, "reports", "Báo cáo", cardLayout, contentPanel, "reports", reportManagementPanel);
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
        add(createFooter("Trạng thái: Quản trị viên đang đăng nhập | Hệ thống sẵn sàng."), BorderLayout.SOUTH);
    }

    /**
     * Đăng ký một mục vào menu sidebar và liên kết với panel tương ứng trong
     * CardLayout.
     */
    private void registerMenuItem(
            SidebarMenu sidebarMenu,
            com.qlsv.utils.IconUtil.IconType iconType,
            String itemKey,
            String text,
            CardLayout cardLayout,
            JPanel contentPanel,
            String cardName,
            BasePanel panel) {
        sidebarMenu.addMenuItem(iconType, itemKey, text, () -> {
            panel.reloadData();
            cardLayout.show(contentPanel, cardName);
        });
    }

    /**
     * Cấu hình kiểu dáng (style) cho các nút bấm trên thanh tiêu đề.
     */
    private void configureHeaderButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(AppColors.SIDEBAR_BUTTON_HOVER);
        button.setForeground(AppColors.BUTTON_TEXT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
    }

    /**
     * Gắn một nút chức năng vào vị trí cụ thể trên thanh tiêu đề của khung.
     */
    private void attachHeaderAction(JPanel headerPanel, JButton actionButton, boolean insertBeforeLast) {
        if (headerPanel == null || actionButton == null) {
            return;
        }
        Component eastComponent = ((BorderLayout) headerPanel.getLayout()).getLayoutComponent(BorderLayout.EAST);
        if (!(eastComponent instanceof JPanel rightPanel)) {
            return;
        }
        int index = insertBeforeLast ? Math.max(1, rightPanel.getComponentCount() - 1) : rightPanel.getComponentCount();
        rightPanel.add(actionButton, index);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    /**
     * Hiển thị hộp thoại và xử lý yêu cầu đổi mật khẩu của quản trị viên.
     */
    private void openAdminChangePasswordDialog() {
        ChangePasswordDialog.PasswordChangeRequest request = ChangePasswordDialog.showSelfChangeDialog(
                this,
                "Đổi mật khẩu quản trị viên");
        if (request == null) {
            return;
        }

        try {
            userController.changeCurrentPassword(
                    request.currentPassword(),
                    request.newPassword(),
                    request.confirmPassword());
            DialogUtil.showInfo(this, "Đổi mật khẩu quản trị viên thành công.");
        } catch (Exception exception) {
            DialogUtil.showError(this, exception.getMessage());
        }
    }
}
