package com.qlsv.model;

public class Faculty {

    private Long id;
    private String facultyCode;
    private String facultyName;
    private String description;

    public Faculty() {
    }

    public Faculty(Long id, String facultyCode, String facultyName, String description) {
        this.id = id;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacultyCode() {
        return facultyCode;
    }

    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return facultyCode + " - " + facultyName;
    }
}
