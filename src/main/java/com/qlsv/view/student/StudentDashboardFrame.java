package com.qlsv.view.student;

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

public class StudentDashboardFrame extends BaseFrame {

    private final LoginController loginController = new LoginController();

    public StudentDashboardFrame(User user) {
        super("Trang sinh viên");
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
        contentPanel.add(new StudentProfilePanel(), "profile");
        contentPanel.add(new StudentEnrollmentPanel(), "enrollment");
        contentPanel.add(new StudentRegisteredSubjectsPanel(), "registered");
        contentPanel.add(new StudentScorePanel(), "scores");
        contentPanel.add(new StudentSchedulePanel(), "schedule");

        SidebarMenu sidebarMenu = new SidebarMenu(
                "Không gian sinh viên",
                "Các tác vụ học tập được đặt gọn trong một sidebar bên trái để theo dõi và chuyển màn hình thuận tiện."
        );
        registerMenuItem(sidebarMenu, "profile", "Thông tin cá nhân", cardLayout, contentPanel, "profile");
        registerMenuItem(sidebarMenu, "enrollment", "Đăng ký học phần", cardLayout, contentPanel, "enrollment");
        registerMenuItem(sidebarMenu, "registered", "Học phần đã đăng ký", cardLayout, contentPanel, "registered");
        registerMenuItem(sidebarMenu, "scores", "Xem điểm", cardLayout, contentPanel, "scores");
        registerMenuItem(sidebarMenu, "schedule", "Lịch học", cardLayout, contentPanel, "schedule");
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
        add(createFooter("Trạng thái: Sinh viên đang đăng nhập | Có thể đăng ký học phần, xem điểm và lịch học"), BorderLayout.SOUTH);
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
