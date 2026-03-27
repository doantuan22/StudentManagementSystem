package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Room;
import com.qlsv.model.Student;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Enrollment is loaded via JPA; course section room/schedule text are hydrated as compatibility fields for legacy screens.
 */
public class EnrollmentDAO {

    private static final String FETCH_BASE = """
            SELECT DISTINCT e
            FROM Enrollment e
            JOIN FETCH e.student student
            JOIN FETCH student.faculty studentFaculty
            JOIN FETCH student.classRoom classRoom
            JOIN FETCH classRoom.faculty classFaculty
            JOIN FETCH e.courseSection courseSection
            JOIN FETCH courseSection.subject subject
            JOIN FETCH subject.faculty subjectFaculty
            JOIN FETCH courseSection.lecturer lecturer
            """;

    public List<Enrollment> findAll() {
        return executeRead("Không thể tải danh sách đăng ký học phần.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " ORDER BY e.id",
                            Enrollment.class
                    )
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public Optional<Enrollment> findById(Long id) {
        return executeRead("Không thể tìm đăng ký học phần theo mã định danh.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " WHERE e.id = :id",
                            Enrollment.class
                    )
                    .setParameter("id", id)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments.stream().findFirst();
        });
    }

    public List<Enrollment> findByStudentId(Long studentId) {
        return executeRead("Không thể tải học phần đã đăng ký của sinh viên.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " WHERE student.id = :studentId ORDER BY e.id",
                            Enrollment.class
                    )
                    .setParameter("studentId", studentId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public List<Enrollment> findByLecturerId(Long lecturerId) {
        return executeRead("Không thể tải danh sách đăng ký của giảng viên.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " WHERE lecturer.id = :lecturerId ORDER BY e.id",
                            Enrollment.class
                    )
                    .setParameter("lecturerId", lecturerId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public List<Enrollment> findByClassRoomId(Long classRoomId) {
        return executeRead("Không thể tải danh sách đăng ký theo lớp học.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " WHERE classRoom.id = :classRoomId ORDER BY e.id",
                            Enrollment.class
                    )
                    .setParameter("classRoomId", classRoomId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public List<Enrollment> findByFacultyId(Long facultyId) {
        return executeRead("Không thể tải danh sách đăng ký theo khoa.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " WHERE studentFaculty.id = :facultyId ORDER BY e.id",
                            Enrollment.class
                    )
                    .setParameter("facultyId", facultyId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public List<Enrollment> findByCourseSectionId(Long courseSectionId) {
        return executeRead("Không thể tải danh sách sinh viên của học phần.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + " WHERE courseSection.id = :courseSectionId ORDER BY e.id",
                            Enrollment.class
                    )
                    .setParameter("courseSectionId", courseSectionId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public Optional<Enrollment> findByStudentAndCourseSection(Long studentId, Long courseSectionId) {
        return executeRead("Không thể tìm đăng ký theo sinh viên và học phần.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + """
                                     WHERE student.id = :studentId
                                       AND courseSection.id = :courseSectionId
                                    """,
                            Enrollment.class
                    )
                    .setParameter("studentId", studentId)
                    .setParameter("courseSectionId", courseSectionId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments.stream().findFirst();
        });
    }

    public List<Enrollment> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm đăng ký học phần.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery("""
                            SELECT DISTINCT e
                            FROM Enrollment e
                            JOIN FETCH e.student student
                            JOIN FETCH student.faculty studentFaculty
                            JOIN FETCH student.classRoom classRoom
                            JOIN FETCH classRoom.faculty classFaculty
                            JOIN FETCH e.courseSection courseSection
                            JOIN FETCH courseSection.subject subject
                            JOIN FETCH subject.faculty subjectFaculty
                            JOIN FETCH courseSection.lecturer lecturer
                            WHERE LOWER(student.studentCode) LIKE :keyword
                               OR LOWER(student.fullName) LIKE :keyword
                               OR LOWER(COALESCE(student.email, '')) LIKE :keyword
                               OR LOWER(courseSection.sectionCode) LIKE :keyword
                               OR LOWER(subject.subjectCode) LIKE :keyword
                               OR LOWER(subject.subjectName) LIKE :keyword
                               OR LOWER(lecturer.fullName) LIKE :keyword
                               OR LOWER(COALESCE(e.status, '')) LIKE :keyword
                            ORDER BY e.id
                            """, Enrollment.class)
                    .setParameter("keyword", normalizedKeyword)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public boolean existsByStudentAndSubject(Long studentId, Long subjectId) {
        return executeRead("Không thể kiểm tra đăng ký trùng môn học.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(e)
                            FROM Enrollment e
                            JOIN e.courseSection courseSection
                            WHERE e.student.id = :studentId
                              AND courseSection.subject.id = :subjectId
                            """, Long.class)
                    .setParameter("studentId", studentId)
                    .setParameter("subjectId", subjectId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    public boolean existsByStudentAndCourseSection(Long studentId, Long courseSectionId) {
        return executeRead("Không thể kiểm tra đăng ký trùng học phần.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(e)
                            FROM Enrollment e
                            WHERE e.student.id = :studentId
                              AND e.courseSection.id = :courseSectionId
                            """, Long.class)
                    .setParameter("studentId", studentId)
                    .setParameter("courseSectionId", courseSectionId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    public int countByCourseSectionId(Long courseSectionId) {
        return executeRead("Không thể đếm số lượng đăng ký của học phần.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(e)
                            FROM Enrollment e
                            WHERE e.courseSection.id = :courseSectionId
                            """, Long.class)
                    .setParameter("courseSectionId", courseSectionId)
                    .getSingleResult();
            return total == null ? 0 : total.intValue();
        });
    }

    public Enrollment insert(Enrollment enrollment) {
        Long enrollmentId = executeWrite(
                "Không thể thêm đăng ký học phần.",
                "Không thể thêm đăng ký học phần do sinh viên, học phần không hợp lệ hoặc dữ liệu bị trùng.",
                entityManager -> {
                    Enrollment entity = new Enrollment();
                    copyState(entityManager, enrollment, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    return entity.getId();
                }
        );
        return findById(enrollmentId)
                .orElseThrow(() -> new AppException("Không thể tải lại đăng ký học phần sau khi thêm."));
    }

    public boolean update(Enrollment enrollment) {
        return executeWrite(
                "Không thể cập nhật đăng ký học phần.",
                "Không thể cập nhật đăng ký học phần do sinh viên, học phần không hợp lệ hoặc dữ liệu bị trùng.",
                entityManager -> {
                    Enrollment entity = entityManager.find(Enrollment.class, enrollment.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(entityManager, enrollment, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa đăng ký học phần.",
                "Không thể xóa đăng ký học phần vì đã có dữ liệu điểm liên quan.",
                entityManager -> {
                    Enrollment entity = entityManager.find(Enrollment.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private void copyState(EntityManager entityManager, Enrollment source, Enrollment target) {
        target.setStudent(resolveStudentReference(entityManager, source.getStudent()));
        target.setCourseSection(resolveCourseSectionReference(entityManager, source.getCourseSection()));
        target.setStatus(source.getStatus());
        target.setEnrolledAt(source.getEnrolledAt());
    }

    private Student resolveStudentReference(EntityManager entityManager, Student student) {
        if (student == null || student.getId() == null) {
            return null;
        }
        return entityManager.getReference(Student.class, student.getId());
    }

    private CourseSection resolveCourseSectionReference(EntityManager entityManager, CourseSection courseSection) {
        if (courseSection == null || courseSection.getId() == null) {
            return null;
        }
        return entityManager.getReference(CourseSection.class, courseSection.getId());
    }

    private void hydrateCourseSectionCompatibility(EntityManager entityManager, List<Enrollment> enrollments) {
        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        Map<Long, List<CourseSection>> sectionsById = new HashMap<>();
        for (Enrollment enrollment : enrollments) {
            CourseSection courseSection = enrollment.getCourseSection();
            if (courseSection == null || courseSection.getId() == null) {
                continue;
            }
            courseSection.applyScheduleCompatibility(null, null);
            sectionsById.computeIfAbsent(courseSection.getId(), ignored -> new ArrayList<>()).add(courseSection);
        }

        if (sectionsById.isEmpty()) {
            return;
        }

        List<Object[]> rows = entityManager.createQuery("""
                        SELECT schedule.courseSection.id,
                               schedule.dayOfWeek,
                               schedule.startPeriod,
                               schedule.endPeriod,
                               room.id,
                               room.roomCode,
                               room.roomName
                        FROM Schedule schedule
                        JOIN schedule.room room
                        WHERE schedule.courseSection.id IN :courseSectionIds
                        ORDER BY schedule.courseSection.id, schedule.dayOfWeek, schedule.startPeriod, schedule.id
                        """, Object[].class)
                .setParameter("courseSectionIds", sectionsById.keySet())
                .getResultList();

        Map<Long, Room> firstRooms = new HashMap<>();
        Map<Long, StringBuilder> textBuilders = new HashMap<>();
        for (Object[] row : rows) {
            Long courseSectionId = toLong(row[0]);
            List<CourseSection> courseSections = sectionsById.get(courseSectionId);
            if (courseSections == null || courseSections.isEmpty()) {
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
            List<CourseSection> courseSections = sectionsById.get(entry.getKey());
            if (courseSections == null) {
                continue;
            }
            for (CourseSection courseSection : courseSections) {
                courseSection.applyScheduleCompatibility(firstRooms.get(entry.getKey()), entry.getValue().toString());
            }
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
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
