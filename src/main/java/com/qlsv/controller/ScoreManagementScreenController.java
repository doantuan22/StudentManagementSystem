/**
 * Điều phối dữ liệu cho quản lý điểm màn hình.
 */
package com.qlsv.controller;

import com.qlsv.dto.DisplayDtoMapper;
import com.qlsv.dto.ScoreDisplayDto;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Score;

import java.util.List;

public class ScoreManagementScreenController {

    private final ScoreController scoreController = new ScoreController();
    private final EnrollmentController enrollmentController = new EnrollmentController();
    private final CourseSectionController courseSectionController = new CourseSectionController();
    private final ClassRoomController classRoomController = new ClassRoomController();

    /**
     * Nạp items.
     */
    public List<Score> loadItems(boolean filterReady, String filterType, Object filterValue,
                                 String filterAll, String filterSectionCode, String filterClassRoom) {
        if (!filterReady) {
            return List.of();
        }
        String normalizedFilterType = filterType == null ? "" : filterType;
        if (normalizedFilterType.equals(filterAll)) {
            return scoreController.getAllScores();
        }
        if (normalizedFilterType.equals(filterSectionCode) && filterValue instanceof CourseSection courseSection) {
            return scoreController.getScoresByCourseSection(courseSection.getId());
        }
        if (normalizedFilterType.equals(filterClassRoom) && filterValue instanceof ClassRoom classRoom) {
            return scoreController.getScoresByClassRoom(classRoom.getId());
        }
        return List.of();
    }

    /**
     * Tạo chi tiết trường.
     */
    public List<DisplayField> buildDetailFields(Score score) {
        if (score == null) {
            return List.of();
        }
        ScoreDisplayDto displayDto = toDisplayDto(score);
        return List.of(
                new DisplayField("Mã sinh viên", displayDto.studentCode()),
                new DisplayField("Sinh viên", displayDto.studentName()),
                new DisplayField("Lớp", displayDto.classRoomName()),
                new DisplayField("Mã học phần", displayDto.sectionCode()),
                new DisplayField("Môn học", displayDto.subjectName()),
                new DisplayField("Phòng học", displayDto.roomName()),
                new DisplayField("Điểm quá trình", displayDto.processScore()),
                new DisplayField("Điểm giữa kỳ", displayDto.midtermScore()),
                new DisplayField("Điểm cuối kỳ", displayDto.finalScore()),
                new DisplayField("Điểm tổng kết", displayDto.totalScore()),
                new DisplayField("Kết quả", displayDto.resultText())
        );
    }

    /**
     * Xử lý to hiển thị dto.
     */
    public ScoreDisplayDto toDisplayDto(Score score) {
        return DisplayDtoMapper.toScoreDisplayDto(score);
    }

    /**
     * Nạp đăng ký.
     */
    public List<Enrollment> loadEnrollments() {
        return enrollmentController.getAllEnrollments();
    }

    /**
     * Nạp đăng ký chưa có điểm của một sinh viên.
     */
    public List<Enrollment> loadEnrollmentsWithoutScoreByStudentId(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        return enrollmentController.getEnrollmentsWithoutScoreByStudentId(studentId);
    }

    /**
     * Tìm điểm theo enrollment ID.
     */
    public Score findScoreByEnrollmentId(Long enrollmentId) {
        if (enrollmentId == null) {
            return null;
        }
        return scoreController.findScoreByEnrollmentId(enrollmentId);
    }

    /**
     * Nạp đăng ký theo student ID.
     */
    public List<Enrollment> loadEnrollmentsByStudentId(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        return enrollmentController.getEnrollmentsByStudentId(studentId);
    }

    /**
     * Nạp điểm theo student ID.
     */
    public List<Score> loadScoresByStudentId(Long studentId) {
        if (studentId == null) {
            return List.of();
        }
        return scoreController.getScoresByStudentId(studentId);
    }

    /**
     * Nạp tất cả điểm.
     */
    public List<Score> loadAllScores() {
        return scoreController.getAllScores();
    }

    /**
     * Nạp học phần.
     */
    public List<CourseSection> loadCourseSections() {
        return courseSectionController.getAllCourseSectionsForSelection();
    }

    /**
     * Nạp lớp phòng.
     */
    public List<ClassRoom> loadClassRooms() {
        return classRoomController.getClassRoomsForSelection();
    }

    /**
     * Xử lý apply biểu mẫu dữ liệu.
     */
    public Score applyFormData(Score existingItem, ScoreFormData formData) {
        Score score = existingItem == null ? new Score() : existingItem;
        score.setEnrollment(formData.enrollment());
        score.setProcessScore(parseScore(formData.processScore()));
        score.setMidtermScore(parseScore(formData.midtermScore()));
        score.setFinalScore(parseScore(formData.finalScore()));
        return score;
    }

    /**
     * Lưu điểm.
     */
    public Score saveScore(Score score) {
        return scoreController.saveScore(score);
    }

    /**
     * Xóa điểm.
     */
    public void deleteScore(Score score) {
        scoreController.deleteScore(score.getId());
    }

    /**
     * Phân tích điểm.
     */
    private Double parseScore(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? 0.0 : Double.parseDouble(rawValue.trim());
    }

    /**
     * Xử lý điểm biểu mẫu dữ liệu.
     */
    public record ScoreFormData(Enrollment enrollment, String processScore, String midtermScore, String finalScore) {
    }
}
