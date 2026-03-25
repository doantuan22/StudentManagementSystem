package com.qlsv.controller;

import com.qlsv.model.Score;
import com.qlsv.service.ScoreService;

import java.util.List;

public class ScoreController {

    private final ScoreService scoreService = new ScoreService();

    public List<Score> getAllScores() {
        return scoreService.findAll();
    }

    public List<Score> getCurrentStudentScores() {
        return scoreService.findByCurrentStudent();
    }

    public List<Score> getCurrentLecturerScores() {
        return scoreService.findByCurrentLecturer();
    }

    public List<Score> getScoresByCourseSection(Long courseSectionId) {
        return scoreService.findByCourseSectionId(courseSectionId);
    }

    public List<Score> getScoresByClassRoom(Long classRoomId) {
        return scoreService.findByClassRoomId(classRoomId);
    }

    public Score saveScore(Score score) {
        return scoreService.save(score);
    }

    public boolean deleteScore(Long id) {
        return scoreService.delete(id);
    }
}
