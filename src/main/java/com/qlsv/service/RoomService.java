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
                "KhÃ´ng thá»ƒ lÆ°u phÃ²ng há»c.",
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
            throw new AppException("Dá»¯ liá»‡u khÃ´ng há»£p lá»‡ Ä‘á»ƒ xÃ³a phÃ²ng há»c.");
        }
        JpaBootstrap.executeInTransaction(
                "KhÃ´ng thá»ƒ xÃ³a phÃ²ng há»c.",
                ignored -> {
                    roomDAO.delete(id);
                    return null;
                }
        );
    }

    private void validateRoom(Room room) {
        if (room == null) {
            throw new AppException("Dá»¯ liá»‡u phÃ²ng há»c khÃ´ng há»£p lá»‡.");
        }
        if (room.getRoomCode() == null || room.getRoomCode().isBlank()) {
            throw new AppException("Vui lÃ²ng nháº­p mÃ£ phÃ²ng há»c.");
        }
        if (room.getRoomName() == null || room.getRoomName().isBlank()) {
            throw new AppException("Vui lÃ²ng nháº­p tÃªn phÃ²ng há»c.");
        }
    }
}
