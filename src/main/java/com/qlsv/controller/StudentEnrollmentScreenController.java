package com.qlsv.controller;

import com.qlsv.dto.CourseSectionDisplayDto;
import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.EnrollmentDisplayDto;
import com.qlsv.dto.StudentEnrollmentDataDto;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.utils.DisplayTextUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StudentEnrollmentScreenController {

    private static final String ALL_SEMESTERS = "Tất cả học kỳ";

    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();

    public StudentEnrollmentDataDto loadData(String keyword, String semesterFilter) {
        List<CourseSection> allCourseSections = courseSectionController.getAllCourseSectionsForSelection();
        List<Enrollment> currentEnrollments = enrollmentController.getCurrentStudentEnrollments();

        List<String> semesterOptions = buildSemesterOptions(allCourseSections);
        List<CourseSection> displayedCourseSections = filterCourseSections(allCourseSections, keyword, semesterFilter);
        List<CourseSectionDisplayDto> courseRows = buildCourseRows(displayedCourseSections);
        List<EnrollmentDisplayDto> enrollmentRows = buildEnrollmentRows(currentEnrollments);

        return new StudentEnrollmentDataDto(
                currentEnrollments,
                displayedCourseSections,
                semesterOptions,
                courseRows,
                enrollmentRows,
                displayedCourseSections.size() + " học phần phù hợp",
                currentEnrollments.size() + " học phần đang theo dõi"
        );
    }

    public void registerCourseSection(CourseSection selectedCourseSection) {
        enrollmentController.registerCurrentStudent(selectedCourseSection.getId());
    }

    public void cancelEnrollment(Enrollment enrollment) {
        enrollmentController.cancelCurrentStudentEnrollment(enrollment.getId());
    }

    private List<String> buildSemesterOptions(List<CourseSection> courseSections) {
        Set<String> options = new LinkedHashSet<>();
        options.add(ALL_SEMESTERS);
        for (CourseSection courseSection : courseSections) {
            String semester = courseSection.getSemester();
            if (semester != null && !semester.isBlank()) {
                options.add(semester);
            }
        }
        return new ArrayList<>(options);
    }

    private List<CourseSection> filterCourseSections(List<CourseSection> allCourseSections, String keyword, String semesterFilter) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedSemester = semesterFilter == null || semesterFilter.isBlank() ? ALL_SEMESTERS : semesterFilter;

        List<CourseSection> displayedCourseSections = new ArrayList<>();
        for (CourseSection courseSection : allCourseSections) {
            boolean matchesKeyword = normalizedKeyword.isBlank()
                    || containsIgnoreCase(courseSection.getSectionCode(), normalizedKeyword)
                    || (courseSection.getSubject() != null && containsIgnoreCase(courseSection.getSubject().getSubjectName(), normalizedKeyword))
                    || (courseSection.getLecturer() != null && containsIgnoreCase(courseSection.getLecturer().getFullName(), normalizedKeyword));

            boolean matchesSemester = ALL_SEMESTERS.equalsIgnoreCase(normalizedSemester)
                    || normalizedSemester.equalsIgnoreCase(DisplayTextUtil.defaultText(courseSection.getSemester()));

            if (matchesKeyword && matchesSemester) {
                displayedCourseSections.add(courseSection);
            }
        }
        return displayedCourseSections;
    }

    private List<CourseSectionDisplayDto> buildCourseRows(List<CourseSection> displayedCourseSections) {
        List<CourseSectionDisplayDto> rows = new ArrayList<>();
        for (CourseSection courseSection : displayedCourseSections) {
            int currentEnrollmentsCount = enrollmentController.countEnrollmentsByCourseSection(courseSection.getId());
            String slots = currentEnrollmentsCount + "/" + courseSection.getMaxStudents();
            rows.add(DisplayDtoMapper.toCourseSectionDisplayDto(courseSection, slots));
        }
        return rows;
    }

    private List<EnrollmentDisplayDto> buildEnrollmentRows(List<Enrollment> currentEnrollments) {
        List<EnrollmentDisplayDto> rows = new ArrayList<>();
        for (Enrollment enrollment : currentEnrollments) {
            rows.add(DisplayDtoMapper.toEnrollmentDisplayDto(enrollment));
        }
        return rows;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
