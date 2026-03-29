/**
 * Khởi tạo và quản lý JPA cho ứng dụng.
 */
package com.qlsv.config;

import com.qlsv.exception.AppException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class JpaBootstrap {

    private static final String PERSISTENCE_UNIT =
            AppConfig.getProperty("jpa.persistence.unit", "student-management-jpa");
    private static final ThreadLocal<EntityManager> CURRENT_ENTITY_MANAGER = new ThreadLocal<>();

    private static volatile EntityManagerFactory entityManagerFactory;

    /**
     * Khởi tạo JPA bootstrap.
     */
    private JpaBootstrap() {
    }

    /**
     * Trả về entity manager factory.
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            return entityManagerFactory;
        }

        synchronized (JpaBootstrap.class) {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                return entityManagerFactory;
            }
            try {
                entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, buildOverrides());
                return entityManagerFactory;
            } catch (Exception exception) {
                throw new AppException("Không thể khởi tạo Hibernate/JPA EntityManagerFactory.", exception);
            }
        }
    }

    /**
     * Tạo entity manager.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Thực thi with entity manager.
     */
    public static <T> T executeWithEntityManager(Function<EntityManager, T> action) {
        EntityManager currentEntityManager = getCurrentEntityManager();
        if (currentEntityManager != null) {
            return action.apply(currentEntityManager);
        }

        EntityManager entityManager = null;
        try {
            entityManager = createEntityManager();
            return action.apply(entityManager);
        } finally {
            closeQuietly(entityManager);
        }
    }

    /**
     * Thực thi in transaction.
     */
    public static <T> T executeInTransaction(Function<EntityManager, T> action) {
        EntityManager currentEntityManager = getCurrentEntityManager();
        if (currentEntityManager != null) {
            return executeInCurrentTransaction(action);
        }

        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        try {
            entityManager = createEntityManager();
            CURRENT_ENTITY_MANAGER.set(entityManager);
            transaction = entityManager.getTransaction();
            transaction.begin();
            T result = action.apply(entityManager);
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            rollbackQuietly(transaction);
            throw exception;
        } finally {
            CURRENT_ENTITY_MANAGER.remove();
            closeQuietly(entityManager);
        }
    }

    /**
     * Thực thi in hiện tại transaction.
     */
    public static <T> T executeInCurrentTransaction(Function<EntityManager, T> action) {
        EntityManager currentEntityManager = getCurrentEntityManager();
        EntityTransaction transaction = currentEntityManager == null ? null : currentEntityManager.getTransaction();
        if (currentEntityManager == null || transaction == null || !transaction.isActive()) {
            throw new AppException("Không tìm thấy transaction JPA đang hoạt động cho thao tác ghi dữ liệu.");
        }

        try {
            return action.apply(currentEntityManager);
        } catch (RuntimeException exception) {
            markRollbackOnly(transaction);
            throw exception;
        }
    }

    /**
     * Thực thi in transaction.
     */
    public static <T> T executeInTransaction(String errorMessage, Function<EntityManager, T> action) {
        try {
            return executeInTransaction(action);
        } catch (AppException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AppException(errorMessage, exception);
        }
    }

    /**
     * Kiểm tra khả năng bootstrap.
     */
    public static boolean canBootstrap() {
        try {
            return getEntityManagerFactory().isOpen();
        } catch (Exception exception) {
            return false;
        }
    }

    /**
     * Kiểm tra required schema.
     */
    public static boolean hasRequiredSchema() {
        try {
            executeWithEntityManager(entityManager -> {
                entityManager.createQuery("select count(r) from RoleEntity r", Long.class).getSingleResult();
                entityManager.createQuery("select count(u) from User u", Long.class).getSingleResult();
                entityManager.createQuery("select count(f) from Faculty f", Long.class).getSingleResult();
                entityManager.createQuery("select count(c) from ClassRoom c", Long.class).getSingleResult();
                entityManager.createQuery("select count(rm) from Room rm", Long.class).getSingleResult();
                entityManager.createQuery("select count(s) from Student s", Long.class).getSingleResult();
                entityManager.createQuery("select count(l) from Lecturer l", Long.class).getSingleResult();
                entityManager.createQuery("select count(su) from Subject su", Long.class).getSingleResult();
                entityManager.createQuery("select count(ls) from LecturerSubject ls", Long.class).getSingleResult();
                entityManager.createQuery("select count(cs) from CourseSection cs", Long.class).getSingleResult();
                entityManager.createQuery("select count(e) from Enrollment e", Long.class).getSingleResult();
                entityManager.createQuery("select count(sc) from Score sc", Long.class).getSingleResult();
                entityManager.createQuery("select count(sch) from Schedule sch", Long.class).getSingleResult();
                return Boolean.TRUE;
            });
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    /**
     * Xử lý run smoke test.
     */
    public static String runSmokeTest() {
        return executeWithEntityManager(entityManager -> {
            Long facultyCount = entityManager.createQuery("select count(f) from Faculty f", Long.class)
                    .getSingleResult();
            return "JPA boot OK. facultyCount=" + facultyCount;
        });
    }

    /**
     * Đóng dữ liệu hiện tại.
     */
    public static void close() {
        synchronized (JpaBootstrap.class) {
            if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
            entityManagerFactory = null;
        }
    }

    /**
     * Khởi động điểm vào của ứng dụng.
     */
    public static void main(String[] args) {
        try {
            System.out.println(runSmokeTest());
        } finally {
            close();
        }
    }

    /**
     * Tạo overrides.
     */
    private static Map<String, Object> buildOverrides() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("jakarta.persistence.jdbc.url", AppConfig.getDbUrl());
        properties.put("jakarta.persistence.jdbc.user", AppConfig.getDbUsername());
        properties.put("jakarta.persistence.jdbc.password", AppConfig.getDbPassword());
        properties.put(
                "hibernate.dialect",
                AppConfig.getProperty("jpa.hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
        );
        properties.put("hibernate.hbm2ddl.auto", AppConfig.getProperty("jpa.hibernate.ddl-auto", "none"));
        properties.put("hibernate.show_sql", AppConfig.getProperty("jpa.show-sql", "false"));
        properties.put("hibernate.format_sql", "true");
        properties.put("hibernate.highlight_sql", "false");
        return properties;
    }

    /**
     * Trả về entity manager hiện tại.
     */
    private static EntityManager getCurrentEntityManager() {
        EntityManager entityManager = CURRENT_ENTITY_MANAGER.get();
        if (entityManager == null) {
            return null;
        }
        if (!entityManager.isOpen()) {
            CURRENT_ENTITY_MANAGER.remove();
            return null;
        }
        return entityManager;
    }

    /**
     * Xử lý mark rollback only.
     */
    private static void markRollbackOnly(EntityTransaction transaction) {
        try {
            if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
                transaction.setRollbackOnly();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Xử lý rollback quietly.
     */
    private static void rollbackQuietly(EntityTransaction transaction) {
        try {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Đóng quietly.
     */
    private static void closeQuietly(EntityManager entityManager) {
        try {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        } catch (Exception ignored) {
        }
    }
}
