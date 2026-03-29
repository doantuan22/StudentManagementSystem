/**
 * Truy vấn dữ liệu lịch bằng JPA.
 */
package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Room;
import com.qlsv.model.Schedule;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Schedule is loaded via JPA; fetch plans are tuned for detached Swing screens and avoid legacy JDBC hydration.
 */
public class ScheduleDAO {

    private static final String EFFECTIVE_ENROLLMENT_STATUS = "REGISTERED";

    private static final String FETCH_BASE = """
            SELECT DISTINCT s
            FROM Schedule s
            JOIN FETCH s.courseSection cs
            JOIN FETCH cs.subject subject
            JOIN FETCH subject.faculty
            JOIN FETCH cs.lecturer lecturer
            JOIN FETCH s.room room
            """;

    /**
     * Lấy danh sách tất cả các lịch học trong hệ thống, sắp xếp theo thứ và tiết học.
     */
    public List<Schedule> findAll() {
        return executeRead("Không thể tải danh sách lịch học.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " ORDER BY s.dayOfWeek, s.startPeriod, cs.id, s.id",
                        Schedule.class
                )
                .getResultList());
    }

    /**
     * Tìm kiếm một bản ghi lịch học dựa trên mã định danh duy nhất.
     */
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

    /**
     * Truy xuất thời khóa biểu của một sinh viên cụ thể dựa trên danh sách học phần đã đăng ký.
     */
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

    /**
     * Truy xuất lịch giảng dạy của một giảng viên cụ thể.
     */
    public List<Schedule> findByLecturerId(Long lecturerId) {
        return executeRead("Không thể tải lịch dạy của giảng viên.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE cs.lecturer.id = :lecturerId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("lecturerId", lecturerId)
                .getResultList());
    }

    /**
     * Lấy danh sách các buổi học/lịch học của một học phần xác định.
     */
    public List<Schedule> findByCourseSectionId(Long courseSectionId) {
        return executeRead("Không thể tải lịch học của học phần.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE cs.id = :courseSectionId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("courseSectionId", courseSectionId)
                .getResultList());
    }

    /**
     * Lọc danh sách lịch học theo phòng học cụ thể.
     */
    public List<Schedule> findByRoom(Long roomId) {
        return executeRead("Không thể tải lịch học theo phòng học.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE room.id = :roomId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("roomId", roomId)
                .getResultList());
    }

    /**
     * Lọc danh sách lịch học theo khoa quản lý.
     */
    public List<Schedule> findByFacultyId(Long facultyId) {
        return executeRead("Không thể tải lịch học theo khoa.", entityManager -> entityManager.createQuery(
                        FETCH_BASE + " WHERE subject.faculty.id = :facultyId ORDER BY s.dayOfWeek, s.startPeriod, s.id",
                        Schedule.class
                )
                .setParameter("facultyId", facultyId)
                .getResultList());
    }

    /**
     * Tìm kiếm lịch học theo từ khóa trên nhiều trường thông tin (mã học phần, môn học, giảng viên, phòng...).
     */
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
                        /**
                         * Xử lý lower.
                         */
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

    /**
     * Thêm mới một bản ghi lịch học vào cơ sở dữ liệu.
     */
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

    /**
     * Cập nhật thông tin của một bản ghi lịch học hiện có.
     */
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

    /**
     * Xóa một bản ghi lịch học khỏi hệ thống theo mã định danh.
     */
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

    /**
     * Kiểm tra xem giảng viên có bị trùng lịch dạy vào thời gian đã chọn hay không.
     */
    public boolean hasLecturerScheduleConflict(Long lecturerId, String dayOfWeek, Integer startPeriod,
                                               Integer endPeriod, Long excludeScheduleId) {
        return executeRead("Không thể kiểm tra trùng lịch của giảng viên.", entityManager -> {
            Long total = entityManager.createQuery("""
                            /**
                             * Xử lý count.
                             */
                            SELECT COUNT(s)
                            FROM Schedule s
                            JOIN s.courseSection otherSection
                            WHERE otherSection.lecturer.id = :lecturerId
                              AND s.dayOfWeek = :dayOfWeek
                              AND NOT (s.endPeriod < :startPeriod OR s.startPeriod > :endPeriod)
                              AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
                            """, Long.class)
                    .setParameter("lecturerId", lecturerId)
                    .setParameter("dayOfWeek", dayOfWeek)
                    .setParameter("startPeriod", startPeriod)
                    .setParameter("endPeriod", endPeriod)
                    .setParameter("excludeScheduleId", excludeScheduleId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    /**
     * Kiểm tra xem phòng học có bị trùng lịch sử dụng vào thời gian đã chọn hay không.
     */
    public boolean hasRoomScheduleConflict(Long roomId, String dayOfWeek, Integer startPeriod,
                                           Integer endPeriod, Long excludeScheduleId) {
        return executeRead("Không thể kiểm tra trùng lịch của phòng học.", entityManager -> {
            Long total = entityManager.createQuery("""
                            /**
                             * Xử lý count.
                             */
                            SELECT COUNT(s)
                            FROM Schedule s
                            WHERE s.room.id = :roomId
                              AND s.dayOfWeek = :dayOfWeek
                              AND NOT (s.endPeriod < :startPeriod OR s.startPeriod > :endPeriod)
                              AND (:excludeScheduleId IS NULL OR s.id <> :excludeScheduleId)
                            """, Long.class)
                    .setParameter("roomId", roomId)
                    .setParameter("dayOfWeek", dayOfWeek)
                    .setParameter("startPeriod", startPeriod)
                    .setParameter("endPeriod", endPeriod)
                    .setParameter("excludeScheduleId", excludeScheduleId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    /**
     * Kiểm tra xem sinh viên có bị trùng thời khóa biểu khi đăng ký thêm học phần mới hay không.
     */
    public boolean hasStudentScheduleConflict(Long studentId, Long courseSectionId, Long excludeEnrollmentId) {
        return executeRead("Không thể kiểm tra trùng lịch của sinh viên.", entityManager -> {
            Long count = entityManager.createQuery("""
                            SELECT COUNT(existingSchedule)
                            FROM Schedule existingSchedule
                            JOIN Enrollment enrollment ON enrollment.courseSection = existingSchedule.courseSection
                            JOIN enrollment.courseSection existingSection
                            WHERE enrollment.student.id = :studentId
                              AND (:excludeEnrollmentId IS NULL OR enrollment.id <> :excludeEnrollmentId)
                              AND UPPER(COALESCE(enrollment.status, 'REGISTERED')) = :effectiveStatus
                              AND EXISTS (
                                    SELECT newSchedule.id
                                    FROM Schedule newSchedule
                                    JOIN newSchedule.courseSection newSection
                                    WHERE newSection.id = :courseSectionId
                                      AND UPPER(REPLACE(existingSection.semester, ' ', '')) = UPPER(REPLACE(newSection.semester, ' ', ''))
                                      AND REPLACE(existingSection.schoolYear, ' ', '') = REPLACE(newSection.schoolYear, ' ', '')
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
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getSingleResult();
            return count != null && count.intValue() > 0;
        });
    }

    /**
     * Sao chép state.
     */
    private void copyState(EntityManager entityManager, Schedule source, Schedule target) {
        target.setCourseSection(resolveCourseSectionReference(entityManager, source.getCourseSection()));
        target.setDayOfWeek(source.getDayOfWeek());
        target.setStartPeriod(source.getStartPeriod());
        target.setEndPeriod(source.getEndPeriod());
        target.setRoom(resolveRoomReference(entityManager, source.getRoom()));
        target.setNote(source.getNote());
    }

    /**
     * Xác định học phần reference.
     */
    private CourseSection resolveCourseSectionReference(EntityManager entityManager, CourseSection courseSection) {
        if (courseSection == null || courseSection.getId() == null) {
            return null;
        }
        return entityManager.getReference(CourseSection.class, courseSection.getId());
    }

    /**
     * Xác định phòng reference.
     */
    private Room resolveRoomReference(EntityManager entityManager, Room room) {
        if (room == null || room.getId() == null) {
            return null;
        }
        return entityManager.getReference(Room.class, room.getId());
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

    /**
     * Thực thi write.
     */
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

    /**
     * Kiểm tra constraint violation.
     */
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
