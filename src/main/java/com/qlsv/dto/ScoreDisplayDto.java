/**
 * Gói dữ liệu hiển thị cho điểm hiển thị.
 */
package com.qlsv.dto;

/**
 * Xử lý điểm hiển thị dto.
 */
public record ScoreDisplayDto(
        Long id,
        String studentCode,
        String studentName,
        String classRoomName,
        String sectionCode,
        String subjectName,
        String lecturerName,
        String roomName,
        String processScore,
        String midtermScore,
        String finalScore,
        String totalScore,
        String resultText
) {
}
