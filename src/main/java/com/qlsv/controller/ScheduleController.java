package com.qlsv.controller;

import com.qlsv.model.Schedule;
import com.qlsv.service.ScheduleService;

import java.util.List;

public class ScheduleController {

    private final ScheduleService scheduleService = new ScheduleService();

    public List<Schedule> getAllSchedules() {
        return scheduleService.findAll();
    }

    public List<Schedule> getCurrentStudentSchedules() {
        return scheduleService.findByCurrentStudent();
    }

    public List<Schedule> getCurrentLecturerSchedules() {
        return scheduleService.findByCurrentLecturer();
    }

    public List<Schedule> getSchedulesByCourseSection(Long courseSectionId) {
        return scheduleService.findByCourseSectionId(courseSectionId);
    }

    public List<Schedule> getSchedulesByRoom(String room) {
        return scheduleService.findByRoom(room);
    }

    public List<Schedule> getSchedulesByFaculty(Long facultyId) {
        return scheduleService.findByFacultyId(facultyId);
    }

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleService.save(schedule);
    }

    public boolean deleteSchedule(Long id) {
        return scheduleService.delete(id);
    }
}
