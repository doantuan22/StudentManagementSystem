/**
 * Mô tả thực thể giảng viên của hệ thống.
 */
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

    /**
     * Khởi tạo giảng viên.
     */
    public Lecturer() {
    }

    /**
     * Khởi tạo giảng viên.
     */
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
     * Trả về người dùng id.
     */
    public Long getUserId() {
        return user == null ? null : user.getId();
    }

    /**
     * Cập nhật người dùng id.
     */
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

    /**
     * Trả về người dùng.
     */
    public User getUser() {
        return user;
    }

    /**
     * Cập nhật người dùng.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Trả về giảng viên mã.
     */
    public String getLecturerCode() {
        return lecturerCode;
    }

    /**
     * Cập nhật giảng viên mã.
     */
    public void setLecturerCode(String lecturerCode) {
        this.lecturerCode = lecturerCode;
    }

    /**
     * Trả về họ tên.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Cập nhật họ tên.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Trả về gender.
     */
    public String getGender() {
        return gender;
    }

    /**
     * Cập nhật gender.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * Trả về email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Cập nhật email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Trả về ngày sinh.
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Cập nhật ngày sinh.
     */
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Trả về điện thoại.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Cập nhật điện thoại.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Trả về địa chỉ.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Cập nhật địa chỉ.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Trả về khoa.
     */
    public Faculty getFaculty() {
        return faculty;
    }

    /**
     * Cập nhật khoa.
     */
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    /**
     * Trả về trạng thái.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Cập nhật trạng thái.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Trả về chuỗi hiển thị của đối tượng.
     */
    @Override
    public String toString() {
        return lecturerCode + " - " + fullName;
    }

    /**
     * So sánh đối tượng theo định danh phù hợp.
     */
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

    /**
     * Tạo mã băm cho đối tượng.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : lecturerCode);
    }
}
