/**
 * Xử lý nghiệp vụ khoa.
 */
package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.FacultyDAO;
import com.qlsv.model.Faculty;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class FacultyService {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Trả về toàn bộ dữ liệu dữ liệu hiện tại.
     */
    public List<Faculty> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        return facultyDAO.findAll();
    }

    /**
     * Trả về toàn bộ dữ liệu for selection.
     */
    public List<Faculty> findAllForSelection() {
        permissionService.requireLogin();
        return facultyDAO.findAll();
    }

    /**
     * Tìm dữ liệu theo mã.
     */
    public List<Faculty> findByCode(String facultyCode) {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        return facultyDAO.findByCode(facultyCode)
                .map(List::of)
                .orElseGet(List::of);
    }

    /**
     * Lưu dữ liệu hiện tại.
     */
    public Faculty save(Faculty faculty) {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu khoa.",
                ignored -> {
                    validate(faculty);
                    return faculty.getId() == null ? facultyDAO.insert(faculty) : updateAndReturn(faculty);
                }
        );
    }

    /**
     * Xóa dữ liệu hiện tại.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa khoa.",
                ignored -> facultyDAO.delete(id)
        );
    }

    /**
     * Cập nhật and return.
     */
    private Faculty updateAndReturn(Faculty faculty) {
        facultyDAO.update(faculty);
        return faculty;
    }

    /**
     * Kiểm tra dữ liệu hiện tại.
     */
    private void validate(Faculty faculty) {
        ValidationUtil.requireWithinLength(faculty.getFacultyCode(), 50, "Mã khoa");
        ValidationUtil.requireNotBlank(faculty.getFacultyName(), "Tên khoa không được để trống.");
    }
}
