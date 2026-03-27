package com.qlsv.dto;

import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;

import java.util.List;

public record StudentEnrollmentDataDto(
        List<Enrollment> currentEnrollments,
        List<CourseSection> displayedCourseSections,
        List<String> semesterOptions,
        List<CourseSectionDisplayDto> availableCourseRows,
        List<EnrollmentDisplayDto> enrollmentRows,
        String availableSummary,
        String registeredSummary
) {
}
