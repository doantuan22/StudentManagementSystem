package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.SystemStatistics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO tong hop du lieu bao cao va thong ke.
 */
public class ReportDAO {

    public List<Object[]> findStudentsByClassRoom(Long classRoomId) {
        String sql = """
                SELECT s.student_code, s.full_name, s.email, s.phone, s.status
                FROM students s
                WHERE s.class_room_id = ?
                ORDER BY s.student_code
                """;
        return queryRows(sql, classRoomId, 5, "Khong the tai danh sach sinh vien theo lop.");
    }

    public List<Object[]> findLecturersByFaculty(Long facultyId) {
        String sql = """
                SELECT l.lecturer_code, l.full_name, l.email, l.phone, l.status
                FROM lecturers l
                WHERE l.faculty_id = ?
                ORDER BY l.lecturer_code
                """;
        return queryRows(sql, facultyId, 5, "Khong the tai danh sach giang vien theo khoa.");
    }

    public List<Object[]> findStudentsByCourseSection(Long courseSectionId) {
        String sql = """
                SELECT s.student_code, s.full_name, s.email, e.status, e.enrolled_at
                FROM enrollments e
                JOIN students s ON s.id = e.student_id
                WHERE e.course_section_id = ?
                ORDER BY s.student_code
                """;
        return queryRows(sql, courseSectionId, 5, "Khong the tai danh sach sinh vien trong hoc phan.");
    }

    public List<Object[]> findScoresByCourseSection(Long courseSectionId) {
        String sql = """
                SELECT s.student_code,
                       s.full_name,
                       COALESCE(sc.process_score, 0),
                       COALESCE(sc.midterm_score, 0),
                       COALESCE(sc.final_score, 0),
                       COALESCE(sc.total_score, 0),
                       COALESCE(sc.result, 'FAIL')
                FROM enrollments e
                JOIN students s ON s.id = e.student_id
                LEFT JOIN scores sc ON sc.enrollment_id = e.id
                WHERE e.course_section_id = ?
                ORDER BY s.student_code
                """;
        return queryRows(sql, courseSectionId, 7, "Khong the tai bang diem theo hoc phan.");
    }

    public SystemStatistics getSystemStatistics() {
        String sql = """
                SELECT
                    (SELECT COUNT(*) FROM students) AS total_students,
                    (SELECT COUNT(*) FROM lecturers) AS total_lecturers,
                    (SELECT COUNT(*) FROM subjects) AS total_subjects,
                    (SELECT COUNT(*) FROM course_sections) AS total_course_sections,
                    (SELECT COUNT(*) FROM enrollments) AS total_enrollments
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return new SystemStatistics(
                        resultSet.getLong("total_students"),
                        resultSet.getLong("total_lecturers"),
                        resultSet.getLong("total_subjects"),
                        resultSet.getLong("total_course_sections"),
                        resultSet.getLong("total_enrollments")
                );
            }
            return new SystemStatistics();
        } catch (SQLException exception) {
            throw new AppException("Khong the tai thong ke he thong.", exception);
        }
    }

    private List<Object[]> queryRows(String sql, Long id, int columnCount, String message) {
        List<Object[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Object[] row = new Object[columnCount];
                    for (int index = 0; index < columnCount; index++) {
                        row[index] = resultSet.getObject(index + 1);
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (SQLException exception) {
            throw new AppException(message, exception);
        }
    }
}
