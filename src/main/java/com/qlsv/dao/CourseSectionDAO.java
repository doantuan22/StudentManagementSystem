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

    private static final String BASE_SELECT = """
            SELECT cs.id,
                   cs.section_code,
                   cs.subject_id,
                   cs.lecturer_id,
                   (
                       SELECT sc.room_id
                       FROM schedules sc
                       WHERE sc.course_section_id = cs.id
                       ORDER BY sc.day_of_week, sc.start_period, sc.id
                       LIMIT 1
                   ) AS room_id,
                   cs.semester,
                   cs.school_year,
                   (
                       SELECT GROUP_CONCAT(
                                  CONCAT(sc.day_of_week, ' tiet ', sc.start_period, '-', sc.end_period, ' phong ', rr.room_name)
                                  ORDER BY sc.day_of_week, sc.start_period, sc.id
                                  SEPARATOR '; '
                              )
                       FROM schedules sc
                       JOIN rooms rr ON rr.id = sc.room_id
                       WHERE sc.course_section_id = cs.id
                   ) AS schedule_text,
                   cs.max_students
            FROM course_sections cs
            """;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final LecturerDAO lecturerDAO = new LecturerDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    public List<CourseSection> findAll() {
        String sql = BASE_SELECT + " ORDER BY cs.id";
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
        String sql = BASE_SELECT + " WHERE cs.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim hoc phan theo ma dinh danh.", exception);
        }
    }

    public List<CourseSection> findByLecturerId(Long lecturerId) {
        String sql = BASE_SELECT + " WHERE cs.lecturer_id = ? ORDER BY cs.id";
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

    public List<CourseSection> findByRoomId(Long roomId) {
        String sql = BASE_SELECT + """
                WHERE EXISTS (
                    SELECT 1
                    FROM schedules sc
                    WHERE sc.course_section_id = cs.id
                      AND sc.room_id = ?
                )
                ORDER BY cs.id
                """;
        List<CourseSection> courseSections = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courseSections.add(mapRow(resultSet));
                }
                return courseSections;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tai hoc phan theo phong hoc.", exception);
        }
    }

    public List<CourseSection> searchByKeyword(String keyword) {
        String sql = BASE_SELECT + """
                 WHERE cs.section_code LIKE ? OR cs.semester LIKE ? OR cs.school_year LIKE ?
                 ORDER BY cs.id
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
                INSERT INTO course_sections(section_code, subject_id, lecturer_id, semester, school_year, max_students)
                VALUES (?, ?, ?, ?, ?, ?)
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
                SET section_code = ?, subject_id = ?, lecturer_id = ?, semester = ?, school_year = ?, max_students = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, courseSection);
            statement.setLong(7, courseSection.getId());
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
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Khong the xoa hoc phan vi van con lich hoc, dang ky hoc phan hoac diem lien quan.",
                    exception);
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa hoc phan.", exception);
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
            throw new AppException("Khong the dem so luong dang ky cua hoc phan.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, CourseSection courseSection) throws SQLException {
        statement.setString(1, courseSection.getSectionCode());
        statement.setLong(2, courseSection.getSubject().getId());
        statement.setLong(3, courseSection.getLecturer().getId());
        statement.setString(4, courseSection.getSemester());
        statement.setString(5, courseSection.getSchoolYear());
        statement.setInt(6, courseSection.getMaxStudents());
    }

    private CourseSection mapRow(ResultSet resultSet) throws SQLException {
        Subject subject = subjectDAO.findById(resultSet.getLong("subject_id")).orElse(null);
        Lecturer lecturer = lecturerDAO.findById(resultSet.getLong("lecturer_id")).orElse(null);
        Long roomId = resultSet.getObject("room_id", Long.class);
        Room room = roomId == null ? null : roomDAO.findById(roomId).orElse(null);
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
