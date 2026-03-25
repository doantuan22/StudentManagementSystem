package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Room;
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
 * DAO thao tac voi bang hoc phan mo.
 */
public class CourseSectionDAO {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public List<CourseSection> findAll() {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, room_id, semester, school_year, schedule_text, max_students
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
            throw new AppException("Không thể tải danh sách học phần.", exception);
        }
    }

    public Optional<CourseSection> findById(Long id) {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, room_id, semester, school_year, schedule_text, max_students
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
            throw new AppException("Không thể tìm học phần theo mã định danh.", exception);
        }
    }

    public List<CourseSection> findByLecturerId(Long lecturerId) {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, room_id, semester, school_year, schedule_text, max_students
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
            throw new AppException("Không thể tải học phần của giảng viên.", exception);
        }
    }

    public List<CourseSection> searchByKeyword(String keyword) {
        String sql = """
                SELECT id, section_code, subject_id, lecturer_id, room_id, semester, school_year, schedule_text, max_students
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
            throw new AppException("Không thể tìm kiếm học phần.", exception);
        }
    }

    public CourseSection insert(CourseSection courseSection) {
        String sql = """
                INSERT INTO course_sections(section_code, subject_id, lecturer_id, room_id, semester, school_year, schedule_text, max_students)
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
            throw new AppException("Không thể thêm học phần.", exception);
        }
    }

    public boolean update(CourseSection courseSection) {
        String sql = """
                UPDATE course_sections
                SET section_code = ?, subject_id = ?, lecturer_id = ?, room_id = ?, semester = ?, school_year = ?, schedule_text = ?, max_students = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, courseSection);
            statement.setLong(9, courseSection.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể cập nhật học phần.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM course_sections WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Không thể xóa học phần vì vẫn còn lịch học, đăng ký học phần hoặc điểm liên quan.",
                    exception);
        } catch (SQLException exception) {
            throw new AppException("Không thể xóa học phần.", exception);
        }
    }

    public int countEnrollments(Long courseSectionId) {
        String sql = "SELECT COUNT(1) FROM enrollments WHERE course_section_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, courseSectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể đếm số lượng đăng ký của học phần.", exception);
        }
    }

    public void updateScheduleSummary(Long courseSectionId, String scheduleText, Room room) {
        String sql = "UPDATE course_sections SET schedule_text = ?, room_id = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, scheduleText);
            statement.setLong(2, room.getId());
            statement.setLong(3, courseSectionId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new AppException("Không thể đồng bộ thông tin lịch học cho học phần.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, CourseSection courseSection) throws SQLException {
        statement.setString(1, courseSection.getSectionCode());
        statement.setLong(2, courseSection.getSubject().getId());
        statement.setLong(3, courseSection.getLecturer().getId());
        statement.setLong(4, courseSection.getRoom().getId());
        statement.setString(5, courseSection.getSemester());
        statement.setString(6, courseSection.getSchoolYear());
        statement.setString(7, courseSection.getScheduleText());
        statement.setInt(8, courseSection.getMaxStudents());
    }

    private CourseSection mapRow(ResultSet resultSet) throws SQLException {
        Subject subject = subjectDAO.findById(resultSet.getLong("subject_id")).orElse(null);
        Lecturer lecturer = lecturerDAO.findById(resultSet.getLong("lecturer_id")).orElse(null);
        Room room = roomDAO.findById(resultSet.getLong("room_id")).orElse(null);
        return new CourseSection(
                resultSet.getLong("id"),
                resultSet.getString("section_code"),
                subject,
                lecturer,
                room,
                resultSet.getString("semester"),
                resultSet.getString("school_year"),
                resultSet.getString("schedule_text"),
                resultSet.getInt("max_students"));
    }
}
