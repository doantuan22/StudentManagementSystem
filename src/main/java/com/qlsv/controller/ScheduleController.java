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

    public Schedule saveSchedule(Schedule schedule) {
        return scheduleService.save(schedule);
    }

    public boolean deleteSchedule(Long id) {
        return scheduleService.delete(id);
    }
}
