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

    /**
     * Lấy danh sách toàn bộ điểm trong hệ thống dành cho người quản trị.
     */
    public List<Score> findAll() {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return scoreDAO.findAll();
    }

    /**
     * Lấy danh sách điểm của sinh viên đang đăng nhập.
     */
    public List<Score> findByCurrentStudent() {
        permissionService.requirePermission(RolePermission.VIEW_OWN_SCORES);
        Student student = studentDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy sinh viên đang đăng nhập."));
        return scoreDAO.findByStudentId(student.getId());
    }

    /**
     * Lấy danh sách điểm của các học phần do giảng viên đang đăng nhập phụ trách.
     */
    public List<Score> findByCurrentLecturer() {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy giảng viên đang đăng nhập."));

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

    /**
     * Lấy bảng điểm chi tiết cho một học phần cụ thể.
     */
    public List<Score> findByCourseSectionId(Long courseSectionId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return scoreDAO.findByCourseSectionId(courseSectionId);
    }

    /**
     * Lấy bảng điểm của tất cả sinh viên thuộc một lớp hành chính.
     */
    public List<Score> findByClassRoomId(Long classRoomId) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return scoreDAO.findByClassRoomId(classRoomId);
    }

    /**
     * Tính toán tổng kết và lưu thông tin điểm số (có kiểm tra quyền hạn).
     */
    public Score save(Score score) {
        permissionService.requirePermission(RolePermission.MANAGE_SCORES);
        return JpaBootstrap.executeInTransaction(
                "Không thể lưu điểm.",
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

    /**
     * Xóa bản ghi điểm hệ thống (chỉ dành cho quản trị viên).
     */
    public boolean delete(Long id) {
        permissionService.requireAnyRole(Role.ADMIN);
        return JpaBootstrap.executeInTransaction(
                "Không thể xóa điểm.",
                ignored -> scoreDAO.delete(id)
        );
    }

    private Score updateAndReturn(Score score) {
        scoreDAO.update(score);
        return score;
    }

    private Enrollment validate(Score score) {
        if (score.getEnrollment() == null || score.getEnrollment().getId() == null) {
            throw new ValidationException("Điểm phải gắn với một đăng ký học phần.");
        }

        Enrollment enrollment = enrollmentDAO.findById(score.getEnrollment().getId())
                .orElseThrow(() -> new ValidationException("Đăng ký học phần của điểm không tồn tại."));

        ValidationUtil.requireScoreRange(score.getProcessScore(), "Điểm quá trình");
        ValidationUtil.requireScoreRange(score.getMidtermScore(), "Điểm giữa kỳ");
        ValidationUtil.requireScoreRange(score.getFinalScore(), "Điểm cuối kỳ");
        return enrollment;
    }

    private void enforceLecturerScope(Score score) {
        if (!AuthManager.hasRole(Role.LECTURER)) {
            return;
        }
        Lecturer lecturer = lecturerDAO.findByUserId(SessionManager.requireCurrentUser().getId())
                .orElseThrow(() -> new ValidationException("Không tìm thấy giảng viên đang đăng nhập."));
        Long assignedLecturerId = score.getEnrollment().getCourseSection().getLecturer().getId();
        if (!lecturer.getId().equals(assignedLecturerId)) {
            throw new ValidationException("Giảng viên chỉ được nhập điểm cho học phần được phân công.");
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
