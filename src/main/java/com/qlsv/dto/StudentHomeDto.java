package com.qlsv.dto;

import com.qlsv.controller.DisplayField;

import java.util.List;

public record StudentHomeDto(
        String subtitle,
        String enrollmentCount,
        String totalCredits,
        String averageScore,
        String scheduleCount,
        List<DisplayField> infoFields,
        List<ScheduleDisplayDto> scheduleRows,
        String scoreSummary
) {
}
