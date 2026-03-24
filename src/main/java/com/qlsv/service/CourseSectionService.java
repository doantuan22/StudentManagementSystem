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
        ValidationUtil.requireWithinLength(courseSection.getSectionCode(), 50, "Ma hoc phan");
        ValidationUtil.requireNotBlank(courseSection.getSemester(), "Hoc ky khong duoc de trong.");
        ValidationUtil.requireNotBlank(courseSection.getSchoolYear(), "Nam hoc khong duoc de trong.");
        ValidationUtil.requirePositive(courseSection.getMaxStudents(), "Si so toi da phai lon hon 0.");
        if (courseSection.getSubject() == null || courseSection.getSubject().getId() == null) {
            throw new IllegalArgumentException("Hoc phan phai gan voi mon hoc.");
        }
        if (courseSection.getLecturer() == null || courseSection.getLecturer().getId() == null) {
            throw new IllegalArgumentException("Hoc phan phai co giang vien phu trach.");
        }
        if (courseSection.getClassRoom() == null || courseSection.getClassRoom().getId() == null) {
            throw new IllegalArgumentException("Hoc phan phai thuoc mot lop hoc.");
        }
    }
}
