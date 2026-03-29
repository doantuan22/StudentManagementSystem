/**
 * Gói dữ liệu hiển thị cho học phần hiển thị.
 */
package com.qlsv.dto;

/**
 * Xử lý học phần hiển thị dto.
 */
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
