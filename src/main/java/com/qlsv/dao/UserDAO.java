package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.Role;
import com.qlsv.model.RoleEntity;
import com.qlsv.model.User;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Auth queries always JOIN FETCH role so detached User objects still expose getRole() safely in Swing/UI code.
 */
public class UserDAO {

    private static final String FETCH_BASE = """
            SELECT u
            FROM User u
            JOIN FETCH u.roleEntity
            """;

    /**
     * Lấy danh sách tất cả người dùng trong hệ thống.
     */
    public List<User> findAll() {
        return executeRead("Không thể tải danh sách người dùng.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY u.id", User.class)
                        .getResultList());
    }

    /**
     * Tìm kiếm người dùng theo mã định danh.
     */
    public Optional<User> findById(Long id) {
        return executeRead("Không thể tìm người dùng theo mã định danh.", entityManager ->
                findById(entityManager, id));
    }

    /**
     * Tìm kiếm người dùng theo tên đăng nhập (không phân biệt hoa thường).
     */
    public Optional<User> findByUsername(String username) {
        String normalizedUsername = username == null ? "" : username.trim();
        return executeRead("Không thể tìm người dùng theo tên đăng nhập.", entityManager ->
                findByUsername(entityManager, normalizedUsername));
    }

    /**
     * Tìm kiếm người dùng theo từ khóa (tên đăng nhập, họ tên hoặc email).
     */
    public List<User> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm người dùng.", entityManager ->
                entityManager.createQuery(FETCH_BASE + """
                                WHERE LOWER(u.username) LIKE :keyword
                                   OR LOWER(u.fullName) LIKE :keyword
                                   OR LOWER(COALESCE(u.email, '')) LIKE :keyword
                                ORDER BY u.id
                                """, User.class)
                        .setParameter("keyword", normalizedKeyword)
                        .getResultList());
    }

    /**
     * Thêm mới một tài khoản người dùng vào hệ thống.
     */
    public User insert(User user) {
        Long userId = executeWrite(
                "Không thể thêm người dùng.",
                "Không thể thêm người dùng do tên đăng nhập đã tồn tại hoặc vai trò không hợp lệ.",
                entityManager -> insert(entityManager, user).getId()
        );
        return findById(userId)
                .orElseThrow(() -> new AppException("Không thể tải lại người dùng sau khi thêm."));
    }

    /**
     * Cập nhật thông tin tài khoản người dùng hiện có.
     */
    public boolean update(User user) {
        return executeWrite(
                "Không thể cập nhật người dùng.",
                "Không thể cập nhật người dùng do tên đăng nhập đã tồn tại hoặc vai trò không hợp lệ.",
                entityManager -> update(entityManager, user)
        );
    }

    /**
     * Xóa tài khoản người dùng khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa người dùng.",
                "Không thể xóa người dùng vì vẫn còn giảng viên hoặc sinh viên đang liên kết.",
                entityManager -> {
                    User entity = entityManager.find(User.class, id);
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
     * Cập nhật họ tên hiển thị cho người dùng.
     */
    public boolean updateFullName(Long userId, String fullName) {
        return executeWrite(
                "Không thể cập nhật họ tên người dùng.",
                "Không thể cập nhật họ tên người dùng.",
                entityManager -> updateFullName(entityManager, userId, fullName)
        );
    }

    /**
     * Cập nhật địa chỉ email cho người dùng.
     */
    public boolean updateEmail(Long userId, String email) {
        return executeWrite(
                "Không thể cập nhật email người dùng.",
                "Không thể cập nhật email người dùng.",
                entityManager -> updateEmail(entityManager, userId, email)
        );
    }

    /**
     * Cập nhật mã băm mật khẩu mới cho người dùng.
     */
    public boolean updatePasswordHash(Long userId, String passwordHash) {
        return executeWrite(
                "Không thể cập nhật mật khẩu người dùng.",
                "Không thể cập nhật mật khẩu người dùng.",
                entityManager -> updatePasswordHash(entityManager, userId, passwordHash)
        );
    }

    private Optional<User> findById(EntityManager entityManager, Long id) {
        return entityManager.createQuery(FETCH_BASE + " WHERE u.id = :id", User.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    private Optional<User> findByUsername(EntityManager entityManager, String username) {
        return entityManager.createQuery(FETCH_BASE + " WHERE LOWER(u.username) = LOWER(:username)", User.class)
                .setParameter("username", username == null ? "" : username.trim())
                .getResultStream()
                .findFirst();
    }

    private User insert(EntityManager entityManager, User user) {
        User entity = new User();
        copyState(entityManager, user, entity);
        entityManager.persist(entity);
        entityManager.flush();
        user.setId(entity.getId());
        user.setRoleEntity(entity.getRoleEntity());
        return user;
    }

    private boolean update(EntityManager entityManager, User user) {
        User entity = entityManager.find(User.class, user.getId());
        if (entity == null) {
            return false;
        }
        copyState(entityManager, user, entity);
        entityManager.flush();
        return true;
    }

    private boolean updateFullName(EntityManager entityManager, Long userId, String fullName) {
        User entity = entityManager.find(User.class, userId);
        if (entity == null) {
            return false;
        }
        entity.setFullName(fullName);
        entityManager.flush();
        return true;
    }

    private boolean updateEmail(EntityManager entityManager, Long userId, String email) {
        User entity = entityManager.find(User.class, userId);
        if (entity == null) {
            return false;
        }
        entity.setEmail(email);
        entityManager.flush();
        return true;
    }

    private boolean updatePasswordHash(EntityManager entityManager, Long userId, String passwordHash) {
        User entity = entityManager.find(User.class, userId);
        if (entity == null) {
            return false;
        }
        entity.setPasswordHash(passwordHash);
        entityManager.flush();
        return true;
    }

    private void copyState(EntityManager entityManager, User source, User target) {
        target.setUsername(source.getUsername());
        target.setPasswordHash(source.getPasswordHash());
        target.setFullName(source.getFullName());
        target.setEmail(source.getEmail());
        target.setActive(source.isActive());
        target.setRoleEntity(resolveRoleEntity(entityManager, source.getRoleEntity(), source.getRole()));
    }

    private RoleEntity resolveRoleEntity(EntityManager entityManager, RoleEntity roleEntity, Role role) {
        Role resolvedRole = role != null ? role : roleEntity == null ? null : roleEntity.toRoleEnum();
        if (resolvedRole == null) {
            throw new AppException("Không thể lưu User khi chưa có vai trò hợp lệ.");
        }
        return entityManager.createQuery("""
                        SELECT r
                        FROM RoleEntity r
                        WHERE UPPER(r.roleCode) = UPPER(:roleCode)
                        """, RoleEntity.class)
                .setParameter("roleCode", resolvedRole.getCode())
                .getResultStream()
                .findFirst()
                .orElseThrow(() -> new AppException("Không tìm thấy vai trò " + resolvedRole.getCode() + " trong bảng roles."));
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
