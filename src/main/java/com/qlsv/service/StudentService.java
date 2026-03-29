package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.StudentDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Role;
import com.qlsv.model.Student;
import com.qlsv.model.User;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class StudentService {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Lấy danh sách tất cả sinh viên (yêu cầu quyền quản lý).
     */
    public List<Student> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findAll();
    }

    /**
     * Lấy danh sách sinh viên để chọn trong các chức năng khác.
     */
    public List<Student> findAllForSelection() {
        permissionService.requireLogin();
        return studentDAO.findAll();
    }

    /**
     * Lọc danh sách sinh viên theo khoa.
     */
    public List<Student> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findByFacultyId(facultyId);
    }

    /**
     * Lọc danh sách sinh viên theo lớp học.
     */
    public List<Student> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findByClassRoomId(classRoomId);
    }

    /**
     * Lọc danh sách sinh viên theo niên khóa.
     */
    public List<Student> findByAcademicYear(String academicYear) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findByAcademicYear(academicYear);
    }

    /**
     * Lấy danh sách tất cả niên khóa của sinh viên trong hệ thống.
     */
    public List<String> findAcademicYears() {
        permissionService.requireLogin();
        return studentDAO.findAcademicYears();
    }

    /**
     * Tìm kiếm sinh viên theo nhiều tiêu chí kết hợp.
     */
    public List<Student> searchStudents(String keyword, Long facultyId, Long classRoomId, String academicYear) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.searchByCriteria(keyword, facultyId, classRoomId, academicYear);
    }

    /**
     * Lấy thông tin hồ sơ của sinh viên đang đăng nhập.
     */
    public Student findCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy hồ sơ sinh viên của tài khoản đang đăng nhập."));
    }

    /**
     * Tìm kiếm sinh viên theo mã định danh hệ thống.
     */
    public Student findById(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findById(id)
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên theo mã định danh."));
    }

    /**
     * Lưu hồ sơ sinh viên và tự động đồng bộ tài khoản người dùng tương ứng.
     */
    public Student save(Student student) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        validate(student);

        Long studentId = JpaBootstrap.executeInTransaction(
                "Lỗi khi lưu sinh viên và đồng bộ tài khoản bằng JPA.",
                ignored -> {
                    boolean isNew = student.getId() == null;
                    if (!isNew && studentDAO.findById(student.getId()).isEmpty()) {
                        throw new ValidationException("Không tìm thấy sinh viên để cập nhật.");
                    }

                    ensureLinkedUser(student);
                    if (isNew) {
                        studentDAO.insert(student);
                    } else {
                        studentDAO.update(student);
                    }
                    syncLinkedUser(student);
                    return student.getId();
                }
        );

        return studentDAO.findById(studentId)
                .orElseThrow(() -> new ValidationException("Không thể tải lại sinh viên sau khi lưu."));
    }

    /**
     * Cho phép sinh viên tự cập nhật thông tin liên hệ của chính mình.
     */
    public Student updateCurrentStudentContactInfo(String email, String phone, String address) {
        permissionService.requirePermission(RolePermission.EDIT_OWN_PROFILE);

        Student currentStudent = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy hồ sơ sinh viên của tài khoản đang đăng nhập."));

        String normalizedEmail = ValidationUtil.requireEmail(email, "Email sinh viên");
        String normalizedPhone = ValidationUtil.requirePhone(phone, "Số điện thoại sinh viên");
        String normalizedAddress = normalizeAddress(address);

        Long studentId = JpaBootstrap.executeInTransaction(
                "Lỗi khi cập nhật thông tin liên hệ sinh viên bằng JPA.",
                ignored -> {
                    if (!studentDAO.updateContactInfo(currentStudent.getId(), normalizedEmail, normalizedPhone, normalizedAddress)) {
                        throw new ValidationException("Không tìm thấy sinh viên để cập nhật thông tin liên hệ.");
                    }

                    if (currentStudent.getUserId() != null) {
                        userDAO.updateEmail(currentStudent.getUserId(), normalizedEmail);
                    }
                    return currentStudent.getId();
                }
        );

        SessionManager.requireCurrentUser().setEmail(normalizedEmail);
        return studentDAO.findById(studentId)
                .orElseThrow(() -> new ValidationException("Không thể tải lại thông tin sinh viên sau khi cập nhật."));
    }

    /**
     * Xóa sinh viên khỏi hệ thống (yêu cầu quyền quản lý).
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa sinh viên.",
                ignored -> studentDAO.delete(id)
        );
    }

    private void validate(Student student) {
        student.setStudentCode(ValidationUtil.normalizeCodePrefix(student.getStudentCode(), "SV", "MÃ£ sinh viÃªn"));
        ValidationUtil.requireWithinLength(student.getStudentCode(), 50, "Mã sinh viên");
        ValidationUtil.requireNotBlank(student.getFullName(), "Họ tên sinh viên không được để trống.");
        ValidationUtil.requireEmail(student.getEmail(), "Email sinh viên");
        ValidationUtil.requirePhone(student.getPhone(), "Số điện thoại sinh viên");
        student.setAcademicYear(AcademicFormatUtil.normalizeAcademicYear(student.getAcademicYear(), "Niên khóa"));
        if (student.getFaculty() == null || student.getFaculty().getId() == null) {
            throw new ValidationException("Sinh viên phải thuộc một khoa.");
        }
        if (student.getClassRoom() == null || student.getClassRoom().getId() == null) {
            throw new ValidationException("Sinh viên phải thuộc một lớp.");
        }
    }

    private String normalizeAddress(String address) {
        String normalizedAddress = address == null ? "" : address.trim();
        if (normalizedAddress.length() > 255) {
            throw new ValidationException("Địa chỉ không được vượt quá 255 ký tự.");
        }
        return normalizedAddress;
    }

    private void ensureLinkedUser(Student student) {
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
                throw new ValidationException("Mã sinh viên đang trùng với tài khoản không phải sinh viên.");
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
        userDAO.insert(user);
        student.setUserId(user.getId());
    }

    private void syncLinkedUser(Student student) {
        if (student.getUserId() == null) {
            return;
        }
        userDAO.updateFullName(student.getUserId(), student.getFullName());
        userDAO.updateEmail(student.getUserId(), student.getEmail());
    }
}
