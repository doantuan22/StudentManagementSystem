/**
 * Xử lý nghiệp vụ báo cáo.
 */
package com.qlsv.service;

import com.qlsv.dao.ReportDAO;
import com.qlsv.model.SystemStatistics;
import com.qlsv.security.RolePermission;

import java.util.List;

public class ReportService {

    private final ReportDAO reportDAO = new ReportDAO();
    private final PermissionService permissionService = new PermissionService();

    /**
     * Trả về sinh viên theo lớp.
     */
    public List<Object[]> getStudentsByClassRoom(Long classRoomId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findStudentsByClassRoom(classRoomId);
    }

    /**
     * Trả về giảng viên theo khoa.
     */
    public List<Object[]> getLecturersByFaculty(Long facultyId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findLecturersByFaculty(facultyId);
    }

    /**
     * Trả về sinh viên theo học phần.
     */
    public List<Object[]> getStudentsByCourseSection(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findStudentsByCourseSection(courseSectionId);
    }

    /**
     * Trả về điểm theo học phần.
     */
    public List<Object[]> getScoresByCourseSection(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.VIEW_REPORTS);
        return reportDAO.findScoresByCourseSection(courseSectionId);
    }

    /**
     * Trả về thống kê hệ thống.
     */
    public SystemStatistics getSystemStatistics() {
        permissionService.requirePermission(RolePermission.VIEW_SYSTEM_STATISTICS);
        return reportDAO.getSystemStatistics();
    }
}
