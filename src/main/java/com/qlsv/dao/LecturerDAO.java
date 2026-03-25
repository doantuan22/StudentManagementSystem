package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;

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
 * DAO thao tac voi bang giang vien.
 */
public class LecturerDAO {

    private static final String BASE_SELECT = """
            SELECT l.id,
                   l.user_id,
                   l.lecturer_code,
                   l.full_name,
                   l.email,
                   l.phone,
                   l.status,
                   f.id AS faculty_id,
                   f.faculty_code,
                   f.faculty_name,
                   f.description AS faculty_description
            FROM lecturers l
            JOIN faculties f ON f.id = l.faculty_id
            """;

    public List<Lecturer> findAll() {
        String sql = BASE_SELECT + " ORDER BY l.id";
        List<Lecturer> lecturers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                lecturers.add(mapRow(resultSet));
            }
            return lecturers;
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách giảng viên.", exception);
        }
    }

    public Optional<Lecturer> findById(Long id) {
        String sql = BASE_SELECT + " WHERE l.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm giảng viên theo mã định danh.", exception);
        }
    }

    public Optional<Lecturer> findByUserId(Long userId) {
        String sql = BASE_SELECT + " WHERE l.user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm giảng viên theo tài khoản người dùng.", exception);
        }
    }

    public List<Lecturer> searchByKeyword(String keyword) {
        String sql = BASE_SELECT + """
                 WHERE l.lecturer_code LIKE ?
                    OR l.full_name LIKE ?
                    OR l.email LIKE ?
                 ORDER BY l.id
                """;
        String searchValue = "%" + keyword + "%";
        List<Lecturer> lecturers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lecturers.add(mapRow(resultSet));
                }
                return lecturers;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm kiếm giảng viên.", exception);
        }
    }

    public Lecturer insert(Lecturer lecturer) {
        String sql = """
                INSERT INTO lecturers(user_id, lecturer_code, full_name, email, phone, faculty_id, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, lecturer);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    lecturer.setId(resultSet.getLong(1));
                }
            }
            return lecturer;
        } catch (SQLException exception) {
            throw new AppException("Không thể thêm giảng viên.", exception);
        }
    }

    public boolean update(Lecturer lecturer) {
        String sql = """
                UPDATE lecturers
                SET user_id = ?, lecturer_code = ?, full_name = ?, email = ?, phone = ?, faculty_id = ?, status = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, lecturer);
            statement.setLong(8, lecturer.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể cập nhật giảng viên.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM lecturers WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Không thể xóa giảng viên vì vẫn còn học phần hoặc phân công giảng dạy đang tham chiếu.", exception);
        } catch (SQLException exception) {
            throw new AppException("Không thể xóa giảng viên.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Lecturer lecturer) throws SQLException {
        statement.setObject(1, lecturer.getUserId());
        statement.setString(2, lecturer.getLecturerCode());
        statement.setString(3, lecturer.getFullName());
        statement.setString(4, lecturer.getEmail());
        statement.setString(5, lecturer.getPhone());
        statement.setLong(6, lecturer.getFaculty().getId());
        statement.setString(7, lecturer.getStatus());
    }

    private Lecturer mapRow(ResultSet resultSet) throws SQLException {
        Faculty faculty = new Faculty(
                resultSet.getLong("faculty_id"),
                resultSet.getString("faculty_code"),
                resultSet.getString("faculty_name"),
                resultSet.getString("faculty_description")
        );
        return new Lecturer(
                resultSet.getLong("id"),
                resultSet.getObject("user_id", Long.class),
                resultSet.getString("lecturer_code"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                faculty,
                resultSet.getString("status")
        );
    }
}
