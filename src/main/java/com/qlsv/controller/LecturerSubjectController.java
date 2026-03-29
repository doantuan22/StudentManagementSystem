/**
 * Điều phối dữ liệu cho giảng viên môn học.
 */
package com.qlsv.controller;

import com.qlsv.model.Lecturer;
import com.qlsv.model.Subject;
import com.qlsv.service.LecturerSubjectService;

import java.util.List;

public class LecturerSubjectController {

    private final LecturerSubjectService lecturerSubjectService = new LecturerSubjectService();

    /**
     * Trả về môn học theo giảng viên.
     */
    public List<Subject> getSubjectsByLecturer(Long lecturerId) {
        return lecturerSubjectService.getSubjectsByLecturer(lecturerId);
    }

    /**
     * Trả về giảng viên theo môn học.
     */
    public List<Lecturer> getLecturersBySubject(Long subjectId) {
        return lecturerSubjectService.getLecturersBySubject(subjectId);
    }

    /**
     * Xử lý exists.
     */
    public boolean exists(Long lecturerId, Long subjectId) {
        return lecturerSubjectService.exists(lecturerId, subjectId);
    }

    /**
     * Lưu môn học for giảng viên.
     */
    public void saveSubjectsForLecturer(Long lecturerId, List<Subject> subjects) {
        lecturerSubjectService.saveSubjectsForLecturer(lecturerId, subjects);
    }

    /**
     * Xử lý backfill from học phần if needed.
     */
    public int backfillFromCourseSectionsIfNeeded() {
        return lecturerSubjectService.backfillFromCourseSectionsIfNeeded();
    }
}
