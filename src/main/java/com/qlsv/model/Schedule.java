/**
 * Mô tả thực thể lịch của hệ thống.
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
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_section_id", nullable = false)
    private CourseSection courseSection;

    @Column(name = "day_of_week", nullable = false, length = 20)
    private String dayOfWeek;

    @Column(name = "start_period", nullable = false)
    private Integer startPeriod;

    @Column(name = "end_period", nullable = false)
    private Integer endPeriod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "note", length = 255)
    private String note;

    /**
     * Khởi tạo lịch.
     */
    public Schedule() {
    }

    /**
     * Khởi tạo lịch.
     */
    public Schedule(Long id, CourseSection courseSection, String dayOfWeek, Integer startPeriod,
                    Integer endPeriod, Room room, String note) {
        this.id = id;
        this.courseSection = courseSection;
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.room = room;
        this.note = note;
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
     * Trả về day of week.
     */
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Cập nhật day of week.
     */
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    /**
     * Trả về start period.
     */
    public Integer getStartPeriod() {
        return startPeriod;
    }

    /**
     * Cập nhật start period.
     */
    public void setStartPeriod(Integer startPeriod) {
        this.startPeriod = startPeriod;
    }

    /**
     * Trả về end period.
     */
    public Integer getEndPeriod() {
        return endPeriod;
    }

    /**
     * Cập nhật end period.
     */
    public void setEndPeriod(Integer endPeriod) {
        this.endPeriod = endPeriod;
    }

    /**
     * Trả về phòng.
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Cập nhật phòng.
     */
    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * Trả về note.
     */
    public String getNote() {
        return note;
    }

    /**
     * Cập nhật note.
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Xử lý to hiển thị văn bản.
     */
    public String toDisplayText() {
        return dayOfWeek + " tiết " + startPeriod + "-" + endPeriod + " phòng " + (room != null ? room.getRoomName() : "Trống");
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        String sectionCode = courseSection == null ? "" : courseSection.getSectionCode();
        return sectionCode + " - " + toDisplayText();
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
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

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
