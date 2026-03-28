package com.qlsv.controller;

import com.qlsv.model.Role;
import com.qlsv.service.UserService;

public class UserController {

    private final UserService userService = new UserService();

    public void changeCurrentPassword(String currentPassword, String newPassword, String confirmPassword) {
        userService.changeCurrentPassword(currentPassword, newPassword, confirmPassword);
    }

    public void adminChangePassword(Long userId, Role expectedRole, String newPassword, String confirmPassword) {
        userService.adminChangePassword(userId, expectedRole, newPassword, confirmPassword);
    }
}
