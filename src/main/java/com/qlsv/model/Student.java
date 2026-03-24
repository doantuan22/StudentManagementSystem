package com.qlsv.model;

import java.time.LocalDate;
import java.util.Objects;

public class Student {

    private Long id;
    private Long userId;
    private String studentCode;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String phone;
    private Faculty faculty;
    private ClassRoom classRoom;
    private String status;

    public Student() {
    }

    public Student(Long id, Long userId, String studentCode, String fullName, String gender, LocalDate dateOfBirth,
                   String email, String phone, Faculty faculty, ClassRoom classRoom, String status) {
        this.id = id;
        this.userId = userId;
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phone = phone;
        this.faculty = faculty;
        this.classRoom = classRoom;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Faculty getFaculty() {
        return faculty;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    public ClassRoom getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(ClassRoom classRoom) {
        this.classRoom = classRoom;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return studentCode + " - " + fullName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Student student)) {
            return false;
        }
        if (id != null && student.id != null) {
            return Objects.equals(id, student.id);
        }
        return Objects.equals(studentCode, student.studentCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : studentCode);
    }
}
