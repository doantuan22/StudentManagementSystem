package com.qlsv.controller;

import com.qlsv.model.CourseSection;
import com.qlsv.service.CourseSectionService;

import java.util.List;

public class CourseSectionController {

    private final CourseSectionService courseSectionService = new CourseSectionService();

    public List<CourseSection> getAllCourseSections() {
        return courseSectionService.findAllForAdmin();
    }

    public List<CourseSection> getAllCourseSectionsForSelection() {
        return courseSectionService.findAll();
    }

    public List<CourseSection> getCourseSectionsByLecturer(Long lecturerId) {
        return courseSectionService.findByLecturerId(lecturerId);
    }

    public List<CourseSection> getCourseSectionsByFaculty(Long facultyId) {
        return courseSectionService.findByFacultyId(facultyId);
    }

    public List<CourseSection> getCourseSectionsByRoom(String room) {
        return courseSectionService.findByRoom(room);
    }

    public List<CourseSection> getCourseSectionsBySectionCode(String sectionCode) {
        return courseSectionService.findBySectionCode(sectionCode);
    }

    public CourseSection saveCourseSection(CourseSection courseSection) {
        return courseSectionService.save(courseSection);
    }

    public boolean deleteCourseSection(Long id) {
        return courseSectionService.delete(id);
    }
}
