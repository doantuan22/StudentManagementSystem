/**
 * Điều phối dữ liệu cho điểm.
 */
package com.qlsv.controller;

import com.qlsv.model.Score;
import com.qlsv.service.ScoreService;

import java.util.List;

public class ScoreController {

    private final ScoreService scoreService = new ScoreService();

    /**
     * Trả về toàn bộ điểm.
     */
    public List<Score> getAllScores() {
        return scoreService.findAll();
    }

    /**
     * Trả về sinh viên điểm hiện tại.
     */
    public List<Score> getCurrentStudentScores() {
        return scoreService.findByCurrentStudent();
    }

    /**
     * Trả về giảng viên điểm hiện tại.
     */
    public List<Score> getCurrentLecturerScores() {
        return scoreService.findByCurrentLecturer();
    }

    /**
     * Trả về điểm theo học phần.
     */
    public List<Score> getScoresByCourseSection(Long courseSectionId) {
        return scoreService.findByCourseSectionId(courseSectionId);
    }

    /**
     * Trả về điểm theo lớp.
     */
    public List<Score> getScoresByClassRoom(Long classRoomId) {
        return scoreService.findByClassRoomId(classRoomId);
    }

    /**
     * Lưu điểm.
     */
    public Score saveScore(Score score) {
        return scoreService.save(score);
    }

    /**
     * Xóa điểm.
     */
    public boolean deleteScore(Long id) {
        return scoreService.delete(id);
    }
}
