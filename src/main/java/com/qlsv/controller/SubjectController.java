/**
 * Điều phối dữ liệu cho môn học.
 */
package com.qlsv.controller;

import com.qlsv.model.Subject;
import com.qlsv.service.SubjectService;

import java.util.List;

public class SubjectController {

    private final SubjectService subjectService = new SubjectService();

    /**
     * Trả về toàn bộ môn học.
     */
    public List<Subject> getAllSubjects() {
        return subjectService.findAll();
    }

    /**
     * Trả về môn học for selection.
     */
    public List<Subject> getSubjectsForSelection() {
        return subjectService.findAllForSelection();
    }

    /**
     * Trả về môn học theo khoa.
     */
    public List<Subject> getSubjectsByFaculty(Long facultyId) {
        return subjectService.findByFacultyId(facultyId);
    }

    /**
     * Lưu môn học.
     */
    public Subject saveSubject(Subject subject) {
        return subjectService.save(subject);
    }

    /**
     * Xóa môn học.
     */
    public boolean deleteSubject(Long id) {
        return subjectService.delete(id);
    }
}
