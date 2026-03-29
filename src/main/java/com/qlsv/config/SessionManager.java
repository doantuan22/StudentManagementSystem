/**
 * Lưu người dùng đang đăng nhập trong phiên hiện tại.
 */
package com.qlsv.config;

import com.qlsv.exception.AuthenticationException;
import com.qlsv.model.User;

public final class SessionManager {

    private static User currentUser;

    /**
     * Khởi tạo session manager.
     */
    private SessionManager() {
    }

    /**
     * Cập nhật người dùng hiện tại.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Trả về người dùng hiện tại.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Bắt buộc người dùng hiện tại.
     */
    public static User requireCurrentUser() {
        if (currentUser == null) {
            throw new AuthenticationException("Bạn chưa đăng nhập vào hệ thống.");
        }
        return currentUser;
    }

    /**
     * Kiểm tra logged in.
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Xử lý clear.
     */
    public static void clear() {
        currentUser = null;
    }
}
