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
import jakarta.persistence.Transient;

import java.util.Objects;

@Entity
@Table(name = "course_sections")
public class CourseSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_code", nullable = false, unique = true, length = 50)
    private String sectionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private Lecturer lecturer;

    // Compatibility - remove later when all screens read room data from Schedule/DTO instead of CourseSection.
    @Transient
    private Room room;

    @Column(name = "semester", nullable = false, length = 30)
    private String semester;

    @Column(name = "school_year", nullable = false, length = 30)
    private String schoolYear;

    // Compatibility - remove later when all screens read schedule text from DTO/query projection layers.
    @Transient
    private String scheduleText;

    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    public CourseSection() {
    }

    public CourseSection(Long id, String sectionCode, Subject subject, Lecturer lecturer, Room room,
                         String semester, String schoolYear, String scheduleText, Integer maxStudents) {
        this.id = id;
        this.sectionCode = sectionCode;
        this.subject = subject;
        this.lecturer = lecturer;
        this.semester = semester;
        this.schoolYear = schoolYear;
        applyScheduleCompatibility(room, scheduleText);
        this.maxStudents = maxStudents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public Room getRoom() {
        return room;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(String schoolYear) {
        this.schoolYear = schoolYear;
    }

    public String getScheduleText() {
        return scheduleText;
    }

    public void applyScheduleCompatibility(Room room, String scheduleText) {
        this.room = room;
        this.scheduleText = scheduleText;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    @Override
    public String toString() {
        String subjectName = subject == null ? "" : subject.getSubjectName();
        return sectionCode + " - " + subjectName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof CourseSection courseSection)) {
            return false;
        }
        if (id != null && courseSection.id != null) {
            return Objects.equals(id, courseSection.id);
        }
        return Objects.equals(sectionCode, courseSection.sectionCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : sectionCode);
    }
}
