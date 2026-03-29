/**
 * Điều phối dữ liệu cho giảng viên.
 */
package com.qlsv.controller;

import com.qlsv.model.Lecturer;
import com.qlsv.model.Subject;
import com.qlsv.service.LecturerService;

import java.util.List;

public class LecturerController {

    private final LecturerService lecturerService = new LecturerService();

    /**
     * Trả về toàn bộ giảng viên.
     */
    public List<Lecturer> getAllLecturers() {
        return lecturerService.findAll();
    }

    /**
     * Trả về giảng viên for selection.
     */
    public List<Lecturer> getLecturersForSelection() {
        return lecturerService.findAllForSelection();
    }

    /**
     * Trả về giảng viên theo khoa.
     */
    public List<Lecturer> getLecturersByFaculty(Long facultyId) {
        return lecturerService.findByFacultyId(facultyId);
    }

    /**
     * Trả về giảng viên hiện tại.
     */
    public Lecturer getCurrentLecturer() {
        return lecturerService.findCurrentLecturer();
    }

    /**
     * Lưu giảng viên.
     */
    public Lecturer saveLecturer(Lecturer lecturer) {
        return lecturerService.save(lecturer);
    }

    /**
     * Lưu giảng viên with môn học.
     */
    public Lecturer saveLecturerWithSubjects(Lecturer lecturer, List<Subject> subjects) {
        return lecturerService.saveWithSubjects(lecturer, subjects);
    }

    /**
     * Xóa giảng viên.
     */
    public boolean deleteLecturer(Long id) {
        return lecturerService.delete(id);
    }
}
