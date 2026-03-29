package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.Faculty;
import com.qlsv.model.Lecturer;
import com.qlsv.model.User;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * JPA repository for Lecturer.
 */
public class LecturerDAO {

    private static final String FETCH_BASE = """
            SELECT l
            FROM Lecturer l
            LEFT JOIN FETCH l.user
            JOIN FETCH l.faculty
            """;

    /**
     * Lấy danh sách tất cả giảng viên.
     */
    public List<Lecturer> findAll() {
        return executeRead("Không thể tải danh sách giảng viên.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY l.id", Lecturer.class)
                        .getResultList());
    }

    /**
     * Tìm giảng viên theo mã định danh.
     */
    public Optional<Lecturer> findById(Long id) {
        return executeRead("Không thể tìm giảng viên theo mã định danh.", entityManager ->
                findById(entityManager, id));
    }

    /**
     * Tìm giảng viên dựa trên mã tài khoản người dùng liên kết.
     */
    public Optional<Lecturer> findByUserId(Long userId) {
        return executeRead("Không thể tìm giảng viên theo tài khoản người dùng.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE l.user.id = :userId", Lecturer.class)
                        .setParameter("userId", userId)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Lấy danh sách giảng viên thuộc một khoa cụ thể.
     */
    public List<Lecturer> findByFacultyId(Long facultyId) {
        return executeRead("Không thể lọc giảng viên theo khoa.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE l.faculty.id = :facultyId ORDER BY l.id", Lecturer.class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    /**
     * Tìm kiếm giảng viên theo từ khóa (mã giảng viên, họ tên hoặc email).
     */
    public List<Lecturer> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm giảng viên.", entityManager ->
                entityManager.createQuery(FETCH_BASE + """
                                WHERE LOWER(l.lecturerCode) LIKE :keyword
                                   OR LOWER(l.fullName) LIKE :keyword
                                   OR LOWER(COALESCE(l.email, '')) LIKE :keyword
                                ORDER BY l.id
                                """, Lecturer.class)
                        .setParameter("keyword", normalizedKeyword)
                        .getResultList());
    }

    /**
     * Thêm mới một giảng viên vào hệ thống.
     */
    public Lecturer insert(Lecturer lecturer) {
        Long lecturerId = executeWrite(
                "Không thể thêm giảng viên.",
                "Không thể thêm giảng viên do mã giảng viên đã tồn tại hoặc khoa không hợp lệ.",
                entityManager -> insert(entityManager, lecturer).getId()
        );
        return findById(lecturerId)
                .orElseThrow(() -> new AppException("Không thể tải lại giảng viên sau khi thêm."));
    }

    /**
     * Cập nhật thông tin giảng viên hiện có.
     */
    public boolean update(Lecturer lecturer) {
        return executeWrite(
                "Không thể cập nhật giảng viên.",
                "Không thể cập nhật giảng viên do mã giảng viên đã tồn tại hoặc khoa không hợp lệ.",
                entityManager -> update(entityManager, lecturer)
        );
    }

    /**
     * Xóa giảng viên khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa giảng viên.",
                "Không thể xóa giảng viên vì vẫn còn học phần hoặc phân công giảng dạy tham chiếu.",
                entityManager -> {
                    Lecturer entity = entityManager.find(Lecturer.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private Optional<Lecturer> findById(EntityManager entityManager, Long id) {
        return entityManager.createQuery(FETCH_BASE + " WHERE l.id = :id", Lecturer.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    private Lecturer insert(EntityManager entityManager, Lecturer lecturer) {
        Lecturer entity = new Lecturer();
        copyState(entityManager, lecturer, entity);
        entityManager.persist(entity);
        entityManager.flush();
        lecturer.setId(entity.getId());
        return lecturer;
    }

    private boolean update(EntityManager entityManager, Lecturer lecturer) {
        Lecturer entity = entityManager.find(Lecturer.class, lecturer.getId());
        if (entity == null) {
            return false;
        }
        copyState(entityManager, lecturer, entity);
        entityManager.flush();
        return true;
    }

    private void copyState(EntityManager entityManager, Lecturer source, Lecturer target) {
        target.setUser(resolveUserReference(entityManager, source.getUserId()));
        target.setLecturerCode(source.getLecturerCode());
        target.setFullName(source.getFullName());
        target.setGender(source.getGender());
        target.setEmail(source.getEmail());
        target.setDateOfBirth(source.getDateOfBirth());
        target.setPhone(source.getPhone());
        target.setAddress(source.getAddress());
        target.setFaculty(resolveFacultyReference(entityManager, source.getFaculty()));
        target.setStatus(source.getStatus());
    }

    private Faculty resolveFacultyReference(EntityManager entityManager, Faculty faculty) {
        if (faculty == null || faculty.getId() == null) {
            return null;
        }
        return entityManager.getReference(Faculty.class, faculty.getId());
    }

    private User resolveUserReference(EntityManager entityManager, Long userId) {
        if (userId == null) {
            return null;
        }
        return entityManager.getReference(User.class, userId);
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
