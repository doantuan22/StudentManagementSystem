package com.qlsv.model;

import java.util.Objects;

public class Room {

    private Long id;
    private String roomCode;
    private String roomName;

    public Room() {
    }

    public Room(Long id, String roomCode, String roomName) {
        this.id = id;
        this.roomCode = roomCode;
        this.roomName = roomName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    @Override
    public String toString() {
        return roomName;
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : roomCode);
    }
}
