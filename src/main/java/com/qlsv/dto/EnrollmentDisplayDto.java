/**
 * Gói dữ liệu hiển thị cho đăng ký hiển thị.
 */
package com.qlsv.dto;

/**
 * Xử lý đăng ký hiển thị dto.
 */
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
