package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "faculties")
public class Faculty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faculty_code", nullable = false, unique = true, length = 50)
    private String facultyCode;

    @Column(name = "faculty_name", nullable = false, length = 150)
    private String facultyName;

    @Column(name = "description", length = 255)
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Faculty faculty)) {
            return false;
        }
        if (id != null && faculty.id != null) {
            return Objects.equals(id, faculty.id);
        }
        return Objects.equals(facultyCode, faculty.facultyCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : facultyCode);
    }
}
