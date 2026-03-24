package com.qlsv.service;

import com.qlsv.config.SessionManager;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScoreDAO;
import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
import com.qlsv.security.AuthManager;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.List;

public class ScoreService {

    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Score> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return scoreDAO.findAll();
    }

    public List<Score> findByCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCORES);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay sinh vien dang dang nhap."));
        return scoreDAO.findByStudentId(student.getId());
    }

    public List<Score> findByCurrentLecturer() {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay giang vien dang dang nhap."));
        return scoreDAO.findByLecturerId(lecturer.getId());
    }

    public Score save(Score score) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        validate(score);
        enforceLecturerScope(score);

        // Tinh diem tong ket o service de co the tai su dung cho UI va API sau nay.
        double processScore = ValidationUtil.defaultScore(score.getProcessScore());
        double midtermScore = ValidationUtil.defaultScore(score.getMidtermScore());
        double finalScore = ValidationUtil.defaultScore(score.getFinalScore());
        double totalScore = processScore * 0.3 + midtermScore * 0.2 + finalScore * 0.5;

        score.setProcessScore(processScore);
        score.setMidtermScore(midtermScore);
        score.setFinalScore(finalScore);
        score.setTotalScore(Math.round(totalScore * 100.0) / 100.0);
        score.setResult(score.getTotalScore() >= 5.0 ? "PASS" : "FAIL");

        if (score.getId() == null && scoreDAO.findByEnrollmentId(score.getEnrollment().getId()).isPresent()) {
            Score existingScore = scoreDAO.findByEnrollmentId(score.getEnrollment().getId()).orElseThrow();
            score.setId(existingScore.getId());
        }

        return score.getId() == null ? scoreDAO.insert(score) : updateAndReturn(score);
    }

    public boolean delete(Long id) {
        permissionService.requireAnyRole(com.qlsv.model.Role.ADMIN);
        return scoreDAO.delete(id);
    }

    private Score updateAndReturn(Score score) {
        scoreDAO.update(score);
        return score;
    }

    private void validate(Score score) {
        if (score.getEnrollment() == null || score.getEnrollment().getId() == null) {
            throw new ValidationException("Diem phai gan voi mot dang ky hoc phan.");
        }
        // Kiem tra du lieu diem truoc khi luu xuong DB.
        ValidationUtil.requireScoreRange(score.getProcessScore(), "Diem qua trinh");
        ValidationUtil.requireScoreRange(score.getMidtermScore(), "Diem giua ky");
        ValidationUtil.requireScoreRange(score.getFinalScore(), "Diem cuoi ky");
    }

    private void enforceLecturerScope(Score score) {
        if (!AuthManager.hasRole(com.qlsv.model.Role.LECTURER)) {
            return;
        }
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay giang vien dang dang nhap."));
        Long assignedLecturerId = score.getEnrollment().getCourseSection().getLecturer().getId();
        if (!lecturer.getId().equals(assignedLecturerId)) {
            throw new ValidationException("Giang vien chi duoc nhap diem cho hoc phan duoc phan cong.");
        }
    }
}
