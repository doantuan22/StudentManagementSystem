/**
 * Gói dữ liệu hiển thị cho lịch hiển thị.
 */
package com.qlsv.dto;

/**
 * Xử lý lịch hiển thị dto.
 */
public record ScheduleDisplayDto(
        Long id,
        String sectionCode,
        String subjectName,
        String lecturerName,
        String semester,
        String schoolYear,
        String dayOfWeek,
        String periodText,
        String roomName,
        String note,
        boolean unscheduled
) {
}
