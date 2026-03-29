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

    /**
     * Lấy danh sách tất cả các lớp học khi có quyền quản lý.
     */
    public List<ClassRoom> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.findAll();
    }

    /**
     * Lấy tất cả lớp học để phục vụ việc chọn (combo box) trên giao diện.
     */
    public List<ClassRoom> findAllForSelection() {
        permissionService.requireLogin();
        return classRoomDAO.findAll();
    }

    /**
     * Lọc danh sách lớp học theo mã khoa.
     */
    public List<ClassRoom> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.findByFacultyId(facultyId);
    }

    /**
     * Lọc danh sách lớp học theo niên khóa.
     */
    public List<ClassRoom> findByAcademicYear(String academicYear) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return classRoomDAO.findByAcademicYear(academicYear);
    }

    /**
     * Lưu thông tin lớp học (thêm mới hoặc cập nhật sau khi validate).
     */
    public ClassRoom save(ClassRoom classRoom) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu lớp học.",
                ignored -> {
                    validate(classRoom);
                    return classRoom.getId() == null ? classRoomDAO.insert(classRoom) : updateAndReturn(classRoom);
                }
        );
    }

    /**
     * Xóa lớp học khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_CLASSES);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa lớp học.",
                ignored -> classRoomDAO.delete(id)
        );
    }

    private ClassRoom updateAndReturn(ClassRoom classRoom) {
        classRoomDAO.update(classRoom);
        return classRoom;
    }

    private void validate(ClassRoom classRoom) {
        ValidationUtil.requireWithinLength(classRoom.getClassCode(), 50, "Mã lớp");
        ValidationUtil.requireNotBlank(classRoom.getClassName(), "Tên lớp không được để trống.");
        classRoom.setAcademicYear(AcademicFormatUtil.normalizeAcademicYear(classRoom.getAcademicYear(), "Niên khóa"));
        if (classRoom.getFaculty() == null || classRoom.getFaculty().getId() == null) {
            throw new IllegalArgumentException("Lớp học phải thuộc một khoa.");
        }
    }
}
