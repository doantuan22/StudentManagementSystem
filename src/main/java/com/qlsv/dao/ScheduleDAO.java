package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO quan ly bang schedules de phuc vu lich hoc, dang ky hoc phan va kiem tra trung lich.
 */
public class ScheduleDAO {

    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();

    public List<Schedule> findAll() {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room, note
                FROM schedules
                ORDER BY day_of_week, start_period, course_section_id
                """;
        List<Schedule> schedules = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                schedules.add(mapRow(resultSet));
            }
            return schedules;
        } catch (SQLException exception) {
            throw new AppException("Khong the tai danh sach lich hoc.", exception);
        }
    }

    public Optional<Schedule> findById(Long id) {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room, note
                FROM schedules
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the tim lich hoc theo id.", exception);
        }
    }

    public List<Schedule> findByStudentId(Long studentId) {
        String sql = """
                SELECT s.id, s.course_section_id, s.day_of_week, s.start_period, s.end_period, s.room, s.note
                FROM schedules s
                JOIN enrollments e ON e.course_section_id = s.course_section_id
                WHERE e.student_id = ?
                ORDER BY s.day_of_week, s.start_period
                """;
        return findManyBySingleId(sql, studentId, "Khong the tai lich hoc cua sinh vien.");
    }

    public List<Schedule> findByLecturerId(Long lecturerId) {
        String sql = """
                SELECT s.id, s.course_section_id, s.day_of_week, s.start_period, s.end_period, s.room, s.note
                FROM schedules s
                JOIN course_sections cs ON cs.id = s.course_section_id
                WHERE cs.lecturer_id = ?
                ORDER BY s.day_of_week, s.start_period
                """;
        return findManyBySingleId(sql, lecturerId, "Khong the tai lich day cua giang vien.");
    }

    public List<Schedule> findByCourseSectionId(Long courseSectionId) {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room, note
                FROM schedules
                WHERE course_section_id = ?
                ORDER BY day_of_week, start_period
                """;
        return findManyBySingleId(sql, courseSectionId, "Khong the tai lich hoc cua hoc phan.");
    }

    public Schedule insert(Schedule schedule) {
        String sql = """
                INSERT INTO schedules(course_section_id, day_of_week, start_period, end_period, room, note)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, schedule);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    schedule.setId(resultSet.getLong(1));
                }
            }
            return schedule;
        } catch (SQLException exception) {
            throw new AppException("Khong the them lich hoc.", exception);
        }
    }

    public boolean update(Schedule schedule) {
        String sql = """
                UPDATE schedules
                SET course_section_id = ?, day_of_week = ?, start_period = ?, end_period = ?, room = ?, note = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, schedule);
            statement.setLong(7, schedule.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the cap nhat lich hoc.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Khong the xoa lich hoc.", exception);
        }
    }

    public boolean hasLecturerScheduleConflict(Schedule schedule, Long excludeScheduleId) {
        String sql = """
                SELECT COUNT(1)
                FROM schedules s
                JOIN course_sections current_section ON current_section.id = ?
                JOIN course_sections other_section ON other_section.id = s.course_section_id
                WHERE other_section.lecturer_id = current_section.lecturer_id
                  AND s.day_of_week = ?
                  AND NOT (s.end_period < ? OR s.start_period > ?)
                  AND (? IS NULL OR s.id <> ?)
                """;
        return hasConflict(sql, schedule, excludeScheduleId, "Khong the kiem tra trung lich cua giang vien.");
    }

    public boolean hasClassRoomScheduleConflict(Schedule schedule, Long excludeScheduleId) {
        String sql = """
                SELECT COUNT(1)
                FROM schedules s
                JOIN course_sections current_section ON current_section.id = ?
                JOIN course_sections other_section ON other_section.id = s.course_section_id
                WHERE other_section.class_room_id = current_section.class_room_id
                  AND s.day_of_week = ?
                  AND NOT (s.end_period < ? OR s.start_period > ?)
                  AND (? IS NULL OR s.id <> ?)
                """;
        return hasConflict(sql, schedule, excludeScheduleId, "Khong the kiem tra trung lich cua lop hoc.");
    }

    public boolean hasStudentScheduleConflict(Long studentId, Long courseSectionId, Long excludeEnrollmentId) {
        String sql = """
                SELECT COUNT(1)
                FROM schedules existing_schedule
                JOIN enrollments e ON e.course_section_id = existing_schedule.course_section_id
                JOIN schedules new_schedule ON new_schedule.course_section_id = ?
                WHERE e.student_id = ?
                  AND existing_schedule.day_of_week = new_schedule.day_of_week
                  AND NOT (existing_schedule.end_period < new_schedule.start_period
                           OR existing_schedule.start_period > new_schedule.end_period)
                  AND (? IS NULL OR e.id <> ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, courseSectionId);
            statement.setLong(2, studentId);
            statement.setObject(3, excludeEnrollmentId);
            statement.setObject(4, excludeEnrollmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new AppException("Khong the kiem tra trung lich cua sinh vien.", exception);
        }
    }

    private List<Schedule> findManyBySingleId(String sql, Long id, String message) {
        List<Schedule> schedules = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    schedules.add(mapRow(resultSet));
                }
                return schedules;
            }
        } catch (SQLException exception) {
            throw new AppException(message, exception);
        }
    }

    private boolean hasConflict(String sql, Schedule schedule, Long excludeScheduleId, String message) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, schedule.getCourseSection().getId());
            statement.setString(2, schedule.getDayOfWeek());
            statement.setInt(3, schedule.getStartPeriod());
            statement.setInt(4, schedule.getEndPeriod());
            statement.setObject(5, excludeScheduleId);
            statement.setObject(6, excludeScheduleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new AppException(message, exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Schedule schedule) throws SQLException {
        statement.setLong(1, schedule.getCourseSection().getId());
        statement.setString(2, schedule.getDayOfWeek());
        statement.setInt(3, schedule.getStartPeriod());
        statement.setInt(4, schedule.getEndPeriod());
        statement.setString(5, schedule.getRoom());
        statement.setString(6, schedule.getNote());
    }

    private Schedule mapRow(ResultSet resultSet) throws SQLException {
        CourseSection courseSection = courseSectionDAO.findById(resultSet.getLong("course_section_id")).orElse(null);
        return new Schedule(
                resultSet.getLong("id"),
                courseSection,
                resultSet.getString("day_of_week"),
                resultSet.getInt("start_period"),
                resultSet.getInt("end_period"),
                resultSet.getString("room"),
                resultSet.getString("note")
        );
    }
}
