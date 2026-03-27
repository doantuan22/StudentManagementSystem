package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.model.CourseSection;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.AcademicFormatUtil;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class CourseSectionService {

    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();
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
                    validate(courseSection);
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
        ValidationUtil.requireWithinLength(courseSection.getSectionCode(), 50, "Mã học phần");
        courseSection.setSemester(AcademicFormatUtil.normalizeSemester(courseSection.getSemester(), "Học kỳ"));
        courseSection.setSchoolYear(AcademicFormatUtil.normalizeAcademicYear(courseSection.getSchoolYear(), "Năm học"));
        ValidationUtil.requirePositive(courseSection.getMaxStudents(), "Sĩ số tối đa phải lớn hơn 0.");
        if (courseSection.getSubject() == null || courseSection.getSubject().getId() == null) {
            throw new IllegalArgumentException("Học phần phải gắn với môn học.");
        }
        if (courseSection.getLecturer() == null || courseSection.getLecturer().getId() == null) {
            throw new IllegalArgumentException("Học phần phải có giảng viên phụ trách.");
        }
    }
}
