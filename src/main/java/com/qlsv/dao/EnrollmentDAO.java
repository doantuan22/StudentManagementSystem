/**
 * Truy vấn dữ liệu đăng ký bằng JPA.
 */
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Enrollment is loaded via JPA; course section room/schedule text are hydrated as compatibility fields for legacy screens.
 */
public class EnrollmentDAO {

    private static final String EFFECTIVE_ENROLLMENT_STATUS = "REGISTERED";

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

    /**
     * Lấy danh sách tất cả các đăng ký học phần trong hệ thống.
     */
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

    /**
     * Tìm kiếm đăng ký học phần theo mã định danh.
     */
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

    /**
     * Lấy danh sách học phần đã đăng ký của một sinh viên.
     */
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

    /**
     * Lấy danh sách học phần đã đăng ký nhưng chưa có điểm của một sinh viên.
     */
    public List<Enrollment> findByStudentIdWithoutScore(Long studentId) {
        return executeRead("Không thể tải học phần chưa có điểm.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + """
                                      WHERE student.id = :studentId
                                        AND NOT EXISTS (
                                            SELECT 1 FROM Score s WHERE s.enrollment.id = e.id
                                        )
                                      ORDER BY e.id
                                     """,
                            Enrollment.class
                    )
                    .setParameter("studentId", studentId)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    /**
     * Lấy danh sách đăng ký học phần của các lớp do giảng viên phụ trách.
     */
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

    /**
     * Lấy danh sách đăng ký học phần còn hiệu lực của các lớp do giảng viên phụ trách.
     */
    public List<Enrollment> findEffectiveByLecturerId(Long lecturerId) {
        return executeRead("Không thể tải danh sách đăng ký còn hiệu lực của giảng viên.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + """
                                     WHERE lecturer.id = :lecturerId
                                       AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                                     ORDER BY e.id
                                    """,
                            Enrollment.class
                    )
                    .setParameter("lecturerId", lecturerId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    /**
     * Lấy danh sách đăng ký học phần của sinh viên thuộc một lớp hành chính.
     */
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

    /**
     * Lấy danh sách đăng ký học phần theo khoa của sinh viên.
     */
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

    /**
     * Lấy danh sách sinh viên đã đăng ký vào một học phần cụ thể.
     */
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

    public List<Enrollment> findEffectiveByCourseSectionId(Long courseSectionId) {
        return executeRead("Khong the tai danh sach dang ky con hieu luc cua hoc phan.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + """
                                     WHERE courseSection.id = :courseSectionId
                                       AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                                     ORDER BY e.enrolledAt, e.id
                                    """,
                            Enrollment.class
                    )
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    /**
     * Tìm kiếm thông tin đăng ký dựa trên mã sinh viên và mã học phần.
     */
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

    /**
     * Tìm kiếm đăng ký học phần theo từ khóa (sinh viên, học phần, môn học, giảng viên).
     */
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
                            /**
                             * Xử lý lower.
                             */
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

    /**
     * Kiểm tra sinh viên đã đăng ký môn học này ở bất kỳ học phần nào chưa.
     */
    public boolean existsByStudentAndSubject(Long studentId, String subjectName,
                                             String semester, String schoolYear,
                                             Long excludeEnrollmentId) {
        String normalizedSubjectName = normalizeSubjectName(subjectName);
        if (normalizedSubjectName == null) {
            return false;
        }
        return executeRead("Không thể kiểm tra đăng ký trùng môn học.", entityManager -> {
            Long total = entityManager.createQuery("""
                            /**
                             * Xử lý count.
                             */
                            SELECT COUNT(e)
                            FROM Enrollment e
                            JOIN e.courseSection courseSection
                            JOIN courseSection.subject subject
                            WHERE e.student.id = :studentId
                              AND LOWER(TRIM(subject.subjectName)) = :subjectName
                              /**
                               * Chuẩn hóa học kỳ và năm học để so sánh chính xác hơn.
                               */
                              AND UPPER(REPLACE(courseSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
                              AND REPLACE(courseSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
                              AND (:excludeEnrollmentId IS NULL OR e.id <> :excludeEnrollmentId)
                              AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                            """, Long.class)
                    .setParameter("studentId", studentId)
                    .setParameter("subjectName", normalizedSubjectName)
                    .setParameter("semester", semester)
                    .setParameter("schoolYear", schoolYear)
                    .setParameter("excludeEnrollmentId", excludeEnrollmentId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    /**
     * Kiểm tra sinh viên đã đăng ký vào học phần cụ thể này chưa.
     */
    public boolean existsByStudentAndCourseSection(Long studentId, Long courseSectionId, Long excludeEnrollmentId) {
        return executeRead("Không thể kiểm tra đăng ký trùng học phần.", entityManager -> {
            Long total = entityManager.createQuery("""
                            /**
                             * Xử lý count.
                             */
                            SELECT COUNT(e)
                            FROM Enrollment e
                            WHERE e.student.id = :studentId
                              AND e.courseSection.id = :courseSectionId
                              AND (:excludeEnrollmentId IS NULL OR e.id <> :excludeEnrollmentId)
                              AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                            """, Long.class)
                    .setParameter("studentId", studentId)
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("excludeEnrollmentId", excludeEnrollmentId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    /**
     * Đếm tổng số lượng sinh viên đã đăng ký vào một học phần.
     */
    public int countByCourseSectionId(Long courseSectionId) {
        return countByCourseSectionId(courseSectionId, null);
    }

    public int countByCourseSectionId(Long courseSectionId, Long excludeEnrollmentId) {
        return executeRead("Không thể đếm số lượng đăng ký của học phần.", entityManager -> {
            Long total = entityManager.createQuery("""
                            /**
                             * Xử lý count.
                             */
                            SELECT COUNT(e)
                            FROM Enrollment e
                            WHERE e.courseSection.id = :courseSectionId
                              AND (:excludeEnrollmentId IS NULL OR e.id <> :excludeEnrollmentId)
                              AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                            """, Long.class)
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("excludeEnrollmentId", excludeEnrollmentId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getSingleResult();
            return total == null ? 0 : total.intValue();
        });
    }

    /**
     * Thêm mới một bản ghi đăng ký học phần.
     */
    public boolean hasEffectiveStudentSubjectConflictForCourseSection(Long courseSectionId,
                                                                      String subjectName,
                                                                      String semester,
                                                                      String schoolYear) {
        String normalizedSubjectName = normalizeSubjectName(subjectName);
        if (courseSectionId == null || normalizedSubjectName == null || semester == null || schoolYear == null) {
            return false;
        }
        return executeRead("Không thể kiểm tra xung đột môn học khi cập nhật học phần.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(currentEnrollment)
                            FROM Enrollment currentEnrollment
                            JOIN currentEnrollment.courseSection currentSection
                            WHERE currentSection.id = :courseSectionId
                              AND UPPER(COALESCE(currentEnrollment.status, 'REGISTERED')) = :effectiveStatus
                              AND EXISTS (
                                    SELECT otherEnrollment.id
                                    FROM Enrollment otherEnrollment
                                    JOIN otherEnrollment.courseSection otherSection
                                    JOIN otherSection.subject otherSubject
                                    WHERE otherEnrollment.student.id = currentEnrollment.student.id
                                      AND otherSection.id <> currentSection.id
                                      AND LOWER(TRIM(otherSubject.subjectName)) = :subjectName
                                      /**
                                       * Chuẩn hóa học kỳ và năm học để so sánh chính xác hơn.
                                       */
                                      AND UPPER(REPLACE(otherSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
                                      AND REPLACE(otherSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
                                      AND UPPER(COALESCE(otherEnrollment.status, 'REGISTERED')) = :effectiveStatus
                              )
                            """, Long.class)
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("subjectName", normalizedSubjectName)
                    .setParameter("semester", semester)
                    .setParameter("schoolYear", schoolYear)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    public boolean hasEffectiveStudentScheduleConflictForCourseSection(Long courseSectionId,
                                                                        String semester,
                                                                        String schoolYear) {
        if (courseSectionId == null || semester == null || schoolYear == null) {
            return false;
        }
        return executeRead("Không thể kiểm tra xung đột lịch học khi cập nhật học phần.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(currentEnrollment)
                            FROM Enrollment currentEnrollment
                            JOIN currentEnrollment.courseSection currentSection
                            WHERE currentSection.id = :courseSectionId
                              AND UPPER(COALESCE(currentEnrollment.status, 'REGISTERED')) = :effectiveStatus
                              AND EXISTS (
                                    SELECT otherEnrollment.id
                                    FROM Enrollment otherEnrollment
                                    JOIN otherEnrollment.courseSection otherSection
                                    WHERE otherEnrollment.student.id = currentEnrollment.student.id
                                      AND otherSection.id <> currentSection.id
                                      /**
                                       * Chuẩn hóa học kỳ và năm học để so sánh chính xác hơn.
                                       */
                                      AND UPPER(REPLACE(otherSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
                                      AND REPLACE(otherSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
                                      AND UPPER(COALESCE(otherEnrollment.status, 'REGISTERED')) = :effectiveStatus
                                      AND EXISTS (
                                            SELECT s1.id
                                            FROM Schedule s1
                                            WHERE s1.courseSection.id = otherSection.id
                                              AND EXISTS (
                                                    SELECT s2.id
                                                    FROM Schedule s2
                                                    WHERE s2.courseSection.id = currentSection.id
                                                      AND s1.dayOfWeek = s2.dayOfWeek
                                                      AND NOT (s1.endPeriod < s2.startPeriod OR s1.startPeriod > s2.endPeriod)
                                              )
                                      )
                              )
                            """, Long.class)
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("semester", semester)
                    .setParameter("schoolYear", schoolYear)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    public List<Enrollment> findEffectiveConflictsByStudentAndSubject(Long studentId,
                                                                      String subjectName,
                                                                      String semester,
                                                                      String schoolYear,
                                                                      Long excludeCourseSectionId) {
        String normalizedSubjectName = normalizeSubjectName(subjectName);
        if (normalizedSubjectName == null) {
            return List.of();
        }
        return executeRead("Không the tai danh sach dang ky trung mon hoc trong cung hoc ky va nam hoc.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + """
                                      WHERE student.id = :studentId
                                        AND LOWER(TRIM(subject.subjectName)) = :subjectName
                                        /**
                                         * Chuẩn hóa học kỳ và năm học để so sánh chính xác hơn.
                                         */
                                        AND UPPER(REPLACE(courseSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
                                        AND REPLACE(courseSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
                                        AND (:excludeCourseSectionId IS NULL OR courseSection.id <> :excludeCourseSectionId)
                                        AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                                      ORDER BY e.enrolledAt, e.id
                                     """,
                            Enrollment.class
                    )
                    .setParameter("studentId", studentId)
                    .setParameter("subjectName", normalizedSubjectName)
                    .setParameter("semester", semester)
                    .setParameter("schoolYear", schoolYear)
                    .setParameter("excludeCourseSectionId", excludeCourseSectionId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
        });
    }

    public List<Enrollment> findEffectiveScheduleConflictsByStudent(Long studentId,
                                                                     Long courseSectionId,
                                                                     String semester,
                                                                     String schoolYear,
                                                                     Long excludeCourseSectionId) {
        return executeRead("Khong the tai danh sach dang ky trung lich hoc.", entityManager -> {
            List<Enrollment> enrollments = entityManager.createQuery(
                            FETCH_BASE + """
                                      WHERE student.id = :studentId
                                        /**
                                         * Chuẩn hóa học kỳ và năm học để so sánh chính xác hơn.
                                         */
                                        AND UPPER(REPLACE(courseSection.semester, ' ', '')) = UPPER(REPLACE(:semester, ' ', ''))
                                        AND REPLACE(courseSection.schoolYear, ' ', '') = REPLACE(:schoolYear, ' ', '')
                                        AND (:excludeCourseSectionId IS NULL OR courseSection.id <> :excludeCourseSectionId)
                                        AND UPPER(COALESCE(e.status, 'REGISTERED')) = :effectiveStatus
                                        AND EXISTS (
                                              SELECT s1.id
                                              FROM Schedule s1
                                              WHERE s1.courseSection.id = courseSection.id
                                                AND EXISTS (
                                                      SELECT s2.id
                                                      FROM Schedule s2
                                                      WHERE s2.courseSection.id = :courseSectionId
                                                        AND s1.dayOfWeek = s2.dayOfWeek
                                                        AND NOT (s1.endPeriod < s2.startPeriod OR s1.startPeriod > s2.endPeriod)
                                                )
                                        )
                                      ORDER BY e.enrolledAt, e.id
                                     """,
                            Enrollment.class
                    )
                    .setParameter("studentId", studentId)
                    .setParameter("courseSectionId", courseSectionId)
                    .setParameter("semester", semester)
                    .setParameter("schoolYear", schoolYear)
                    .setParameter("excludeCourseSectionId", excludeCourseSectionId)
                    .setParameter("effectiveStatus", EFFECTIVE_ENROLLMENT_STATUS)
                    .getResultList();
            hydrateCourseSectionCompatibility(entityManager, enrollments);
            return enrollments;
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

    /**
     * Cập nhật trạng thái hoặc thông tin đăng ký học phần.
     */
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

    /**
     * Xóa bản ghi đăng ký học phần theo mã định danh.
     */
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

    /**
     * Sao chép state.
     */
    private void copyState(EntityManager entityManager, Enrollment source, Enrollment target) {
        target.setStudent(resolveStudentReference(entityManager, source.getStudent()));
        target.setCourseSection(resolveCourseSectionReference(entityManager, source.getCourseSection()));
        target.setStatus(source.getStatus());
        target.setEnrolledAt(source.getEnrolledAt());
    }

    /**
     * Xác định sinh viên reference.
     */
    private Student resolveStudentReference(EntityManager entityManager, Student student) {
        if (student == null || student.getId() == null) {
            return null;
        }
        return entityManager.getReference(Student.class, student.getId());
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
     * Đồng bộ thông tin phòng học và lịch học cho học phần để hiển thị trên giao diện cũ.
     */
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

    private String normalizeSubjectName(String subjectName) {
        if (subjectName == null) {
            return null;
        }
        String normalizedSubjectName = subjectName.trim().toLowerCase(Locale.ROOT);
        return normalizedSubjectName.isEmpty() ? null : normalizedSubjectName;
    }
}
