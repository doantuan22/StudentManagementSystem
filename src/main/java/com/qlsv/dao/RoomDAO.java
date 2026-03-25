package com.qlsv.dao;

import com.qlsv.config.DBConnection;
import com.qlsv.exception.AppException;
import com.qlsv.model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAO {

    public List<Room> findAll() {
        String sql = "SELECT id, room_code, room_name FROM rooms ORDER BY id";
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                rooms.add(mapRow(resultSet));
            }
            return rooms;
        } catch (SQLException exception) {
            throw new AppException("Không thể tải danh sách phòng học.", exception);
        }
    }

    public Optional<Room> findById(Long id) {
        String sql = "SELECT id, room_code, room_name FROM rooms WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm kiếm phòng học theo mã định danh.", exception);
        }
    }

    public List<Room> searchByKeyword(String keyword) {
        String sql = "SELECT id, room_code, room_name FROM rooms WHERE room_code LIKE ? OR room_name LIKE ? ORDER BY id";
        String searchValue = "%" + keyword + "%";
        List<Room> rooms = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchValue);
            statement.setString(2, searchValue);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rooms.add(mapRow(resultSet));
                }
                return rooms;
            }
        } catch (SQLException exception) {
            throw new AppException("Không thể tìm kiếm phòng học.", exception);
        }
    }

    public Room insert(Room room) {
        String sql = "INSERT INTO rooms(room_code, room_name) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, room.getRoomCode());
            statement.setString(2, room.getRoomName());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    room.setId(resultSet.getLong(1));
                }
            }
            return room;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Mã phòng học đã tồn tại trong hệ thống.", exception);
        } catch (SQLException exception) {
            throw new AppException("Không thể thêm phòng học.", exception);
        }
    }

    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_code = ?, room_name = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, room.getRoomCode());
            statement.setString(2, room.getRoomName());
            statement.setLong(3, room.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Mã phòng học đã tồn tại trong hệ thống.", exception);
        } catch (SQLException exception) {
            throw new AppException("Không thể cập nhật phòng học.", exception);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new AppException("Không thể xóa phòng học vì đang được tham chiếu bởi học phần hoặc lịch học.", exception);
        } catch (SQLException exception) {
            throw new AppException("Không thể xóa phòng học.", exception);
        }
    }

    private Room mapRow(ResultSet resultSet) throws SQLException {
        return new Room(
                resultSet.getLong("id"),
                resultSet.getString("room_code"),
                resultSet.getString("room_name")
        );
    }
}
