package com.qlsv.controller;

import com.qlsv.model.Student;
import com.qlsv.service.StudentService;

import java.util.List;

public class StudentController {

    private final StudentService studentService = new StudentService();

    public List<Student> getAllStudents() {
        return studentService.findAll();
    }

    public List<Student> getStudentsForSelection() {
        return studentService.findAllForSelection();
    }

    public List<Student> getStudentsByFaculty(Long facultyId) {
        return studentService.findByFacultyId(facultyId);
    }

    public List<Student> getStudentsByClassRoom(Long classRoomId) {
        return studentService.findByClassRoomId(classRoomId);
    }

    public List<Student> getStudentsByAcademicYear(String academicYear) {
        return studentService.findByAcademicYear(academicYear);
    }

    public List<String> getAcademicYearsForSelection() {
        return studentService.findAcademicYears();
    }

    public Student getCurrentStudent() {
        return studentService.findCurrentStudent();
    }

    public Student getStudentById(Long id) {
        return studentService.findById(id);
    }

    public Student saveStudent(Student student) {
        return studentService.save(student);
    }

    public Student updateCurrentStudentContactInfo(String email, String phone, String address) {
        return studentService.updateCurrentStudentContactInfo(email, phone, address);
    }

    public boolean deleteStudent(Long id) {
        return studentService.delete(id);
    }
}
