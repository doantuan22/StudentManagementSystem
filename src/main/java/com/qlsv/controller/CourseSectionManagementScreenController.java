package com.qlsv.controller;

import com.qlsv.dto.CourseSectionDisplayDto;
import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Room;
import com.qlsv.model.Subject;
import com.qlsv.utils.AcademicFormatUtil;

import java.util.List;

public class CourseSectionManagementScreenController {

    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final SubjectController subjectController = new SubjectController();
    private final LecturerController lecturerController = new LecturerController();
    private final FacultyController facultyController = new FacultyController();
    private final RoomController roomController = new RoomController();

    public List<CourseSection> loadItems(boolean filterReady, String filterType, Object filterValue,
                                         String filterAll, String filterSectionCode, String filterRoom, String filterFaculty) {
        if (!filterReady) {
            return List.of();
        }
        String normalizedFilterType = filterType == null ? "" : filterType;
        if (normalizedFilterType.equals(filterAll)) {
            return courseSectionController.getAllCourseSections();
        }
        if (normalizedFilterType.equals(filterSectionCode) && filterValue instanceof String sectionCode) {
            return courseSectionController.getCourseSectionsBySectionCode(sectionCode);
        }
        if (normalizedFilterType.equals(filterRoom) && filterValue instanceof Room room) {
            return courseSectionController.getCourseSectionsByRoom(room.getId());
        }
        if (normalizedFilterType.equals(filterFaculty) && filterValue instanceof Faculty faculty) {
            return courseSectionController.getCourseSectionsByFaculty(faculty.getId());
        }
        return List.of();
    }

    public List<DisplayField> buildDetailFields(CourseSection courseSection) {
        if (courseSection == null) {
            return List.of();
        }
        CourseSectionDisplayDto displayDto = toDisplayDto(courseSection);
        return List.of(
                new DisplayField("Mã học phần", displayDto.sectionCode()),
                new DisplayField("Môn học", displayDto.subjectName()),
                new DisplayField("Giảng viên", displayDto.lecturerName()),
                new DisplayField("Học kỳ", displayDto.semester()),
                new DisplayField("Năm học", displayDto.schoolYear()),
                new DisplayField("Lịch học", displayDto.scheduleText()),
                new DisplayField("Sĩ số tối đa", displayDto.maxStudents())
        );
    }

    public CourseSectionDisplayDto toDisplayDto(CourseSection courseSection) {
        return DisplayDtoMapper.toCourseSectionDisplayDto(courseSection);
    }

    public List<CourseSection> loadCourseSections() {
        return courseSectionController.getAllCourseSectionsForSelection();
    }

    public List<Subject> loadSubjects() {
        return subjectController.getSubjectsForSelection();
    }

    public List<Lecturer> loadLecturers() {
        return lecturerController.getLecturersForSelection();
    }

    public List<Faculty> loadFaculties() {
        return facultyController.getFacultiesForSelection();
    }

    public List<Room> loadRooms() {
        return roomController.getRoomsForSelection();
    }

    public CourseSection applyFormData(CourseSection existingItem, CourseSectionFormData formData) {
        CourseSection courseSection = existingItem == null ? new CourseSection() : existingItem;
        courseSection.setSectionCode(formData.sectionCode().trim());
        courseSection.setSubject(formData.subject());
        courseSection.setLecturer(formData.lecturer());
        courseSection.setSemester(AcademicFormatUtil.normalizeSemester(formData.semester(), "Học kỳ"));
        courseSection.setSchoolYear(AcademicFormatUtil.normalizeAcademicYear(formData.schoolYear(), "Năm học"));
        courseSection.setMaxStudents(Integer.parseInt(formData.maxStudents().trim()));
        return courseSection;
    }

    public void saveCourseSection(CourseSection courseSection) {
        courseSectionController.saveCourseSection(courseSection);
    }

    public void deleteCourseSection(CourseSection courseSection) {
        courseSectionController.deleteCourseSection(courseSection.getId());
    }

    public record CourseSectionFormData(
            String sectionCode,
            Subject subject,
            Lecturer lecturer,
            String semester,
            String schoolYear,
            String maxStudents
    ) {
    }
}
