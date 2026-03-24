package com.qlsv.service;

import com.qlsv.dao.FacultyDAO;
import com.qlsv.model.Faculty;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class FacultyService {

    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Faculty> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        return facultyDAO.findAll();
    }

    public List<Faculty> findAllForSelection() {
        permissionService.requireLogin();
        return facultyDAO.findAll();
    }

    public Faculty save(Faculty faculty) {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        validate(faculty);
        return faculty.getId() == null ? facultyDAO.insert(faculty) : updateAndReturn(faculty);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_FACULTIES);
        return facultyDAO.delete(id);
    }

    private Faculty updateAndReturn(Faculty faculty) {
        facultyDAO.update(faculty);
        return faculty;
    }

    private void validate(Faculty faculty) {
        ValidationUtil.requireNotBlank(faculty.getFacultyCode(), "Ma khoa khong duoc de trong.");
        ValidationUtil.requireNotBlank(faculty.getFacultyName(), "Ten khoa khong duoc de trong.");
    }
}
