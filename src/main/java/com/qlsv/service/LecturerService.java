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

    public List<Lecturer> findByFacultyId(Long facultyId) {
        return findAll().stream()
                .filter(lecturer -> lecturer.getFaculty() != null
                        && lecturer.getFaculty().getId() != null
                        && lecturer.getFaculty().getId().equals(facultyId))
                .toList();
    }

    public Lecturer findCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy hồ sơ giảng viên của tài khoản đang đăng nhập."));
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
        ValidationUtil.requireWithinLength(lecturer.getLecturerCode(), 50, "Mã giảng viên");
        ValidationUtil.requireNotBlank(lecturer.getFullName(), "Họ tên giảng viên không được để trống.");
        ValidationUtil.requireEmail(lecturer.getEmail(), "Email giảng viên");
        ValidationUtil.requirePhone(lecturer.getPhone(), "Số điện thoại giảng viên");
        if (lecturer.getFaculty() == null || lecturer.getFaculty().getId() == null) {
            throw new ValidationException("Giảng viên phải thuộc một khoa.");
        }
    }
}
