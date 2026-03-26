package com.qlsv.model;

import java.util.Objects;

public class Lecturer {

    private Long id;
    private Long userId;
    private String lecturerCode;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private Faculty faculty;
    private String status;

    public Lecturer() {
    }

    public Lecturer(Long id, Long userId, String lecturerCode, String fullName, String email, String phone,
                    String address, Faculty faculty, String status) {
        this.id = id;
        this.userId = userId;
        this.lecturerCode = lecturerCode;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Lecturer lecturer)) {
            return false;
        }
        if (id != null && lecturer.id != null) {
            return Objects.equals(id, lecturer.id);
        }
        return Objects.equals(lecturerCode, lecturer.lecturerCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : lecturerCode);
    }
}
