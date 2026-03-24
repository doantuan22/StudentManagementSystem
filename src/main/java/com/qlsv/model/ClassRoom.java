package com.qlsv.model;

public class ClassRoom {

    private Long id;
    private String classCode;
    private String className;
    private String academicYear;
    private Faculty faculty;

    public ClassRoom() {
    }

    public ClassRoom(Long id, String classCode, String className, String academicYear, Faculty faculty) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.academicYear = academicYear;
        this.faculty = faculty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    @Override
    public String toString() {
        return classCode + " - " + className;
    }
}
