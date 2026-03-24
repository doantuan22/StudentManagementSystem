package com.qlsv.controller;

import com.qlsv.model.SystemStatistics;
import com.qlsv.service.ReportService;

import java.util.List;

public class ReportController {

    private final ReportService reportService = new ReportService();

    public List<Object[]> getStudentsByClassRoom(Long classRoomId) {
        return reportService.getStudentsByClassRoom(classRoomId);
    }

    public List<Object[]> getLecturersByFaculty(Long facultyId) {
        return reportService.getLecturersByFaculty(facultyId);
    }

    public List<Object[]> getStudentsByCourseSection(Long courseSectionId) {
        return reportService.getStudentsByCourseSection(courseSectionId);
    }

    public List<Object[]> getScoresByCourseSection(Long courseSectionId) {
        return reportService.getScoresByCourseSection(courseSectionId);
    }

    public SystemStatistics getSystemStatistics() {
        return reportService.getSystemStatistics();
    }
}
