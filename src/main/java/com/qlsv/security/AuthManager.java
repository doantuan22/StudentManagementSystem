package com.qlsv.security;

import com.qlsv.config.SessionManager;
import com.qlsv.model.Role;
import com.qlsv.model.User;

public final class AuthManager {

    private AuthManager() {
    }

    public static void login(User user) {
        SessionManager.setCurrentUser(user);
    }

    public static void logout() {
        SessionManager.clear();
    }

    public static User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    public static boolean hasRole(Role role) {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null && currentUser.getRole() == role;
    }

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

    public static boolean hasPermission(String permission) {
        User currentUser = SessionManager.getCurrentUser();
        return currentUser != null && RolePermission.hasPermission(currentUser.getRole(), permission);
    }
}
