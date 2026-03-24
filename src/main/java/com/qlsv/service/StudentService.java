package com.qlsv.service;

import com.qlsv.config.SessionManager;
import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Student;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Student> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findAll();
    }

    public List<Student> findAllForSelection() {
        permissionService.requireLogin();
        return studentDAO.findAll();
    }

    public Student findCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay ho so sinh vien cua tai khoan dang nhap."));
    }

    public Student save(Student student) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        validate(student);
        return student.getId() == null ? studentDAO.insert(student) : updateAndReturn(student);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.delete(id);
    }

    private Student updateAndReturn(Student student) {
        studentDAO.update(student);
        return student;
    }

    private void validate(Student student) {
        ValidationUtil.requireNotBlank(student.getStudentCode(), "Ma sinh vien khong duoc de trong.");
        ValidationUtil.requireNotBlank(student.getFullName(), "Ho ten sinh vien khong duoc de trong.");
        ValidationUtil.requireNotBlank(student.getEmail(), "Email sinh vien khong duoc de trong.");
        if (student.getFaculty() == null || student.getFaculty().getId() == null) {
            throw new ValidationException("Sinh vien phai thuoc mot khoa.");
        }
        if (student.getClassRoom() == null || student.getClassRoom().getId() == null) {
            throw new ValidationException("Sinh vien phai thuoc mot lop.");
        }
    }
}
