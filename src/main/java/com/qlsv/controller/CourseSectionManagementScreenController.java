/**
 * Điều phối dữ liệu cho quản lý học phần màn hình.
 */
package com.qlsv.controller;

import com.qlsv.dto.CourseSectionDisplayDto;
import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Room;
import com.qlsv.model.Subject;
import com.qlsv.utils.AcademicFormatUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseSectionManagementScreenController {

    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final SubjectController subjectController = new SubjectController();
    private final LecturerController lecturerController = new LecturerController();
    private final LecturerSubjectController lecturerSubjectController = new LecturerSubjectController();
    private final FacultyController facultyController = new FacultyController();
    private final RoomController roomController = new RoomController();

    /**
     * Tải danh sách các học phần dựa trên các tiêu chí lọc (tất cả, mã, phòng, khoa).
     */
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

    /**
     * Xây dựng danh sách các trường thông tin chi tiết của học phần để hiển thị.
     */
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

    /**
     * Chuyển đổi đối tượng CourseSection sang DTO để phục vụ hiển thị trên bảng.
     */
    public CourseSectionDisplayDto toDisplayDto(CourseSection courseSection) {
        return DisplayDtoMapper.toCourseSectionDisplayDto(courseSection);
    }

    /**
     * Nạp học phần.
     */
    public List<CourseSection> loadCourseSections() {
        return courseSectionController.getAllCourseSectionsForSelection();
    }

    /**
     * Nạp môn học.
     */
    public List<Subject> loadSubjects() {
        return subjectController.getSubjectsForSelection();
    }

    /**
     * Nạp giảng viên.
     */
    public List<Lecturer> loadLecturers() {
        return lecturerController.getLecturersForSelection();
    }

    /**
     * Lấy danh sách giảng viên có khả năng giảng dạy các môn học tương ứng.
     */
    public Map<Long, List<Lecturer>> loadLecturersBySubject(List<Subject> subjects) {
        lecturerSubjectController.backfillFromCourseSectionsIfNeeded();
        Map<Long, List<Lecturer>> lecturersBySubjectId = new LinkedHashMap<>();
        if (subjects == null) {
            return lecturersBySubjectId;
        }
        for (Subject subject : subjects) {
            if (subject == null || subject.getId() == null) {
                continue;
            }
            lecturersBySubjectId.put(subject.getId(), lecturerSubjectController.getLecturersBySubject(subject.getId()));
        }
        return lecturersBySubjectId;
    }

    /**
     * Nạp khoa.
     */
    public List<Faculty> loadFaculties() {
        return facultyController.getFacultiesForSelection();
    }

    /**
     * Nạp phòng.
     */
    public List<Room> loadRooms() {
        return roomController.getRoomsForSelection();
    }

    /**
     * Cập nhật thông tin đối tượng CourseSection từ dữ liệu form nhập liệu.
     */
    public CourseSection applyFormData(CourseSection existingItem, CourseSectionFormData formData) {
        CourseSection courseSection = existingItem == null ? new CourseSection() : existingItem;
        courseSection.setSectionCode(formData.sectionCode().trim());
        courseSection.setSubject(formData.subject());
        courseSection.setLecturer(formData.lecturer());
        courseSection.setSemester(AcademicFormatUtil.normalizeSemester(formData.semester(), "Hoc ky"));
        courseSection.setSchoolYear(AcademicFormatUtil.normalizeAcademicYear(formData.schoolYear(), "Nam hoc"));
        courseSection.setMaxStudents(Integer.parseInt(formData.maxStudents().trim()));
        return courseSection;
    }

    /**
     * Gửi yêu cầu lưu học phần xuống tầng controller quản lý.
     */
    public void saveCourseSection(CourseSection courseSection) {
        courseSectionController.saveCourseSection(courseSection);
    }

    /**
     * Gửi yêu cầu xóa học phần xuống tầng controller quản lý.
     */
    public void deleteCourseSection(CourseSection courseSection) {
        courseSectionController.deleteCourseSection(courseSection.getId());
    }

    /**
     * Xử lý học phần biểu mẫu dữ liệu.
     */
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
