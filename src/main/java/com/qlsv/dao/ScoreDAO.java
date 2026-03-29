package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Room;
import com.qlsv.model.Score;
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
 * Score is loaded via JPA; course section room/schedule text are hydrated as compatibility fields for existing DTO/UI flows.
 */
public class ScoreDAO {

    private static final String FETCH_BASE = """
            SELECT DISTINCT score
            FROM Score score
            JOIN FETCH score.enrollment enrollment
            JOIN FETCH enrollment.student student
            JOIN FETCH student.faculty studentFaculty
            JOIN FETCH student.classRoom classRoom
            JOIN FETCH classRoom.faculty classFaculty
            JOIN FETCH enrollment.courseSection courseSection
            JOIN FETCH courseSection.subject subject
            JOIN FETCH subject.faculty subjectFaculty
            JOIN FETCH courseSection.lecturer lecturer
            """;

    /**
     * Lấy danh sách điểm của tất cả sinh viên trong hệ thống.
     */
    public List<Score> findAll() {
        return executeRead("Không thể tải danh sách điểm.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " ORDER BY score.id",
                            Score.class
                    )
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores;
        });
    }

    /**
     * Tìm kiếm thông tin điểm theo mã định danh.
     */
    public Optional<Score> findById(Long id) {
        return executeRead("Không thể tìm điểm theo mã định danh.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " WHERE score.id = :id",
                            Score.class
                    )
                    .setParameter("id", id)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores.stream().findFirst();
        });
    }

    /**
     * Tìm điểm của một lượt đăng ký học phần cụ thể.
     */
    public Optional<Score> findByEnrollmentId(Long enrollmentId) {
        return executeRead("Không thể tìm điểm theo đăng ký học phần.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " WHERE enrollment.id = :enrollmentId",
                            Score.class
                    )
                    .setParameter("enrollmentId", enrollmentId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores.stream().findFirst();
        });
    }

    /**
     * Lấy danh sách bảng điểm của một sinh viên.
     */
    public List<Score> findByStudentId(Long studentId) {
        return executeRead("Không thể tải điểm của sinh viên.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " WHERE student.id = :studentId ORDER BY score.id",
                            Score.class
                    )
                    .setParameter("studentId", studentId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores;
        });
    }

    /**
     * Lấy danh sách điểm của các học phần do một giảng viên phụ trách.
     */
    public List<Score> findByLecturerId(Long lecturerId) {
        return executeRead("Không thể tải điểm của giảng viên phụ trách.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " WHERE lecturer.id = :lecturerId ORDER BY score.id",
                            Score.class
                    )
                    .setParameter("lecturerId", lecturerId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores;
        });
    }

    /**
     * Lấy bảng điểm của tất cả sinh viên trong một học phần.
     */
    public List<Score> findByCourseSectionId(Long courseSectionId) {
        return executeRead("Không thể tải bảng điểm theo học phần.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " WHERE courseSection.id = :courseSectionId ORDER BY score.id",
                            Score.class
                    )
                    .setParameter("courseSectionId", courseSectionId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores;
        });
    }

    /**
     * Lấy danh sách điểm của sinh viên thuộc một lớp hành chính.
     */
    public List<Score> findByClassRoomId(Long classRoomId) {
        return executeRead("Không thể tải bảng điểm theo lớp học.", entityManager -> {
            List<Score> scores = entityManager.createQuery(
                            FETCH_BASE + " WHERE classRoom.id = :classRoomId ORDER BY score.id",
                            Score.class
                    )
                    .setParameter("classRoomId", classRoomId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores;
        });
    }

    /**
     * Tìm kiếm điểm theo từ khóa (sinh viên, học phần, môn học, giảng viên, kết quả).
     */
    public List<Score> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm điểm.", entityManager -> {
            List<Score> scores = entityManager.createQuery("""
                            SELECT DISTINCT score
                            FROM Score score
                            JOIN FETCH score.enrollment enrollment
                            JOIN FETCH enrollment.student student
                            JOIN FETCH student.faculty studentFaculty
                            JOIN FETCH student.classRoom classRoom
                            JOIN FETCH classRoom.faculty classFaculty
                            JOIN FETCH enrollment.courseSection courseSection
                            JOIN FETCH courseSection.subject subject
                            JOIN FETCH subject.faculty subjectFaculty
                            JOIN FETCH courseSection.lecturer lecturer
                            WHERE LOWER(student.studentCode) LIKE :keyword
                               OR LOWER(student.fullName) LIKE :keyword
                               OR LOWER(COALESCE(student.email, '')) LIKE :keyword
                               OR LOWER(courseSection.sectionCode) LIKE :keyword
                               OR LOWER(subject.subjectCode) LIKE :keyword
                               OR LOWER(subject.subjectName) LIKE :keyword
                               OR LOWER(lecturer.lecturerCode) LIKE :keyword
                               OR LOWER(lecturer.fullName) LIKE :keyword
                               OR LOWER(COALESCE(score.result, '')) LIKE :keyword
                            ORDER BY score.id
                            """, Score.class)
                    .setParameter("keyword", normalizedKeyword)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, scores);
            return scores;
        });
    }

    /**
     * Ghi nhận điểm mới cho một lượt đăng ký học phần.
     */
    public Score insert(Score score) {
        Long scoreId = executeWrite(
                "Không thể thêm điểm.",
                "Không thể thêm điểm do đăng ký học phần không hợp lệ hoặc đã tồn tại điểm.",
                entityManager -> {
                    Score entity = new Score();
                    copyState(entityManager, score, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    return entity.getId();
                }
        );
        return findById(scoreId)
                .orElseThrow(() -> new AppException("Không thể tải lại điểm sau khi thêm."));
    }

    /**
     * Cập nhật thông tin điểm số hiện có.
     */
    public boolean update(Score score) {
        return executeWrite(
                "Không thể cập nhật điểm.",
                "Không thể cập nhật điểm do đăng ký học phần không hợp lệ hoặc dữ liệu điểm bị trùng.",
                entityManager -> {
                    Score entity = entityManager.find(Score.class, score.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(entityManager, score, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    /**
     * Xóa bản ghi điểm theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa điểm.",
                "Không thể xóa điểm do có ràng buộc dữ liệu liên quan.",
                entityManager -> {
                    Score entity = entityManager.find(Score.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private void copyState(EntityManager entityManager, Score source, Score target) {
        target.setEnrollment(resolveEnrollmentReference(entityManager, source.getEnrollment()));
        target.setProcessScore(source.getProcessScore());
        target.setMidtermScore(source.getMidtermScore());
        target.setFinalScore(source.getFinalScore());
        target.setTotalScore(source.getTotalScore());
        target.setResult(source.getResult());
    }

    private Enrollment resolveEnrollmentReference(EntityManager entityManager, Enrollment enrollment) {
        if (enrollment == null || enrollment.getId() == null) {
            return null;
        }
        return entityManager.getReference(Enrollment.class, enrollment.getId());
    }

    private void hydrateCourseSectionCompatibility(EntityManager entityManager, List<Score> scores) {
        if (scores == null || scores.isEmpty()) {
            return;
        }

        Map<Long, List<CourseSection>> sectionsById = new HashMap<>();
        for (Score score : scores) {
            if (score.getEnrollment() == null || score.getEnrollment().getCourseSection() == null) {
                continue;
            }
            CourseSection courseSection = score.getEnrollment().getCourseSection();
            if (courseSection.getId() == null) {
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
