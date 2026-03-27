package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.EnrollmentDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Student;

import java.time.LocalDateTime;
import java.util.List;

public class EnrollmentManagementScreenController {

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final StudentController studentController = new StudentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    public List<Enrollment> loadItems(boolean filterReady, String filterType, Object filterValue,
                                      String filterAll, String filterSectionCode, String filterClassRoom, String filterStudent) {
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
        if (normalizedFilterType.equals(filterStudent) && filterValue instanceof Student student) {
            return enrollmentController.getEnrollmentsByStudent(student.getId());
        }
        return List.of();
    }

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

    public void saveEnrollment(Enrollment enrollment) {
        enrollmentController.saveEnrollment(enrollment);
    }

    public void deleteEnrollment(Enrollment enrollment) {
        enrollmentController.deleteEnrollment(enrollment.getId());
    }

    public record EnrollmentFormData(Student student, CourseSection courseSection, String status) {
    }
}
