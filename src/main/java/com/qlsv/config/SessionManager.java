package com.qlsv.config;

import com.qlsv.exception.AuthenticationException;
import com.qlsv.model.User;

public final class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static User requireCurrentUser() {
        if (currentUser == null) {
            throw new AuthenticationException("Ban chua dang nhap vao he thong.");
        }
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}
