/**
 * Mô tả thực thể đăng ký của hệ thống.
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

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_section_id", nullable = false)
    private CourseSection courseSection;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    /**
     * Khởi tạo đăng ký.
     */
    public Enrollment() {
    }

    /**
     * Khởi tạo đăng ký.
     */
    public Enrollment(Long id, Student student, CourseSection courseSection, String status, LocalDateTime enrolledAt) {
        this.id = id;
        this.student = student;
        this.courseSection = courseSection;
        this.status = status;
        this.enrolledAt = enrolledAt;
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
     * Trả về sinh viên.
     */
    public Student getStudent() {
        return student;
    }

    /**
     * Cập nhật sinh viên.
     */
    public void setStudent(Student student) {
        this.student = student;
    }

    /**
     * Trả về học phần.
     */
    public CourseSection getCourseSection() {
        return courseSection;
    }

    /**
     * Cập nhật học phần.
     */
    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
    }

    /**
     * Trả về trạng thái.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Cập nhật trạng thái.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Trả về enrolled at.
     */
    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    /**
     * Cập nhật enrolled at.
     */
    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        String studentName = student == null ? "" : student.getFullName();
        String sectionCode = courseSection == null ? "" : courseSection.getSectionCode();
        return studentName + " - " + sectionCode;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Enrollment enrollment)) {
            return false;
        }
        return Objects.equals(id, enrollment.id);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
