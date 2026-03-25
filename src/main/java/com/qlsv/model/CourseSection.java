package com.qlsv.model;

import java.util.Objects;

public class CourseSection {

    private Long id;
    private String sectionCode;
    private Subject subject;
    private Lecturer lecturer;
    private Room room;
    private String semester;
    private String schoolYear;
    private String scheduleText;
    private Integer maxStudents;

    public CourseSection() {
    }

    public CourseSection(Long id, String sectionCode, Subject subject, Lecturer lecturer, Room room,
                         String semester, String schoolYear, String scheduleText, Integer maxStudents) {
        this.id = id;
        this.sectionCode = sectionCode;
        this.subject = subject;
        this.lecturer = lecturer;
        this.room = room;
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.scheduleText = scheduleText;
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

    public void setRoom(Room room) {
        this.room = room;
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

    public void setScheduleText(String scheduleText) {
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
        String normalizedRoom = room == null ? "" : " - " + room.getRoomName();
        return sectionCode + " - " + subjectName + normalizedRoom;
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
