package com.qlsv.service;

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
                .orElseThrow(() -> new ValidationException("Khong tim thay sinh vien dang dang nhap."));
        return enrollmentDAO.findByStudentId(student.getId());
    }

    public List<Enrollment> findByLecturer(Long lecturerId) {
        permissionService.requirePermission(RolePermission.VIEW_ASSIGNED_STUDENTS);
        return enrollmentDAO.findByLecturerId(lecturerId);
    }

    public Enrollment save(Enrollment enrollment) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        validate(enrollment, enrollment.getId() == null);
        return enrollment.getId() == null ? enrollmentDAO.insert(enrollment) : updateAndReturn(enrollment);
    }

    public Enrollment registerCurrentStudent(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay sinh vien dang dang nhap."));
        CourseSection courseSection = courseSectionDAO.findById(courseSectionId)
                .orElseThrow(() -> new ValidationException("Khong tim thay hoc phan can dang ky."));

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourseSection(courseSection);
        enrollment.setStatus("REGISTERED");
        enrollment.setEnrolledAt(LocalDateTime.now());

        validate(enrollment, true);
        return enrollmentDAO.insert(enrollment);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.delete(id);
    }

    public boolean cancelCurrentStudentEnrollment(Long enrollmentId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay sinh vien dang dang nhap."));
        Enrollment enrollment = enrollmentDAO.findById(enrollmentId)
                .orElseThrow(() -> new ValidationException("Khong tim thay dang ky hoc phan."));
        if (!student.getId().equals(enrollment.getStudent().getId())) {
            throw new ValidationException("Sinh vien chi duoc huy hoc phan cua chinh minh.");
        }
        return enrollmentDAO.delete(enrollmentId);
    }

    private Enrollment updateAndReturn(Enrollment enrollment) {
        enrollmentDAO.update(enrollment);
        return enrollment;
    }

    private void validate(Enrollment enrollment, boolean checkDuplicate) {
        if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null) {
            throw new ValidationException("Dang ky hoc phan phai co sinh vien.");
        }
        if (enrollment.getCourseSection() == null || enrollment.getCourseSection().getId() == null) {
            throw new ValidationException("Dang ky hoc phan phai co hoc phan.");
        }
        if (checkDuplicate && enrollmentDAO.existsByStudentAndCourseSection(
                enrollment.getStudent().getId(),
                enrollment.getCourseSection().getId())) {
            // Chong dang ky trung hoc phan o muc service de de thay doi quy tac ve sau.
            throw new ValidationException("Sinh vien da dang ky hoc phan nay.");
        }
        if (checkDuplicate) {
            int currentSize = enrollmentDAO.countByCourseSectionId(enrollment.getCourseSection().getId());
            Integer maxStudents = enrollment.getCourseSection().getMaxStudents();
            if (maxStudents != null && currentSize >= maxStudents) {
                throw new ValidationException("Hoc phan da day si so toi da.");
            }
            // Kiem tra trung lich dua tren bang schedules de chan dang ky sai luong nghiep vu.
            if (scheduleDAO.hasStudentScheduleConflict(enrollment.getStudent().getId(), enrollment.getCourseSection().getId(), null)) {
                throw new ValidationException("Hoc phan moi bi trung lich voi hoc phan sinh vien da dang ky.");
            }
        }
    }
}
