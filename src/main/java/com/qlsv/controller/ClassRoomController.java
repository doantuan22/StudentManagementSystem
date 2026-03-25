package com.qlsv.controller;

import com.qlsv.model.ClassRoom;
import com.qlsv.service.ClassRoomService;

import java.util.List;

public class ClassRoomController {

    private final ClassRoomService classRoomService = new ClassRoomService();

    public List<ClassRoom> getAllClassRooms() {
        return classRoomService.findAll();
    }

    public List<ClassRoom> getClassRoomsForSelection() {
        return classRoomService.findAllForSelection();
    }

    public List<ClassRoom> getClassRoomsByFaculty(Long facultyId) {
        return classRoomService.findByFacultyId(facultyId);
    }

    public List<ClassRoom> getClassRoomsByAcademicYear(String academicYear) {
        return classRoomService.findByAcademicYear(academicYear);
    }

    public ClassRoom saveClassRoom(ClassRoom classRoom) {
        return classRoomService.save(classRoom);
    }

    public boolean deleteClassRoom(Long id) {
        return classRoomService.delete(id);
    }
}
