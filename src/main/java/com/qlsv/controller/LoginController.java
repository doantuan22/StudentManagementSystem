package com.qlsv.controller;

import com.qlsv.model.User;
import com.qlsv.service.AuthService;

public class LoginController {

    private final AuthService authService = new AuthService();

    public User login(String username, String password) {
        return authService.login(username, password);
    }

    public void logout() {
        authService.logout();
    }
}
