package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.config.SessionManager;
import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.dao.LecturerDAO;
import com.qlsv.dao.ScoreDAO;
import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Role;
import com.qlsv.model.Score;
import com.qlsv.model.Student;
import com.qlsv.security.AuthManager;
import com.qlsv.security.RolePermission;
import com.qlsv.utils.ValidationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScoreService {

    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
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

        List<Enrollment> enrollments = enrollmentDAO.findByLecturerId(lecturer.getId());
        Map<Long, Score> persistedScoresByEnrollmentId = scoreDAO.findByLecturerId(lecturer.getId()).stream()
                .filter(score -> score.getEnrollment() != null && score.getEnrollment().getId() != null)
                .collect(Collectors.toMap(
                        score -> score.getEnrollment().getId(),
                        Function.identity(),
                        (existing, ignored) -> existing
                ));

        List<Score> scores = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            scores.add(persistedScoresByEnrollmentId.getOrDefault(
                    enrollment.getId(),
                    createCompatibilityPlaceholder(enrollment)
            ));
        }
        return scores;
    }

    public List<Score> findByCourseSectionId(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return scoreDAO.findByCourseSectionId(courseSectionId);
    }

    public List<Score> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return scoreDAO.findByClassRoomId(classRoomId);
    }

    public Score save(Score score) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ lÆ°u Ä‘iá»ƒm.",
                ignored -> {
                    Enrollment existingEnrollment = validate(score);
                    score.setEnrollment(existingEnrollment);
                    enforceLecturerScope(score);

                    double processScore = ValidationUtil.defaultScore(score.getProcessScore());
                    double midtermScore = ValidationUtil.defaultScore(score.getMidtermScore());
                    double finalScore = ValidationUtil.defaultScore(score.getFinalScore());
                    double totalScore = processScore * 0.3 + midtermScore * 0.2 + finalScore * 0.5;

                    score.setProcessScore(processScore);
                    score.setMidtermScore(midtermScore);
                    score.setFinalScore(finalScore);
                    score.setTotalScore(Math.round(totalScore * 100.0) / 100.0);
                    score.setResult(score.getTotalScore() >= 5.0 ? "PASS" : "FAIL");

                    if (score.getId() == null) {
                        Score existingScore = scoreDAO.findByEnrollmentId(existingEnrollment.getId()).orElse(null);
                        if (existingScore != null) {
                            score.setId(existingScore.getId());
                        }
                    }

                    return score.getId() == null ? scoreDAO.insert(score) : updateAndReturn(score);
                }
        );
    }

    public boolean delete(Long id) {
        permissionService.requireAnyRole(Role.ADMIN);
        return JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a Ä‘iá»ƒm.",
                ignored -> scoreDAO.delete(id)
        );
    }

    private Score updateAndReturn(Score score) {
        scoreDAO.update(score);
        return score;
    }

    private Enrollment validate(Score score) {
        if (score.getEnrollment() == null || score.getEnrollment().getId() == null) {
            throw new ValidationException("Diem phai gan voi mot dang ky hoc phan.");
        }

        Enrollment enrollment = enrollmentDAO.findById(score.getEnrollment().getId())
                .orElseThrow(() -> new ValidationException("Dang ky hoc phan cua diem khong ton tai."));

        ValidationUtil.requireScoreRange(score.getProcessScore(), "Diem qua trinh");
        ValidationUtil.requireScoreRange(score.getMidtermScore(), "Diem giua ky");
        ValidationUtil.requireScoreRange(score.getFinalScore(), "Diem cuoi ky");
        return enrollment;
    }

    private void enforceLecturerScope(Score score) {
        if (!AuthManager.hasRole(Role.LECTURER)) {
            return;
        }
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Khong tim thay giang vien dang dang nhap."));
        Long assignedLecturerId = score.getEnrollment().getCourseSection().getLecturer().getId();
        if (!lecturer.getId().equals(assignedLecturerId)) {
            throw new ValidationException("Giang vien chi duoc nhap diem cho hoc phan duoc phan cong.");
        }
    }

    private Score createCompatibilityPlaceholder(Enrollment enrollment) {
        Score score = new Score();
        score.setEnrollment(enrollment);
        score.setProcessScore(0.0);
        score.setMidtermScore(0.0);
        score.setFinalScore(0.0);
        score.setTotalScore(0.0);
        score.setResult("FAIL");
        return score;
    }
}
