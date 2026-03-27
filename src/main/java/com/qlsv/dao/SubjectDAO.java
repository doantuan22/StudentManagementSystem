package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.Faculty;
import com.qlsv.model.Subject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * JOIN FETCH keeps Faculty available after EntityManager closes for the current Swing screens.
 */
public class SubjectDAO {

    private static final String FETCH_BASE = """
            SELECT s
            FROM Subject s
            JOIN FETCH s.faculty
            """;

    public List<Subject> findAll() {
        return executeRead("Không thể tải danh sách môn học.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY s.id", Subject.class)
                        .getResultList());
    }

    public Optional<Subject> findById(Long id) {
        return executeRead("Không thể tìm môn học theo mã định danh.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.id = :id", Subject.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    public List<Subject> findByFacultyId(Long facultyId) {
        return executeRead("Không thể lọc môn học theo khoa.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE s.faculty.id = :facultyId ORDER BY s.id", Subject.class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    public List<Subject> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm môn học.", entityManager ->
                entityManager.createQuery(FETCH_BASE + """
                                WHERE LOWER(s.subjectCode) LIKE :keyword
                                   OR LOWER(s.subjectName) LIKE :keyword
                                ORDER BY s.id
                                """, Subject.class)
                        .setParameter("keyword", normalizedKeyword)
                        .getResultList());
    }

    public Subject insert(Subject subject) {
        Long subjectId = executeWrite(
                "Không thể thêm môn học.",
                "Không thể thêm môn học do mã môn học đã tồn tại hoặc khoa không hợp lệ.",
                entityManager -> {
                    Subject entity = new Subject();
                    copyState(entityManager, subject, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    subject.setId(entity.getId());
                    return entity.getId();
                }
        );
        return findById(subjectId)
                .orElseThrow(() -> new AppException("Không thể tải lại môn học sau khi thêm."));
    }

    public boolean update(Subject subject) {
        return executeWrite(
                "Không thể cập nhật môn học.",
                "Không thể cập nhật môn học do mã môn học đã tồn tại hoặc khoa không hợp lệ.",
                entityManager -> {
                    Subject entity = entityManager.find(Subject.class, subject.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(entityManager, subject, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa môn học.",
                "Không thể xóa môn học vì vẫn còn học phần đang mở hoặc phân công giảng dạy liên quan.",
                entityManager -> {
                    Subject entity = entityManager.find(Subject.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private void copyState(EntityManager entityManager, Subject source, Subject target) {
        target.setSubjectCode(source.getSubjectCode());
        target.setSubjectName(source.getSubjectName());
        target.setCredits(source.getCredits());
        target.setDescription(source.getDescription());
        target.setFaculty(resolveFacultyReference(entityManager, source.getFaculty()));
    }

    private Faculty resolveFacultyReference(EntityManager entityManager, Faculty faculty) {
        if (faculty == null || faculty.getId() == null) {
            return null;
        }
        return entityManager.getReference(Faculty.class, faculty.getId());
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
            current = current.getCause();
        }
        return false;
    }
}
