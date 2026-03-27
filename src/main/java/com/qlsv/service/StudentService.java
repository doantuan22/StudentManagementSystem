package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.StudentDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Role;
import com.qlsv.model.Student;
import com.qlsv.model.User;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

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
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findByFacultyId(facultyId);
    }

    public List<Student> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findByClassRoomId(classRoomId);
    }

    public List<Student> findByAcademicYear(String academicYear) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.findByAcademicYear(academicYear);
    }

    public List<String> findAcademicYears() {
        permissionService.requireLogin();
        return studentDAO.findAcademicYears();
    }

    public List<Student> searchStudents(String keyword, Long facultyId, Long classRoomId, String academicYear) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return studentDAO.searchByCriteria(keyword, facultyId, classRoomId, academicYear);
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

        Long studentId = JpaBootstrap.executeInTransaction(
                "Loi khi luu sinh vien va dong bo tai khoan bang JPA.",
                ignored -> {
                    boolean isNew = student.getId() == null;
                    if (!isNew && studentDAO.findById(student.getId()).isEmpty()) {
                        throw new ValidationException("Khong tim thay sinh vien de cap nhat.");
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
                .orElseThrow(() -> new ValidationException("Khong the tai lai sinh vien sau khi luu."));
    }

    public Student updateCurrentStudentContactInfo(String email, String phone, String address) {
        permissionService.requirePermission(RolePermission.EDIT_OWN_PROFILE);

        Student currentStudent = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay ho so sinh vien cua tai khoan dang dang nhap."));

        String normalizedEmail = ValidationUtil.requireEmail(email, "Email sinh vien");
        String normalizedPhone = ValidationUtil.requirePhone(phone, "So dien thoai sinh vien");
        String normalizedAddress = normalizeAddress(address);

        Long studentId = JpaBootstrap.executeInTransaction(
                "Loi khi cap nhat thong tin lien he sinh vien bang JPA.",
                ignored -> {
                    if (!studentDAO.updateContactInfo(currentStudent.getId(), normalizedEmail, normalizedPhone, normalizedAddress)) {
                        throw new ValidationException("Khong tim thay sinh vien de cap nhat thong tin lien he.");
                    }

                    if (currentStudent.getUserId() != null) {
                        userDAO.updateEmail(currentStudent.getUserId(), normalizedEmail);
                    }
                    return currentStudent.getId();
                }
        );

        SessionManager.requireCurrentUser().setEmail(normalizedEmail);
        return studentDAO.findById(studentId)
                .orElseThrow(() -> new ValidationException("Khong the tai lai thong tin sinh vien sau khi cap nhat."));
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_STUDENTS);
        return JpaBootstrap.executeInTransaction(
                "Khong the xoa sinh vien.",
                ignored -> studentDAO.delete(id)
        );
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
