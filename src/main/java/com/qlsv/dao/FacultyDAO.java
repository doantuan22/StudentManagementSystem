package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.Faculty;

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
 * DAO thao tac voi bang khoa.
 */
public class FacultyDAO {

    public List<Faculty> findAll() {
        String sql = "SELECT id, faculty_code, faculty_name, description FROM faculties ORDER BY id";
        List<Faculty> faculties = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                faculties.add(mapRow(resultSet));
            }
            return faculties;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach khoa.", exception);
        }
    }

    public Optional<Faculty> findById(Long id) {
        String sql = "SELECT id, faculty_code, faculty_name, description FROM faculties WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim khoa theo id.", exception);
        }
    }

    public List<Faculty> searchByKeyword(String keyword) {
        String sql = """
                SELECT id, faculty_code, faculty_name, description
                FROM faculties
                WHERE faculty_code LIKE ? OR faculty_name LIKE ?
                ORDER BY id
                """;
        String searchValue = "%" + keyword + "%";
        List<Faculty> faculties = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    faculties.add(mapRow(resultSet));
                }
                return faculties;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim kiem khoa.", exception);
        }
    }

    public Faculty insert(Faculty faculty) {
        String sql = "INSERT INTO faculties(faculty_code, faculty_name, description) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, faculty.getFacultyCode());
            statement.setString(2, faculty.getFacultyName());
            statement.setString(3, faculty.getDescription());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    faculty.setId(resultSet.getLong(1));
                }
            }
            return faculty;
        } catch (SQLException exception) {
            throw new AppException("Khong the them khoa.", exception);
        }
    }

    public boolean update(Faculty faculty) {
        String sql = "UPDATE faculties SET faculty_code = ?, faculty_name = ?, description = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, faculty.getFacultyCode());
            statement.setString(2, faculty.getFacultyName());
            statement.setString(3, faculty.getDescription());
            statement.setLong(4, faculty.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat khoa.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM faculties WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Khong the xoa khoa vi van con lop, giang vien, sinh vien hoac mon hoc dang tham chieu.", exception);
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa khoa.", exception);
        }
    }

    private Faculty mapRow(ResultSet resultSet) throws SQLException {
        return new Faculty(
                resultSet.getLong("id"),
                resultSet.getString("faculty_code"),
                resultSet.getString("faculty_name"),
                resultSet.getString("description")
        );
    }
}
