/**
 * Điều phối dữ liệu cho học phần.
 */
package com.qlsv.controller;

import com.qlsv.model.CourseSection;
import com.qlsv.service.CourseSectionService;

import java.util.List;

public class CourseSectionController {

    private final CourseSectionService courseSectionService = new CourseSectionService();

    /**
     * Lấy toàn bộ danh sách học phần dành cho quản trị viên.
     */
    public List<CourseSection> getAllCourseSections() {
        return courseSectionService.findAllForAdmin();
    }

    /**
     * Lấy danh sách học phần để phục vụ việc chọn (combo box) trên giao diện.
     */
    public List<CourseSection> getAllCourseSectionsForSelection() {
        return courseSectionService.findAll();
    }

    /**
     * Lấy danh sách học phần do một giảng viên cụ thể phụ trách.
     */
    public List<CourseSection> getCourseSectionsByLecturer(Long lecturerId) {
        return courseSectionService.findByLecturerId(lecturerId);
    }

    /**
     * Lấy danh sách học phần thuộc một khoa cụ thể.
     */
    public List<CourseSection> getCourseSectionsByFaculty(Long facultyId) {
        return courseSectionService.findByFacultyId(facultyId);
    }

    /**
     * Lấy danh sách học phần được tổ chức tại một phòng học nhất định.
     */
    public List<CourseSection> getCourseSectionsByRoom(Long roomId) {
        return courseSectionService.findByRoom(roomId);
    }

    /**
     * Tìm kiếm học phần dựa trên mã học phần cụ thể.
     */
    public List<CourseSection> getCourseSectionsBySectionCode(String sectionCode) {
        return courseSectionService.findBySectionCode(sectionCode);
    }

    /**
     * Gửi yêu cầu lưu (thêm mới hoặc cập nhật) thông tin học phần qua service.
     */
    public CourseSection saveCourseSection(CourseSection courseSection) {
        return courseSectionService.save(courseSection);
    }

    /**
     * Gửi yêu cầu xóa học phần theo mã định danh hệ thống.
     */
    public boolean deleteCourseSection(Long id) {
        return courseSectionService.delete(id);
    }
}
