package com.qlsv.controller;

import com.qlsv.model.User;
import com.qlsv.service.AuthService;

public class LoginController {

    private final AuthService authService = new AuthService();

    /**
     * Điều phối yêu cầu đăng nhập từ giao diện đến dịch vụ xác thực.
     */
    public User login(String username, String password) {
        return authService.login(username, password);
    }

    /**
     * Điều phối yêu cầu đăng xuất để kết thúc phiên làm việc.
     */
    public void logout() {
        authService.logout();
    }
}
