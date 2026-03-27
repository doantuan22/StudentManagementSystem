package com.qlsv.dto;

public record ScheduleDisplayDto(
        Long id,
        String sectionCode,
        String subjectName,
        String lecturerName,
        String dayOfWeek,
        String periodText,
        String roomName,
        String note,
        boolean unscheduled
) {
}
