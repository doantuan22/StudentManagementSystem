/**
 * Xử lý nghiệp vụ lịch.
 */
package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.ScoreDAO;
import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Schedule;
import com.qlsv.model.Student;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScheduleService {

    private static final String ROOM_CONFLICT_MESSAGE =
            "Phòng học đã bị trùng lịch trong ngày và khoảng tiết đã chọn.";
    private static final String LECTURER_CONFLICT_MESSAGE =
            "Giảng viên đã có lịch dạy trùng trong ngày và khoảng tiết đã chọn.";

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Lấy danh sách tất cả các lịch học trong hệ thống.
     */
    public List<Schedule> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findAll();
    }

    /**
     * Tải lịch học cá nhân của sinh viên đang đăng nhập.
     */
    public List<Schedule> findByCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCHEDULE);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        return scheduleDAO.findByStudentId(student.getId());
    }

    /**
     * Tải lịch giảng dạy của giảng viên đang đăng nhập.
     */
    public List<Schedule> findByCurrentLecturer() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCHEDULE);
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy giảng viên đang đăng nhập."));
        return scheduleDAO.findByLecturerId(lecturer.getId());
    }

    /**
     * Lấy danh sách lịch học của một học phần cụ thể.
     */
    public List<Schedule> findByCourseSectionId(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findByCourseSectionId(courseSectionId);
    }

    /**
     * Lọc lịch học theo phòng học.
     */
    public List<Schedule> findByRoom(Long roomId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findByRoom(roomId);
    }

    /**
     * Lọc lịch học theo khoa quản lý.
     */
    public List<Schedule> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return scheduleDAO.findByFacultyId(facultyId);
    }

    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    /**
     * Lưu thông tin lịch học sau khi kiểm tra xung đột phòng và giảng viên.
     */
    public Schedule save(Schedule schedule) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu lịch học.",
                ignored -> {
                    CourseSection courseSection = validate(schedule);
                    validateScheduleConflicts(schedule, courseSection);
                    Schedule savedSchedule = schedule.getId() == null ? scheduleDAO.insert(schedule) : updateAndReturn(schedule);
                    resolveStudentScheduleConflicts(courseSection);
                    return savedSchedule;
                }
        );
    }

    private void resolveStudentScheduleConflicts(CourseSection courseSection) {
        List<Enrollment> currentEnrollments = enrollmentDAO.findEffectiveByCourseSectionId(courseSection.getId());
        for (Enrollment currentEnrollment : currentEnrollments) {
            List<Enrollment> conflicts = enrollmentDAO.findEffectiveScheduleConflictsByStudent(
                    currentEnrollment.getStudent().getId(),
                    courseSection.getId(),
                    courseSection.getSemester(),
                    courseSection.getSchoolYear(),
                    courseSection.getId()
            );

            if (conflicts.isEmpty()) {
                continue;
            }

            List<Enrollment> allDuplicates = new ArrayList<>();
            allDuplicates.add(currentEnrollment);
            allDuplicates.addAll(conflicts);

            // Sort by age (keep oldest)
            allDuplicates.sort(
                    Comparator.comparing(Enrollment::getEnrolledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(Enrollment::getId, Comparator.nullsLast(Comparator.naturalOrder()))
            );

            // Keep index 0. Check scores for others.
            for (int i = 1; i < allDuplicates.size(); i++) {
                Enrollment laterEnrollment = allDuplicates.get(i);
                if (scoreDAO.findByEnrollmentId(laterEnrollment.getId()).isPresent()) {
                    throw new ValidationException("Thay đổi lịch học gây xung đột cho sinh viên " + laterEnrollment.getStudent().getFullName() + " đã có điểm. Không thể tự động xử lý.");
                }
                enrollmentDAO.delete(laterEnrollment.getId());
            }
        }
    }

    /**
     * Xóa lịch học theo mã định danh.
     */
    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_SCHEDULES);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa lịch học.",
                ignored -> {
                    scheduleDAO.findById(id)
                            .orElseThrow(() -> new ValidationException("Không tìm thấy lịch học cần xóa."));
                    return scheduleDAO.delete(id);
                }
        );
    }

    /**
     * Cập nhật and return.
     */
    private Schedule updateAndReturn(Schedule schedule) {
        scheduleDAO.update(schedule);
        return schedule;
    }

    /**
     * Kiểm tra dữ liệu hiện tại.
     */
    private CourseSection validate(Schedule schedule) {
        if (schedule.getCourseSection() == null || schedule.getCourseSection().getId() == null) {
            throw new ValidationException("Lịch học phải gắn với một học phần.");
        }
        schedule.setDayOfWeek(ValidationUtil.requireNotBlank(
                schedule.getDayOfWeek(),
                "Thứ học không được để trống."
        ));
        ValidationUtil.requirePositive(schedule.getStartPeriod(), "Tiết bắt đầu phải lớn hơn 0.");
        ValidationUtil.requirePositive(schedule.getEndPeriod(), "Tiết kết thúc phải lớn hơn 0.");
        if (schedule.getRoom() == null || schedule.getRoom().getId() == null) {
            throw new ValidationException("Phòng học không được để trống.");
        }
        if (schedule.getStartPeriod() >= schedule.getEndPeriod()) {
            throw new ValidationException("Tiết bắt đầu phải nhỏ hơn tiết kết thúc.");
        }
        return courseSectionDAO.findById(schedule.getCourseSection().getId())
                .orElseThrow(() -> new ValidationException("Học phần của lịch học không tồn tại."));
    }

    /**
     * Kiểm tra lịch conflicts.
     */
    private void validateScheduleConflicts(Schedule schedule, CourseSection courseSection) {
        if (scheduleDAO.hasRoomScheduleConflict(
                schedule.getRoom().getId(),
                schedule.getDayOfWeek(),
                schedule.getStartPeriod(),
                schedule.getEndPeriod(),
                schedule.getId()
        )) {
            throw new ValidationException(ROOM_CONFLICT_MESSAGE);
        }

        Long lecturerId = courseSection.getLecturer() == null ? null : courseSection.getLecturer().getId();
        if (lecturerId == null) {
            throw new ValidationException("Học phần phải có giảng viên phụ trách.");
        }

        if (scheduleDAO.hasLecturerScheduleConflict(
                lecturerId,
                schedule.getDayOfWeek(),
                schedule.getStartPeriod(),
                schedule.getEndPeriod(),
                schedule.getId()
        )) {
            throw new ValidationException(LECTURER_CONFLICT_MESSAGE);
        }
    }
}
