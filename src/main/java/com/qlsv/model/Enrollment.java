package com.qlsv.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Enrollment {

    private Long id;
    private Student student;
    private CourseSection courseSection;
    private String status;
    private LocalDateTime enrolledAt;

    public Enrollment() {
    }

    public Enrollment(Long id, Student student, CourseSection courseSection, String status, LocalDateTime enrolledAt) {
        this.id = id;
        this.student = student;
        this.courseSection = courseSection;
        this.status = status;
        this.enrolledAt = enrolledAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public CourseSection getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    @Override
    public String toString() {
        String studentName = student == null ? "" : student.getFullName();
        String sectionCode = courseSection == null ? "" : courseSection.getSectionCode();
        return studentName + " - " + sectionCode;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
