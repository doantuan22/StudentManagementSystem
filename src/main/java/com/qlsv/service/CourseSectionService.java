package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.SubjectDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Schedule;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.ValidationUtil;

import java.util.List;
import java.util.Objects;

public class CourseSectionService {

    private static final String LECTURER_CONFLICT_MESSAGE =
            "Giang vien da co lich day trung voi lich hoc hien co cua hoc phan.";
    private static final String LECTURER_SUBJECT_MISMATCH_MESSAGE =
            "Giang vien chua nam trong whitelist mon hoc da chon.";

    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
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
                "Khong the luu hoc phan.",
                ignored -> {
                    CourseSection existingCourseSection = loadExistingCourseSection(courseSection.getId());
                    validate(courseSection, existingCourseSection);
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
                "Khong the xoa hoc phan.",
                ignored -> courseSectionDAO.delete(id)
        );
    }

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
                "Ma hoc phan"
        ));
        courseSection.setSemester(AcademicFormatUtil.normalizeSemester(courseSection.getSemester(), "Hoc ky"));
        courseSection.setSchoolYear(AcademicFormatUtil.normalizeAcademicYear(courseSection.getSchoolYear(), "Nam hoc"));
        ValidationUtil.requirePositive(courseSection.getMaxStudents(), "Si so toi da phai lon hon 0.");

        if (courseSection.getSubject() == null || courseSection.getSubject().getId() == null) {
            throw new ValidationException("Hoc phan phai gan voi mon hoc.");
        }
        if (courseSection.getLecturer() == null || courseSection.getLecturer().getId() == null) {
            throw new ValidationException("Hoc phan phai co giang vien phu trach.");
        }

        subjectDAO.findById(courseSection.getSubject().getId())
                .orElseThrow(() -> new ValidationException("Mon hoc cua hoc phan khong ton tai."));
        lecturerDAO.findById(courseSection.getLecturer().getId())
                .orElseThrow(() -> new ValidationException("Giang vien cua hoc phan khong ton tai."));

        validateLecturerSubjectWhitelist(courseSection, existingCourseSection);
    }

    private CourseSection loadExistingCourseSection(Long courseSectionId) {
        if (courseSectionId == null) {
            return null;
        }
        return courseSectionDAO.findById(courseSectionId)
                .orElseThrow(() -> new ValidationException("Khong tim thay hoc phan can cap nhat."));
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
