/**
 * Điều phối dữ liệu cho khoa.
 */
package com.qlsv.controller;

import com.qlsv.model.Faculty;
import com.qlsv.service.FacultyService;

import java.util.List;

public class FacultyController {

    private final FacultyService facultyService = new FacultyService();

    /**
     * Trả về toàn bộ khoa.
     */
    public List<Faculty> getAllFaculties() {
        return facultyService.findAll();
    }

    /**
     * Trả về khoa for selection.
     */
    public List<Faculty> getFacultiesForSelection() {
        return facultyService.findAllForSelection();
    }

    /**
     * Trả về khoa theo mã.
     */
    public List<Faculty> getFacultiesByCode(String facultyCode) {
        return facultyService.findByCode(facultyCode);
    }

    /**
     * Lưu khoa.
     */
    public Faculty saveFaculty(Faculty faculty) {
        return facultyService.save(faculty);
    }

    /**
     * Xóa khoa.
     */
    public boolean deleteFaculty(Long id) {
        return facultyService.delete(id);
    }
}
