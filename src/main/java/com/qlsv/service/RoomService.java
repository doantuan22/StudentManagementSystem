package com.qlsv.service;

import com.qlsv.dao.RoomDAO;
import com.qlsv.exception.AppException;
import com.qlsv.model.Room;

import java.util.List;

public class RoomService {

    private final RoomDAO roomDAO = new RoomDAO();

    public List<Room> getAllRooms() {
        return roomDAO.findAll();
    }

    public List<Room> searchRooms(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return roomDAO.findAll();
        }
        return roomDAO.searchByKeyword(keyword.trim());
    }

    public void saveRoom(Room room) {
        validateRoom(room);
        if (room.getId() == null) {
            roomDAO.insert(room);
        } else {
            roomDAO.update(room);
        }
    }

    public void deleteRoom(Long id) {
        if (id == null || id <= 0) {
            throw new AppException("Dữ liệu không hợp lệ para xóa phòng học.");
        }
        roomDAO.delete(id);
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
