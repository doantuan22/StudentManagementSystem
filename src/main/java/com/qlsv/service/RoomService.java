package com.qlsv.service;

import com.qlsv.config.JpaBootstrap;
import com.qlsv.dao.RoomDAO;
import com.qlsv.exception.AppException;
import com.qlsv.model.Role;
import com.qlsv.model.Room;

import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO = new RoomDAO();
    private final PermissionService permissionService = new PermissionService();

    public List<Room> getAllRooms() {
        permissionService.requireAnyRole(Role.ADMIN);
        return roomDAO.findAll();
    }

    public List<Room> searchRooms(String keyword) {
        permissionService.requireAnyRole(Role.ADMIN);
        if (keyword == null || keyword.isBlank()) {
            return roomDAO.findAll();
        }
        return roomDAO.searchByKeyword(keyword.trim());
    }

    public void saveRoom(Room room) {
        permissionService.requireAnyRole(Role.ADMIN);
        JpaBootstrap.executeInTransaction(
                "Không thể lưu phòng học.",
                ignored -> {
                    validateRoom(room);
                    if (room.getId() == null) {
                        roomDAO.insert(room);
                    } else {
                        roomDAO.update(room);
                    }
                    return null;
                }
        );
    }

    public void deleteRoom(Long id) {
        permissionService.requireAnyRole(Role.ADMIN);
        if (id == null || id <= 0) {
            throw new AppException("Dữ liệu không hợp lệ để xóa phòng học.");
        }
        JpaBootstrap.executeInTransaction(
                "Không thể xóa phòng học.",
                ignored -> {
                    roomDAO.delete(id);
                    return null;
                }
        );
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new AppException("Dữ liệu phòng học không hợp lệ.");
        }
        if (room.getRoomCode() == null || room.getRoomCode().isBlank()) {
            throw new AppException("Vui lòng nhập mã phòng học.");
        }
        if (room.getRoomName() == null || room.getRoomName().isBlank()) {
            throw new AppException("Vui lòng nhập tên phòng học.");
        }
    }
}
