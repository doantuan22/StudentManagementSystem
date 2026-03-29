/**
 * Mô tả thực thể môn học của hệ thống.
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
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_code", nullable = false, unique = true, length = 50)
    private String subjectCode;

    @Column(name = "subject_name", nullable = false, length = 150)
    private String subjectName;

    @Column(name = "credits", nullable = false)
    private Integer credits;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "description", length = 255)
    private String description;

    /**
     * Khởi tạo môn học.
     */
    public Subject() {
    }

    /**
     * Khởi tạo môn học.
     */
    public Subject(Long id, String subjectCode, String subjectName, Integer credits, Faculty faculty, String description) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.credits = credits;
        this.faculty = faculty;
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
     * Trả về môn học mã.
     */
    public String getSubjectCode() {
        return subjectCode;
    }

    /**
     * Cập nhật môn học mã.
     */
    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    /**
     * Trả về môn học tên.
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * Cập nhật môn học tên.
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * Trả về credits.
     */
    public Integer getCredits() {
        return credits;
    }

    /**
     * Cập nhật credits.
     */
    public void setCredits(Integer credits) {
        this.credits = credits;
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
        return subjectCode + " - " + subjectName;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Subject subject)) {
            return false;
        }
        if (id != null && subject.id != null) {
            return Objects.equals(id, subject.id);
        }
        return Objects.equals(subjectCode, subject.subjectCode);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : subjectCode);
    }
}
