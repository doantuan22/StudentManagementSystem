package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tac voi bang hoc phan mo.
 */
public class CourseSectionDAO {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final ClassRoomDAO classRoomDAO = new ClassRoomDAO();

    public List<CourseSection> findAll() {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students
                FROM course_sections
                ORDER BY id
                """;
        List<CourseSection> courseSections = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                courseSections.add(mapRow(resultSet));
            }
            return courseSections;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach hoc phan.", exception);
        }
    }

    public Optional<CourseSection> findById(Long id) {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students
                FROM course_sections
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim hoc phan theo id.", exception);
        }
    }

    public List<CourseSection> findByLecturerId(Long lecturerId) {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students
                FROM course_sections
                WHERE lecturer_id = ?
                ORDER BY id
                """;
        List<CourseSection> courseSections = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, lecturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courseSections.add(mapRow(resultSet));
                }
                return courseSections;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tai hoc phan cua giang vien.", exception);
        }
    }

    public List<CourseSection> searchByKeyword(String keyword) {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students
                FROM course_sections
                WHERE section_code LIKE ? OR semester LIKE ? OR school_year LIKE ?
                ORDER BY id
                """;
        String searchValue = "%" + keyword + "%";
        List<CourseSection> courseSections = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            statement.setString(3, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courseSections.add(mapRow(resultSet));
                }
                return courseSections;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim kiem hoc phan.", exception);
        }
    }

    public CourseSection insert(CourseSection courseSection) {
        String sql = """
                INSERT INTO course_sections(section_code, subject_id, lecturer_id, class_room_id, semester, school_year, schedule_text, max_students)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, courseSection);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    courseSection.setId(resultSet.getLong(1));
                }
            }
            return courseSection;
        } catch (SQLException exception) {
            throw new AppException("Khong the them hoc phan.", exception);
        }
    }

    public boolean update(CourseSection courseSection) {
        String sql = """
                UPDATE course_sections
                SET section_code = ?, subject_id = ?, lecturer_id = ?, class_room_id = ?, semester = ?, school_year = ?, schedule_text = ?, max_students = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, courseSection);
            statement.setLong(9, courseSection.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat hoc phan.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM course_sections WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa hoc phan.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, CourseSection courseSection) throws SQLException {
        statement.setString(1, courseSection.getSectionCode());
        statement.setLong(2, courseSection.getSubject().getId());
        statement.setLong(3, courseSection.getLecturer().getId());
        statement.setLong(4, courseSection.getClassRoom().getId());
        statement.setString(5, courseSection.getSemester());
        statement.setString(6, courseSection.getSchoolYear());
        statement.setString(7, courseSection.getScheduleText());
        statement.setInt(8, courseSection.getMaxStudents());
    }

    private CourseSection mapRow(ResultSet resultSet) throws SQLException {
        Subject subject = subjectDAO.findById(resultSet.getLong("subject_id")).orElse(null);
        Lecturer lecturer = lecturerDAO.findById(resultSet.getLong("lecturer_id")).orElse(null);
        ClassRoom classRoom = classRoomDAO.findById(resultSet.getLong("class_room_id")).orElse(null);
        return new CourseSection(
                resultSet.getLong("id"),
                resultSet.getString("section_code"),
                subject,
                lecturer,
                classRoom,
                resultSet.getString("semester"),
                resultSet.getString("school_year"),
                resultSet.getString("schedule_text"),
                resultSet.getInt("max_students")
        );
    }
}
