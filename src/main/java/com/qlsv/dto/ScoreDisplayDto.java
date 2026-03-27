package com.qlsv.dto;

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
