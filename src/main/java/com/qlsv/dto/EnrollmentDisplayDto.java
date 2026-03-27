package com.qlsv.dto;

public record EnrollmentDisplayDto(
        Long id,
        String studentCode,
        String studentName,
        String classRoomName,
        String sectionCode,
        String subjectName,
        String lecturerName,
        String roomName,
        String scheduleText,
        String statusText,
        String enrolledAtText
) {
}
