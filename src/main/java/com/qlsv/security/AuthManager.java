/**
 * Điều phối đăng nhập, đăng xuất và phiên người dùng.
 */
package com.qlsv.security;

import com.qlsv.config.SessionManager;
import com.qlsv.model.Role;
import com.qlsv.model.User;

public final class AuthManager {

    /**
     * Khởi tạo xác thực manager.
     */
    private AuthManager() {
    }

    /**
     * Thiết lập người dùng hiện tại vào phiên làm việc sau khi đăng nhập thành công.
     */
    public static void login(User user) {
        SessionManager.setCurrentUser(user);
    }

    /**
     * Xóa thông tin người dùng hiện tại khỏi phiên làm việc (đăng xuất).
     */
    public static void logout() {
        SessionManager.clear();
    }

    /**
     * Lấy thông tin người dùng hiện tại đang đăng nhập trong hệ thống.
     */
    public static User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    /**
     * Kiểm tra xem người dùng hiện tại có vai trò cụ thể nào đó không.
     */
    public static boolean hasRole(Role role) {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null && currentUser.getRole() == role;
    }

    /**
     * Kiểm tra xem người dùng hiện tại có thuộc về một trong các vai trò được chỉ định không.
     */
    public static boolean hasAnyRole(Role... roles) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            return false;
        }
        for (Role role : roles) {
            if (currentUser.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem người dùng hiện tại có quyền thực hiện một hành động cụ thể không.
     */
    public static boolean hasPermission(String permission) {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null && RolePermission.hasPermission(currentUser.getRole(), permission);
    }
}
