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

    public List<Object[]> findStudentsByClassRoom(Long classRoomId) {
        return executeRead("Khong the tai danh sach sinh vien theo lop.", entityManager ->
                entityManager.createQuery("""
                                SELECT s.studentCode, s.fullName, s.email, s.phone, s.status
                                FROM Student s
                                WHERE s.classRoom.id = :classRoomId
                                ORDER BY s.studentCode
                                """, Object[].class)
                        .setParameter("classRoomId", classRoomId)
                        .getResultList());
    }

    public List<Object[]> findLecturersByFaculty(Long facultyId) {
        return executeRead("Khong the tai danh sach giang vien theo khoa.", entityManager ->
                entityManager.createQuery("""
                                SELECT l.lecturerCode, l.fullName, l.email, l.phone, l.status
                                FROM Lecturer l
                                WHERE l.faculty.id = :facultyId
                                ORDER BY l.lecturerCode
                                """, Object[].class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    public List<Object[]> findStudentsByCourseSection(Long courseSectionId) {
        return executeRead("Khong the tai danh sach sinh vien trong hoc phan.", entityManager ->
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

    public List<Object[]> findScoresByCourseSection(Long courseSectionId) {
        return executeRead("Khong the tai bang diem theo hoc phan.", entityManager ->
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

    public SystemStatistics getSystemStatistics() {
        return executeRead("Khong the tai thong ke he thong.", entityManager -> new SystemStatistics(
                count(entityManager, "SELECT COUNT(s) FROM Student s"),
                count(entityManager, "SELECT COUNT(l) FROM Lecturer l"),
                count(entityManager, "SELECT COUNT(su) FROM Subject su"),
                count(entityManager, "SELECT COUNT(cs) FROM CourseSection cs"),
                count(entityManager, "SELECT COUNT(e) FROM Enrollment e")
        ));
    }

    private long count(EntityManager entityManager, String jpql) {
        return entityManager.createQuery(jpql, Long.class).getSingleResult();
    }

    private <T> T executeRead(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeWithEntityManager(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }
}
