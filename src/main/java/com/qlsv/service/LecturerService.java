package com.qlsv.service;

import com.qlsv.config.DBConnection;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.AppException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Role;
import com.qlsv.model.User;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LecturerService {

    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final UserDAO userDAO = new UserDAO();
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
        if (lecturer.getId() != null) {
            if (permissionService.hasPermission(RolePermission.MANAGE_LECTURERS)) {
                // Admin duoc phep cap nhat day du.
            } else if (permissionService.hasPermission(RolePermission.EDIT_OWN_PROFILE)) {
                Lecturer current = findCurrentLecturer();
                if (!current.getId().equals(lecturer.getId())) {
                    throw new ValidationException("Bạn không thể chỉnh sửa hồ sơ của người khác.");
                }
            } else {
                permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
            }
        } else {
            permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        }

        validate(lecturer);
        return lecturer.getId() == null ? lecturerDAO.insert(lecturer) : updateAndReturn(lecturer);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return lecturerDAO.delete(id);
    }

    private Lecturer updateAndReturn(Lecturer lecturer) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureLinkedUser(connection, lecturer);
                lecturerDAO.update(connection, lecturer);

                if (lecturer.getUserId() != null) {
                    userDAO.updateFullName(connection, lecturer.getUserId(), lecturer.getFullName());
                    userDAO.updateEmail(connection, lecturer.getUserId(), lecturer.getEmail());
                }

                connection.commit();
                if (SessionManager.isLoggedIn()
                        && lecturer.getUserId() != null
                        && lecturer.getUserId().equals(SessionManager.requireCurrentUser().getId())) {
                    SessionManager.requireCurrentUser().setFullName(lecturer.getFullName());
                    SessionManager.requireCurrentUser().setEmail(lecturer.getEmail());
                }
                return lecturer;
            } catch (SQLException exception) {
                connection.rollback();
                throw new AppException("Lỗi khi cập nhật giảng viên và đồng bộ tài khoản.", exception);
            }
        } catch (SQLException exception) {
            throw new AppException("Lỗi kết nối database khi cập nhật giảng viên.", exception);
        }
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

    private void ensureLinkedUser(Connection connection, Lecturer lecturer) throws SQLException {
        if (lecturer.getUserId() != null) {
            return;
        }

        String username = lecturer.getLecturerCode() == null ? "" : lecturer.getLecturerCode().trim().toLowerCase();
        if (username.isBlank()) {
            return;
        }

        User existingUser = userDAO.findByUsername(username).orElse(null);
        if (existingUser != null) {
            if (existingUser.getRole() != Role.LECTURER) {
                throw new ValidationException("Ma giang vien dang trung voi tai khoan khong phai giang vien.");
            }
            lecturer.setUserId(existingUser.getId());
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordHasher.hash("123456"));
        user.setFullName(lecturer.getFullName());
        user.setEmail(lecturer.getEmail());
        user.setRole(Role.LECTURER);
        user.setActive(true);
        userDAO.insert(connection, user);
        lecturer.setUserId(user.getId());
    }
}
