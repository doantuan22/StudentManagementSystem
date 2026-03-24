package com.qlsv.service;

import com.qlsv.config.SessionManager;
import com.qlsv.exception.AuthenticationException;
import com.qlsv.exception.AuthorizationException;
import com.qlsv.model.Role;
import com.qlsv.security.AuthManager;

public class PermissionService {

    public void requireLogin() {
        if (!SessionManager.isLoggedIn()) {
            throw new AuthenticationException("Ban can dang nhap de su dung chuc nang nay.");
        }
    }

    public void requirePermission(String permission) {
        requireLogin();
        // Kiem tra quyen tap trung de service va UI cung dung mot quy tac.
        if (!AuthManager.hasPermission(permission)) {
            throw new AuthorizationException("Ban khong co quyen thuc hien thao tac nay.");
        }
    }

    public void requireAnyRole(Role... roles) {
        requireLogin();
        if (!AuthManager.hasAnyRole(roles)) {
            throw new AuthorizationException("Vai tro hien tai khong duoc phep truy cap chuc nang nay.");
        }
    }
}
