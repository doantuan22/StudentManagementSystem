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

    /**
     * Lấy danh sách tất cả các bản ghi đăng ký học phần (quyền quản lý).
     */
    public List<Enrollment> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findAll();
    }

    /**
     * Lấy danh sách học phần đã đăng ký của sinh viên hiện đang đăng nhập.
     */
    public List<Enrollment> findByCurrentStudent() {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        return enrollmentDAO.findByStudentId(student.getId());
    }

    /**
     * Tìm kiếm danh sách sinh viên đăng ký các học phần do giảng viên phụ trách.
     */
    public List<Enrollment> findByLecturer(Long lecturerId) {
        permissionService.requirePermission(RolePermission.VIEW_ASSIGNED_STUDENTS);
        return enrollmentDAO.findByLecturerId(lecturerId);
    }

    /**
     * Lấy danh sách sinh viên đã đăng ký vào một học phần cụ thể.
     */
    public List<Enrollment> findByCourseSectionId(Long courseSectionId) {
        permissionService.requireLogin();
        return enrollmentDAO.findByCourseSectionId(courseSectionId);
    }

    /**
     * Đếm số lượng sinh viên hiện có trong một học phần.
     */
    public int countByCourseSectionId(Long courseSectionId) {
        permissionService.requireLogin();
        return enrollmentDAO.countByCourseSectionId(courseSectionId);
    }

    /**
     * Lấy danh sách đăng ký học phần thuộc về một lớp sinh hoạt cụ thể.
     */
    public List<Enrollment> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByClassRoomId(classRoomId);
    }

    /**
     * Lọc danh sách đăng ký học phần theo khoa.
     */
    public List<Enrollment> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByFacultyId(facultyId);
    }

    /**
     * Lưu thông tin đăng ký (thêm mới/cập nhật) và thực hiện kiểm tra ràng buộc sĩ số, trùng lịch.
     */
    public Enrollment save(Enrollment enrollment) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu đăng ký học phần.",
                ignored -> {
                    validate(enrollment, enrollment.getId() == null);
                    return enrollment.getId() == null ? enrollmentDAO.insert(enrollment) : updateAndReturn(enrollment);
                }
        );
    }

    /**
     * Thực hiện quy trình đăng ký học phần cho sinh viên hiện tại kèm theo validate nghiệp vụ.
     */
    public Enrollment registerCurrentStudent(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        return JpaBootstrap.executeInTransaction(
                "Không thể đăng ký học phần.",
                ignored -> {
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
        );
    }

    /**
     * Xóa một bản ghi đăng ký học phần theo mã định danh.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa đăng ký học phần.",
                ignored -> enrollmentDAO.delete(id)
        );
    }

    /**
     * Hủy đăng ký học phần cho sinh viên hiện tại (chỉ được hủy phần của chính mình).
     */
    public boolean cancelCurrentStudentEnrollment(Long enrollmentId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        return JpaBootstrap.executeInTransaction(
                "Không thể hủy đăng ký học phần.",
                ignored -> {
                    Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                            .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
                    Enrollment enrollment = enrollmentDAO.findById(enrollmentId)
                            .orElseThrow(() -> new ValidationException("Không tìm thấy đăng ký học phần."));
                    if (!student.getId().equals(enrollment.getStudent().getId())) {
                        throw new ValidationException("Sinh viên chỉ được hủy học phần của chính mình.");
                    }
                    return enrollmentDAO.delete(enrollmentId);
                }
        );
    }

    private Enrollment updateAndReturn(Enrollment enrollment) {
        enrollmentDAO.update(enrollment);
        return enrollment;
    }

    /**
     * Kiểm tra tính hợp lệ của việc đăng ký (trùng môn, trùng học phần, sĩ số, xung đột lịch).
     */
    private void validate(Enrollment enrollment, boolean checkDuplicate) {
        if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null) {
            throw new ValidationException("Đăng ký học phần phải có sinh viên.");
        }
        if (enrollment.getCourseSection() == null || enrollment.getCourseSection().getId() == null) {
            throw new ValidationException("Đăng ký học phần phải có học phần.");
        }

        Student existingStudent = studentDAO.findById(enrollment.getStudent().getId())
                .orElseThrow(() -> new ValidationException("Sinh viên của đăng ký không tồn tại."));
        CourseSection existingCourseSection = courseSectionDAO.findById(enrollment.getCourseSection().getId())
                .orElseThrow(() -> new ValidationException("Học phần của đăng ký không tồn tại."));

        if (checkDuplicate && enrollmentDAO.existsByStudentAndSubject(
                existingStudent.getId(),
                existingCourseSection.getSubject().getId())) {
            throw new ValidationException("Sinh viên đã đăng ký học phần khác của cùng môn học này.");
        }
        if (checkDuplicate && enrollmentDAO.existsByStudentAndCourseSection(
                existingStudent.getId(),
                existingCourseSection.getId())) {
            throw new ValidationException("Sinh viên đã đăng ký học phần này.");
        }
        if (checkDuplicate) {
            int currentSize = enrollmentDAO.countByCourseSectionId(existingCourseSection.getId());
            Integer maxStudents = existingCourseSection.getMaxStudents();
            if (maxStudents != null && currentSize >= maxStudents) {
                throw new ValidationException("Học phần đã đủ sĩ số tối đa.");
            }
            if (scheduleDAO.hasStudentScheduleConflict(existingStudent.getId(), existingCourseSection.getId(), null)) {
                throw new ValidationException("Học phần mới bị trùng lịch với học phần sinh viên đã đăng ký.");
            }
        }
    }
}
