/**
 * Điều phối dữ liệu cho báo cáo.
 */
package com.qlsv.controller;

import com.qlsv.model.SystemStatistics;
import com.qlsv.service.ReportService;

import java.util.List;

public class ReportController {

    private final ReportService reportService = new ReportService();

    /**
     * Trả về sinh viên theo lớp.
     */
    public List<Object[]> getStudentsByClassRoom(Long classRoomId) {
        return reportService.getStudentsByClassRoom(classRoomId);
    }

    /**
     * Trả về giảng viên theo khoa.
     */
    public List<Object[]> getLecturersByFaculty(Long facultyId) {
        return reportService.getLecturersByFaculty(facultyId);
    }

    /**
     * Trả về sinh viên theo học phần.
     */
    public List<Object[]> getStudentsByCourseSection(Long courseSectionId) {
        return reportService.getStudentsByCourseSection(courseSectionId);
    }

    /**
     * Trả về điểm theo học phần.
     */
    public List<Object[]> getScoresByCourseSection(Long courseSectionId) {
        return reportService.getScoresByCourseSection(courseSectionId);
    }

    /**
     * Trả về thống kê hệ thống.
     */
    public SystemStatistics getSystemStatistics() {
        return reportService.getSystemStatistics();
    }
}
