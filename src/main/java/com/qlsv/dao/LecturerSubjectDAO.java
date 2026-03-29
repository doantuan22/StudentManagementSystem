package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.CourseSection;
import com.qlsv.model.Lecturer;
import com.qlsv.model.LecturerSubject;
import com.qlsv.model.LecturerSubjectId;
import com.qlsv.model.Subject;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class LecturerSubjectDAO {

    /**
     * Lấy danh sách các môn học mà một giảng viên cụ thể được phép giảng dạy.
     */
    public List<LecturerSubject> findByLecturerId(Long lecturerId) {
        return executeRead("KhÃ´ng thá»ƒ táº£i whitelist mÃ´n giáº£ng dáº¡y cá»§a giáº£ng viÃªn.", entityManager ->
                entityManager.createQuery("""
                                SELECT ls
                                FROM LecturerSubject ls
                                JOIN FETCH ls.subject subject
                                JOIN FETCH subject.faculty
                                WHERE ls.lecturer.id = :lecturerId
                                ORDER BY subject.subjectCode, subject.subjectName
                                """, LecturerSubject.class)
                        .setParameter("lecturerId", lecturerId)
                        .getResultList());
    }

    /**
     * Lấy danh sách các giảng viên có đủ điều kiện giảng dạy một môn học cụ thể.
     */
    public List<LecturerSubject> findBySubjectId(Long subjectId) {
        return executeRead("KhÃ´ng thá»ƒ táº£i danh sÃ¡ch giáº£ng viÃªn theo mÃ´n há»c whitelist.", entityManager ->
                entityManager.createQuery("""
                                SELECT ls
                                FROM LecturerSubject ls
                                JOIN FETCH ls.lecturer lecturer
                                JOIN FETCH lecturer.faculty
                                LEFT JOIN FETCH lecturer.user
                                WHERE ls.subject.id = :subjectId
                                ORDER BY lecturer.lecturerCode, lecturer.fullName
                                """, LecturerSubject.class)
                        .setParameter("subjectId", subjectId)
                        .getResultList());
    }

    /**
     * Kiểm tra xem giảng viên có nằm trong danh sách được phép dạy môn học này hay không.
     */
    public boolean exists(Long lecturerId, Long subjectId) {
        return executeRead("KhÃ´ng thá»ƒ kiá»ƒm tra whitelist giáº£ng viÃªn - mÃ´n há»c.", entityManager -> {
            Long total = entityManager.createQuery("""
                            SELECT COUNT(ls)
                            FROM LecturerSubject ls
                            WHERE ls.lecturer.id = :lecturerId
                              AND ls.subject.id = :subjectId
                            """, Long.class)
                    .setParameter("lecturerId", lecturerId)
                    .setParameter("subjectId", subjectId)
                    .getSingleResult();
            return total != null && total > 0;
        });
    }

    /**
     * Cập nhật toàn bộ danh sách môn học được phép dạy cho một giảng viên.
     */
    public void saveAll(Long lecturerId, Collection<Long> subjectIds) {
        executeWrite("KhÃ´ng thá»ƒ cáº­p nháº­t whitelist mÃ´n giáº£ng dáº¡y cá»§a giáº£ng viÃªn.", entityManager -> {
            entityManager.createQuery("""
                            DELETE FROM LecturerSubject ls
                            WHERE ls.lecturer.id = :lecturerId
                            """)
                    .setParameter("lecturerId", lecturerId)
                    .executeUpdate();

            Set<Long> normalizedSubjectIds = new LinkedHashSet<>();
            if (subjectIds != null) {
                for (Long subjectId : subjectIds) {
                    if (subjectId != null) {
                        normalizedSubjectIds.add(subjectId);
                    }
                }
            }

            Lecturer lecturerReference = entityManager.getReference(Lecturer.class, lecturerId);
            int index = 0;
            for (Long subjectId : normalizedSubjectIds) {
                Subject subjectReference = entityManager.getReference(Subject.class, subjectId);
                entityManager.persist(new LecturerSubject(lecturerReference, subjectReference));
                index++;
                if (index % 25 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    lecturerReference = entityManager.getReference(Lecturer.class, lecturerId);
                }
            }
            entityManager.flush();
            return null;
        });
    }

    /**
     * Tự động khởi tạo danh sách môn dạy cho giảng viên dựa trên dữ liệu học phần hiện có.
     */
    public int backfillFromCourseSectionsIfEmpty() {
        return executeWrite("KhÃ´ng thá»ƒ backfill whitelist giáº£ng viÃªn - mÃ´n há»c tá»« dá»¯ liá»‡u há»c pháº§n.", entityManager -> {
            Long existingCount = entityManager.createQuery("SELECT COUNT(ls) FROM LecturerSubject ls", Long.class)
                    .getSingleResult();
            if (existingCount != null && existingCount > 0) {
                return 0;
            }

            List<Object[]> rows = entityManager.createQuery("""
                            SELECT DISTINCT cs.lecturer.id, cs.subject.id
                            FROM CourseSection cs
                            WHERE cs.lecturer.id IS NOT NULL
                              AND cs.subject.id IS NOT NULL
                            ORDER BY cs.lecturer.id, cs.subject.id
                            """, Object[].class)
                    .getResultList();

            Map<Long, Set<Long>> subjectsByLecturerId = new LinkedHashMap<>();
            for (Object[] row : rows) {
                Long lecturerId = toLong(row[0]);
                Long subjectId = toLong(row[1]);
                if (lecturerId == null || subjectId == null) {
                    continue;
                }
                subjectsByLecturerId
                        .computeIfAbsent(lecturerId, ignored -> new LinkedHashSet<>())
                        .add(subjectId);
            }

            int inserted = 0;
            for (Map.Entry<Long, Set<Long>> entry : subjectsByLecturerId.entrySet()) {
                Lecturer lecturerReference = entityManager.getReference(Lecturer.class, entry.getKey());
                for (Long subjectId : entry.getValue()) {
                    LecturerSubjectId id = new LecturerSubjectId(entry.getKey(), subjectId);
                    if (entityManager.find(LecturerSubject.class, id) != null) {
                        continue;
                    }
                    Subject subjectReference = entityManager.getReference(Subject.class, subjectId);
                    entityManager.persist(new LecturerSubject(lecturerReference, subjectReference));
                    inserted++;
                }
            }
            entityManager.flush();
            return inserted;
        });
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

    private <T> T executeWrite(String errorMessage, Function<EntityManager, T> action) {
        try {
            return JpaBootstrap.executeInCurrentTransaction(action);
        } catch (AppException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }
}
