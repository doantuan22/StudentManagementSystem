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
                "Không thể lưu môn học.",
                ignored -> {
                    validate(subject);
                    return subject.getId() == null ? subjectDAO.insert(subject) : updateAndReturn(subject);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa môn học.",
                ignored -> subjectDAO.delete(id)
        );
    }

    private Subject updateAndReturn(Subject subject) {
        subjectDAO.update(subject);
        return subject;
    }

    private void validate(Subject subject) {
        ValidationUtil.requireWithinLength(subject.getSubjectCode(), 50, "Mã môn học");
        ValidationUtil.requireNotBlank(subject.getSubjectName(), "Tên môn học không được để trống.");
        ValidationUtil.requirePositive(subject.getCredits(), "Số tín chỉ phải lớn hơn 0.");
        if (subject.getFaculty() == null || subject.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Môn học phải thuộc một khoa.");
        }
    }
}
