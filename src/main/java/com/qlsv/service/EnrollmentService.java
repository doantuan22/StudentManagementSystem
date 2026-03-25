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
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        return enrollmentDAO.findByStudentId(student.getId());
    }

    public List<Enrollment> findByLecturer(Long lecturerId) {
        permissionService.requirePermission(RolePermission.VIEW_ASSIGNED_STUDENTS);
        return enrollmentDAO.findByLecturerId(lecturerId);
    }

    public List<Enrollment> findByCourseSectionId(Long courseSectionId) {
        return findAll().stream()
                .filter(enrollment -> enrollment.getCourseSection() != null
                        && enrollment.getCourseSection().getId() != null
                        && enrollment.getCourseSection().getId().equals(courseSectionId))
                .toList();
    }

    public List<Enrollment> findByClassRoomId(Long classRoomId) {
        return findAll().stream()
                .filter(enrollment -> enrollment.getStudent() != null
                        && enrollment.getStudent().getClassRoom() != null
                        && enrollment.getStudent().getClassRoom().getId() != null
                        && enrollment.getStudent().getClassRoom().getId().equals(classRoomId))
                .toList();
    }

    public List<Enrollment> findByStudentId(Long studentId) {
        return findAll().stream()
                .filter(enrollment -> enrollment.getStudent() != null
                        && enrollment.getStudent().getId() != null
                        && enrollment.getStudent().getId().equals(studentId))
                .toList();
    }

    public Enrollment save(Enrollment enrollment) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        validate(enrollment, enrollment.getId() == null);
        return enrollment.getId() == null ? enrollmentDAO.insert(enrollment) : updateAndReturn(enrollment);
    }

    public Enrollment registerCurrentStudent(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        CourseSection courseSection = courseSectionDAO.findById(courseSectionId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy học phần cần đăng ký."));

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
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        Enrollment enrollment = enrollmentDAO.findById(enrollmentId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy đăng ký học phần."));
        if (!student.getId().equals(enrollment.getStudent().getId())) {
            throw new ValidationException("Sinh viên chỉ được hủy học phần của chính mình.");
        }
        return enrollmentDAO.delete(enrollmentId);
    }

    private Enrollment updateAndReturn(Enrollment enrollment) {
        enrollmentDAO.update(enrollment);
        return enrollment;
    }

    private void validate(Enrollment enrollment, boolean checkDuplicate) {
        if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null) {
            throw new ValidationException("Đăng ký học phần phải có sinh viên.");
        }
        if (enrollment.getCourseSection() == null || enrollment.getCourseSection().getId() == null) {
            throw new ValidationException("Đăng ký học phần phải có học phần.");
        }
        if (checkDuplicate && enrollmentDAO.existsByStudentAndCourseSection(
                enrollment.getStudent().getId(),
                enrollment.getCourseSection().getId())) {
            // Chong dang ky trung hoc phan o muc service de de thay doi quy tac ve sau.
            throw new ValidationException("Sinh viên đã đăng ký học phần này.");
        }
        if (checkDuplicate) {
            int currentSize = enrollmentDAO.countByCourseSectionId(enrollment.getCourseSection().getId());
            Integer maxStudents = enrollment.getCourseSection().getMaxStudents();
            if (maxStudents != null && currentSize >= maxStudents) {
                throw new ValidationException("Học phần đã đủ sĩ số tối đa.");
            }
            // Kiem tra trung lich dua tren bang schedules de chan dang ky sai luong nghiep vu.
            if (scheduleDAO.hasStudentScheduleConflict(enrollment.getStudent().getId(), enrollment.getCourseSection().getId(), null)) {
                throw new ValidationException("Học phần mới bị trùng lịch với học phần sinh viên đã đăng ký.");
            }
        }
    }
}
