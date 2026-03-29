package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.EnrollmentDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;

import java.time.LocalDateTime;
import java.util.List;

public class EnrollmentManagementScreenController {

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final StudentController studentController = new StudentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final ClassRoomController classRoomController = new ClassRoomController();
    private final FacultyController facultyController = new FacultyController();

    /**
     * Tải danh sách đăng ký học phần dựa trên các tiêu chí lọc đa dạng.
     */
    public List<Enrollment> loadItems(boolean filterReady, String filterType, Object filterValue,
                                      String filterAll, String filterSectionCode, String filterClassRoom, String filterFaculty) {
        if (!filterReady) {
            return List.of();
        }
        String normalizedFilterType = filterType == null ? "" : filterType;
        if (normalizedFilterType.equals(filterAll)) {
            return enrollmentController.getAllEnrollments();
        }
        if (normalizedFilterType.equals(filterSectionCode) && filterValue instanceof CourseSection courseSection) {
            return enrollmentController.getEnrollmentsByCourseSection(courseSection.getId());
        }
        if (normalizedFilterType.equals(filterClassRoom) && filterValue instanceof ClassRoom classRoom) {
            return enrollmentController.getEnrollmentsByClassRoom(classRoom.getId());
        }
        if (normalizedFilterType.equals(filterFaculty) && filterValue instanceof Faculty faculty) {
            return enrollmentController.getEnrollmentsByFaculty(faculty.getId());
        }
        return List.of();
    }

    /**
     * Xây dựng danh sách thông tin chi tiết của bản ghi đăng ký để hiển thị.
     */
    public List<DisplayField> buildDetailFields(Enrollment enrollment) {
        if (enrollment == null) {
            return List.of();
        }
        EnrollmentDisplayDto displayDto = toDisplayDto(enrollment);
        return List.of(
                new DisplayField("Mã sinh viên", displayDto.studentCode()),
                new DisplayField("Sinh viên", displayDto.studentName()),
                new DisplayField("Lớp", displayDto.classRoomName()),
                new DisplayField("Mã học phần", displayDto.sectionCode()),
                new DisplayField("Môn học", displayDto.subjectName()),
                new DisplayField("Phòng học", displayDto.roomName()),
                new DisplayField("Trạng thái", displayDto.statusText()),
                new DisplayField("Thời gian đăng ký", displayDto.enrolledAtText())
        );
    }

    /**
     * Chuyển đổi Enrollment sang EnrollmentDisplayDto để hiển thị trên UI.
     */
    public EnrollmentDisplayDto toDisplayDto(Enrollment enrollment) {
        return DisplayDtoMapper.toEnrollmentDisplayDto(enrollment);
    }

    public List<Student> loadStudents() {
        return studentController.getStudentsForSelection();
    }

    public List<CourseSection> loadCourseSections() {
        return courseSectionController.getAllCourseSectionsForSelection();
    }

    public List<ClassRoom> loadClassRooms() {
        return classRoomController.getClassRoomsForSelection();
    }

    public List<Faculty> loadFaculties() {
        return facultyController.getFacultiesForSelection();
    }

    /**
     * Đồng bộ dữ liệu từ form nhập liệu vào đối tượng Enrollment.
     */
    public Enrollment applyFormData(Enrollment existingItem, EnrollmentFormData formData) {
        Enrollment enrollment = existingItem == null ? new Enrollment() : existingItem;
        enrollment.setStudent(formData.student());
        enrollment.setCourseSection(formData.courseSection());
        enrollment.setStatus(formData.status());
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(LocalDateTime.now());
        }
        return enrollment;
    }

    /**
     * Gửi yêu cầu lưu bản ghi đăng ký xuống tầng nghiệp vụ.
     */
    public void saveEnrollment(Enrollment enrollment) {
        enrollmentController.saveEnrollment(enrollment);
    }

    /**
     * Gửi yêu cầu xóa bản ghi đăng ký khỏi hệ thống.
     */
    public void deleteEnrollment(Enrollment enrollment) {
        enrollmentController.deleteEnrollment(enrollment.getId());
    }

    public record EnrollmentFormData(Student student, CourseSection courseSection, String status) {
    }
}
