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

    public List<Student> findByFacultyId(Long facultyId) {
        return findAll().stream()
                .filter(student -> student.getFaculty() != null
                        && student.getFaculty().getId() != null
                        && student.getFaculty().getId().equals(facultyId))
                .toList();
    }

    public List<Student> findByClassRoomId(Long classRoomId) {
        return findAll().stream()
                .filter(student -> student.getClassRoom() != null
                        && student.getClassRoom().getId() != null
                        && student.getClassRoom().getId().equals(classRoomId))
                .toList();
    }

    public List<Student> findByAcademicYear(String academicYear) {
        return findAll().stream()
                .filter(student -> student.getAcademicYear() != null
                        && student.getAcademicYear().trim()
                        .equalsIgnoreCase(academicYear == null ? "" : academicYear.trim()))
                .toList();
    }

    public List<String> findAcademicYears() {
        permissionService.requireLogin();
        return studentDAO.findAll().stream()
                .map(Student::getAcademicYear)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public Student findCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy hồ sơ sinh viên của tài khoản đang đăng nhập."));
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
        ValidationUtil.requireWithinLength(student.getStudentCode(), 50, "Mã sinh viên");
        ValidationUtil.requireNotBlank(student.getFullName(), "Họ tên sinh viên không được để trống.");
        ValidationUtil.requireEmail(student.getEmail(), "Email sinh viên");
        ValidationUtil.requirePhone(student.getPhone(), "Số điện thoại sinh viên");
        ValidationUtil.requireNotBlank(student.getAcademicYear(), "Niên khóa không được để trống.");
        if (student.getFaculty() == null || student.getFaculty().getId() == null) {
            throw new ValidationException("Sinh viên phải thuộc một khoa.");
        }
        if (student.getClassRoom() == null || student.getClassRoom().getId() == null) {
            throw new ValidationException("Sinh viên phải thuộc một lớp.");
        }
    }
}
