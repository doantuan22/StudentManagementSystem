package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.ClassRoomDAO;
import com.qlsv.model.ClassRoom;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.AcademicFormatUtil;
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
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.findByFacultyId(facultyId);
    }

    public List<ClassRoom> findByAcademicYear(String academicYear) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.findByAcademicYear(academicYear);
    }

    public ClassRoom save(ClassRoom classRoom) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ lÆ°u lá»›p há»c.",
                ignored -> {
                    validate(classRoom);
                    return classRoom.getId() == null ? classRoomDAO.insert(classRoom) : updateAndReturn(classRoom);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a lá»›p há»c.",
                ignored -> classRoomDAO.delete(id)
        );
    }

    private ClassRoom updateAndReturn(ClassRoom classRoom) {
        classRoomDAO.update(classRoom);
        return classRoom;
    }

    private void validate(ClassRoom classRoom) {
        ValidationUtil.requireWithinLength(classRoom.getClassCode(), 50, "MÃ£ lá»›p");
        ValidationUtil.requireNotBlank(classRoom.getClassName(), "TÃªn lá»›p khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng.");
        classRoom.setAcademicYear(AcademicFormatUtil.normalizeAcademicYear(classRoom.getAcademicYear(), "Niên khóa"));
        if (classRoom.getFaculty() == null || classRoom.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Lá»›p há»c pháº£i thuá»™c má»™t khoa.");
        }
    }
}
