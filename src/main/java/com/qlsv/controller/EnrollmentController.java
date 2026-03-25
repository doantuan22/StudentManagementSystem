package com.qlsv.controller;

import com.qlsv.model.Enrollment;
import com.qlsv.service.EnrollmentService;

import java.util.List;

public class EnrollmentController {

    private final EnrollmentService enrollmentService = new EnrollmentService();

    public List<Enrollment> getAllEnrollments() {
        return enrollmentService.findAll();
    }

    public List<Enrollment> getCurrentStudentEnrollments() {
        return enrollmentService.findByCurrentStudent();
    }

    public List<Enrollment> getLecturerEnrollments(Long lecturerId) {
        return enrollmentService.findByLecturer(lecturerId);
    }

    public List<Enrollment> getEnrollmentsByCourseSection(Long courseSectionId) {
        return enrollmentService.findByCourseSectionId(courseSectionId);
    }

    public List<Enrollment> getEnrollmentsByClassRoom(Long classRoomId) {
        return enrollmentService.findByClassRoomId(classRoomId);
    }

    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentService.findByStudentId(studentId);
    }

    public Enrollment saveEnrollment(Enrollment enrollment) {
        return enrollmentService.save(enrollment);
    }

    public Enrollment registerCurrentStudent(Long courseSectionId) {
        return enrollmentService.registerCurrentStudent(courseSectionId);
    }

    public boolean cancelCurrentStudentEnrollment(Long enrollmentId) {
        return enrollmentService.cancelCurrentStudentEnrollment(enrollmentId);
    }

    public boolean deleteEnrollment(Long id) {
        return enrollmentService.delete(id);
    }
}
