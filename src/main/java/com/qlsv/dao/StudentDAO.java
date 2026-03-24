package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.model.Student;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tac voi bang sinh vien.
 */
public class StudentDAO {

    private static final String BASE_SELECT = """
            SELECT s.id,
                   s.user_id,
                   s.student_code,
                   s.full_name,
                   s.gender,
                   s.date_of_birth,
                   s.email,
                   s.phone,
                   s.status,
                   f.id AS faculty_id,
                   f.faculty_code,
                   f.faculty_name,
                   f.description AS faculty_description,
                   c.id AS class_room_id,
                   c.class_code,
                   c.class_name,
                   c.academic_year
            FROM students s
            JOIN faculties f ON f.id = s.faculty_id
            JOIN class_rooms c ON c.id = s.class_room_id
            """;

    public List<Student> findAll() {
        String sql = BASE_SELECT + " ORDER BY s.id";
        List<Student> students = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                students.add(mapRow(resultSet));
            }
            return students;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach sinh vien.", exception);
        }
    }

    public Optional<Student> findById(Long id) {
        String sql = BASE_SELECT + " WHERE s.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim sinh vien theo id.", exception);
        }
    }

    public Optional<Student> findByUserId(Long userId) {
        String sql = BASE_SELECT + " WHERE s.user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim sinh vien theo user.", exception);
        }
    }

    public List<Student> searchByKeyword(String keyword) {
        String sql = BASE_SELECT + """
                 WHERE s.student_code LIKE ?
                    OR s.full_name LIKE ?
                    OR s.email LIKE ?
                 ORDER BY s.id
                """;
        String searchValue = "%" + keyword + "%";
        List<Student> students = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    students.add(mapRow(resultSet));
                }
                return students;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim kiem sinh vien.", exception);
        }
    }

    public Student insert(Student student) {
        String sql = """
                INSERT INTO students(user_id, student_code, full_name, gender, date_of_birth, email, phone, faculty_id, class_room_id, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, student);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    student.setId(resultSet.getLong(1));
                }
            }
            return student;
        } catch (SQLException exception) {
            throw new AppException("Khong the them sinh vien.", exception);
        }
    }

    public boolean update(Student student) {
        String sql = """
                UPDATE students
                SET user_id = ?, student_code = ?, full_name = ?, gender = ?, date_of_birth = ?, email = ?,
                    phone = ?, faculty_id = ?, class_room_id = ?, status = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, student);
            statement.setLong(11, student.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat sinh vien.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa sinh vien.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Student student) throws SQLException {
        statement.setObject(1, student.getUserId());
        statement.setString(2, student.getStudentCode());
        statement.setString(3, student.getFullName());
        statement.setString(4, student.getGender());
        if (student.getDateOfBirth() == null) {
            statement.setDate(5, null);
        } else {
            statement.setDate(5, Date.valueOf(student.getDateOfBirth()));
        }
        statement.setString(6, student.getEmail());
        statement.setString(7, student.getPhone());
        statement.setLong(8, student.getFaculty().getId());
        statement.setLong(9, student.getClassRoom().getId());
        statement.setString(10, student.getStatus());
    }

    private Student mapRow(ResultSet resultSet) throws SQLException {
        Faculty faculty = new Faculty(
                resultSet.getLong("faculty_id"),
                resultSet.getString("faculty_code"),
                resultSet.getString("faculty_name"),
                resultSet.getString("faculty_description")
        );
        ClassRoom classRoom = new ClassRoom(
                resultSet.getLong("class_room_id"),
                resultSet.getString("class_code"),
                resultSet.getString("class_name"),
                resultSet.getString("academic_year"),
                faculty
        );
        Date dateOfBirth = resultSet.getDate("date_of_birth");
        return new Student(
                resultSet.getLong("id"),
                resultSet.getObject("user_id", Long.class),
                resultSet.getString("student_code"),
                resultSet.getString("full_name"),
                resultSet.getString("gender"),
                dateOfBirth == null ? null : dateOfBirth.toLocalDate(),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                faculty,
                classRoom,
                resultSet.getString("status")
        );
    }
}
