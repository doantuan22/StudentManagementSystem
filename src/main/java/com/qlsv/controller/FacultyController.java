package com.qlsv.controller;

import com.qlsv.model.Faculty;
import com.qlsv.service.FacultyService;

import java.util.List;

public class FacultyController {

    private final FacultyService facultyService = new FacultyService();

    public List<Faculty> getAllFaculties() {
        return facultyService.findAll();
    }

    public List<Faculty> getFacultiesForSelection() {
        return facultyService.findAllForSelection();
    }

    public Faculty saveFaculty(Faculty faculty) {
        return facultyService.save(faculty);
    }

    public boolean deleteFaculty(Long id) {
        return facultyService.delete(id);
    }
}
