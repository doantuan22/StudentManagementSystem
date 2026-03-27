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

    public List<Room> getAllRooms() {
        try {
            return roomService.getAllRooms();
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    public List<Room> searchRooms(String keyword) {
        try {
            return roomService.searchRooms(keyword);
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    public List<Room> getRoomsForSelection() {
        return getAllRooms();
    }

    public void saveRoom(Room room) {
        try {
            roomService.saveRoom(room);
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    public void deleteRoom(Long id) {
        try {
            roomService.deleteRoom(id);
        } catch (AppException exception) {
            throw propagate(exception);
        }
    }

    private AppException propagate(AppException exception) {
        LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        return exception;
    }
}
