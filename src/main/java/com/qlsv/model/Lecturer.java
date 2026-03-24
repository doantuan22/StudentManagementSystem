package com.qlsv.model;

public class Lecturer {

    private Long id;
    private Long userId;
    private String lecturerCode;
    private String fullName;
    private String email;
    private String phone;
    private Faculty faculty;
    private String status;

    public Lecturer() {
    }

    public Lecturer(Long id, Long userId, String lecturerCode, String fullName, String email, String phone,
                    Faculty faculty, String status) {
        this.id = id;
        this.userId = userId;
        this.lecturerCode = lecturerCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.faculty = faculty;
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

    public String getLecturerCode() {
        return lecturerCode;
    }

    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return lecturerCode + " - " + fullName;
    }
}
