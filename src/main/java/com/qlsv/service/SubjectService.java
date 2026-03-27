package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.SubjectDAO;
import com.qlsv.model.Subject;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class SubjectService {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Subject> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return subjectDAO.findAll();
    }

    public List<Subject> findAllForSelection() {
        permissionService.requireLogin();
        return subjectDAO.findAll();
    }

    public List<Subject> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return subjectDAO.findByFacultyId(facultyId);
    }

    public Subject save(Subject subject) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ lÆ°u mÃ´n há»c.",
                ignored -> {
                    validate(subject);
                    return subject.getId() == null ? subjectDAO.insert(subject) : updateAndReturn(subject);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a mÃ´n há»c.",
                ignored -> subjectDAO.delete(id)
        );
    }

    private Subject updateAndReturn(Subject subject) {
        subjectDAO.update(subject);
        return subject;
    }

    private void validate(Subject subject) {
        ValidationUtil.requireWithinLength(subject.getSubjectCode(), 50, "MÃ£ mÃ´n há»c");
        ValidationUtil.requireNotBlank(subject.getSubjectName(), "TÃªn mÃ´n há»c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        ValidationUtil.requirePositive(subject.getCredits(), "Sá»‘ tÃ­n chá»‰ pháº£i lá»›n hÆ¡n 0.");
        if (subject.getFaculty() == null || subject.getFaculty().getId() == null) {
            throw new IllegalArgumentException("MÃ´n há»c pháº£i thuá»™c má»™t khoa.");
        }
    }
}
