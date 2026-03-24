package com.qlsv.controller;

import com.qlsv.model.Subject;
import com.qlsv.service.SubjectService;

import java.util.List;

public class SubjectController {

    private final SubjectService subjectService = new SubjectService();

    public List<Subject> getAllSubjects() {
        return subjectService.findAll();
    }

    public List<Subject> getSubjectsForSelection() {
        return subjectService.findAllForSelection();
    }

    public Subject saveSubject(Subject subject) {
        return subjectService.save(subject);
    }

    public boolean deleteSubject(Long id) {
        return subjectService.delete(id);
    }
}
