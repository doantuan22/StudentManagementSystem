package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.Faculty;
import com.qlsv.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tac voi bang mon hoc.
 */
public class SubjectDAO {

    private static final String BASE_SELECT = """
            SELECT s.id,
                   s.subject_code,
                   s.subject_name,
                   s.credits,
                   s.description,
                   f.id AS faculty_id,
                   f.faculty_code,
                   f.faculty_name,
                   f.description AS faculty_description
            FROM subjects s
            JOIN faculties f ON f.id = s.faculty_id
            """;

    public List<Subject> findAll() {
        String sql = BASE_SELECT + " ORDER BY s.id";
        List<Subject> subjects = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                subjects.add(mapRow(resultSet));
            }
            return subjects;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach mon hoc.", exception);
        }
    }

    public Optional<Subject> findById(Long id) {
        String sql = BASE_SELECT + " WHERE s.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim mon hoc theo id.", exception);
        }
    }

    public List<Subject> searchByKeyword(String keyword) {
        String sql = BASE_SELECT + """
                 WHERE s.subject_code LIKE ?
                    OR s.subject_name LIKE ?
                 ORDER BY s.id
                """;
        String searchValue = "%" + keyword + "%";
        List<Subject> subjects = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    subjects.add(mapRow(resultSet));
                }
                return subjects;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim kiem mon hoc.", exception);
        }
    }

    public Subject insert(Subject subject) {
        String sql = """
                INSERT INTO subjects(subject_code, subject_name, credits, faculty_id, description)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, subject.getSubjectCode());
            statement.setString(2, subject.getSubjectName());
            statement.setInt(3, subject.getCredits());
            statement.setLong(4, subject.getFaculty().getId());
            statement.setString(5, subject.getDescription());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    subject.setId(resultSet.getLong(1));
                }
            }
            return subject;
        } catch (SQLException exception) {
            throw new AppException("Khong the them mon hoc.", exception);
        }
    }

    public boolean update(Subject subject) {
        String sql = """
                UPDATE subjects
                SET subject_code = ?, subject_name = ?, credits = ?, faculty_id = ?, description = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, subject.getSubjectCode());
            statement.setString(2, subject.getSubjectName());
            statement.setInt(3, subject.getCredits());
            statement.setLong(4, subject.getFaculty().getId());
            statement.setString(5, subject.getDescription());
            statement.setLong(6, subject.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat mon hoc.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM subjects WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Khong the xoa mon hoc vi van con hoc phan dang mo hoac phan cong giang day lien quan.", exception);
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa mon hoc.", exception);
        }
    }

    private Subject mapRow(ResultSet resultSet) throws SQLException {
        Faculty faculty = new Faculty(
                resultSet.getLong("faculty_id"),
                resultSet.getString("faculty_code"),
                resultSet.getString("faculty_name"),
                resultSet.getString("faculty_description")
        );
        return new Subject(
                resultSet.getLong("id"),
                resultSet.getString("subject_code"),
                resultSet.getString("subject_name"),
                resultSet.getInt("credits"),
                faculty,
                resultSet.getString("description")
        );
    }
}
