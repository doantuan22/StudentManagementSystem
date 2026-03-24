package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
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
 * DAO thao tac voi bang lop hoc.
 */
public class ClassRoomDAO {

    private static final String BASE_SELECT = """
            SELECT c.id,
                   c.class_code,
                   c.class_name,
                   c.academic_year,
                   f.id AS faculty_id,
                   f.faculty_code,
                   f.faculty_name,
                   f.description AS faculty_description
            FROM class_rooms c
            JOIN faculties f ON f.id = c.faculty_id
            """;

    public List<ClassRoom> findAll() {
        String sql = BASE_SELECT + " ORDER BY c.id";
        List<ClassRoom> classRooms = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                classRooms.add(mapRow(resultSet));
            }
            return classRooms;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach lop.", exception);
        }
    }

    public Optional<ClassRoom> findById(Long id) {
        String sql = BASE_SELECT + " WHERE c.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim lop theo id.", exception);
        }
    }

    public List<ClassRoom> searchByKeyword(String keyword) {
        String sql = BASE_SELECT + """
                 WHERE c.class_code LIKE ?
                    OR c.class_name LIKE ?
                    OR c.academic_year LIKE ?
                 ORDER BY c.id
                """;
        String searchValue = "%" + keyword + "%";
        List<ClassRoom> classRooms = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    classRooms.add(mapRow(resultSet));
                }
                return classRooms;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim kiem lop.", exception);
        }
    }

    public ClassRoom insert(ClassRoom classRoom) {
        String sql = "INSERT INTO class_rooms(class_code, class_name, academic_year, faculty_id) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, classRoom.getClassCode());
            statement.setString(2, classRoom.getClassName());
            statement.setString(3, classRoom.getAcademicYear());
            statement.setLong(4, classRoom.getFaculty().getId());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    classRoom.setId(resultSet.getLong(1));
                }
            }
            return classRoom;
        } catch (SQLException exception) {
            throw new AppException("Khong the them lop.", exception);
        }
    }

    public boolean update(ClassRoom classRoom) {
        String sql = """
                UPDATE class_rooms
                SET class_code = ?, class_name = ?, academic_year = ?, faculty_id = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, classRoom.getClassCode());
            statement.setString(2, classRoom.getClassName());
            statement.setString(3, classRoom.getAcademicYear());
            statement.setLong(4, classRoom.getFaculty().getId());
            statement.setLong(5, classRoom.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat lop.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM class_rooms WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Khong the xoa lop vi van con sinh vien hoac hoc phan dang su dung.", exception);
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa lop.", exception);
        }
    }

    private ClassRoom mapRow(ResultSet resultSet) throws SQLException {
        Faculty faculty = new Faculty(
                resultSet.getLong("faculty_id"),
                resultSet.getString("faculty_code"),
                resultSet.getString("faculty_name"),
                resultSet.getString("faculty_description")
        );
        return new ClassRoom(
                resultSet.getLong("id"),
                resultSet.getString("class_code"),
                resultSet.getString("class_name"),
                resultSet.getString("academic_year"),
                faculty
        );
    }
}
