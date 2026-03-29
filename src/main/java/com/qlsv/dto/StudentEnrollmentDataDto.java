/**
 * Gói dữ liệu hiển thị cho sinh viên đăng ký dữ liệu.
 */
package com.qlsv.dto;

import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;

import java.util.List;

/**
 * Xử lý sinh viên đăng ký dữ liệu dto.
 */
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
