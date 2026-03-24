package com.qlsv.service;

import com.qlsv.dao.UserDAO;
import com.qlsv.exception.AuthenticationException;
import com.qlsv.model.User;
import com.qlsv.security.AuthManager;
import com.qlsv.security.PasswordHasher;
import com.qlsv.utils.ValidationUtil;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        // Xu ly dang nhap tap trung tai service de UI chi lo hien thi.
        String normalizedUsername = ValidationUtil.requireNotBlank(username, "Username khong duoc de trong.");
        String normalizedPassword = ValidationUtil.requireNotBlank(password, "Password khong duoc de trong.");

        User user = userDAO.findByUsername(normalizedUsername)
                .orElseThrow(() -> new AuthenticationException("Sai username hoac password."));

        if (!user.isActive()) {
            throw new AuthenticationException("Tai khoan da bi khoa.");
        }

        if (!PasswordHasher.matches(normalizedPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Sai username hoac password.");
        }

        AuthManager.login(user);
        return user;
    }

    public void logout() {
        AuthManager.logout();
    }
}
