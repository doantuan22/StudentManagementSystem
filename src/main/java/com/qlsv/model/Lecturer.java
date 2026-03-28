package com.qlsv.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "lecturers")
public class Lecturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "lecturer_code", nullable = false, unique = true, length = 50)
    private String lecturerCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    public Lecturer() {
    }

    public Lecturer(Long id, Long userId, String lecturerCode, String fullName, String gender, String email, LocalDate dateOfBirth, String phone,
                    String address, Faculty faculty, String status) {
        this.id = id;
        setUserId(userId);
        this.lecturerCode = lecturerCode;
        this.fullName = fullName;
        this.gender = gender;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
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
        return user == null ? null : user.getId();
    }

    public void setUserId(Long userId) {
        if (userId == null) {
            this.user = null;
            return;
        }
        if (this.user == null) {
            this.user = new User();
        }
        this.user.setId(userId);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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
