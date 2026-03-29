package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.ClassRoom;
import com.qlsv.model.Faculty;
import com.qlsv.utils.AcademicFormatUtil;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * JOIN FETCH keeps Faculty available after EntityManager closes for the existing Swing flow.
 */
public class ClassRoomDAO {

    private static final String FETCH_BASE = """
            SELECT c
            FROM ClassRoom c
            JOIN FETCH c.faculty
            """;

    /**
     * Lấy danh sách tất cả các lớp học.
     */
    public List<ClassRoom> findAll() {
        return executeRead("Không thể tải danh sách lớp.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY c.id", ClassRoom.class)
                        .getResultList());
    }

    /**
     * Tìm kiếm lớp học theo mã định danh.
     */
    public Optional<ClassRoom> findById(Long id) {
        return executeRead("Không thể tìm lớp theo mã định danh.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE c.id = :id", ClassRoom.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Lấy danh sách lớp học thuộc một khoa cụ thể.
     */
    public List<ClassRoom> findByFacultyId(Long facultyId) {
        return executeRead("Không thể lọc lớp theo khoa.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " WHERE c.faculty.id = :facultyId ORDER BY c.id", ClassRoom.class)
                        .setParameter("facultyId", facultyId)
                        .getResultList());
    }

    /**
     * Lọc danh sách lớp học theo niên khóa.
     */
    public List<ClassRoom> findByAcademicYear(String academicYear) {
        String normalizedAcademicYear = AcademicFormatUtil.normalizeAcademicYear(academicYear, "Niên khóa");
        return executeRead("Không thể lọc lớp theo niên khóa.", entityManager ->
                entityManager.createQuery(FETCH_BASE + " ORDER BY c.id", ClassRoom.class)
                        .getResultList()
                        .stream()
                        .filter(classRoom -> AcademicFormatUtil.academicYearsEqual(classRoom.getAcademicYear(), normalizedAcademicYear))
                        .toList());
    }

    /**
     * Tìm kiếm lớp học theo từ khóa (mã lớp, tên lớp hoặc niên khóa).
     */
    public List<ClassRoom> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm lớp.", entityManager ->
                entityManager.createQuery(FETCH_BASE + """
                                WHERE LOWER(c.classCode) LIKE :keyword
                                   OR LOWER(c.className) LIKE :keyword
                                   OR LOWER(c.academicYear) LIKE :keyword
                                ORDER BY c.id
                                """, ClassRoom.class)
                        .setParameter("keyword", normalizedKeyword)
                        .getResultList());
    }

    /**
     * Thêm mới một lớp học vào hệ thống.
     */
    public ClassRoom insert(ClassRoom classRoom) {
        Long classRoomId = executeWrite(
                "Không thể thêm lớp.",
                "Không thể thêm lớp do mã lớp đã tồn tại hoặc khoa không hợp lệ.",
                entityManager -> {
                    ClassRoom entity = new ClassRoom();
                    copyState(entityManager, classRoom, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    classRoom.setId(entity.getId());
                    return entity.getId();
                }
        );
        return findById(classRoomId)
                .orElseThrow(() -> new AppException("Không thể tải lại lớp sau khi thêm."));
    }

    /**
     * Cập nhật thông tin lớp học hiện có.
     */
    public boolean update(ClassRoom classRoom) {
        return executeWrite(
                "Không thể cập nhật lớp.",
                "Không thể cập nhật lớp do mã lớp đã tồn tại hoặc khoa không hợp lệ.",
                entityManager -> {
                    ClassRoom entity = entityManager.find(ClassRoom.class, classRoom.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(entityManager, classRoom, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    /**
     * Xóa lớp học khỏi hệ thống theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa lớp.",
                "Không thể xóa lớp vì vẫn còn sinh viên hoặc học phần đang sử dụng.",
                entityManager -> {
                    ClassRoom entity = entityManager.find(ClassRoom.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private void copyState(EntityManager entityManager, ClassRoom source, ClassRoom target) {
        target.setClassCode(source.getClassCode());
        target.setClassName(source.getClassName());
        target.setAcademicYear(source.getAcademicYear());
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

    /**
     * Thực thi tác vụ ghi dữ liệu trong một transaction.
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
