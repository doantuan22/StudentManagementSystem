package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Room;
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
    private final RoomDAO roomDAO = new RoomDAO();

    public List<Schedule> findAll() {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room_id, note
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
            throw new AppException("Không thể tải danh sách lịch học.", exception);
        }
    }

    public Optional<Schedule> findById(Long id) {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room_id, note
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
            throw new AppException("Không thể tìm lịch học theo mã định danh.", exception);
        }
    }

    public List<Schedule> findByStudentId(Long studentId) {
        String sql = """
                SELECT s.id, s.course_section_id, s.day_of_week, s.start_period, s.end_period, s.room_id, s.note
                FROM schedules s
                JOIN enrollments e ON e.course_section_id = s.course_section_id
                WHERE e.student_id = ?
                ORDER BY s.day_of_week, s.start_period
                """;
        return findManyBySingleId(sql, studentId, "Không thể tải lịch học của sinh viên.");
    }

    public List<Schedule> findByLecturerId(Long lecturerId) {
        String sql = """
                SELECT s.id, s.course_section_id, s.day_of_week, s.start_period, s.end_period, s.room_id, s.note
                FROM schedules s
                JOIN course_sections cs ON cs.id = s.course_section_id
                WHERE cs.lecturer_id = ?
                ORDER BY s.day_of_week, s.start_period
                """;
        return findManyBySingleId(sql, lecturerId, "Không thể tải lịch dạy của giảng viên.");
    }

    public List<Schedule> findByCourseSectionId(Long courseSectionId) {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room_id, note
                FROM schedules
                WHERE course_section_id = ?
                ORDER BY day_of_week, start_period
        """;
        return findManyBySingleId(sql, courseSectionId, "Không thể tải lịch học của học phần.");
    }

    public List<Schedule> findByRoom(Long roomId) {
        String sql = """
                SELECT id, course_section_id, day_of_week, start_period, end_period, room_id, note
                FROM schedules
                WHERE room_id = ?
                ORDER BY day_of_week, start_period
                """;
        List<Schedule> schedules = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    schedules.add(mapRow(resultSet));
                }
                return schedules;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tải lịch học theo phòng học.", exception);
        }
    }

    public Schedule insert(Schedule schedule) {
        String sql = """
                INSERT INTO schedules(course_section_id, day_of_week, start_period, end_period, room_id, note)
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
            throw new AppException("Không thể thêm lịch học.", exception);
        }
    }

    public boolean update(Schedule schedule) {
        String sql = """
                UPDATE schedules
                SET course_section_id = ?, day_of_week = ?, start_period = ?, end_period = ?, room_id = ?, note = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, schedule);
            statement.setLong(7, schedule.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể cập nhật lịch học.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể xóa lịch học.", exception);
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
            throw new AppException("Không thể kiểm tra trùng lịch của giảng viên.", exception);
        }
    }

    public boolean hasRoomScheduleConflict(Schedule schedule, Long excludeScheduleId) {
        String sql = """
                SELECT COUNT(1)
                FROM schedules s
                WHERE s.room_id = ?
                  AND s.day_of_week = ?
                  AND NOT (s.end_period < ? OR s.start_period > ?)
                  AND (? IS NULL OR s.id <> ?)
                """;
        return hasConflict(sql, schedule, excludeScheduleId, "Không thể kiểm tra trùng lịch của phòng học.");
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
            throw new AppException("Không thể kiểm tra trùng lịch của sinh viên.", exception);
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
            statement.setLong(1, schedule.getRoom().getId());
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
        statement.setLong(5, schedule.getRoom().getId());
        statement.setString(6, schedule.getNote());
    }

    private Schedule mapRow(ResultSet resultSet) throws SQLException {
        CourseSection courseSection = courseSectionDAO.findById(resultSet.getLong("course_section_id")).orElse(null);
        Room room = roomDAO.findById(resultSet.getLong("room_id")).orElse(null);
        return new Schedule(
                resultSet.getLong("id"),
                courseSection,
                resultSet.getString("day_of_week"),
                resultSet.getInt("start_period"),
                resultSet.getInt("end_period"),
                room,
                resultSet.getString("note")
        );
    }
}
