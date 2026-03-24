package com.qlsv.controller;

import com.qlsv.model.Lecturer;
import com.qlsv.service.LecturerService;

import java.util.List;

public class LecturerController {

    private final LecturerService lecturerService = new LecturerService();

    public List<Lecturer> getAllLecturers() {
        return lecturerService.findAll();
    }

    public List<Lecturer> getLecturersForSelection() {
        return lecturerService.findAllForSelection();
    }

    public Lecturer getCurrentLecturer() {
        return lecturerService.findCurrentLecturer();
    }

    public Lecturer saveLecturer(Lecturer lecturer) {
        return lecturerService.save(lecturer);
    }

    public boolean deleteLecturer(Long id) {
        return lecturerService.delete(id);
    }
}
