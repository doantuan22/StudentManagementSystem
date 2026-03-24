package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Score;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tac voi bang diem.
 */
public class ScoreDAO {

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    public List<Score> findAll() {
        String sql = """
                SELECT id, enrollment_id, process_score, midterm_score, final_score, total_score, result
                FROM scores
                ORDER BY id
                """;
        List<Score> scores = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                scores.add(mapRow(resultSet));
            }
            return scores;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach diem.", exception);
        }
    }

    public Optional<Score> findById(Long id) {
        String sql = """
                SELECT id, enrollment_id, process_score, midterm_score, final_score, total_score, result
                FROM scores
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim diem theo id.", exception);
        }
    }

    public Optional<Score> findByEnrollmentId(Long enrollmentId) {
        String sql = """
                SELECT id, enrollment_id, process_score, midterm_score, final_score, total_score, result
                FROM scores
                WHERE enrollment_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, enrollmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim diem theo dang ky hoc phan.", exception);
        }
    }

    public List<Score> findByStudentId(Long studentId) {
        String sql = """
                SELECT s.id, s.enrollment_id, s.process_score, s.midterm_score, s.final_score, s.total_score, s.result
                FROM scores s
                JOIN enrollments e ON e.id = s.enrollment_id
                WHERE e.student_id = ?
                ORDER BY s.id
                """;
        List<Score> scores = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    scores.add(mapRow(resultSet));
                }
                return scores;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tai diem cua sinh vien.", exception);
        }
    }

    public List<Score> findByLecturerId(Long lecturerId) {
        String sql = """
                SELECT s.id, s.enrollment_id, s.process_score, s.midterm_score, s.final_score, s.total_score, s.result
                FROM scores s
                JOIN enrollments e ON e.id = s.enrollment_id
                JOIN course_sections cs ON cs.id = e.course_section_id
                WHERE cs.lecturer_id = ?
                ORDER BY s.id
                """;
        List<Score> scores = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, lecturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    scores.add(mapRow(resultSet));
                }
                return scores;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tai diem cua giang vien phu trach.", exception);
        }
    }

    public Score insert(Score score) {
        String sql = """
                INSERT INTO scores(enrollment_id, process_score, midterm_score, final_score, total_score, result)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, score);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    score.setId(resultSet.getLong(1));
                }
            }
            return score;
        } catch (SQLException exception) {
            throw new AppException("Khong the them diem.", exception);
        }
    }

    public boolean update(Score score) {
        String sql = """
                UPDATE scores
                SET enrollment_id = ?, process_score = ?, midterm_score = ?, final_score = ?, total_score = ?, result = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, score);
            statement.setLong(7, score.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat diem.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM scores WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa diem.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Score score) throws SQLException {
        statement.setLong(1, score.getEnrollment().getId());
        statement.setDouble(2, score.getProcessScore());
        statement.setDouble(3, score.getMidtermScore());
        statement.setDouble(4, score.getFinalScore());
        statement.setDouble(5, score.getTotalScore());
        statement.setString(6, score.getResult());
    }

    private Score mapRow(ResultSet resultSet) throws SQLException {
        Enrollment enrollment = enrollmentDAO.findById(resultSet.getLong("enrollment_id")).orElse(null);
        return new Score(
                resultSet.getLong("id"),
                enrollment,
                resultSet.getDouble("process_score"),
                resultSet.getDouble("midterm_score"),
                resultSet.getDouble("final_score"),
                resultSet.getDouble("total_score"),
                resultSet.getString("result")
        );
    }
}
