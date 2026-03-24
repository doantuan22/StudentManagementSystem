package com.qlsv.model;

import java.util.Objects;

public class Schedule {

    private Long id;
    private CourseSection courseSection;
    private String dayOfWeek;
    private Integer startPeriod;
    private Integer endPeriod;
    private String room;
    private String note;

    public Schedule() {
    }

    public Schedule(Long id, CourseSection courseSection, String dayOfWeek, Integer startPeriod,
                    Integer endPeriod, String room, String note) {
        this.id = id;
        this.courseSection = courseSection;
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.room = room;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseSection getCourseSection() {
        return courseSection;
    }

    public void setCourseSection(CourseSection courseSection) {
        this.courseSection = courseSection;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(Integer startPeriod) {
        this.startPeriod = startPeriod;
    }

    public Integer getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(Integer endPeriod) {
        this.endPeriod = endPeriod;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String toDisplayText() {
        return dayOfWeek + " tiet " + startPeriod + "-" + endPeriod + " phong " + room;
    }

    @Override
    public String toString() {
        String sectionCode = courseSection == null ? "" : courseSection.getSectionCode();
        return sectionCode + " - " + toDisplayText();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Schedule schedule)) {
            return false;
        }
        return Objects.equals(id, schedule.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
