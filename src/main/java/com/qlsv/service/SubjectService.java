package com.qlsv.service;

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

    public Subject save(Subject subject) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        validate(subject);
        return subject.getId() == null ? subjectDAO.insert(subject) : updateAndReturn(subject);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return subjectDAO.delete(id);
    }

    private Subject updateAndReturn(Subject subject) {
        subjectDAO.update(subject);
        return subject;
    }

    private void validate(Subject subject) {
        ValidationUtil.requireNotBlank(subject.getSubjectCode(), "Ma mon hoc khong duoc de trong.");
        ValidationUtil.requireNotBlank(subject.getSubjectName(), "Ten mon hoc khong duoc de trong.");
        ValidationUtil.requirePositive(subject.getCredits(), "So tin chi phai lon hon 0.");
        if (subject.getFaculty() == null || subject.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Mon hoc phai thuoc mot khoa.");
        }
    }
}
