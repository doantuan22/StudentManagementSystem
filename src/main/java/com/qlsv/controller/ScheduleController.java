/**
 * Điều phối dữ liệu cho lịch.
 */
package com.qlsv.controller;

import com.qlsv.model.Schedule;
import com.qlsv.service.ScheduleService;

import java.util.List;

public class ScheduleController {

    private final ScheduleService scheduleService = new ScheduleService();

    /**
     * Trả về toàn bộ lịch.
     */
    public List<Schedule> getAllSchedules() {
        return scheduleService.findAll();
    }

    /**
     * Trả về sinh viên lịch hiện tại.
     */
    public List<Schedule> getCurrentStudentSchedules() {
        return scheduleService.findByCurrentStudent();
    }

    /**
     * Trả về giảng viên lịch hiện tại.
     */
    public List<Schedule> getCurrentLecturerSchedules() {
        return scheduleService.findByCurrentLecturer();
    }

    /**
     * Trả về lịch theo học phần.
     */
    public List<Schedule> getSchedulesByCourseSection(Long courseSectionId) {
        return scheduleService.findByCourseSectionId(courseSectionId);
    }

    /**
     * Trả về lịch theo phòng.
     */
    public List<Schedule> getSchedulesByRoom(Long roomId) {
        return scheduleService.findByRoom(roomId);
    }

    /**
     * Trả về lịch theo khoa.
     */
    public List<Schedule> getSchedulesByFaculty(Long facultyId) {
        return scheduleService.findByFacultyId(facultyId);
    }

    /**
     * Lưu lịch.
     */
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleService.save(schedule);
    }

    /**
     * Xóa lịch.
     */
    public boolean deleteSchedule(Long id) {
        return scheduleService.delete(id);
    }
}
