package com.qlsv.view.lecturer;

import com.qlsv.controller.LoginController;
import com.qlsv.model.User;
import com.qlsv.view.auth.LoginFrame;
import com.qlsv.view.common.AppColors;
import com.qlsv.view.common.BaseFrame;
import com.qlsv.view.common.SidebarMenu;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

public class LecturerDashboardFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public LecturerDashboardFrame(User user) {
        super("Trang giảng viên");
        initComponents(user);
    }

    private void initComponents(User user) {
        JButton logoutButton = new JButton("Đăng xuất");
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

        SidebarMenu sidebarMenu = new SidebarMenu(
                "Không gian giảng viên",
                ""
        );
        registerMenuItem(sidebarMenu, "profile", "Thông tin cá nhân", cardLayout, contentPanel, "profile");
        registerMenuItem(sidebarMenu, "sections", "Học phần phụ trách", cardLayout, contentPanel, "sections");
        registerMenuItem(sidebarMenu, "students", "Sinh viên theo học phần", cardLayout, contentPanel, "students");
        registerMenuItem(sidebarMenu, "scores", "Nhập hoặc xem điểm", cardLayout, contentPanel, "scores");
        registerMenuItem(sidebarMenu, "schedule", "Lịch dạy", cardLayout, contentPanel, "schedule");
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
        add(createFooter("Trạng thái: Giảng viên đang đăng nhập | Đang quản lý các học phần được phân công"), BorderLayout.SOUTH);
    }

    private void registerMenuItem(
            SidebarMenu sidebarMenu,
            String itemKey,
            String text,
            CardLayout cardLayout,
            JPanel contentPanel,
            String cardName
    ) {
        sidebarMenu.addMenuItem(itemKey, text, () -> cardLayout.show(contentPanel, cardName));
    }
}
