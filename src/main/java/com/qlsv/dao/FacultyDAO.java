package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.Faculty;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * JPA repository for Faculty.
 */
public class FacultyDAO {

    /**
     * Lấy danh sách tất cả các khoa.
     */
    public List<Faculty> findAll() {
        return executeRead("Không thể tải danh sách khoa.", entityManager ->
                entityManager.createQuery("""
                                SELECT f
                                FROM Faculty f
                                ORDER BY f.id
                                """, Faculty.class)
                        .getResultList());
    }

    /**
     * Tìm khoa theo mã định danh.
     */
    public Optional<Faculty> findById(Long id) {
        return executeRead("Không thể tìm khoa theo mã định danh.", entityManager ->
                entityManager.createQuery("""
                                SELECT f
                                FROM Faculty f
                                WHERE f.id = :id
                                """, Faculty.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Tìm khoa theo mã khoa (ví dụ: CNTT).
     */
    public Optional<Faculty> findByCode(String facultyCode) {
        String normalizedCode = facultyCode == null ? "" : facultyCode.trim();
        return executeRead("Không thể tìm khoa theo mã khoa.", entityManager ->
                entityManager.createQuery("""
                                SELECT f
                                FROM Faculty f
                                WHERE LOWER(f.facultyCode) = LOWER(:facultyCode)
                                """, Faculty.class)
                        .setParameter("facultyCode", normalizedCode)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Tìm kiếm khoa theo từ khóa (mã khoa hoặc tên khoa).
     */
    public List<Faculty> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm khoa.", entityManager ->
                entityManager.createQuery("""
                                SELECT f
                                FROM Faculty f
                                WHERE LOWER(f.facultyCode) LIKE :keyword
                                   OR LOWER(f.facultyName) LIKE :keyword
                                ORDER BY f.id
                                """, Faculty.class)
                        .setParameter("keyword", normalizedKeyword)
                        .getResultList());
    }

    /**
     * Thêm mới một khoa vào hệ thống.
     */
    public Faculty insert(Faculty faculty) {
        Long facultyId = executeWrite(
                "Không thể thêm khoa.",
                "Không thể thêm khoa do mã khoa đã tồn tại hoặc dữ liệu không hợp lệ.",
                entityManager -> {
                    Faculty entity = new Faculty();
                    copyState(faculty, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    faculty.setId(entity.getId());
                    return entity.getId();
                }
        );
        return findById(facultyId)
                .orElseThrow(() -> new AppException("Không thể tải lại khoa sau khi thêm."));
    }

    /**
     * Cập nhật thông tin khoa hiện có.
     */
    public boolean update(Faculty faculty) {
        return executeWrite(
                "Không thể cập nhật khoa.",
                "Không thể cập nhật khoa do mã khoa đã tồn tại hoặc dữ liệu không hợp lệ.",
                entityManager -> {
                    Faculty entity = entityManager.find(Faculty.class, faculty.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(faculty, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    /**
     * Xóa khoa khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa khoa.",
                "Không thể xóa khoa vì vẫn còn lớp, giảng viên, sinh viên hoặc môn học tham chiếu.",
                entityManager -> {
                    Faculty entity = entityManager.find(Faculty.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private void copyState(Faculty source, Faculty target) {
        target.setFacultyCode(source.getFacultyCode());
        target.setFacultyName(source.getFacultyName());
        target.setDescription(source.getDescription());
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
