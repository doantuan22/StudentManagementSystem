package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.Role;
import com.qlsv.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO xu ly CRUD tai khoan dang nhap.
 */
public class UserDAO {

    private static final String BASE_SELECT = """
            SELECT u.id,
                   u.username,
                   u.password_hash,
                   u.full_name,
                   u.email,
                   u.active,
                   r.role_code
            FROM users u
            JOIN roles r ON r.id = u.role_id
            """;

    public List<User> findAll() {
        String sql = BASE_SELECT + " ORDER BY u.id";
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                users.add(mapRow(resultSet));
            }
            return users;
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách người dùng.", exception);
        }
    }

    public Optional<User> findById(Long id) {
        String sql = BASE_SELECT + " WHERE u.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm người dùng theo mã định danh.", exception);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = BASE_SELECT + " WHERE u.username = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm người dùng theo tên đăng nhập.", exception);
        }
    }

    public List<User> searchByKeyword(String keyword) {
        String sql = BASE_SELECT + """
                 WHERE u.username LIKE ?
                    OR u.full_name LIKE ?
                    OR u.email LIKE ?
                 ORDER BY u.id
                """;
        String searchValue = "%" + keyword + "%";
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapRow(resultSet));
                }
                return users;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm kiếm người dùng.", exception);
        }
    }

    public User insert(User user) {
        String sql = """
                INSERT INTO users(username, password_hash, full_name, email, role_id, active)
                VALUES (?, ?, ?, ?, (SELECT id FROM roles WHERE role_code = ?), ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            // Truy van CRUD chinh duoc gom trong DAO, UI khong thao tac SQL truc tiep.
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getRole().getCode());
            statement.setBoolean(6, user.isActive());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getLong(1));
                }
            }
            return user;
        } catch (SQLException exception) {
            throw new AppException("Không thể thêm người dùng.", exception);
        }
    }

    public boolean update(User user) {
        String sql = """
                UPDATE users
                SET username = ?,
                    password_hash = ?,
                    full_name = ?,
                    email = ?,
                    role_id = (SELECT id FROM roles WHERE role_code = ?),
                    active = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getRole().getCode());
            statement.setBoolean(6, user.isActive());
            statement.setLong(7, user.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể cập nhật người dùng.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể xóa người dùng.", exception);
        }
    }

    private User mapRow(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("full_name"),
                resultSet.getString("email"),
                Role.fromCode(resultSet.getString("role_code")),
                resultSet.getBoolean("active")
        );
    }
}
