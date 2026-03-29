/**
 * Xử lý nghiệp vụ học phần.
 */
package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.ScoreDAO;
import com.qlsv.dao.SubjectDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Schedule;
import com.qlsv.model.Subject;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CourseSectionService {

    private static final String LECTURER_CONFLICT_MESSAGE =
            "Giảng viên đã có lịch dạy trùng với lịch học hiện có của học phần.";
    private static final String LECTURER_SUBJECT_MISMATCH_MESSAGE =
            "Giảng viên chưa nằm trong whitelist môn học đã chọn.";
    private static final String STUDENT_ENROLLMENT_CONFLICT_MESSAGE =
            "Khong the dong bo dang ky hoc phan trung mon trong cung hoc ky va nam hoc.";

    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final LecturerSubjectService lecturerSubjectService = new LecturerSubjectService();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Lấy danh sách tất cả các học phần trong hệ thống.
     */
    public List<CourseSection> findAll() {
        permissionService.requireLogin();
        return courseSectionDAO.findAll();
    }

    /**
     * Lấy danh sách học phần dành cho quản trị viên với quyền quản lý.
     */
    public List<CourseSection> findAllForAdmin() {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findAll();
    }

    /**
     * Tìm kiếm các học phần do một giảng viên cụ thể phụ trách.
     */
    public List<CourseSection> findByLecturerId(Long lecturerId) {
        permissionService.requireLogin();
        return courseSectionDAO.findByLecturerId(lecturerId);
    }

    /**
     * Lọc danh sách học phần thuộc về một khoa cụ thể.
     */
    public List<CourseSection> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findByFacultyId(facultyId);
    }

    /**
     * Tìm danh sách học phần được xếp lịch tại một phòng học nhất định.
     */
    public List<CourseSection> findByRoom(Long roomId) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findByRoomId(roomId);
    }

    /**
     * Tìm kiếm học phần dựa trên mã học phần duy nhất.
     */
    public List<CourseSection> findBySectionCode(String sectionCode) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findBySectionCode(sectionCode);
    }

    /**
     * Lưu thông tin học phần (thêm mới hoặc cập nhật) sau khi kiểm tra các ràng buộc.
     */
    public CourseSection save(CourseSection courseSection) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu học phần.",
                ignored -> {
                    CourseSection existingCourseSection = loadExistingCourseSection(courseSection.getId());
                    validate(courseSection, existingCourseSection);
                    resolveStudentEnrollmentConflicts(courseSection, existingCourseSection);
                    validateLecturerScheduleConflicts(courseSection, existingCourseSection);
                    return courseSection.getId() == null ? courseSectionDAO.insert(courseSection) : updateAndReturn(courseSection);
                }
        );
    }

    /**
     * Xóa học phần khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa học phần.",
                ignored -> courseSectionDAO.delete(id)
        );
    }

    /**
     * Cập nhật and return.
     */
    private CourseSection updateAndReturn(CourseSection courseSection) {
        courseSectionDAO.update(courseSection);
        return courseSection;
    }

    /**
     * Thực hiện kiểm tra tính hợp lệ của dữ liệu học phần trước khi lưu.
     */
    private void validate(CourseSection courseSection, CourseSection existingCourseSection) {
        courseSection.setSectionCode(ValidationUtil.requireWithinLength(
                courseSection.getSectionCode(),
                50,
                "Mã học phần"
        ));
        courseSection.setSemester(AcademicFormatUtil.normalizeSemester(courseSection.getSemester(), "Học kỳ"));
        courseSection.setSchoolYear(AcademicFormatUtil.normalizeAcademicYear(courseSection.getSchoolYear(), "Năm học"));
        ValidationUtil.requirePositive(courseSection.getMaxStudents(), "Sĩ số tối đa phải lớn hơn 0.");

        if (courseSection.getSubject() == null || courseSection.getSubject().getId() == null) {
            throw new ValidationException("Học phần phải gắn với môn học.");
        }
        if (courseSection.getLecturer() == null || courseSection.getLecturer().getId() == null) {
            throw new ValidationException("Học phần phải có giảng viên phụ trách.");
        }

        subjectDAO.findById(courseSection.getSubject().getId())
                .orElseThrow(() -> new ValidationException("Môn học của học phần không tồn tại."));
        lecturerDAO.findById(courseSection.getLecturer().getId())
                .orElseThrow(() -> new ValidationException("Giảng viên của học phần không tồn tại."));

        validateLecturerSubjectWhitelist(courseSection, existingCourseSection);
    }

    /**
     * Nạp học phần hiện có.
     */
    private CourseSection loadExistingCourseSection(Long courseSectionId) {
        if (courseSectionId == null) {
            return null;
        }
        return courseSectionDAO.findById(courseSectionId)
                .orElseThrow(() -> new ValidationException("Không thể tìm thấy học phần cần cập nhật."));
    }

    /**
     * Kiểm tra xem giảng viên có được phép dạy môn học này hay không (dựa trên whitelist).
     */
    private void validateLecturerSubjectWhitelist(CourseSection courseSection, CourseSection existingCourseSection) {
        if (!shouldValidateWhitelist(courseSection, existingCourseSection)) {
            return;
        }

        Long subjectId = courseSection.getSubject().getId();
        Long lecturerId = courseSection.getLecturer().getId();
        List<Lecturer> configuredLecturers = lecturerSubjectService.getLecturersBySubject(subjectId);
        if (configuredLecturers.isEmpty()) {
            return;
        }
        if (!lecturerSubjectService.exists(lecturerId, subjectId)) {
            throw new ValidationException(LECTURER_SUBJECT_MISMATCH_MESSAGE);
        }
    }

    /**
     * Xử lý should validate whitelist.
     */
    private boolean shouldValidateWhitelist(CourseSection courseSection, CourseSection existingCourseSection) {
        if (courseSection == null || courseSection.getSubject() == null || courseSection.getLecturer() == null) {
            return false;
        }
        if (existingCourseSection == null) {
            return true;
        }
        Long currentSubjectId = existingCourseSection.getSubject() == null
                ? null
                : existingCourseSection.getSubject().getId();
        Long currentLecturerId = existingCourseSection.getLecturer() == null
                ? null
                : existingCourseSection.getLecturer().getId();
        Long newSubjectId = courseSection.getSubject().getId();
        Long newLecturerId = courseSection.getLecturer().getId();
        return !Objects.equals(currentSubjectId, newSubjectId)
                || !Objects.equals(currentLecturerId, newLecturerId);
    }

    /**
     * Kiểm tra xem giảng viên mới có bị xung đột lịch dạy với các lịch học hiện có của học phần không.
     */
    private void resolveStudentEnrollmentConflicts(CourseSection courseSection, CourseSection existingCourseSection) {
        if (!shouldValidateStudentEnrollmentConflicts(courseSection, existingCourseSection)) {
            return;
        }
        String subjectName = resolveSubjectName(courseSection);
        List<Enrollment> currentEnrollments = enrollmentDAO.findEffectiveByCourseSectionId(courseSection.getId());
        for (Enrollment currentEnrollment : currentEnrollments) {
            // Find subject name conflicts
            List<Enrollment> subjectConflicts = enrollmentDAO.findEffectiveConflictsByStudentAndSubject(
                    currentEnrollment.getStudent().getId(),
                    subjectName,
                    courseSection.getSemester(),
                    courseSection.getSchoolYear(),
                    courseSection.getId()
            );

            // Find schedule conflicts
            List<Enrollment> scheduleConflicts = enrollmentDAO.findEffectiveScheduleConflictsByStudent(
                    currentEnrollment.getStudent().getId(),
                    courseSection.getId(),
                    courseSection.getSemester(),
                    courseSection.getSchoolYear(),
                    courseSection.getId()
            );

            List<Enrollment> allDuplicates = new ArrayList<>();
            allDuplicates.add(currentEnrollment);
            allDuplicates.addAll(subjectConflicts);
            allDuplicates.addAll(scheduleConflicts);

            // De-duplicate the list of enrollments (by ID)
            Map<Long, Enrollment> uniqueDuplicates = new HashMap<>();
            for (Enrollment e : allDuplicates) {
                uniqueDuplicates.put(e.getId(), e);
            }

            if (uniqueDuplicates.size() > 1) {
                removeDuplicateEnrollments(new ArrayList<>(uniqueDuplicates.values()));
            }
        }

        if (enrollmentDAO.hasEffectiveStudentSubjectConflictForCourseSection(
                courseSection.getId(),
                subjectName,
                courseSection.getSemester(),
                courseSection.getSchoolYear()
        )) {
            throw new ValidationException(STUDENT_ENROLLMENT_CONFLICT_MESSAGE + " (Trùng môn học)");
        }

        if (enrollmentDAO.hasEffectiveStudentScheduleConflictForCourseSection(
                courseSection.getId(),
                courseSection.getSemester(),
                courseSection.getSchoolYear()
        )) {
            throw new ValidationException(STUDENT_ENROLLMENT_CONFLICT_MESSAGE + " (Trùng lịch học)");
        }
    }

    private boolean shouldValidateStudentEnrollmentConflicts(CourseSection courseSection, CourseSection existingCourseSection) {
        if (existingCourseSection == null || courseSection == null || courseSection.getSubject() == null) {
            return false;
        }
        Long currentSubjectId = existingCourseSection.getSubject() == null ? null : existingCourseSection.getSubject().getId();
        Long newSubjectId = courseSection.getSubject().getId();

        // Check if subject, semester, school year, or schedule changed
        boolean baseChanged = !Objects.equals(currentSubjectId, newSubjectId)
                || !Objects.equals(existingCourseSection.getSemester(), courseSection.getSemester())
                || !Objects.equals(existingCourseSection.getSchoolYear(), courseSection.getSchoolYear());

        if (baseChanged) return true;

        // Schedule might have changed via the same CourseSection object if schedules are edited in a separate flow but we're re-saving the section.
        // Actually, schedules are usually handled in ScheduleService. But let's check.
        return true; // Always check for safety during any save for now.
    }

    private void removeDuplicateEnrollments(List<Enrollment> duplicateEnrollments) {
        if (duplicateEnrollments == null || duplicateEnrollments.size() <= 1) {
            return;
        }

        // Group by Student to ensure we handle conflicts student by student (though we already do this above)
        // Sort by enrolledAt (ASC), then ID (ASC) to find the 'later' ones
        duplicateEnrollments.sort(
                Comparator.comparing(Enrollment::getEnrolledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Enrollment::getId, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        // Keep the first one. For all others, check scores.
        for (int i = 1; i < duplicateEnrollments.size(); i++) {
            Enrollment laterEnrollment = duplicateEnrollments.get(i);
            boolean hasScore = scoreDAO.findByEnrollmentId(laterEnrollment.getId()).isPresent();
            if (hasScore) {
                throw new ValidationException(STUDENT_ENROLLMENT_CONFLICT_MESSAGE +
                        " (Sinh viên " + laterEnrollment.getStudent().getFullName() + " đã có điểm ở đăng ký bị trùng mới phát sinh)");
            }
            // Safe to delete
            if (!enrollmentDAO.delete(laterEnrollment.getId())) {
                throw new ValidationException("Không thể tự động xóa đăng ký trùng của sinh viên " + laterEnrollment.getStudent().getFullName());
            }
        }
    }

    private String resolveSubjectName(CourseSection courseSection) {
        if (courseSection == null || courseSection.getSubject() == null) {
            return null;
        }
        String subjectName = courseSection.getSubject().getSubjectName();
        if (subjectName != null && !subjectName.isBlank()) {
            return subjectName;
        }
        Long subjectId = courseSection.getSubject().getId();
        if (subjectId == null) {
            return null;
        }
        return subjectDAO.findById(subjectId)
                .map(Subject::getSubjectName)
                .orElse(null);
    }

    private void validateLecturerScheduleConflicts(CourseSection courseSection, CourseSection existingCourseSection) {
        if (existingCourseSection == null) {
            return;
        }

        Long currentLecturerId = existingCourseSection.getLecturer() == null
                ? null
                : existingCourseSection.getLecturer().getId();
        Long newLecturerId = courseSection.getLecturer().getId();
        if (Objects.equals(currentLecturerId, newLecturerId)) {
            return;
        }

        List<Schedule> schedules = scheduleDAO.findByCourseSectionId(courseSection.getId());
        for (Schedule schedule : schedules) {
            if (scheduleDAO.hasLecturerScheduleConflict(
                    newLecturerId,
                    schedule.getDayOfWeek(),
                    schedule.getStartPeriod(),
                    schedule.getEndPeriod(),
                    schedule.getId()
            )) {
                throw new ValidationException(LECTURER_CONFLICT_MESSAGE);
            }
        }
    }
}
