package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Student;
import com.qlsv.security.RolePermission;

import java.time.LocalDateTime;
import java.util.List;

public class EnrollmentService {

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Enrollment> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findAll();
    }

    public List<Enrollment> findByCurrentStudent() {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y sinh viÃªn Ä‘ang Ä‘Äƒng nháº­p."));
        return enrollmentDAO.findByStudentId(student.getId());
    }

    public List<Enrollment> findByLecturer(Long lecturerId) {
        permissionService.requirePermission(RolePermission.VIEW_ASSIGNED_STUDENTS);
        return enrollmentDAO.findByLecturerId(lecturerId);
    }

    public List<Enrollment> findByCourseSectionId(Long courseSectionId) {
        permissionService.requireLogin();
        return enrollmentDAO.findByCourseSectionId(courseSectionId);
    }

    public int countByCourseSectionId(Long courseSectionId) {
        permissionService.requireLogin();
        return enrollmentDAO.countByCourseSectionId(courseSectionId);
    }

    public List<Enrollment> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByClassRoomId(classRoomId);
    }

    public List<Enrollment> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByFacultyId(facultyId);
    }

    public List<Enrollment> findByStudentId(Long studentId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByStudentId(studentId);
    }

    public Enrollment save(Enrollment enrollment) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ lÆ°u Ä‘Äƒng kÃ½ há»c pháº§n.",
                ignored -> {
                    validate(enrollment, enrollment.getId() == null);
                    return enrollment.getId() == null ? enrollmentDAO.insert(enrollment) : updateAndReturn(enrollment);
                }
        );
    }

    public Enrollment registerCurrentStudent(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ Ä‘Äƒng kÃ½ há»c pháº§n.",
                ignored -> {
                    Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                            .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y sinh viÃªn Ä‘ang Ä‘Äƒng nháº­p."));
                    CourseSection courseSection = courseSectionDAO.findById(courseSectionId)
                            .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y há»c pháº§n cáº§n Ä‘Äƒng kÃ½."));

                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudent(student);
                    enrollment.setCourseSection(courseSection);
                    enrollment.setStatus("REGISTERED");
                    enrollment.setEnrolledAt(LocalDateTime.now());

                    validate(enrollment, true);
                    return enrollmentDAO.insert(enrollment);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a Ä‘Äƒng kÃ½ há»c pháº§n.",
                ignored -> enrollmentDAO.delete(id)
        );
    }

    public boolean cancelCurrentStudentEnrollment(Long enrollmentId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ há»§y Ä‘Äƒng kÃ½ há»c pháº§n.",
                ignored -> {
                    Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                            .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y sinh viÃªn Ä‘ang Ä‘Äƒng nháº­p."));
                    Enrollment enrollment = enrollmentDAO.findById(enrollmentId)
                            .orElseThrow(() -> new ValidationException("KhÃ´ng tÃ¬m tháº¥y Ä‘Äƒng kÃ½ há»c pháº§n."));
                    if (!student.getId().equals(enrollment.getStudent().getId())) {
                        throw new ValidationException("Sinh viÃªn chá»‰ Ä‘Æ°á»£c há»§y há»c pháº§n cá»§a chÃ­nh mÃ¬nh.");
                    }
                    return enrollmentDAO.delete(enrollmentId);
                }
        );
    }

    private Enrollment updateAndReturn(Enrollment enrollment) {
        enrollmentDAO.update(enrollment);
        return enrollment;
    }

    private void validate(Enrollment enrollment, boolean checkDuplicate) {
        if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null) {
            throw new ValidationException("ÄÄƒng kÃ½ há»c pháº§n pháº£i cÃ³ sinh viÃªn.");
        }
        if (enrollment.getCourseSection() == null || enrollment.getCourseSection().getId() == null) {
            throw new ValidationException("ÄÄƒng kÃ½ há»c pháº§n pháº£i cÃ³ há»c pháº§n.");
        }

        Student existingStudent = studentDAO.findById(enrollment.getStudent().getId())
                .orElseThrow(() -> new ValidationException("Sinh viÃªn cá»§a Ä‘Äƒng kÃ½ khÃ´ng tá»“n táº¡i."));
        CourseSection existingCourseSection = courseSectionDAO.findById(enrollment.getCourseSection().getId())
                .orElseThrow(() -> new ValidationException("Há»c pháº§n cá»§a Ä‘Äƒng kÃ½ khÃ´ng tá»“n táº¡i."));

        if (checkDuplicate && enrollmentDAO.existsByStudentAndSubject(
                existingStudent.getId(),
                existingCourseSection.getSubject().getId())) {
            throw new ValidationException("Sinh viÃªn Ä‘Ã£ Ä‘Äƒng kÃ½ há»c pháº§n khÃ¡c cá»§a cÃ¹ng mÃ´n há»c nÃ y.");
        }
        if (checkDuplicate && enrollmentDAO.existsByStudentAndCourseSection(
                existingStudent.getId(),
                existingCourseSection.getId())) {
            throw new ValidationException("Sinh viÃªn Ä‘Ã£ Ä‘Äƒng kÃ½ há»c pháº§n nÃ y.");
        }
        if (checkDuplicate) {
            int currentSize = enrollmentDAO.countByCourseSectionId(existingCourseSection.getId());
            Integer maxStudents = existingCourseSection.getMaxStudents();
            if (maxStudents != null && currentSize >= maxStudents) {
                throw new ValidationException("Há»c pháº§n Ä‘Ã£ Ä‘á»§ sÄ© sá»‘ tá»‘i Ä‘a.");
            }
            if (scheduleDAO.hasStudentScheduleConflict(existingStudent.getId(), existingCourseSection.getId(), null)) {
                throw new ValidationException("Há»c pháº§n má»›i bá»‹ trÃ¹ng lá»‹ch vá»›i há»c pháº§n sinh viÃªn Ä‘Ã£ Ä‘Äƒng kÃ½.");
            }
        }
    }
}
