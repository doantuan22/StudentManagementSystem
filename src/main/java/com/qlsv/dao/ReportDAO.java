/**
 * Truy vấn dữ liệu báo cáo bằng JPA.
 */
package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.SystemStatistics;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.function.Function;

/**
 * Read-only reporting DAO backed by JPQL projections.
 * JDBC is intentionally avoided here because this DAO participates in runtime report flows.
 */
public class ReportDAO {

    /**
     * Truy xuất danh sách thông tin cơ bản của sinh viên theo lớp học để lập báo cáo.
     */
    public List<Object[]> findStudentsByClassRoom(Long classRoomId) {
        return executeRead("Không thể tải danh sách sinh viên theo lớp.", entityManager ->
                entityManager.createQuery("""
                                SELECT s.studentCode, s.fullName, s.email, s.phone, s.status
                                FROM Student s
                                WHERE s.classRoom.id = :classRoomId
                                ORDER BY s.studentCode
                                """, Object[].class)
                        .setParameter("classRoomId", classRoomId)
                        .getResultList());
    }

    /**
     * Truy xuất danh sách thông tin cơ bản của giảng viên theo khoa để lập báo cáo.
     */
    public List<Object[]> findLecturersByFaculty(Long facultyId) {
        return executeRead("Không thể tải danh sách giảng viên theo khoa.", entityManager ->
                entityManager.createQuery("""
                                SELECT l.lecturerCode, l.fullName, l.email, l.phone, l.status
                                FROM Lecturer l
                                WHERE l.faculty.id = :facultyId
                                ORDER BY l.lecturerCode
                                """, Object[].class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    /**
     * Truy xuất danh sách sinh viên đã đăng ký vào một học phần cụ thể.
     */
    public List<Object[]> findStudentsByCourseSection(Long courseSectionId) {
        return executeRead("Không thể tải danh sách sinh viên trong học phần.", entityManager ->
                entityManager.createQuery("""
                                SELECT s.studentCode, s.fullName, s.email, e.status, e.enrolledAt
                                FROM Enrollment e
                                JOIN e.student s
                                WHERE e.courseSection.id = :courseSectionId
                                ORDER BY s.studentCode
                                """, Object[].class)
                        .setParameter("courseSectionId", courseSectionId)
                        .getResultList());
    }

    /**
     * Truy xuất bảng điểm chi tiết của tất cả sinh viên trong một học phần.
     */
    public List<Object[]> findScoresByCourseSection(Long courseSectionId) {
        return executeRead("Không thể tải bảng điểm theo học phần.", entityManager ->
                entityManager.createQuery("""
                                SELECT s.studentCode,
                                       s.fullName,
                                       COALESCE(sc.processScore, 0),
                                       COALESCE(sc.midtermScore, 0),
                                       COALESCE(sc.finalScore, 0),
                                       COALESCE(sc.totalScore, 0),
                                       COALESCE(sc.result, 'FAIL')
                                FROM Enrollment e
                                JOIN e.student s
                                LEFT JOIN Score sc ON sc.enrollment = e
                                WHERE e.courseSection.id = :courseSectionId
                                ORDER BY s.studentCode
                                """, Object[].class)
                        .setParameter("courseSectionId", courseSectionId)
                        .getResultList());
    }

    /**
     * Tổng hợp các số liệu thống kê cơ bản của toàn bộ hệ thống (số lượng SV, GV, môn học...).
     */
    public SystemStatistics getSystemStatistics() {
        return executeRead("Không thể tải thống kê hệ thống.", entityManager -> new SystemStatistics(
                count(entityManager, "SELECT COUNT(s) FROM Student s"),
                count(entityManager, "SELECT COUNT(l) FROM Lecturer l"),
                count(entityManager, "SELECT COUNT(su) FROM Subject su"),
                count(entityManager, "SELECT COUNT(cs) FROM CourseSection cs"),
                count(entityManager, "SELECT COUNT(e) FROM Enrollment e")
        ));
    }

    /**
     * Xử lý count.
     */
    private long count(EntityManager entityManager, String jpql) {
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    /**
     * Thực thi read.
     */
    private <T> T executeRead(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeWithEntityManager(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }
}
