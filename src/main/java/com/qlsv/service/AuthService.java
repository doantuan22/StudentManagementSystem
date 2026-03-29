package com.qlsv.service;

import com.qlsv.dao.UserDAO;
import com.qlsv.exception.AuthenticationException;
import com.qlsv.model.User;
import com.qlsv.security.AuthManager;
import com.qlsv.security.PasswordHasher;
import com.qlsv.utils.ValidationUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Thực hiện kiểm tra thông tin đăng nhập và thiết lập phiên làm việc.
     */
    public User login(String username, String password) {
        // Xu ly dang nhap tap trung tai service de UI chi lo hien thi.
        String normalizedUsername = ValidationUtil.requireNotBlank(username, "Tên đăng nhập không được để trống.");
        String normalizedPassword = ValidationUtil.requireNotBlank(password, "Mật khẩu không được để trống.");

        User user = userDAO.findByUsername(normalizedUsername)
                .orElseThrow(() -> new AuthenticationException("Sai tên đăng nhập hoặc mật khẩu."));

        if (!user.isActive()) {
            throw new AuthenticationException("Tài khoản đã bị khóa.");
        }

        if (!PasswordHasher.matches(normalizedPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Sai tên đăng nhập hoặc mật khẩu.");
        }

        AuthManager.login(user);
        return user;
    }

    /**
     * Đăng xuất người dùng hiện tại và xóa phiên làm việc.
     */
    public void logout() {
        AuthManager.logout();
    }
}
