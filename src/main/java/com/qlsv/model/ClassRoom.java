/**
 * Mô tả thực thể lớp của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "class_rooms")
public class ClassRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_code", nullable = false, unique = true, length = 50)
    private String classCode;

    @Column(name = "class_name", nullable = false, length = 150)
    private String className;

    @Column(name = "academic_year", nullable = false, length = 50)
    private String academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    /**
     * Khởi tạo lớp.
     */
    public ClassRoom() {
    }

    /**
     * Khởi tạo lớp.
     */
    public ClassRoom(Long id, String classCode, String className, String academicYear, Faculty faculty) {
        this.id = id;
        this.classCode = classCode;
        this.className = className;
        this.academicYear = academicYear;
        this.faculty = faculty;
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
     * Trả về lớp mã.
     */
    public String getClassCode() {
        return classCode;
    }

    /**
     * Cập nhật lớp mã.
     */
    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    /**
     * Trả về lớp tên.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Cập nhật lớp tên.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Trả về niên khóa.
     */
    public String getAcademicYear() {
        return academicYear;
    }

    /**
     * Cập nhật niên khóa.
     */
    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    /**
     * Trả về khoa.
     */
    public Faculty getFaculty() {
        return faculty;
    }

    /**
     * Cập nhật khoa.
     */
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return classCode + " - " + className;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClassRoom classRoom)) {
            return false;
        }
        if (id != null && classRoom.id != null) {
            return Objects.equals(id, classRoom.id);
        }
        return Objects.equals(classCode, classRoom.classCode);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : classCode);
    }
}
