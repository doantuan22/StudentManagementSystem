/**
 * Xu ly nghiep vu dang ky hoc phan.
 */
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

    private static final String EFFECTIVE_ENROLLMENT_STATUS = "REGISTERED";

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Lay danh sach tat ca cac ban ghi dang ky hoc phan (quyen quan ly).
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
                .orElseThrow(() -> new ValidationException("Không thể tìm thấy sinh viên đang đăng nhập."));
        return enrollmentDAO.findByStudentId(student.getId());
    }

    /**
     * Lấy danh sách học phần đã đăng ký của sinh viên hiện đang đăng nhập mà chưa có điểm.
     */
    public List<Enrollment> findByCurrentStudentWithoutScore() {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không thể tìm thấy sinh viên đang đăng nhập."));
        return enrollmentDAO.findByStudentIdWithoutScore(student.getId());
    }

    /**
     * Tim kiem danh sach sinh vien dang ky cac hoc phan do giang vien phu trach.
     */
    public List<Enrollment> findByLecturer(Long lecturerId) {
        permissionService.requirePermission(RolePermission.VIEW_ASSIGNED_STUDENTS);
        return enrollmentDAO.findByLecturerId(lecturerId);
    }

    /**
     * Lay danh sach sinh vien da dang ky vao mot hoc phan cu the.
     */
    public List<Enrollment> findByCourseSectionId(Long courseSectionId) {
        permissionService.requireLogin();
        return enrollmentDAO.findByCourseSectionId(courseSectionId);
    }

    /**
     * Dem so luong sinh vien hien co trong mot hoc phan.
     */
    public int countByCourseSectionId(Long courseSectionId) {
        permissionService.requireLogin();
        return enrollmentDAO.countByCourseSectionId(courseSectionId);
    }

    /**
     * Lay danh sach dang ky hoc phan thuoc ve mot lop sinh hoat cu the.
     */
    public List<Enrollment> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByClassRoomId(classRoomId);
    }

    /**
     * Loc danh sach dang ky hoc phan theo khoa.
     */
    public List<Enrollment> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return enrollmentDAO.findByFacultyId(facultyId);
    }

    /**
     * Luu thong tin dang ky (them moi/cap nhat).
     */
    public Enrollment save(Enrollment enrollment) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return JpaBootstrap.executeInTransaction(
                "Khong the luu dang ky hoc phan.",
                ignored -> {
                    Enrollment enrollmentToPersist = reuseInactiveEnrollmentIfPossible(enrollment);
                    validate(enrollmentToPersist);
                    return enrollmentToPersist.getId() == null
                            ? enrollmentDAO.insert(enrollmentToPersist)
                            : updateAndReturn(enrollmentToPersist);
                }
        );
    }

    /**
     * Dang ky hoc phan cho sinh vien hien tai.
     */
    public Enrollment registerCurrentStudent(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        return JpaBootstrap.executeInTransaction(
                "Khong the dang ky hoc phan.",
                ignored -> {
                    Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                            .orElseThrow(() -> new ValidationException("Không thể tìm thấy sinh viên đang đăng nhập."));
                    CourseSection courseSection = courseSectionDAO.findById(courseSectionId)
                            .orElseThrow(() -> new ValidationException("Không thể tìm thấy học phần cần đăng ký."));

                    Enrollment enrollment = new Enrollment();
                    enrollment.setStudent(student);
                    enrollment.setCourseSection(courseSection);
                    enrollment.setStatus(EFFECTIVE_ENROLLMENT_STATUS);
                    enrollment.setEnrolledAt(LocalDateTime.now());

                    Enrollment enrollmentToPersist = reuseInactiveEnrollmentIfPossible(enrollment);
                    validate(enrollmentToPersist);
                    return enrollmentToPersist.getId() == null
                            ? enrollmentDAO.insert(enrollmentToPersist)
                            : updateAndReturn(enrollmentToPersist);
                }
        );
    }

    /**
     * Xoa mot ban ghi dang ky hoc phan theo ma dinh danh.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_ENROLLMENTS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa đăng ký học phần.",
                ignored -> enrollmentDAO.delete(id)
        );
    }

    /**
     * Huy dang ky hoc phan cho sinh vien hien tai.
     */
    public boolean cancelCurrentStudentEnrollment(Long enrollmentId) {
        permissionService.requirePermission(RolePermission.REGISTER_ENROLLMENT);
        return JpaBootstrap.executeInTransaction(
                "Không thể hủy đăng ký học phần.",
                ignored -> {
                    Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                            .orElseThrow(() -> new ValidationException("Không thể tìm thấy sinh viên đang đăng nhập."));
                    Enrollment enrollment = enrollmentDAO.findById(enrollmentId)
                            .orElseThrow(() -> new ValidationException("Không thể tìm thấy đăng ký học phần cần hủy."));
                    if (!student.getId().equals(enrollment.getStudent().getId())) {
                        throw new ValidationException("Sinh viên chỉ được hủy học phần của chính mình.");
                    }
                    return enrollmentDAO.delete(enrollmentId);
                }
        );
    }

    /**
     * Cap nhat va tra ve enrollment.
     */
    private Enrollment updateAndReturn(Enrollment enrollment) {
        enrollmentDAO.update(enrollment);
        return enrollment;
    }

    /**
     * Kiem tra tinh hop le cua viec dang ky.
     */
    private void validate(Enrollment enrollment) {
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

        if (!isEffectiveEnrollmentStatus(enrollment.getStatus())) {
            return;
        }

        Long currentEnrollmentId = enrollment.getId();
        if (enrollmentDAO.existsByStudentAndSubject(
                existingStudent.getId(),
                existingCourseSection.getSubject().getSubjectName(),
                existingCourseSection.getSemester(),
                existingCourseSection.getSchoolYear(),
                currentEnrollmentId)) {
            throw new ValidationException("Sinh viên đã đăng ký học phần khác của cùng môn học này trong cùng học kỳ và năm học.");
        }
        if (enrollmentDAO.existsByStudentAndCourseSection(
                existingStudent.getId(),
                existingCourseSection.getId(),
                currentEnrollmentId)) {
            throw new ValidationException("Sinh viên đã đăng ký học phần này.");
        }

        int currentSize = enrollmentDAO.countByCourseSectionId(existingCourseSection.getId(), currentEnrollmentId);
        Integer maxStudents = existingCourseSection.getMaxStudents();
        if (maxStudents != null && currentSize >= maxStudents) {
            throw new ValidationException("Học phần đã đủ sĩ số tối đa.");
        }
        if (scheduleDAO.hasStudentScheduleConflict(
                existingStudent.getId(),
                existingCourseSection.getId(),
                currentEnrollmentId)) {
            throw new ValidationException("Học phần mới bị trùng lịch với học phần sinh viên đã đăng ký.");
        }
    }

    /**
     * Tái sử dụng bản ghi không còn hiệu lực của cùng sinh viên/cùng học phần nếu cần.
     */
    private Enrollment reuseInactiveEnrollmentIfPossible(Enrollment enrollment) {
        if (enrollment.getId() != null || !isEffectiveEnrollmentStatus(enrollment.getStatus())) {
            return enrollment;
        }
        if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null
                || enrollment.getCourseSection() == null || enrollment.getCourseSection().getId() == null) {
            return enrollment;
        }

        Enrollment existingEnrollment = enrollmentDAO.findByStudentAndCourseSection(
                enrollment.getStudent().getId(),
                enrollment.getCourseSection().getId()
        ).orElse(null);
        if (existingEnrollment == null || isEffectiveEnrollmentStatus(existingEnrollment.getStatus())) {
            return enrollment;
        }

        enrollment.setId(existingEnrollment.getId());
        if (enrollment.getEnrolledAt() == null) {
            enrollment.setEnrolledAt(existingEnrollment.getEnrolledAt());
        }
        return enrollment;
    }

    /**
     * Trang thai dang ky con hieu luc.
     */
    private boolean isEffectiveEnrollmentStatus(String status) {
        return status == null || status.isBlank()
                || EFFECTIVE_ENROLLMENT_STATUS.equalsIgnoreCase(status.trim());
    }
}
