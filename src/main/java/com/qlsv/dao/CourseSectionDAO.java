/**
 * Truy vấn dữ liệu học phần bằng JPA.
 */
package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.Room;
import com.qlsv.model.Subject;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * CourseSection is loaded via JPA; scheduleText and room stay as compatibility fields and should be removed later.
 */
public class CourseSectionDAO {

    private static final String FETCH_BASE = """
            SELECT DISTINCT cs
            FROM CourseSection cs
            JOIN FETCH cs.subject s
            JOIN FETCH s.faculty
            JOIN FETCH cs.lecturer l
            """;

    /**
     * Lấy danh sách tất cả các học phần trong hệ thống.
     */
    public List<CourseSection> findAll() {
        return executeRead("Không thể tải danh sách học phần.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery(
                            FETCH_BASE + " ORDER BY cs.id",
                            CourseSection.class
                    )
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections;
        });
    }

    /**
     * Tìm kiếm học phần theo mã định danh.
     */
    public Optional<CourseSection> findById(Long id) {
        return executeRead("Không thể tìm học phần theo mã định danh.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery(
                            FETCH_BASE + " WHERE cs.id = :id",
                            CourseSection.class
                    )
                    .setParameter("id", id)
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections.stream().findFirst();
        });
    }

    /**
     * Lấy danh sách học phần do một giảng viên phụ trách.
     */
    public List<CourseSection> findByLecturerId(Long lecturerId) {
        return executeRead("Không thể tải học phần của giảng viên.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery(
                            FETCH_BASE + " WHERE cs.lecturer.id = :lecturerId ORDER BY cs.id",
                            CourseSection.class
                    )
                    .setParameter("lecturerId", lecturerId)
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections;
        });
    }

    /**
     * Lấy danh sách học phần thuộc một khoa cụ thể.
     */
    public List<CourseSection> findByFacultyId(Long facultyId) {
        return executeRead("Không thể tải học phần theo khoa.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery(
                            FETCH_BASE + " WHERE s.faculty.id = :facultyId ORDER BY cs.id",
                            CourseSection.class
                    )
                    .setParameter("facultyId", facultyId)
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections;
        });
    }

    /**
     * Tìm kiếm học phần theo mã học phần.
     */
    public List<CourseSection> findBySectionCode(String sectionCode) {
        String normalizedSectionCode = sectionCode == null ? "" : sectionCode.trim();
        return executeRead("Không thể tải học phần theo mã học phần.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery(
                            FETCH_BASE + " WHERE LOWER(cs.sectionCode) = LOWER(:sectionCode) ORDER BY cs.id",
                            CourseSection.class
                    )
                    .setParameter("sectionCode", normalizedSectionCode)
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections;
        });
    }

    /**
     * Lấy danh sách học phần được xếp lịch tại một phòng học.
     */
    public List<CourseSection> findByRoomId(Long roomId) {
        return executeRead("Không thể tải học phần theo phòng học.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery("""
                            SELECT DISTINCT cs
                            FROM CourseSection cs
                            JOIN FETCH cs.subject s
                            JOIN FETCH s.faculty
                            JOIN FETCH cs.lecturer l
                            /**
                             * Xử lý exists.
                             */
                            WHERE EXISTS (
                                SELECT 1
                                FROM Schedule sc
                                WHERE sc.courseSection = cs
                                  AND sc.room.id = :roomId
                            )
                            ORDER BY cs.id
                            """, CourseSection.class)
                    .setParameter("roomId", roomId)
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections;
        });
    }

    /**
     * Tìm kiếm học phần theo từ khóa (mã, học kỳ, năm học, tên môn, giảng viên, phòng).
     */
    public List<CourseSection> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm học phần.", entityManager -> {
            List<CourseSection> courseSections = entityManager.createQuery("""
                            SELECT DISTINCT cs
                            FROM CourseSection cs
                            JOIN FETCH cs.subject s
                            JOIN FETCH s.faculty
                            JOIN FETCH cs.lecturer l
                            /**
                             * Xử lý lower.
                             */
                            WHERE LOWER(cs.sectionCode) LIKE :keyword
                               OR LOWER(cs.semester) LIKE :keyword
                               OR LOWER(cs.schoolYear) LIKE :keyword
                               OR LOWER(s.subjectCode) LIKE :keyword
                               OR LOWER(s.subjectName) LIKE :keyword
                               OR LOWER(l.lecturerCode) LIKE :keyword
                               OR LOWER(l.fullName) LIKE :keyword
                               OR EXISTS (
                                    SELECT 1
                                    FROM Schedule sc
                                    JOIN sc.room r
                                    WHERE sc.courseSection = cs
                                      AND (
                                            LOWER(r.roomCode) LIKE :keyword
                                         OR LOWER(r.roomName) LIKE :keyword
                                      )
                               )
                            ORDER BY cs.id
                            """, CourseSection.class)
                    .setParameter("keyword", normalizedKeyword)
                    .getResultList();
            hydrateScheduleCompatibility(entityManager, courseSections);
            return courseSections;
        });
    }

    /**
     * Thêm mới một học phần vào hệ thống.
     */
    public CourseSection insert(CourseSection courseSection) {
        Long courseSectionId = executeWrite(
                "Không thể thêm học phần.",
                "Không thể thêm học phần do mã học phần đã tồn tại hoặc môn học/giảng viên không hợp lệ.",
                entityManager -> {
                    CourseSection entity = new CourseSection();
                    copyState(entityManager, courseSection, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    courseSection.setId(entity.getId());
                    return entity.getId();
                }
        );
        return findById(courseSectionId)
                .orElseThrow(() -> new AppException("Không thể tải lại học phần sau khi thêm."));
    }

    /**
     * Cập nhật thông tin học phần hiện có.
     */
    public boolean update(CourseSection courseSection) {
        return executeWrite(
                "Không thể cập nhật học phần.",
                "Không thể cập nhật học phần do mã học phần đã tồn tại hoặc môn học/giảng viên không hợp lệ.",
                entityManager -> {
                    CourseSection entity = entityManager.find(CourseSection.class, courseSection.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(entityManager, courseSection, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    /**
     * Xóa học phần khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa học phần.",
                "Không thể xóa học phần vì vẫn còn lịch học, đăng ký học phần hoặc điểm liên quan.",
                entityManager -> {
                    CourseSection entity = entityManager.find(CourseSection.class, id);
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
     * Đếm số lượng sinh viên đã đăng ký vào học phần.
     */
    public int countEnrollments(Long courseSectionId) {
        return executeRead("Không thể đếm số lượng đăng ký của học phần.", entityManager -> {
            Long count = entityManager.createQuery("""
                            /**
                             * Xử lý count.
                             */
                            SELECT COUNT(e)
                            FROM Enrollment e
                            WHERE e.courseSection.id = :courseSectionId
                            """, Long.class)
                    .setParameter("courseSectionId", courseSectionId)
                    .getSingleResult();
            return count == null ? 0 : count.intValue();
        });
    }

    /**
     * Sao chép state.
     */
    private void copyState(EntityManager entityManager, CourseSection source, CourseSection target) {
        target.setSectionCode(source.getSectionCode());
        target.setSemester(source.getSemester());
        target.setSchoolYear(source.getSchoolYear());
        target.setMaxStudents(source.getMaxStudents());
        target.setSubject(resolveSubjectReference(entityManager, source.getSubject()));
        target.setLecturer(resolveLecturerReference(entityManager, source.getLecturer()));
    }

    /**
     * Xác định môn học reference.
     */
    private Subject resolveSubjectReference(EntityManager entityManager, Subject subject) {
        if (subject == null || subject.getId() == null) {
            return null;
        }
        return entityManager.getReference(Subject.class, subject.getId());
    }

    /**
     * Xác định giảng viên reference.
     */
    private Lecturer resolveLecturerReference(EntityManager entityManager, Lecturer lecturer) {
        if (lecturer == null || lecturer.getId() == null) {
            return null;
        }
        return entityManager.getReference(Lecturer.class, lecturer.getId());
    }

    /**
     * Đồng bộ dữ liệu lịch học để đảm bảo tương thích với giao diện cũ.
     */
    private void hydrateScheduleCompatibility(EntityManager entityManager, List<CourseSection> courseSections) {
        if (courseSections == null || courseSections.isEmpty()) {
            return;
        }

        Map<Long, CourseSection> sectionsById = new HashMap<>();
        for (CourseSection courseSection : courseSections) {
            // Compatibility - remove later when legacy screens stop reading room/scheduleText from CourseSection.
            courseSection.applyScheduleCompatibility(null, null);
            if (courseSection.getId() != null) {
                sectionsById.put(courseSection.getId(), courseSection);
            }
        }

        List<Object[]> rows = entityManager.createQuery("""
                        SELECT sc.courseSection.id,
                               sc.dayOfWeek,
                               sc.startPeriod,
                               sc.endPeriod,
                               r.id,
                               r.roomCode,
                               r.roomName
                        FROM Schedule sc
                        JOIN sc.room r
                        WHERE sc.courseSection.id IN :courseSectionIds
                        ORDER BY sc.courseSection.id, sc.dayOfWeek, sc.startPeriod, sc.id
                        """, Object[].class)
                .setParameter("courseSectionIds", sectionsById.keySet())
                .getResultList();

        Map<Long, Room> firstRooms = new HashMap<>();
        Map<Long, StringBuilder> textBuilders = new HashMap<>();
        for (Object[] row : rows) {
            Long courseSectionId = toLong(row[0]);
            CourseSection courseSection = sectionsById.get(courseSectionId);
            if (courseSection == null) {
                continue;
            }

            Room room = new Room(
                    toLong(row[4]),
                    row[5] == null ? null : String.valueOf(row[5]),
                    row[6] == null ? null : String.valueOf(row[6])
            );
            firstRooms.putIfAbsent(courseSectionId, room);

            StringBuilder builder = textBuilders.computeIfAbsent(courseSectionId, ignored -> new StringBuilder());
            if (builder.length() > 0) {
                builder.append("; ");
            }
            builder.append(row[1])
                    .append(" tiết ")
                    .append(row[2])
                    .append("-")
                    .append(row[3])
                    .append(" phòng ")
                    .append(room.getRoomName());
        }

        for (Map.Entry<Long, StringBuilder> entry : textBuilders.entrySet()) {
            CourseSection courseSection = sectionsById.get(entry.getKey());
            if (courseSection != null) {
                courseSection.applyScheduleCompatibility(firstRooms.get(entry.getKey()), entry.getValue().toString());
            }
        }
    }

    /**
     * Xử lý to long.
     */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
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
