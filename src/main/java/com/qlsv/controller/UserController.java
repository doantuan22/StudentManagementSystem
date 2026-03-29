/**
 * Điều phối dữ liệu cho người dùng.
 */
package com.qlsv.controller;

import com.qlsv.model.Role;
import com.qlsv.service.UserService;

public class UserController {

    private final UserService userService = new UserService();

    /**
     * Xử lý đổi hiện tại mật khẩu.
     */
    public void changeCurrentPassword(String currentPassword, String newPassword, String confirmPassword) {
        userService.changeCurrentPassword(currentPassword, newPassword, confirmPassword);
    }

    /**
     * Xử lý admin đổi mật khẩu.
     */
    public void adminChangePassword(Long userId, Role expectedRole, String newPassword, String confirmPassword) {
        userService.adminChangePassword(userId, expectedRole, newPassword, confirmPassword);
    }
}
