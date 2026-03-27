package com.qlsv.dto;

public record CourseSectionDisplayDto(
        Long id,
        String sectionCode,
        String subjectName,
        String credits,
        String lecturerName,
        String semester,
        String schoolYear,
        String scheduleText,
        String roomName,
        String maxStudents,
        String slotsText
) {
}
