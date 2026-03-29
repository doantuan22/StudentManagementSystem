/**
 * Mô tả thực thể phòng của hệ thống.
 */
package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true, length = 50)
    private String roomCode;

    @Column(name = "room_name", nullable = false, length = 150)
    private String roomName;

    /**
     * Khởi tạo phòng.
     */
    public Room() {
    }

    /**
     * Khởi tạo phòng.
     */
    public Room(Long id, String roomCode, String roomName) {
        this.id = id;
        this.roomCode = roomCode;
        this.roomName = roomName;
    }

    /**
     * Trả về id.
     */
    public Long getId() {
        return id;
    }

    /**
     * Cập nhật id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Trả về phòng mã.
     */
    public String getRoomCode() {
        return roomCode;
    }

    /**
     * Cập nhật phòng mã.
     */
    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    /**
     * Trả về phòng tên.
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Cập nhật phòng tên.
     */
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return roomName;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Room room)) {
            return false;
        }
        if (id != null && room.id != null) {
            return Objects.equals(id, room.id);
        }
        return Objects.equals(roomCode, room.roomCode);
    }

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : roomCode);
    }
}
