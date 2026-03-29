/**
 * Xử lý nghiệp vụ môn học.
 */
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

    /**
     * Trả về toàn bộ dữ liệu dữ liệu hiện tại.
     */
    public List<Subject> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return subjectDAO.findAll();
    }

    /**
     * Trả về toàn bộ dữ liệu for selection.
     */
    public List<Subject> findAllForSelection() {
        permissionService.requireLogin();
        return subjectDAO.findAll();
    }

    /**
     * Tìm dữ liệu theo khoa id.
     */
    public List<Subject> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return subjectDAO.findByFacultyId(facultyId);
    }

    /**
     * Lưu dữ liệu hiện tại.
     */
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

    /**
     * Xóa dữ liệu hiện tại.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SUBJECTS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa môn học.",
                ignored -> subjectDAO.delete(id)
        );
    }

    /**
     * Cập nhật and return.
     */
    private Subject updateAndReturn(Subject subject) {
        subjectDAO.update(subject);
        return subject;
    }

    /**
     * Kiểm tra dữ liệu hiện tại.
     */
    private void validate(Subject subject) {
        ValidationUtil.requireWithinLength(subject.getSubjectCode(), 50, "Mã môn học");
        ValidationUtil.requireNotBlank(subject.getSubjectName(), "Tên môn học không được để trống.");
        ValidationUtil.requirePositive(subject.getCredits(), "Số tín chỉ phải lớn hơn 0.");
        if (subject.getFaculty() == null || subject.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Môn học phải thuộc một khoa.");
        }
    }
}
