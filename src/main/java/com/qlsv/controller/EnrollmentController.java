package com.qlsv.controller;

import com.qlsv.model.Enrollment;
import com.qlsv.service.EnrollmentService;

import java.util.List;

public class EnrollmentController {

    private final EnrollmentService enrollmentService = new EnrollmentService();

    /**
     * Lấy danh sách tất cả các bản ghi đăng ký học phần trong hệ thống.
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentService.findAll();
    }

    /**
     * Lấy danh sách các học phần mà sinh viên đang đăng nhập đã đăng ký.
     */
    public List<Enrollment> getCurrentStudentEnrollments() {
        return enrollmentService.findByCurrentStudent();
    }

    /**
     * Lấy danh sách đăng ký căn cứ theo mã giảng viên phụ trách.
     */
    public List<Enrollment> getLecturerEnrollments(Long lecturerId) {
        return enrollmentService.findByLecturer(lecturerId);
    }

    /**
     * Lọc danh sách đăng ký theo một mã học phần cụ thể.
     */
    public List<Enrollment> getEnrollmentsByCourseSection(Long courseSectionId) {
        return enrollmentService.findByCourseSectionId(courseSectionId);
    }

    /**
     * Đếm số lượng sinh viên đã đăng ký vào một học phần nhất định.
     */
    public int countEnrollmentsByCourseSection(Long courseSectionId) {
        return enrollmentService.countByCourseSectionId(courseSectionId);
    }

    /**
     * Lọc danh sách đăng ký theo lớp học hành chính.
     */
    public List<Enrollment> getEnrollmentsByClassRoom(Long classRoomId) {
        return enrollmentService.findByClassRoomId(classRoomId);
    }

    /**
     * Lọc danh sách đăng ký theo khoa quản lý.
     */
    public List<Enrollment> getEnrollmentsByFaculty(Long facultyId) {
        return enrollmentService.findByFacultyId(facultyId);
    }

    /**
     * Gửi yêu cầu lưu (thêm mới hoặc cập nhật) một bản ghi đăng ký học phần.
     */
    public Enrollment saveEnrollment(Enrollment enrollment) {
        return enrollmentService.save(enrollment);
    }

    /**
     * Thực hiện đăng ký học phần cho sinh viên hiện tại dựa trên mã học phần.
     */
    public Enrollment registerCurrentStudent(Long courseSectionId) {
        return enrollmentService.registerCurrentStudent(courseSectionId);
    }

    /**
     * Huỷ bản ghi đăng ký học phần của sinh viên hiện tại.
     */
    public boolean cancelCurrentStudentEnrollment(Long enrollmentId) {
        return enrollmentService.cancelCurrentStudentEnrollment(enrollmentId);
    }

    /**
     * Xóa một bản ghi đăng ký học phần khỏi hệ thống (quản trị viên).
     */
    public boolean deleteEnrollment(Long id) {
        return enrollmentService.delete(id);
    }
}
