/**
 * Khai báo điều hướng giữa login và dashboard.
 */
package com.qlsv.navigation;

import com.qlsv.model.User;

public interface AppNavigator {

    /**
     * Hiển thị đăng nhập.
     */
    void showLogin();

    /**
     * Hiển thị dashboard.
     */
    void showDashboard(User user);
}
