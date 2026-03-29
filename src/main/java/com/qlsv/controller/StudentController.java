/**
 * Điều phối dữ liệu cho sinh viên.
 */
package com.qlsv.controller;

import com.qlsv.model.Student;
import com.qlsv.service.StudentService;

import java.util.List;

public class StudentController {

    private final StudentService studentService = new StudentService();

    /**
     * Trả về toàn bộ sinh viên.
     */
    public List<Student> getAllStudents() {
        return studentService.findAll();
    }

    /**
     * Trả về sinh viên for selection.
     */
    public List<Student> getStudentsForSelection() {
        return studentService.findAllForSelection();
    }

    /**
     * Trả về sinh viên theo khoa.
     */
    public List<Student> getStudentsByFaculty(Long facultyId) {
        return studentService.findByFacultyId(facultyId);
    }

    /**
     * Trả về sinh viên theo lớp.
     */
    public List<Student> getStudentsByClassRoom(Long classRoomId) {
        return studentService.findByClassRoomId(classRoomId);
    }

    /**
     * Trả về sinh viên theo niên khóa.
     */
    public List<Student> getStudentsByAcademicYear(String academicYear) {
        return studentService.findByAcademicYear(academicYear);
    }

    /**
     * Trả về học vụ years for selection.
     */
    public List<String> getAcademicYearsForSelection() {
        return studentService.findAcademicYears();
    }

    /**
     * Tìm kiếm sinh viên.
     */
    public List<Student> searchStudents(String keyword, Long facultyId, Long classRoomId, String academicYear) {
        return studentService.searchStudents(keyword, facultyId, classRoomId, academicYear);
    }

    /**
     * Trả về sinh viên hiện tại.
     */
    public Student getCurrentStudent() {
        return studentService.findCurrentStudent();
    }

    /**
     * Trả về sinh viên theo id.
     */
    public Student getStudentById(Long id) {
        return studentService.findById(id);
    }

    /**
     * Lưu sinh viên.
     */
    public Student saveStudent(Student student) {
        return studentService.save(student);
    }

    /**
     * Cập nhật sinh viên contact thông tin hiện tại.
     */
    public Student updateCurrentStudentContactInfo(String email, String phone, String address) {
        return studentService.updateCurrentStudentContactInfo(email, phone, address);
    }

    /**
     * Xóa sinh viên.
     */
    public boolean deleteStudent(Long id) {
        return studentService.delete(id);
    }
}
