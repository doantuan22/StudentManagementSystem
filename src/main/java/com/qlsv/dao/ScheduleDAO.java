package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Room;
import com.qlsv.model.Schedule;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Schedule is loaded via JPA; fetch plans are tuned for detached Swing screens and avoid legacy JDBC hydration.
 */
public class ScheduleDAO {

    private static final String FETCH_BASE = """
            SELECT DISTINCT s
            FROM Schedule s
            JOIN FETCH s.courseSection cs
            JOIN FETCH cs.subject subject
            JOIN FETCH subject.faculty
            JOIN FETCH cs.lecturer lecturer
            JOIN FETCH s.room room
            """;

    public List<Schedule> findAll() {
        return executeRead("Không thể tải danh sách lịch học.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " ORDER BY s.dayOfWeek, s.startPeriod, cs.id, s.id",
                        Schedule.class
                )
                .getResultList());
    }

    public Optional<Schedule> findById(Long id) {
        return executeRead("Không thể tìm lịch học theo mã định danh.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE s.id = :id",
                        Schedule.class
                )
                .setParameter("id", id)
                .getResultList()
                .stream()
                .findFirst());
    }

    public List<Schedule> findByStudentId(Long studentId) {
        return executeRead("Không thể tải lịch học của sinh viên.", entityManager ->
                entityManager.createQuery("""
                                SELECT DISTINCT s
                                FROM Schedule s
                                JOIN FETCH s.courseSection cs
                                JOIN FETCH cs.subject subject
                                JOIN FETCH subject.faculty
                                JOIN FETCH cs.lecturer lecturer
                                JOIN FETCH s.room room
                                JOIN Enrollment enrollment ON enrollment.courseSection = cs
                                WHERE enrollment.student.id = :studentId
                                ORDER BY s.dayOfWeek, s.startPeriod, cs.id, s.id
                                """, Schedule.class)
                    .setParameter("studentId", studentId)
                    .getResultList());
    }

    public List<Schedule> findByLecturerId(Long lecturerId) {
        return executeRead("Không thể tải lịch dạy của giảng viên.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE cs.lecturer.id = :lecturerId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("lecturerId", lecturerId)
                .getResultList());
    }

    public List<Schedule> findByCourseSectionId(Long courseSectionId) {
        return executeRead("Không thể tải lịch học của học phần.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE cs.id = :courseSectionId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("courseSectionId", courseSectionId)
                .getResultList());
    }

    public List<Schedule> findByRoom(Long roomId) {
        return executeRead("Không thể tải lịch học theo phòng học.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE room.id = :roomId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("roomId", roomId)
                .getResultList());
    }

    public List<Schedule> findByFacultyId(Long facultyId) {
        return executeRead("Không thể tải lịch học theo khoa.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE subject.faculty.id = :facultyId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("facultyId", facultyId)
                .getResultList());
    }

    public List<Schedule> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm lịch học.", entityManager -> entityManager.createQuery("""
                        SELECT DISTINCT s
                        FROM Schedule s
                        JOIN FETCH s.courseSection cs
                        JOIN FETCH cs.subject subject
                        JOIN FETCH subject.faculty
                        JOIN FETCH cs.lecturer lecturer
                        JOIN FETCH s.room room
                        WHERE LOWER(cs.sectionCode) LIKE :keyword
                           OR LOWER(subject.subjectCode) LIKE :keyword
                           OR LOWER(subject.subjectName) LIKE :keyword
                           OR LOWER(lecturer.lecturerCode) LIKE :keyword
                           OR LOWER(lecturer.fullName) LIKE :keyword
                           OR LOWER(room.roomCode) LIKE :keyword
                           OR LOWER(room.roomName) LIKE :keyword
                           OR LOWER(s.dayOfWeek) LIKE :keyword
                           OR LOWER(COALESCE(s.note, '')) LIKE :keyword
                           OR STR(s.startPeriod) LIKE :keyword
                           OR STR(s.endPeriod) LIKE :keyword
                        ORDER BY s.dayOfWeek, s.startPeriod, s.id
                        """, Schedule.class)
                .setParameter("keyword", normalizedKeyword)
                .getResultList());
    }

    public Schedule insert(Schedule schedule) {
        Long scheduleId = executeWrite(
                "Không thể thêm lịch học.",
                "Không thể thêm lịch học do học phần hoặc phòng học không hợp lệ, hoặc lịch học bị trùng.",
                entityManager -> {
                    Schedule entity = new Schedule();
                    copyState(entityManager, schedule, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    return entity.getId();
                }
        );
        return findById(scheduleId)
                .orElseThrow(() -> new AppException("Không thể tải lại lịch học sau khi thêm."));
    }

    public boolean update(Schedule schedule) {
        return executeWrite(
                "Không thể cập nhật lịch học.",
                "Không thể cập nhật lịch học do học phần hoặc phòng học không hợp lệ, hoặc lịch học bị trùng.",
                entityManager -> {
                    Schedule entity = entityManager.find(Schedule.class, schedule.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(entityManager, schedule, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa lịch học.",
                "Không thể xóa lịch học vì dữ liệu liên quan vẫn tồn tại.",
                entityManager -> {
                    Schedule entity = entityManager.find(Schedule.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    public boolean hasLecturerScheduleConflict(Schedule schedule, Long excludeScheduleId) {
        return executeRead("Không thể kiểm tra trùng lịch của giảng viên.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(s)
                            FROM Schedule s
                            JOIN s.courseSection otherSection
                            WHERE otherSection.lecturer.id = (
                                SELECT currentSection.lecturer.id
                                FROM CourseSection currentSection
                                WHERE currentSection.id = :courseSectionId
                            )
                              AND s.dayOfWeek = :dayOfWeek
                              AND NOT (s.endPeriod < :startPeriod OR s.startPeriod > :endPeriod)
                              AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
                            """, Long.class)
                    .setParameter("courseSectionId", schedule.getCourseSection().getId())
                    .setParameter("dayOfWeek", schedule.getDayOfWeek())
                    .setParameter("startPeriod", schedule.getStartPeriod())
                    .setParameter("endPeriod", schedule.getEndPeriod())
                    .setParameter("excludeScheduleId", excludeScheduleId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    public boolean hasRoomScheduleConflict(Schedule schedule, Long excludeScheduleId) {
        return executeRead("Không thể kiểm tra trùng lịch của phòng học.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(s)
                            FROM Schedule s
                            WHERE s.room.id = :roomId
                              AND s.dayOfWeek = :dayOfWeek
                              AND NOT (s.endPeriod < :startPeriod OR s.startPeriod > :endPeriod)
                              AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
                            """, Long.class)
                    .setParameter("roomId", schedule.getRoom().getId())
                    .setParameter("dayOfWeek", schedule.getDayOfWeek())
                    .setParameter("startPeriod", schedule.getStartPeriod())
                    .setParameter("endPeriod", schedule.getEndPeriod())
                    .setParameter("excludeScheduleId", excludeScheduleId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    public boolean hasStudentScheduleConflict(Long studentId, Long courseSectionId, Long excludeEnrollmentId) {
        return executeRead("Không thể kiểm tra trùng lịch của sinh viên.", entityManager -> {
            Long count = entityManager.createQuery("""
                            SELECT COUNT(existingSchedule)
                            FROM Schedule existingSchedule
                            JOIN Enrollment enrollment ON enrollment.courseSection = existingSchedule.courseSection
                            WHERE enrollment.student.id = :studentId
                              AND (:excludeEnrollmentId IS NULL OR enrollment.id <> :excludeEnrollmentId)
                              AND EXISTS (
                                    SELECT newSchedule.id
                                    FROM Schedule newSchedule
                                    WHERE newSchedule.courseSection.id = :courseSectionId
                                      AND existingSchedule.dayOfWeek = newSchedule.dayOfWeek
                                      AND NOT (
                                            existingSchedule.endPeriod < newSchedule.startPeriod
                                         OR existingSchedule.startPeriod > newSchedule.endPeriod
                                      )
                              )
                            """, Long.class)
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("studentId", studentId)
                    .setParameter("excludeEnrollmentId", excludeEnrollmentId)
                    .getSingleResult();
            return count != null && count.intValue() > 0;
        });
    }

    private void copyState(EntityManager entityManager, Schedule source, Schedule target) {
        target.setCourseSection(resolveCourseSectionReference(entityManager, source.getCourseSection()));
        target.setDayOfWeek(source.getDayOfWeek());
        target.setStartPeriod(source.getStartPeriod());
        target.setEndPeriod(source.getEndPeriod());
        target.setRoom(resolveRoomReference(entityManager, source.getRoom()));
        target.setNote(source.getNote());
    }

    private CourseSection resolveCourseSectionReference(EntityManager entityManager, CourseSection courseSection) {
        if (courseSection == null || courseSection.getId() == null) {
            return null;
        }
        return entityManager.getReference(CourseSection.class, courseSection.getId());
    }

    private Room resolveRoomReference(EntityManager entityManager, Room room) {
        if (room == null || room.getId() == null) {
            return null;
        }
        return entityManager.getReference(Room.class, room.getId());
    }

    private <T> T executeRead(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeWithEntityManager(action);
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }

    private <T> T executeWrite(String errorMessage, String constraintMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeInCurrentTransaction(action);
        } catch (RuntimeException exception) {
            if (isConstraintViolation(exception)) {
                throw new AppException(constraintMessage, exception);
            }
            throw new AppException(errorMessage, exception);
        }
    }

    private boolean isConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }
            if (current instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
