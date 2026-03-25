package com.qlsv.service;

import com.qlsv.dao.CourseSectionDAO;
import com.qlsv.model.CourseSection;
import com.qlsv.security.RolePermission;
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
        return findAllForAdmin().stream()
                .filter(courseSection -> courseSection.getSubject() != null
                        && courseSection.getSubject().getFaculty() != null
                        && courseSection.getSubject().getFaculty().getId() != null
                        && courseSection.getSubject().getFaculty().getId().equals(facultyId))
                .toList();
    }

    public List<CourseSection> findByRoom(String room) {
        return findAllForAdmin().stream()
                .filter(courseSection -> courseSection.getRoom() != null
                        && courseSection.getRoom().equalsIgnoreCase(room == null ? "" : room.trim()))
                .toList();
    }

    public List<CourseSection> findBySectionCode(String sectionCode) {
        return findAllForAdmin().stream()
                .filter(courseSection -> courseSection.getSectionCode() != null
                        && courseSection.getSectionCode().equalsIgnoreCase(sectionCode == null ? "" : sectionCode.trim()))
                .toList();
    }

    public CourseSection save(CourseSection courseSection) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        validate(courseSection);
        return courseSection.getId() == null ? courseSectionDAO.insert(courseSection) : updateAndReturn(courseSection);
    }

    public boolean delete(Long id) {
        permissionService.requirePermission(RolePermission.MANAGE_COURSE_SECTIONS);
        return courseSectionDAO.delete(id);
    }

    private CourseSection updateAndReturn(CourseSection courseSection) {
        courseSectionDAO.update(courseSection);
        return courseSection;
    }

    private void validate(CourseSection courseSection) {
        ValidationUtil.requireWithinLength(courseSection.getSectionCode(), 50, "Mã học phần");
        ValidationUtil.requireWithinLength(courseSection.getRoom(), 50, "Phòng học");
        ValidationUtil.requireNotBlank(courseSection.getSemester(), "Học kỳ không được để trống.");
        ValidationUtil.requireNotBlank(courseSection.getSchoolYear(), "Năm học không được để trống.");
        ValidationUtil.requirePositive(courseSection.getMaxStudents(), "Sĩ số tối đa phải lớn hơn 0.");
        if (courseSection.getSubject() == null || courseSection.getSubject().getId() == null) {
            throw new IllegalArgumentException("Học phần phải gắn với môn học.");
        }
        if (courseSection.getLecturer() == null || courseSection.getLecturer().getId() == null) {
            throw new IllegalArgumentException("Học phần phải có giảng viên phụ trách.");
        }
    }
}
