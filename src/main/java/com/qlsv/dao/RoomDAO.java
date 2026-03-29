package com.qlsv.dao;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.exception.AppException;
import com.qlsv.model.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class RoomDAO {

    /**
     * Lấy danh sách tất cả các phòng học trong cơ sở dữ liệu.
     */
    public List<Room> findAll() {
        return executeRead("Không thể tải danh sách phòng học.", entityManager ->
                entityManager.createQuery("""
                                SELECT r
                                FROM Room r
                                ORDER BY r.id
                                """, Room.class)
                        .getResultList());
    }

    /**
     * Tìm kiếm phòng học dựa trên mã định danh duy nhất.
     */
    public Optional<Room> findById(Long id) {
        return executeRead("Không thể tìm phòng học theo mã định danh.", entityManager ->
                entityManager.createQuery("""
                                SELECT r
                                FROM Room r
                                WHERE r.id = :id
                                """, Room.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst());
    }

    /**
     * Tìm kiếm phòng học theo từ khóa dựa trên mã phòng hoặc tên phòng.
     */
    public List<Room> searchByKeyword(String keyword) {
        String normalizedKeyword = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return executeRead("Không thể tìm kiếm phòng học.", entityManager ->
                entityManager.createQuery("""
                                SELECT r
                                FROM Room r
                                WHERE LOWER(r.roomCode) LIKE :keyword
                                   OR LOWER(r.roomName) LIKE :keyword
                                ORDER BY r.id
                                """, Room.class)
                        .setParameter("keyword", normalizedKeyword)
                        .getResultList());
    }

    /**
     * Thêm mới một phòng học vào hệ thống.
     */
    public Room insert(Room room) {
        Long roomId = executeWrite(
                "Không thể thêm phòng học.",
                "Mã phòng học đã tồn tại trong hệ thống.",
                entityManager -> {
                    Room entity = new Room();
                    copyState(room, entity);
                    entityManager.persist(entity);
                    entityManager.flush();
                    room.setId(entity.getId());
                    return entity.getId();
                }
        );
        return findById(roomId)
                .orElseThrow(() -> new AppException("Không thể tải lại phòng học sau khi thêm."));
    }

    /**
     * Cập nhật thông tin phòng học hiện có.
     */
    public boolean update(Room room) {
        return executeWrite(
                "Không thể cập nhật phòng học.",
                "Mã phòng học đã tồn tại trong hệ thống.",
                entityManager -> {
                    Room entity = entityManager.find(Room.class, room.getId());
                    if (entity == null) {
                        return false;
                    }
                    copyState(room, entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    /**
     * Xóa phòng học khỏi cơ sở dữ liệu theo mã định danh.
     */
    public boolean delete(Long id) {
        return executeWrite(
                "Không thể xóa phòng học.",
                "Không thể xóa phòng học vì đang được tham chiếu bởi học phần hoặc lịch học.",
                entityManager -> {
                    Room entity = entityManager.find(Room.class, id);
                    if (entity == null) {
                        return false;
                    }
                    entityManager.remove(entity);
                    entityManager.flush();
                    return true;
                }
        );
    }

    private void copyState(Room source, Room target) {
        target.setRoomCode(source.getRoomCode());
        target.setRoomName(source.getRoomName());
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
