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

    public List<ClassRoom> findByFacultyId(Long facultyId) {
        return findAll().stream()
                .filter(classRoom -> classRoom.getFaculty() != null
                        && classRoom.getFaculty().getId() != null
                        && classRoom.getFaculty().getId().equals(facultyId))
                .toList();
    }

    public List<ClassRoom> findByAcademicYear(String academicYear) {
        return findAll().stream()
                .filter(classRoom -> classRoom.getAcademicYear() != null
                        && classRoom.getAcademicYear().equalsIgnoreCase(academicYear == null ? "" : academicYear.trim()))
                .toList();
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
        ValidationUtil.requireWithinLength(classRoom.getClassCode(), 50, "Mã lớp");
        ValidationUtil.requireNotBlank(classRoom.getClassName(), "Tên lớp không được để trống.");
        ValidationUtil.requireNotBlank(classRoom.getAcademicYear(), "Niên khóa không được để trống.");
        if (classRoom.getFaculty() == null || classRoom.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Lớp học phải thuộc một khoa.");
        }
    }
}
