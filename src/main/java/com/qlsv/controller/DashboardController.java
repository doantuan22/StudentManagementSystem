package com.qlsv.controller;

import com.qlsv.model.Role;
import com.qlsv.model.User;
import com.qlsv.view.admin.AdminDashboardFrame;
import com.qlsv.view.lecturer.LecturerDashboardFrame;
import com.qlsv.view.student.StudentDashboardFrame;

import javax.swing.JFrame;

public class DashboardController {

    public JFrame openDashboard(User user) {
        if (user.getRole() == Role.ADMIN) {
            return new AdminDashboardFrame(user);
        }
        if (user.getRole() == Role.LECTURER) {
            return new LecturerDashboardFrame(user);
        }
        return new StudentDashboardFrame(user);
    }
}
