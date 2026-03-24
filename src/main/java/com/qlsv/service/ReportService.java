package com.qlsv.service;

import com.qlsv.dao.ReportDAO;
import com.qlsv.model.SystemStatistics;
import com.qlsv.security.RolePermission;

import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Object[]> getStudentsByClassRoom(Long classRoomId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findStudentsByClassRoom(classRoomId);
    }

    public List<Object[]> getLecturersByFaculty(Long facultyId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findLecturersByFaculty(facultyId);
    }

    public List<Object[]> getStudentsByCourseSection(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findStudentsByCourseSection(courseSectionId);
    }

    public List<Object[]> getScoresByCourseSection(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findScoresByCourseSection(courseSectionId);
    }

    public SystemStatistics getSystemStatistics() {
        permissionService.requirePermission(RolePermission.VIEW_SYSTEM_STATISTICS);
        return reportDAO.getSystemStatistics();
    }
}
