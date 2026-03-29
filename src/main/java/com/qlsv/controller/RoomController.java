/**
 * Điều phối dữ liệu cho phòng.
 */
package com.qlsv.controller;

import com.qlsv.exception.AppException;
import com.qlsv.model.Room;
import com.qlsv.service.RoomService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomController {

    private static final Logger LOGGER = Logger.getLogger(RoomController.class.getName());
    private final RoomService roomService = new RoomService();

    /**
     * Trả về toàn bộ phòng.
     */
    public List<Room> getAllRooms() {
        try {
            return roomService.getAllRooms();
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    /**
     * Tìm kiếm phòng.
     */
    public List<Room> searchRooms(String keyword) {
        try {
            return roomService.searchRooms(keyword);
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    /**
     * Trả về phòng for selection.
     */
    public List<Room> getRoomsForSelection() {
        return getAllRooms();
    }

    /**
     * Lưu phòng.
     */
    public void saveRoom(Room room) {
        try {
            roomService.saveRoom(room);
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    /**
     * Xóa phòng.
     */
    public void deleteRoom(Long id) {
        try {
            roomService.deleteRoom(id);
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    /**
     * Xử lý propagate.
     */
    private AppException propagate(AppException exception) {
        LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        return exception;
    }
}
