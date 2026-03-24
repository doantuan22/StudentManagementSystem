package com.qlsv.service;

import com.qlsv.dao.ClassRoomDAO;
import com.qlsv.model.ClassRoom;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class ClassRoomService {

    private final ClassRoomDAO classRoomDAO = new ClassRoomDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<ClassRoom> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.findAll();
    }

    public List<ClassRoom> findAllForSelection() {
        permissionService.requireLogin();
        return classRoomDAO.findAll();
    }

    public ClassRoom save(ClassRoom classRoom) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        validate(classRoom);
        return classRoom.getId() == null ? classRoomDAO.insert(classRoom) : updateAndReturn(classRoom);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.delete(id);
    }

    private ClassRoom updateAndReturn(ClassRoom classRoom) {
        classRoomDAO.update(classRoom);
        return classRoom;
    }

    private void validate(ClassRoom classRoom) {
        ValidationUtil.requireNotBlank(classRoom.getClassCode(), "Ma lop khong duoc de trong.");
        ValidationUtil.requireNotBlank(classRoom.getClassName(), "Ten lop khong duoc de trong.");
        ValidationUtil.requireNotBlank(classRoom.getAcademicYear(), "Nien khoa khong duoc de trong.");
        if (classRoom.getFaculty() == null || classRoom.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Lop hoc phai thuoc mot khoa.");
        }
    }
}
