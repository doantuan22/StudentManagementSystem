package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Role;
import com.qlsv.model.User;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

public class UserService {

    private final UserDAO userDAO = new UserDAO();
    private final PermissionService permissionService = new PermissionService();

    public void changeCurrentPassword(String currentPassword, String newPassword, String confirmPassword) {
        permissionService.requireLogin();

        User sessionUser = SessionManager.requireCurrentUser();
        User persistedUser = userDAO.findById(sessionUser.getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay tai khoan dang dang nhap."));

        String normalizedCurrentPassword = ValidationUtil.requireNotBlank(
                currentPassword,
                "Mat khau cu khong duoc de trong."
        );
        if (!PasswordHasher.matches(normalizedCurrentPassword, persistedUser.getPasswordHash())) {
            throw new ValidationException("Mat khau cu khong dung.");
        }

        String normalizedNewPassword = validateNewPassword(newPassword, confirmPassword);
        updatePassword(persistedUser, normalizedNewPassword);
    }

    public void adminChangePassword(Long userId, Role expectedRole, String newPassword, String confirmPassword) {
        requireAdminPermission(expectedRole);

        if (userId == null) {
            throw new ValidationException("Khong tim thay tai khoan can doi mat khau.");
        }

        User targetUser = userDAO.findById(userId)
                .orElseThrow(() -> new ValidationException("Khong tim thay tai khoan can doi mat khau."));
        if (expectedRole != null && targetUser.getRole() != expectedRole) {
            throw new ValidationException("Tai khoan duoc chon khong dung vai tro can doi mat khau.");
        }

        String normalizedNewPassword = validateNewPassword(newPassword, confirmPassword);
        updatePassword(targetUser, normalizedNewPassword);
    }

    private void requireAdminPermission(Role expectedRole) {
        if (expectedRole == Role.STUDENT) {
            permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
            return;
        }
        if (expectedRole == Role.LECTURER) {
            permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
            return;
        }
        permissionService.requireAnyRole(Role.ADMIN);
    }

    private String validateNewPassword(String newPassword, String confirmPassword) {
        String normalizedNewPassword = ValidationUtil.requireNotBlank(
                newPassword,
                "Mat khau moi khong duoc de trong."
        );
        String normalizedConfirmPassword = ValidationUtil.requireNotBlank(
                confirmPassword,
                "Nhap lai mat khau moi khong duoc de trong."
        );
        if (!normalizedNewPassword.equals(normalizedConfirmPassword)) {
            throw new ValidationException("Xac nhan mat khau khong khop.");
        }
        if (normalizedNewPassword.length() > 255) {
            throw new ValidationException("Mat khau khong duoc vuot qua 255 ky tu.");
        }
        return normalizedNewPassword;
    }

    private void updatePassword(User user, String rawPassword) {
        String hashedPassword = PasswordHasher.hash(rawPassword);

        JpaBootstrap.executeInTransaction(
                "Khong the cap nhat mat khau cho tai khoan da chon.",
                ignored -> {
                    if (!userDAO.updatePasswordHash(user.getId(), hashedPassword)) {
                        throw new ValidationException("Khong the cap nhat mat khau cho tai khoan da chon.");
                    }
                    return Boolean.TRUE;
                }
        );

        if (SessionManager.isLoggedIn()
                && SessionManager.requireCurrentUser().getId() != null
                && SessionManager.requireCurrentUser().getId().equals(user.getId())) {
            SessionManager.requireCurrentUser().setPasswordHash(hashedPassword);
        }
    }
}
