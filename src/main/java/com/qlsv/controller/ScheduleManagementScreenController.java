package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.ScheduleDisplayDto;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Room;
import com.qlsv.model.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ScheduleManagementScreenController {

    private final ScheduleController scheduleController = new ScheduleController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final FacultyController facultyController = new FacultyController();
    private final RoomController roomController = new RoomController();

    public List<Schedule> loadItems(boolean filterReady, String filterType, Object filterValue,
                                    String filterAll, String filterSectionCode, String filterRoom, String filterFaculty) {
        if (!filterReady) {
            return List.of();
        }

        String normalizedFilterType = filterType == null ? "" : filterType;
        List<Schedule> schedules;
        if (normalizedFilterType.equals(filterAll)) {
            schedules = new ArrayList<>(scheduleController.getAllSchedules());
        } else if (normalizedFilterType.equals(filterSectionCode) && filterValue instanceof CourseSection courseSection) {
            schedules = new ArrayList<>(scheduleController.getSchedulesByCourseSection(courseSection.getId()));
        } else if (normalizedFilterType.equals(filterRoom) && filterValue instanceof Room room) {
            schedules = new ArrayList<>(scheduleController.getSchedulesByRoom(room.getId()));
        } else if (normalizedFilterType.equals(filterFaculty) && filterValue instanceof Faculty faculty) {
            schedules = new ArrayList<>(scheduleController.getSchedulesByFaculty(faculty.getId()));
        } else {
            schedules = new ArrayList<>();
        }

        Set<Long> scheduledCourseSectionIds = schedules.stream()
                .map(schedule -> schedule.getCourseSection() != null ? schedule.getCourseSection().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<CourseSection> allSections;
        if (normalizedFilterType.equals(filterSectionCode) && filterValue instanceof CourseSection courseSection) {
            allSections = List.of(courseSection);
        } else if (normalizedFilterType.equals(filterRoom) && filterValue instanceof Room room) {
            allSections = courseSectionController.getCourseSectionsByRoom(room.getId());
        } else if (normalizedFilterType.equals(filterFaculty) && filterValue instanceof Faculty faculty) {
            allSections = courseSectionController.getCourseSectionsByFaculty(faculty.getId());
        } else {
            allSections = courseSectionController.getAllCourseSectionsForSelection();
        }

        for (CourseSection section : allSections) {
            if (!scheduledCourseSectionIds.contains(section.getId())) {
                Schedule dummy = new Schedule();
                dummy.setCourseSection(section);
                schedules.add(dummy);
            }
        }
        return schedules;
    }

    public List<DisplayField> buildDetailFields(Schedule schedule) {
        if (schedule == null) {
            return List.of();
        }
        ScheduleDisplayDto displayDto = toDisplayDto(schedule);
        return List.of(
                new DisplayField("Mã học phần", displayDto.sectionCode()),
                new DisplayField("Môn học", displayDto.subjectName()),
                new DisplayField("Giảng viên", displayDto.lecturerName()),
                new DisplayField("Thứ học", displayDto.dayOfWeek()),
                new DisplayField("Tiết học", displayDto.periodText()),
                new DisplayField("Phòng học", displayDto.roomName()),
                new DisplayField("Ghi chú", displayDto.note())
        );
    }

    public ScheduleDisplayDto toDisplayDto(Schedule schedule) {
        return DisplayDtoMapper.toScheduleDisplayDto(schedule);
    }

    public List<CourseSection> loadCourseSections() {
        return courseSectionController.getAllCourseSectionsForSelection();
    }

    public List<Faculty> loadFaculties() {
        return facultyController.getFacultiesForSelection();
    }

    public List<Room> loadRooms() {
        return roomController.getRoomsForSelection();
    }

    public Schedule applyFormData(Schedule existingItem, ScheduleFormData formData) {
        Integer startPeriod = formData.startPeriod();
        Integer endPeriod = formData.endPeriod();
        if (startPeriod == null || endPeriod == null || startPeriod >= endPeriod) {
            throw new IllegalArgumentException("Tiết bắt đầu phải nhỏ hơn tiết kết thúc.");
        }

        Schedule schedule = existingItem == null || existingItem.getId() == null ? new Schedule() : existingItem;
        schedule.setCourseSection(formData.courseSection());
        schedule.setDayOfWeek(formData.dayOfWeek());
        schedule.setStartPeriod(startPeriod);
        schedule.setEndPeriod(endPeriod);
        schedule.setRoom(formData.room());
        schedule.setNote(formData.note().trim());
        return schedule;
    }

    public void saveSchedule(Schedule schedule) {
        scheduleController.saveSchedule(schedule);
    }

    public void deleteSchedule(Schedule schedule) {
        if (schedule.getId() == null) {
            throw new IllegalArgumentException("Học phần này chưa có lịch học để xóa.");
        }
        scheduleController.deleteSchedule(schedule.getId());
    }

    public record ScheduleFormData(
            CourseSection courseSection,
            String dayOfWeek,
            Integer startPeriod,
            Integer endPeriod,
            Room room,
            String note
    ) {
    }
}
