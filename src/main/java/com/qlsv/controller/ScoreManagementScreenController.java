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

    public ScoreDisplayDto toDisplayDto(Score score) {
        return DisplayDtoMapper.toScoreDisplayDto(score);
    }

    public List<Enrollment> loadEnrollments() {
        return enrollmentController.getAllEnrollments();
    }

    public List<CourseSection> loadCourseSections() {
        return courseSectionController.getAllCourseSectionsForSelection();
    }

    public List<ClassRoom> loadClassRooms() {
        return classRoomController.getClassRoomsForSelection();
    }

    public Score applyFormData(Score existingItem, ScoreFormData formData) {
        Score score = existingItem == null ? new Score() : existingItem;
        score.setEnrollment(formData.enrollment());
        score.setProcessScore(parseScore(formData.processScore()));
        score.setMidtermScore(parseScore(formData.midtermScore()));
        score.setFinalScore(parseScore(formData.finalScore()));
        return score;
    }

    public void saveScore(Score score) {
        scoreController.saveScore(score);
    }

    public void deleteScore(Score score) {
        scoreController.deleteScore(score.getId());
    }

    private Double parseScore(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? 0.0 : Double.parseDouble(rawValue.trim());
    }

    public record ScoreFormData(Enrollment enrollment, String processScore, String midtermScore, String finalScore) {
    }
}
