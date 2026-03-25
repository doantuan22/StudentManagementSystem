package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO thao tac voi bang dang ky hoc phan.
 */
public class EnrollmentDAO {

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseSectionDAO courseSectionDAO = new CourseSectionDAO();

    public List<Enrollment> findAll() {
        String sql = """
                SELECT id, student_id, course_section_id, status, enrolled_at
                FROM enrollments
                ORDER BY id
                """;
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                enrollments.add(mapRow(resultSet));
            }
            return enrollments;
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách đăng ký học phần.", exception);
        }
    }

    public Optional<Enrollment> findById(Long id) {
        String sql = """
                SELECT id, student_id, course_section_id, status, enrolled_at
                FROM enrollments
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm đăng ký học phần theo mã định danh.", exception);
        }
    }

    public List<Enrollment> findByStudentId(Long studentId) {
        String sql = """
                SELECT id, student_id, course_section_id, status, enrolled_at
                FROM enrollments
                WHERE student_id = ?
                ORDER BY id
                """;
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
                return enrollments;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tải học phần đã đăng ký của sinh viên.", exception);
        }
    }

    public List<Enrollment> findByLecturerId(Long lecturerId) {
        String sql = """
                SELECT e.id, e.student_id, e.course_section_id, e.status, e.enrolled_at
                FROM enrollments e
                JOIN course_sections cs ON cs.id = e.course_section_id
                WHERE cs.lecturer_id = ?
                ORDER BY e.id
                """;
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, lecturerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
                return enrollments;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách đăng ký của giảng viên.", exception);
        }
    }

    public List<Enrollment> findByClassRoomId(Long classRoomId) {
        String sql = """
                SELECT e.id, e.student_id, e.course_section_id, e.status, e.enrolled_at
                FROM enrollments e
                JOIN students s ON s.id = e.student_id
                WHERE s.class_room_id = ?
                ORDER BY e.id
                """;
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, classRoomId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
                return enrollments;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách đăng ký theo lớp học.", exception);
        }
    }

    public List<Enrollment> findByCourseSectionId(Long courseSectionId) {
        String sql = """
                SELECT id, student_id, course_section_id, status, enrolled_at
                FROM enrollments
                WHERE course_section_id = ?
                ORDER BY id
                """;
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, courseSectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    enrollments.add(mapRow(resultSet));
                }
                return enrollments;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách sinh viên của học phần.", exception);
        }
    }

    public Optional<Enrollment> findByStudentAndCourseSection(Long studentId, Long courseSectionId) {
        String sql = """
                SELECT id, student_id, course_section_id, status, enrolled_at
                FROM enrollments
                WHERE student_id = ? AND course_section_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, courseSectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm đăng ký theo sinh viên và học phần.", exception);
        }
    }

    public boolean existsByStudentAndSubject(Long studentId, Long subjectId) {
        String sql = """
                SELECT COUNT(1) FROM enrollments e
                JOIN course_sections cs ON cs.id = e.course_section_id
                WHERE e.student_id = ? AND cs.subject_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, subjectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể kiểm tra đăng ký trùng môn học.", exception);
        }
    }

    public boolean existsByStudentAndCourseSection(Long studentId, Long courseSectionId) {
        String sql = "SELECT COUNT(1) FROM enrollments WHERE student_id = ? AND course_section_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, studentId);
            statement.setLong(2, courseSectionId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể kiểm tra đăng ký trùng học phần.", exception);
        }
    }

    public int countByCourseSectionId(Long courseSectionId) {
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

    public Enrollment insert(Enrollment enrollment) {
        String sql = """
                INSERT INTO enrollments(student_id, course_section_id, status, enrolled_at)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(statement, enrollment);
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    enrollment.setId(resultSet.getLong(1));
                }
            }
            return enrollment;
        } catch (SQLException exception) {
            throw new AppException("Không thể thêm đăng ký học phần.", exception);
        }
    }

    public boolean update(Enrollment enrollment) {
        String sql = """
                UPDATE enrollments
                SET student_id = ?, course_section_id = ?, status = ?, enrolled_at = ?
                WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, enrollment);
            statement.setLong(5, enrollment.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new AppException("Không thể cập nhật đăng ký học phần.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Không thể xóa đăng ký học phần vì đã có dữ liệu điểm liên quan.", exception);
        } catch (SQLException exception) {
            throw new AppException("Không thể xóa đăng ký học phần.", exception);
        }
    }

    private void fillStatement(PreparedStatement statement, Enrollment enrollment) throws SQLException {
        statement.setLong(1, enrollment.getStudent().getId());
        statement.setLong(2, enrollment.getCourseSection().getId());
        statement.setString(3, enrollment.getStatus());
        if (enrollment.getEnrolledAt() == null) {
            statement.setTimestamp(4, null);
        } else {
            statement.setTimestamp(4, Timestamp.valueOf(enrollment.getEnrolledAt()));
        }
    }

    private Enrollment mapRow(ResultSet resultSet) throws SQLException {
        Student student = studentDAO.findById(resultSet.getLong("student_id")).orElse(null);
        CourseSection courseSection = courseSectionDAO.findById(resultSet.getLong("course_section_id")).orElse(null);
        Timestamp enrolledAt = resultSet.getTimestamp("enrolled_at");
        return new Enrollment(
                resultSet.getLong("id"),
                student,
                courseSection,
                resultSet.getString("status"),
                enrolledAt == null ? null : enrolledAt.toLocalDateTime()
        );
    }
}
