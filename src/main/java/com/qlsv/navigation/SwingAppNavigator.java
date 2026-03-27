package com.qlsv.navigation;

import com.qlsv.controller.DashboardController;
import com.qlsv.model.User;
import com.qlsv.view.admin.AdminDashboardFrame;
import com.qlsv.view.auth.LoginFrame;
import com.qlsv.view.lecturer.LecturerDashboardFrame;
import com.qlsv.view.student.StudentDashboardFrame;

import javax.swing.JFrame;

public class SwingAppNavigator implements AppNavigator {

    private final DashboardController dashboardController = new DashboardController();

    @Override
    public void showLogin() {
        new LoginFrame(this).setVisible(true);
    }

    @Override
    public void showDashboard(User user) {
        createDashboardFrame(user).setVisible(true);
    }

    private JFrame createDashboardFrame(User user) {
        return switch (dashboardController.resolveDashboard(user)) {
            case ADMIN -> new AdminDashboardFrame(user, this);
            case LECTURER -> new LecturerDashboardFrame(user, this);
            case STUDENT -> new StudentDashboardFrame(user, this);
        };
    }
}
