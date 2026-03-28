package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.SubjectDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Schedule;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.ValidationUtil;

import java.util.List;
import java.util.Objects;

public class CourseSectionService {

    private static final String LECTURER_CONFLICT_MESSAGE =
            "Giảng viên đã có lịch dạy trùng với lịch học hiện có của học phần.";

    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<CourseSection> findAll() {
        permissionService.requireLogin();
        return courseSectionDAO.findAll();
    }

    public List<CourseSection> findAllForAdmin() {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findAll();
    }

    public List<CourseSection> findByLecturerId(Long lecturerId) {
        permissionService.requireLogin();
        return courseSectionDAO.findByLecturerId(lecturerId);
    }

    public List<CourseSection> findByFacultyId(Long facultyId) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findByFacultyId(facultyId);
    }

    public List<CourseSection> findByRoom(Long roomId) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findByRoomId(roomId);
    }

    public List<CourseSection> findBySectionCode(String sectionCode) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.findBySectionCode(sectionCode);
    }

    public CourseSection save(CourseSection courseSection) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu học phần.",
                ignored -> {
                    CourseSection existingCourseSection = loadExistingCourseSection(courseSection.getId());
                    validate(courseSection);
                    validateLecturerScheduleConflicts(courseSection, existingCourseSection);
                    return courseSection.getId() == null ? courseSectionDAO.insert(courseSection) : updateAndReturn(courseSection);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa học phần.",
                ignored -> courseSectionDAO.delete(id)
        );
    }

    private CourseSection updateAndReturn(CourseSection courseSection) {
        courseSectionDAO.update(courseSection);
        return courseSection;
    }

    private void validate(CourseSection courseSection) {
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
    }

    private CourseSection loadExistingCourseSection(Long courseSectionId) {
        if (courseSectionId == null) {
            return null;
        }
        return courseSectionDAO.findById(courseSectionId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy học phần cần cập nhật."));
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
