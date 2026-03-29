package com.qlsv.controller;

import com.qlsv.model.Lecturer;
import com.qlsv.model.Subject;
import com.qlsv.service.LecturerSubjectService;

import java.util.List;

public class LecturerSubjectController {

    private final LecturerSubjectService lecturerSubjectService = new LecturerSubjectService();

    public List<Subject> getSubjectsByLecturer(Long lecturerId) {
        return lecturerSubjectService.getSubjectsByLecturer(lecturerId);
    }

    public List<Lecturer> getLecturersBySubject(Long subjectId) {
        return lecturerSubjectService.getLecturersBySubject(subjectId);
    }

    public boolean exists(Long lecturerId, Long subjectId) {
        return lecturerSubjectService.exists(lecturerId, subjectId);
    }

    public void saveSubjectsForLecturer(Long lecturerId, List<Subject> subjects) {
        lecturerSubjectService.saveSubjectsForLecturer(lecturerId, subjects);
    }

    public int backfillFromCourseSectionsIfNeeded() {
        return lecturerSubjectService.backfillFromCourseSectionsIfNeeded();
    }
}
