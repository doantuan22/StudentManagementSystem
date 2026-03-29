package com.qlsv.controller;

import com.qlsv.model.ClassRoom;
import com.qlsv.service.ClassRoomService;

import java.util.List;

public class ClassRoomController {

    private final ClassRoomService classRoomService = new ClassRoomService();

    /**
     * Lấy toàn bộ danh sách lớp học thông qua tầng service.
     */
    public List<ClassRoom> getAllClassRooms() {
        return classRoomService.findAll();
    }

    /**
     * Lấy danh sách lớp học để hiển thị trên các thành phần lựa chọn (ComboBox).
     */
    public List<ClassRoom> getClassRoomsForSelection() {
        return classRoomService.findAllForSelection();
    }

    /**
     * Lọc danh sách lớp học theo mã khoa.
     */
    public List<ClassRoom> getClassRoomsByFaculty(Long facultyId) {
        return classRoomService.findByFacultyId(facultyId);
    }

    /**
     * Lọc danh sách lớp học theo niên khóa.
     */
    public List<ClassRoom> getClassRoomsByAcademicYear(String academicYear) {
        return classRoomService.findByAcademicYear(academicYear);
    }

    /**
     * Gửi yêu cầu lưu (thêm mới hoặc cập nhật) thông tin lớp học.
     */
    public ClassRoom saveClassRoom(ClassRoom classRoom) {
        return classRoomService.save(classRoom);
    }

    /**
     * Gửi yêu cầu xóa lớp học theo mã định danh.
     */
    public boolean deleteClassRoom(Long id) {
        return classRoomService.delete(id);
    }
}
