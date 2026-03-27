package com.qlsv.service;

import com.qlsv.config.SessionManager;
import com.qlsv.exception.AuthenticationException;
import com.qlsv.exception.AuthorizationException;
import com.qlsv.model.Role;
import com.qlsv.security.AuthManager;

public class PermissionService {

    public void requireLogin() {
        if (!SessionManager.isLoggedIn()) {
            throw new AuthenticationException("Bạn cần đăng nhập để sử dụng chức năng này.");
        }
    }

    public void requirePermission(String permission) {
        requireLogin();
        // Kiểm tra quyền tập trung để service và UI cùng dùng một quy tắc.
        if (!hasPermission(permission)) {
            throw new AuthorizationException("Bạn không có quyền thực hiện thao tác này.");
        }
    }

    public boolean hasPermission(String permission) {
        return AuthManager.hasPermission(permission);
    }

    public void requireAnyRole(Role... roles) {
        requireLogin();
        if (!AuthManager.hasAnyRole(roles)) {
            throw new AuthorizationException("Vai trò hiện tại không được phép truy cập chức năng này.");
        }
    }
}
