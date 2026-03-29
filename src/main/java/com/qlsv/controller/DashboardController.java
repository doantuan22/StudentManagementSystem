/**
 * Điều phối dữ liệu cho dashboard.
 */
package com.qlsv.controller;

import com.qlsv.model.Role;
import com.qlsv.model.User;

public class DashboardController {

    /**
     * Xác định màn hình bảng điều khiển (Dashboard) phù hợp dựa trên vai trò của người dùng.
     */
    public DashboardDestination resolveDashboard(User user) {
        if (user.getRole() == Role.ADMIN) {
            return DashboardDestination.ADMIN;
        }
        if (user.getRole() == Role.LECTURER) {
            return DashboardDestination.LECTURER;
        }
        return DashboardDestination.STUDENT;
    }

    public enum DashboardDestination {
        ADMIN,
        LECTURER,
        STUDENT
    }
}
