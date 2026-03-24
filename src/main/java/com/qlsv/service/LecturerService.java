package com.qlsv.service;

import com.qlsv.config.SessionManager;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Lecturer;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class LecturerService {

    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Lecturer> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return lecturerDAO.findAll();
    }

    public List<Lecturer> findAllForSelection() {
        permissionService.requireLogin();
        return lecturerDAO.findAll();
    }

    public Lecturer findCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay ho so giang vien cua tai khoan dang nhap."));
    }

    public Lecturer save(Lecturer lecturer) {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        validate(lecturer);
        return lecturer.getId() == null ? lecturerDAO.insert(lecturer) : updateAndReturn(lecturer);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return lecturerDAO.delete(id);
    }

    private Lecturer updateAndReturn(Lecturer lecturer) {
        lecturerDAO.update(lecturer);
        return lecturer;
    }

    private void validate(Lecturer lecturer) {
        ValidationUtil.requireWithinLength(lecturer.getLecturerCode(), 50, "Ma giang vien");
        ValidationUtil.requireNotBlank(lecturer.getFullName(), "Ho ten giang vien khong duoc de trong.");
        ValidationUtil.requireEmail(lecturer.getEmail(), "Email giang vien");
        ValidationUtil.requirePhone(lecturer.getPhone(), "So dien thoai giang vien");
        if (lecturer.getFaculty() == null || lecturer.getFaculty().getId() == null) {
            throw new ValidationException("Giang vien phai thuoc mot khoa.");
        }
    }
}
