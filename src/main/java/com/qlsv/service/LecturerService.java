package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.LecturerSubjectDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Role;
import com.qlsv.model.Subject;
import com.qlsv.model.User;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LecturerService {

    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final LecturerSubjectDAO lecturerSubjectDAO = new LecturerSubjectDAO();
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
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return lecturerDAO.findByFacultyId(facultyId);
    }

    public Lecturer findCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay ho so giang vien cua tai khoan dang dang nhap."));
    }

    public Lecturer save(Lecturer lecturer) {
        return saveInternal(lecturer, null);
    }

    public Lecturer saveWithSubjects(Lecturer lecturer, List<Subject> subjects) {
        return saveInternal(lecturer, normalizeSubjectIds(subjects));
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return JpaBootstrap.executeInTransaction(
                "Khong the xoa giang vien.",
                ignored -> lecturerDAO.delete(id)
        );
    }

    private Lecturer saveInternal(Lecturer lecturer, List<Long> subjectIds) {
        authorizeSave(lecturer);
        validate(lecturer);

        Long lecturerId = JpaBootstrap.executeInTransaction(
                "Loi khi luu giang vien va dong bo tai khoan bang JPA.",
                ignored -> {
                    boolean isNew = lecturer.getId() == null;
                    if (!isNew && lecturerDAO.findById(lecturer.getId()).isEmpty()) {
                        throw new ValidationException("Khong tim thay giang vien de cap nhat.");
                    }

                    ensureLinkedUser(lecturer);
                    if (isNew) {
                        lecturerDAO.insert(lecturer);
                    } else {
                        lecturerDAO.update(lecturer);
                    }
                    syncLinkedUser(lecturer);
                    if (subjectIds != null) {
                        lecturerSubjectDAO.saveAll(lecturer.getId(), subjectIds);
                    }
                    return lecturer.getId();
                }
        );

        Lecturer persistedLecturer = lecturerDAO.findById(lecturerId)
                .orElseThrow(() -> new ValidationException("Khong the tai lai giang vien sau khi luu."));

        if (SessionManager.isLoggedIn()
                && persistedLecturer.getUserId() != null
                && persistedLecturer.getUserId().equals(SessionManager.requireCurrentUser().getId())) {
            SessionManager.requireCurrentUser().setFullName(persistedLecturer.getFullName());
            SessionManager.requireCurrentUser().setEmail(persistedLecturer.getEmail());
        }
        return persistedLecturer;
    }

    private void authorizeSave(Lecturer lecturer) {
        if (lecturer.getId() != null) {
            if (permissionService.hasPermission(RolePermission.MANAGE_LECTURERS)) {
                return;
            }
            if (permissionService.hasPermission(RolePermission.EDIT_OWN_PROFILE)) {
                Lecturer current = findCurrentLecturer();
                if (!current.getId().equals(lecturer.getId())) {
                    throw new ValidationException("Ban khong the chinh sua ho so cua nguoi khac.");
                }
                return;
            }
            permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
            return;
        }
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
    }

    private void validate(Lecturer lecturer) {
        lecturer.setLecturerCode(ValidationUtil.normalizeCodePrefix(lecturer.getLecturerCode(), "GV", "Ma giang vien"));
        ValidationUtil.requireWithinLength(lecturer.getLecturerCode(), 50, "Ma giang vien");
        ValidationUtil.requireNotBlank(lecturer.getFullName(), "Ho ten giang vien khong duoc de trong.");
        ValidationUtil.requireEmail(lecturer.getEmail(), "Email giang vien");
        if (lecturer.getDateOfBirth() == null) {
            throw new ValidationException("Ngay sinh giang vien khong duoc de trong.");
        }
        ValidationUtil.requirePhone(lecturer.getPhone(), "So dien thoai giang vien");
        if (lecturer.getFaculty() == null || lecturer.getFaculty().getId() == null) {
            throw new ValidationException("Giang vien phai thuoc mot khoa.");
        }
    }

    private void ensureLinkedUser(Lecturer lecturer) {
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
        userDAO.insert(user);
        lecturer.setUserId(user.getId());
    }

    private void syncLinkedUser(Lecturer lecturer) {
        if (lecturer.getUserId() == null) {
            return;
        }
        userDAO.updateFullName(lecturer.getUserId(), lecturer.getFullName());
        userDAO.updateEmail(lecturer.getUserId(), lecturer.getEmail());
    }

    private List<Long> normalizeSubjectIds(List<Subject> subjects) {
        Set<Long> subjectIds = new LinkedHashSet<>();
        if (subjects != null) {
            for (Subject subject : subjects) {
                if (subject != null && subject.getId() != null) {
                    subjectIds.add(subject.getId());
                }
            }
        }
        return new ArrayList<>(subjectIds);
    }
}
