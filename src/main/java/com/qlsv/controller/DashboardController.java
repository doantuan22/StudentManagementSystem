package com.qlsv.controller;

import com.qlsv.model.Role;
import com.qlsv.model.User;

public class DashboardController {

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
