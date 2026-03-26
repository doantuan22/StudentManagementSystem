package com.qlsv.service;

import com.qlsv.config.DBConnection;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.StudentDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.AppException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Role;
import com.qlsv.model.Student;
import com.qlsv.model.User;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();
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
                .orElseThrow(() -> new ValidationException("Khong tim thay ho so sinh vien cua tai khoan dang dang nhap."));
    }

    public Student findById(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findById(id)
                .orElseThrow(() -> new ValidationException("Khong tim thay sinh vien theo ma dinh danh."));
    }

    public Student save(Student student) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        validate(student);
        return student.getId() == null ? studentDAO.insert(student) : updateAndReturn(student);
    }

    public Student updateCurrentStudentContactInfo(String email, String phone, String address) {
        permissionService.requirePermission(RolePermission.EDIT_OWN_PROFILE);

        Student currentStudent = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay ho so sinh vien cua tai khoan dang dang nhap."));

        String normalizedEmail = ValidationUtil.requireEmail(email, "Email sinh vien");
        String normalizedPhone = ValidationUtil.requirePhone(phone, "So dien thoai sinh vien");
        String normalizedAddress = normalizeAddress(address);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                studentDAO.updateContactInfo(connection, currentStudent.getId(), normalizedEmail, normalizedPhone, normalizedAddress);

                if (currentStudent.getUserId() != null) {
                    userDAO.updateEmail(connection, currentStudent.getUserId(), normalizedEmail);
                }

                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw new AppException("Loi khi cap nhat thong tin lien he sinh vien.", exception);
            }
        } catch (SQLException exception) {
            throw new AppException("Loi ket noi database khi cap nhat thong tin sinh vien.", exception);
        }

        SessionManager.requireCurrentUser().setEmail(normalizedEmail);
        return studentDAO.findById(currentStudent.getId())
                .orElseThrow(() -> new ValidationException("Khong the tai lai thong tin sinh vien sau khi cap nhat."));
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.delete(id);
    }

    private Student updateAndReturn(Student student) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureLinkedUser(connection, student);
                studentDAO.update(connection, student);

                if (student.getUserId() != null) {
                    userDAO.updateFullName(connection, student.getUserId(), student.getFullName());
                    userDAO.updateEmail(connection, student.getUserId(), student.getEmail());
                }

                connection.commit();
                return student;
            } catch (SQLException exception) {
                connection.rollback();
                throw new AppException("Loi khi cap nhat sinh vien va dong bo tai khoan.", exception);
            }
        } catch (SQLException exception) {
            throw new AppException("Loi ket noi database khi cap nhat sinh vien.", exception);
        }
    }

    private void validate(Student student) {
        ValidationUtil.requireWithinLength(student.getStudentCode(), 50, "Ma sinh vien");
        ValidationUtil.requireNotBlank(student.getFullName(), "Ho ten sinh vien khong duoc de trong.");
        ValidationUtil.requireEmail(student.getEmail(), "Email sinh vien");
        ValidationUtil.requirePhone(student.getPhone(), "So dien thoai sinh vien");
        ValidationUtil.requireNotBlank(student.getAcademicYear(), "Nien khoa khong duoc de trong.");
        if (student.getFaculty() == null || student.getFaculty().getId() == null) {
            throw new ValidationException("Sinh vien phai thuoc mot khoa.");
        }
        if (student.getClassRoom() == null || student.getClassRoom().getId() == null) {
            throw new ValidationException("Sinh vien phai thuoc mot lop.");
        }
    }

    private String normalizeAddress(String address) {
        String normalizedAddress = address == null ? "" : address.trim();
        if (normalizedAddress.length() > 255) {
            throw new ValidationException("Dia chi khong duoc vuot qua 255 ky tu.");
        }
        return normalizedAddress;
    }

    private void ensureLinkedUser(Connection connection, Student student) throws SQLException {
        if (student.getUserId() != null) {
            return;
        }

        String username = student.getStudentCode() == null ? "" : student.getStudentCode().trim().toLowerCase();
        if (username.isBlank()) {
            return;
        }

        User existingUser = userDAO.findByUsername(username).orElse(null);
        if (existingUser != null) {
            if (existingUser.getRole() != Role.STUDENT) {
                throw new ValidationException("Ma sinh vien dang trung voi tai khoan khong phai sinh vien.");
            }
            student.setUserId(existingUser.getId());
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash("123456"));
        user.setFullName(student.getFullName());
        user.setEmail(student.getEmail());
        user.setRole(Role.STUDENT);
        user.setActive(true);
        userDAO.insert(connection, user);
        student.setUserId(user.getId());
    }
}
