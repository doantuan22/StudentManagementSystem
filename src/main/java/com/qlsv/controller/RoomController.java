package com.qlsv.controller;

import com.qlsv.exception.AppException;
import com.qlsv.model.Room;
import com.qlsv.service.RoomService;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RoomController {

    private static final Logger LOGGER = Logger.getLogger(RoomController.class.getName());
    private final RoomService roomService = new RoomService();

    public List<Room> getAllRooms() {
        try {
            return roomService.getAllRooms();
        } catch (AppException exception) {
            handleException(exception);
            return List.of();
        }
    }

    public List<Room> searchRooms(String keyword) {
        try {
            return roomService.searchRooms(keyword);
        } catch (AppException exception) {
            handleException(exception);
            return List.of();
        }
    }

    public List<Room> getRoomsForSelection() {
        return getAllRooms();
    }

    public void saveRoom(Room room) {
        try {
            roomService.saveRoom(room);
            JOptionPane.showMessageDialog(null, "Lưu thông tin phòng học thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException exception) {
            handleException(exception);
        }
    }

    public void deleteRoom(Long id) {
        try {
            roomService.deleteRoom(id);
            JOptionPane.showMessageDialog(null, "Xóa phòng học thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (AppException exception) {
            handleException(exception);
        }
    }

    private void handleException(AppException exception) {
        LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        JOptionPane.showMessageDialog(null, exception.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
