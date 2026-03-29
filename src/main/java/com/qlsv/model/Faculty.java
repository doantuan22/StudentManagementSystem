/**
 * Mô tả thực thể khoa của hệ thống.
 */
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

    /**
     * Khởi tạo khoa.
     */
    public Faculty() {
    }

    /**
     * Khởi tạo khoa.
     */
    public Faculty(Long id, String facultyCode, String facultyName, String description) {
        this.id = id;
        this.facultyCode = facultyCode;
        this.facultyName = facultyName;
        this.description = description;
    }

    /**
     * Trả về id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Cập nhật id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Trả về khoa mã.
     */
    public String getFacultyCode() {
        return facultyCode;
    }

    /**
     * Cập nhật khoa mã.
     */
    public void setFacultyCode(String facultyCode) {
        this.facultyCode = facultyCode;
    }

    /**
     * Trả về khoa tên.
     */
    public String getFacultyName() {
        return facultyName;
    }

    /**
     * Cập nhật khoa tên.
     */
    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    /**
     * Trả về description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Cập nhật description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return facultyCode + " - " + facultyName;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
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

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : facultyCode);
    }
}
