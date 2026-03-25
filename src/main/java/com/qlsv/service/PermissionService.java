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
        // Kiem tra quyen tap trung de service va UI cung dung mot quy tac.
        if (!AuthManager.hasPermission(permission)) {
            throw new AuthorizationException("Bạn không có quyền thực hiện thao tác này.");
        }
    }

    public void requireAnyRole(Role... roles) {
        requireLogin();
        if (!AuthManager.hasAnyRole(roles)) {
            throw new AuthorizationException("Vai trò hiện tại không được phép truy cập chức năng này.");
        }
    }
}
