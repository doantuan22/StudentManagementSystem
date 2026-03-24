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

    public Student getCurrentStudent() {
        return studentService.findCurrentStudent();
    }

    public Student saveStudent(Student student) {
        return studentService.save(student);
    }

    public boolean deleteStudent(Long id) {
        return studentService.delete(id);
    }
}
