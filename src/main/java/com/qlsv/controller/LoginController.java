package com.qlsv.controller;

import com.qlsv.model.User;
import com.qlsv.service.AuthService;

import javax.swing.JFrame;

public class LoginController {

    private final AuthService authService = new AuthService();
    private final DashboardController dashboardController = new DashboardController();

    public User login(String username, String password) {
        return authService.login(username, password);
    }

    public JFrame openDashboard(User user) {
        return dashboardController.openDashboard(user);
    }

    public void logout() {
        authService.logout();
    }
}
