package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.UserDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Role;
import com.qlsv.model.User;
import com.qlsv.security.PasswordHasher;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

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
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return lecturerDAO.findByFacultyId(facultyId);
    }

    public Lecturer findCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_PROFILE);
        return lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y há»“ sÆ¡ giáº£ng viÃªn cá»§a tÃ i khoáº£n Ä‘ang Ä‘Äƒng nháº­p."));
    }

    public Lecturer save(Lecturer lecturer) {
        if (lecturer.getId() != null) {
            if (permissionService.hasPermission(RolePermission.MANAGE_LECTURERS)) {
                // Admin duoc phep cap nhat day du.
            } else if (permissionService.hasPermission(RolePermission.EDIT_OWN_PROFILE)) {
                Lecturer current = findCurrentLecturer();
                if (!current.getId().equals(lecturer.getId())) {
                    throw new ValidationException("Báº¡n khÃ´ng thá»ƒ chá»‰nh sá»­a há»“ sÆ¡ cá»§a ngÆ°á»i khÃ¡c.");
                }
            } else {
                permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
            }
        } else {
            permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        }

        validate(lecturer);

        Long lecturerId = JpaBootstrap.executeInTransaction(
                "Lá»—i khi lÆ°u giáº£ng viÃªn vÃ  Ä‘á»“ng bá»™ tÃ i khoáº£n báº±ng JPA.",
                ignored -> {
                    boolean isNew = lecturer.getId() == null;
                    if (!isNew && lecturerDAO.findById(lecturer.getId()).isEmpty()) {
                        throw new ValidationException("KhÃ´ng tÃ¬m tháº¥y giáº£ng viÃªn Ä‘á»ƒ cáº­p nháº­t.");
                    }

                    ensureLinkedUser(lecturer);
                    if (isNew) {
                        lecturerDAO.insert(lecturer);
                    } else {
                        lecturerDAO.update(lecturer);
                    }
                    syncLinkedUser(lecturer);
                    return lecturer.getId();
                }
        );

        Lecturer persistedLecturer = lecturerDAO.findById(lecturerId)
                .orElseThrow(() -> new ValidationException("KhÃ´ng thá»ƒ táº£i láº¡i giáº£ng viÃªn sau khi lÆ°u."));

        if (SessionManager.isLoggedIn()
                && persistedLecturer.getUserId() != null
                && persistedLecturer.getUserId().equals(SessionManager.requireCurrentUser().getId())) {
            SessionManager.requireCurrentUser().setFullName(persistedLecturer.getFullName());
            SessionManager.requireCurrentUser().setEmail(persistedLecturer.getEmail());
        }
        return persistedLecturer;
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_LECTURERS);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a giáº£ng viÃªn.",
                ignored -> lecturerDAO.delete(id)
        );
    }

    private void validate(Lecturer lecturer) {
        ValidationUtil.requireWithinLength(lecturer.getLecturerCode(), 50, "MÃ£ giáº£ng viÃªn");
        ValidationUtil.requireNotBlank(lecturer.getFullName(), "Há» tÃªn giáº£ng viÃªn khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        ValidationUtil.requireEmail(lecturer.getEmail(), "Email giáº£ng viÃªn");
        ValidationUtil.requirePhone(lecturer.getPhone(), "Sá»‘ Ä‘iá»‡n thoáº¡i giáº£ng viÃªn");
        if (lecturer.getFaculty() == null || lecturer.getFaculty().getId() == null) {
            throw new ValidationException("Giáº£ng viÃªn pháº£i thuá»™c má»™t khoa.");
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
                throw new ValidationException("MÃ£ giáº£ng viÃªn Ä‘ang trÃ¹ng vá»›i tÃ i khoáº£n khÃ´ng pháº£i giáº£ng viÃªn.");
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
}
