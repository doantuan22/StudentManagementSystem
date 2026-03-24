package com.qlsv.model;

public class CourseSection {

    private Long id;
    private String sectionCode;
    private Subject subject;
    private Lecturer lecturer;
    private ClassRoom classRoom;
    private String semester;
    private String schoolYear;
    private String scheduleText;
    private Integer maxStudents;

    public CourseSection() {
    }

    public CourseSection(Long id, String sectionCode, Subject subject, Lecturer lecturer, ClassRoom classRoom,
                         String semester, String schoolYear, String scheduleText, Integer maxStudents) {
        this.id = id;
        this.sectionCode = sectionCode;
        this.subject = subject;
        this.lecturer = lecturer;
        this.classRoom = classRoom;
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

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
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
        return sectionCode + " - " + subjectName;
    }
}
